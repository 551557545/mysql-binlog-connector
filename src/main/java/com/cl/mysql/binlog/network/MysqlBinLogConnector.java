package com.cl.mysql.binlog.network;

import com.cl.mysql.binlog.constant.CapabilitiesFlagsEnum;
import com.cl.mysql.binlog.constant.Sql;
import com.cl.mysql.binlog.network.command.ComQueryCommand;
import com.cl.mysql.binlog.network.command.HandShakeResponseCommand;
import com.cl.mysql.binlog.network.command.SSLCommand;
import com.cl.mysql.binlog.network.command.SSLProtocol41Command;
import com.cl.mysql.binlog.network.protocol.InitialHandshakeProtocol;
import com.cl.mysql.binlog.network.protocol.packet.TextResultSetPacket;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

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

    /**
     * 请求mysql服务器获取binlog的dump
     */
    public void sendComBingLogDump() throws IOException {
        ComQueryCommand queryCommand = new ComQueryCommand(Sql.show_master_status);
        channel.sendCommand(queryCommand);
        TextResultSetPacket textResultSetPacket = channel.readTextResultSetPacket(this.clientCapabilities);
        //TODO 做binlog监听
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

    public void addClientCapabilities(CapabilitiesFlagsEnum e) {
        this.clientCapabilities = CapabilitiesFlagsEnum.add(this.clientCapabilities, e);
    }


}
