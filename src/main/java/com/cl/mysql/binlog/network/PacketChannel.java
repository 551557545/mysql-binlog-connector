package com.cl.mysql.binlog.network;

import com.cl.mysql.binlog.network.command.Command;
import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;
import com.cl.mysql.binlog.stream.ByteArrayIndexOutputStream;
import lombok.Getter;
import lombok.Setter;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @description: mysql对所有的消息采用一个通用的模式进行包装，即  | Data Length（3 bytes）| Package Sequence（1 byte）| Data Content |
 * @author: liuzijian
 * @time: 2023-08-10 10:29
 */
public class PacketChannel {

    private Socket socket;

    private ByteArrayIndexInputStream inputStream;

    private ByteArrayIndexOutputStream outputStream;

    /**
     * @描述 包序号 <br>
     * mysql包序号就是一个完整流程的发包序号，就是由该流程的发出的第一包是从0x00开始的，以后不管回复的包还是分包的包都会在上面加1，直到包序号达到0xff，再从0x001开始计数。
     *
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
