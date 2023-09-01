package com.cl.mysql.binlog.network;

import com.cl.mysql.binlog.constant.CapabilitiesFlagsEnum;
import com.cl.mysql.binlog.constant.ResultSetMetadataEnum;
import com.cl.mysql.binlog.exception.ServerException;
import com.cl.mysql.binlog.network.command.Command;
import com.cl.mysql.binlog.network.protocol.packet.*;
import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;
import com.cl.mysql.binlog.stream.ByteArrayIndexOutputStream;
import com.cl.mysql.binlog.util.PacketUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @description: mysql对所有的消息采用一个通用的模式进行包装，即  | Data Length（3 bytes）| Package Sequence（1 byte）| Data Content |
 * @author: liuzijian
 * @time: 2023-08-10 10:29
 */
@Slf4j
public class PacketChannel {

    private Socket socket;

    private ByteArrayIndexInputStream inputStream;

    private ByteArrayIndexOutputStream outputStream;

    /**
     * @描述 包序号 <br>
     * mysql包序号就是一个完整流程的发包序号，就是由该流程的发出的第一包是从0x00开始的，以后不管回复的包还是分包的包都会在上面加1，直到包序号达到0xff，再从0x001开始计数。
     * <p>
     * 比如我要查询语句"select * from 某表",那么我会发送出第一个包含查询语句"select * from 某表"的mysql包给mysql数据库，这个包的序号是0x00，这时，流程开始，以后的mysql数据库回复的包都会从0x01开始计数，直到回复结束，这时，该流程就结束了。我要查询新的语句就代表新的流程开始，mysql包序号计数重新从0x00开始。
     */
    private int sequence;

    /**
     * 是否已经登录数据库
     */
    @Setter
    private boolean hasAuth;

    @Getter
    private boolean sslSocket;

    public PacketChannel(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.inputStream = new ByteArrayIndexInputStream(this.socket.getInputStream());
        this.outputStream = new ByteArrayIndexOutputStream(this.socket.getOutputStream());
    }

    /**
     * @return
     * @描述 <p> 直接读取Data Content
     * <p> mysql 报文顺序 | Data Length（3 bytes）| Package Sequence（1 byte）| Data Content |
     */
    public byte[] readDataContent() throws IOException {
        int length = this.inputStream.readInt(3);
        int sequence = this.inputStream.read();
        if (sequence != this.sequence) {
            throw new RuntimeException("包序号不一致 当前报文包序号为：" + sequence + "，当前存储序号为：" + this.sequence);
        } else {
            this.sequence++;
        }
        return this.inputStream.readBytes(length);
    }

    public byte[] readBinlogStream() throws IOException {
        int length = this.inputStream.readInt(3);
        if(length == -1){
            throw new RuntimeException("mysql服务器已断开连接");
        }
        int sequence = this.inputStream.read();
        return this.inputStream.readBytes(length);
    }

    /**
     * 读取结果包<a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_query_response_text_resultset.html">相关文档</a><br>
     * <li>第一个包：列长度包 只有一个列的长度</li>
     * <li>紧跟着就是n个字段描述包，每一个字段描述就是一个包（<a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_query_response_text_resultset_column_definition.html">Protocol::ColumnDefinition</a>）
     * 所有字段信息描述后，会发送一个EOF包作为字段定义与数据（Row）的分隔符号。取决于是否设置了CLIENT_DEPRECATE_EOF标记，如果设置了，就不会反回EOF包
     * </li>
     * <li>接下来是行数据包，每一行一个数据包，也包括包头与报文体。</li>
     * <li>最后的结束包，同样可能是EOF或OK包</li>
     */
    public TextResultSetPacket readTextResultSetPacket(int clientCapabilities) throws IOException {
        ByteArrayIndexInputStream in = new ByteArrayIndexInputStream(this.readDataContent());
        int metadataFollows = -1;
        if (CapabilitiesFlagsEnum.has(clientCapabilities, CapabilitiesFlagsEnum.CLIENT_OPTIONAL_RESULTSET_METADATA)) {
            metadataFollows = in.readInt(1);
        }
        // 列长度
        int columnLength = in.readLenencInteger().intValue();
        List<ColumnDefinitionPacket> columnDefinitionPacketList = new ArrayList<>(columnLength);
        if (
                !CapabilitiesFlagsEnum.has(clientCapabilities, CapabilitiesFlagsEnum.CLIENT_OPTIONAL_RESULTSET_METADATA) ||
                        metadataFollows == ResultSetMetadataEnum.RESULTSET_METADATA_FULL.ordinal()
        ) {
            // 过滤字段描述包
            for (int i = 0; i < columnLength; i++) {
                columnDefinitionPacketList.add(new ColumnDefinitionPacket(this.readDataContent(), clientCapabilities));
            }
        }
        List<TextResultRowPacket> resultList = new ArrayList<>();

        // 当配置了CLIENT_DEPRECATE_EOF 的时候，最后一个EOF包就不会发送了
        if (!CapabilitiesFlagsEnum.has(clientCapabilities, CapabilitiesFlagsEnum.CLIENT_DEPRECATE_EOF)) {
            this.readDataContent();// Marker to set the end of metadata 以eof包结束字段描述包
        }
        // 获取行数据包
        for (byte[] bytes; !PacketUtil.isEOFPacket((bytes = this.readDataContent())[0]) && !PacketUtil.isOkPacket(bytes[0]); ) {
            resultList.add(new TextResultRowPacket(bytes, clientCapabilities));
        }
        return new TextResultSetPacket(columnDefinitionPacketList, resultList);
    }

    /**
     * 检查响应报文
     * {@see https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_err_packet.html}
     *
     * @param bytes
     * @throws IOException
     */
    public void checkPacket(byte[] bytes, int clientCapabilities) throws IOException {
        // 如果错误的话 会返回0xff
        if (PacketUtil.isErrorPacket(bytes[0])) {
            // 不拷贝第一个字节
            bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
            ErrorPacket errorPacket = new ErrorPacket(bytes, clientCapabilities);
            throw new ServerException(
                    errorPacket.getErrorMessage(),
                    errorPacket.getErrorCode(),
                    errorPacket.getSqlState()
            );
        } else if (PacketUtil.isOkPacket(bytes[0])) {
            OkPacket okPacket = new OkPacket(Arrays.copyOfRange(bytes, 1, bytes.length), clientCapabilities);
            log.info("【操作成功】ok_packet：{}", okPacket);
        } else if (PacketUtil.isEOFPacket(bytes[0])) {// 文档上说等于0xfe为eof包 默认意思是无符号的意思，要用0xff相与
            EOFPacket eofPacket = new EOFPacket(Arrays.copyOfRange(bytes, 1, bytes.length));
            log.warn("警告：{}", eofPacket);
        }
    }

    public void sendCommand(Command command) throws IOException {
        ByteArrayIndexOutputStream out = new ByteArrayIndexOutputStream();
        byte[] body = command.toByteArray();
        int dataLength = body.length;
        out.writeInt(dataLength, 3);
        // 这里的sequenceId 每次发出新命令的时候要重置为0
        // https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_packets.html#sect_protocol_basic_packets_sequence_id
        if (hasAuth) {
            sequence = 0;
        }
        out.writeInt(sequence++, 1);

        out.write(body, 0, dataLength);
        this.outputStream.write(out.toByteArray());
        this.outputStream.flush();
    }

    public void changeToSSL() throws IOException, KeyManagementException, NoSuchAlgorithmException {
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null,
                new TrustManager[]{
                        new X509TrustManager() {

                            @Override
                            public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
                                    throws CertificateException {
                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
                                    throws CertificateException {
                            }

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }
                        }
                },
                null
        );
        SSLSocketFactory socketFactory = sslContext.getSocketFactory();
        SSLSocket sslSocket = (SSLSocket) socketFactory.createSocket(this.socket, this.socket.getInetAddress().getHostName(), this.socket.getPort(), true);
        sslSocket.startHandshake();
        this.socket = sslSocket;
        this.inputStream = new ByteArrayIndexInputStream(sslSocket.getInputStream());
        this.outputStream = new ByteArrayIndexOutputStream(sslSocket.getOutputStream());
        this.sslSocket = true;
    }



}
