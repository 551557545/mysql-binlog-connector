package com.cl.mysql.binlog.network;

import cn.hutool.core.util.StrUtil;
import com.cl.mysql.binlog.binlogEvent.Event;
import com.cl.mysql.binlog.constant.*;
import com.cl.mysql.binlog.entity.BinlogInfo;
import com.cl.mysql.binlog.listener.EventListener;
import com.cl.mysql.binlog.network.command.*;
import com.cl.mysql.binlog.network.protocol.InitialHandshakeProtocol;
import com.cl.mysql.binlog.network.protocol.packet.TextResultSetPacket;
import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: 连接器
 * @author: liuzijian
 * @time: 2023-08-09 15:55
 */
@Slf4j
public class MysqlBinLogConnector {


    @Setter(AccessLevel.PRIVATE)
    private PacketChannel channel;

    @Setter(AccessLevel.PRIVATE)
    private InitialHandshakeProtocol handshakeProtocol;

    private final String host;

    private final int port;

    private final String userName;

    private final String password;

    private final boolean ssl;

    private final String dataBaseScram;

    private boolean successLogin;

    @Getter
    private int clientCapabilities;

    private BinlogCheckSumEnum checkSum;

    private BinlogRowMetadataEnum rowMetadata;

    private List<EventListener> eventListenerList = new ArrayList<>();

    private MysqlBinLogConnector(String host, int port, String userName, String password, boolean ssl, String dataBaseScram) {
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.ssl = ssl;
        this.dataBaseScram = dataBaseScram;
    }


    /**
     * 开启连接
     *
     * @param host          数据库ip
     * @param port          数据库端口
     * @param userName      用户名
     * @param password      密码
     * @param ssl           是否走ssl
     * @param dataBaseScram 数据库库名（非必填）
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public static MysqlBinLogConnector openConnect(String host, int port, String userName, String password, boolean ssl, String dataBaseScram) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        MysqlBinLogConnector mysqlBinLogConnector = new MysqlBinLogConnector(host, port, userName, password, ssl, dataBaseScram);
        mysqlBinLogConnector.setChannel(new PacketChannel(host, port));
        // 解析握手协议
        byte[] bytes = mysqlBinLogConnector.readDataContent();
        mysqlBinLogConnector.checkPacket(bytes);
        mysqlBinLogConnector.setHandshakeProtocol(new InitialHandshakeProtocol(bytes));
        if (ssl) {
            mysqlBinLogConnector.tryChangeToSSL();
        }
        mysqlBinLogConnector.sendHandshakeResponse();
        return mysqlBinLogConnector;
    }

    /**
     * 尝试转为ssl请求
     * {@see 接入ssl流程 https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase.html#sect_protocol_connection_phase_initial_handshake}
     *
     * @throws IOException
     */
    public void tryChangeToSSL() throws IOException, NoSuchAlgorithmException, KeyManagementException {
        if (handshakeProtocol.supportSSL()) {
            if ((handshakeProtocol.getClientCapabilities() & CapabilitiesFlagsEnum.CLIENT_PROTOCOL_41.getCode()) > 0) {
                // 41协议
                SSLProtocol41Command sslCommand = new SSLProtocol41Command(this.handshakeProtocol);
                // 转为ssl请求
                channel.sendCommand(sslCommand);
            } else {
                // 旧协议
                SSLCommand sslCommand = new SSLCommand(this.handshakeProtocol);
                channel.sendCommand(sslCommand);
            }
            channel.changeToSSL();
        }
        log.info("连接转为ssl成功");
    }

    /**
     * 做mysql登录
     *
     * @throws IOException
     */
    public void sendHandshakeResponse() throws IOException {
        if (successLogin) {
            return;
        }
        HandShakeResponseCommand command = new HandShakeResponseCommand(
                this.handshakeProtocol,
                this.userName,
                this.password,
                this.ssl,
                this.dataBaseScram,
                this.channel
        );
        command.setClientCapabilities(this.clientCapabilities);
        command.send();
        this.clientCapabilities = command.getClientCapabilities();
        successLogin = true;
        log.info("mysql连接成功");
    }

    public void sendComBingLogDump() throws IOException {
        this.sendComBingLogDump(null, -1);
    }

    /**
     * 请求mysql服务器获取binlog的dump线程
     */
    public void sendComBingLogDump(String binlogFileName, int binlogPosition) throws IOException {
        if (StrUtil.isBlank(binlogFileName) && binlogPosition <= 0) {
            ComQueryCommand queryCommand = new ComQueryCommand(Sql.show_master_status);
            channel.sendCommand(queryCommand);
            TextResultSetPacket textResultSetPacket = channel.readTextResultSetPacket(this.clientCapabilities);
            BinlogInfo binlogInfo = new BinlogInfo(textResultSetPacket);
            binlogFileName = binlogInfo.getFileName();
            binlogPosition = binlogInfo.getPosition();
        }
        // 查询当前mysql服务器的checkSum
        this.checkSum = this.fecthCheckSum();
        if (this.checkSum != BinlogCheckSumEnum.NONE) {
            // 设置会话checkSum
            this.setCheckSum(this.checkSum);
        }
        this.rowMetadata = this.fecthBinlogRowMetadata();
        // 设置从服务器连接的uuid
        this.setSlaveUUID();
        // 向dump线程发送注册指令
        ComBinglogDumpCommand command = new ComBinglogDumpCommand(binlogFileName, binlogPosition, this.handshakeProtocol.getThreadId());
        channel.sendCommand(command);
        //this.checkPacket(this.readDataContent());
        this.listenBinlog();
    }

    private void listenBinlog() throws IOException {
        while (true) {
            byte[] bytes = channel.readBinlogStream();
            ByteArrayIndexInputStream indexInputStream = this.checkBinlogPacket(bytes);
            Event event = Event.V4Deserialization(indexInputStream, this.checkSum);
            for (EventListener listener : this.eventListenerList) {
                listener.listenAll(event);
                if (BinlogEventTypeEnum.isUpdateEvent(event.getEventType())) {
                    listener.listenUpdateEvent(event);
                } else if (BinlogEventTypeEnum.isInsertEvent(event.getEventType())) {
                    listener.listenInsertEvent(event);
                } else if (BinlogEventTypeEnum.isDelteEvent(event.getEventType())) {
                    listener.listenDeleteEvent(event);
                }
            }
        }
    }

    /**
     * 查询当前mysql数据库checksum信息
     *
     * @return
     * @throws IOException
     */
    private BinlogCheckSumEnum fecthCheckSum() throws IOException {
        ComQueryCommand queryCommand = new ComQueryCommand(Sql.show_global_variables_like_binlog_checksum);
        channel.sendCommand(queryCommand);
        TextResultSetPacket textResultSetPacket = channel.readTextResultSetPacket(this.clientCapabilities);
        return BinlogCheckSumEnum.getEnum(textResultSetPacket);
    }

    /**
     * 查询当前mysql数据库rowMetaData信息
     *
     * @return
     * @throws IOException
     */
    private BinlogRowMetadataEnum fecthBinlogRowMetadata() throws IOException {
        ComQueryCommand queryCommand = new ComQueryCommand(Sql.show_global_variables_like_binlog_row_metadata);
        channel.sendCommand(queryCommand);
        TextResultSetPacket textResultSetPacket = channel.readTextResultSetPacket(this.clientCapabilities);
        return BinlogRowMetadataEnum.getEnum(textResultSetPacket);
    }

    /**
     * 设置会话的checkSum
     * 设置服务端返回结果时不做编码转化，直接按照数据库的二进制编码进行发送，由客户端自己根据需求进行编码转化
     * <p>
     * set @master_binlog_checksum= @@global.binlog_checksum
     * <p>
     * mysql5.6针对checksum支持需要设置session变量如果不设置会出现错误：
     * <p>
     * Slave can not handle replication events with the checksum that master is configured to log
     * <p>
     * 但也不能乱设置，需要和mysql server的checksum配置一致，不然RotateLogEvent会出现乱码。'@@global.binlog_checksum'需要去掉单引号,在mysql 5.6.29下导致master退出
     * <p>
     * <a href="https://blog.csdn.net/qq_24313635/article/details/122681407">参考博客1</a>
     * <p>
     * <a href="https://www.jianshu.com/p/0957a89d4fb4">参考博客2</a>
     *
     * @param checkSum
     * @throws IOException
     */
    private void setCheckSum(BinlogCheckSumEnum checkSum) throws IOException {
        log.debug("【设置check sum】start");
        ComQueryCommand queryCommand = new ComQueryCommand(Sql.set_master_binlog_checksum_global_binlog_checksum);
        channel.sendCommand(queryCommand);
        this.checkPacket(channel.readDataContent());
        log.debug("【设置check sum】end");
    }

    /**
     * mysql5.6需要设置slave_uuid避免被server kill链接
     * <p>
     * <a href="https://github.com/alibaba/canal/issues/284">参考</a>
     *
     * @throws IOException
     */
    private void setSlaveUUID() throws IOException {
        log.debug("【设置slave uuid】start");
        ComQueryCommand queryCommand = new ComQueryCommand(Sql.set_slave_uuid);
        channel.sendCommand(queryCommand);
        this.checkPacket(channel.readDataContent());
        log.debug("【设置slave uuid】end");
    }


    private byte[] readDataContent() throws IOException {
        return channel.readDataContent();
    }

    /**
     * 检查响应报文
     * {@see https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_err_packet.html}
     *
     * @param bytes
     * @throws IOException
     */
    private void checkPacket(byte[] bytes) throws IOException {
        this.channel.checkPacket(bytes, this.clientCapabilities);
    }

    private ByteArrayIndexInputStream checkBinlogPacket(byte[] bytes) throws IOException {
        this.checkPacket(bytes);
        ByteArrayIndexInputStream indexInputStream = new ByteArrayIndexInputStream(bytes);
        return indexInputStream;
    }

    public void addClientCapabilities(CapabilitiesFlagsEnum e) {
        this.clientCapabilities = CapabilitiesFlagsEnum.add(this.clientCapabilities, e);
    }

    public void registerEventListener(EventListener eventListener) {
        eventListenerList.add(eventListener);
    }


}
