package com.cl.mysql.binlog.network.protocol.packet;

import com.cl.mysql.binlog.constant.CapabilitiesFlagsEnum;
import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;
import lombok.Getter;

import java.io.IOException;

/**
 * @description: 错误码
 * <p> {@see https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_err_packet.html}
 * <p> 也就是当发生了错误之后，服务端发送给客户端的报文。
 * <p> MySQL 的错误包含了三部分：A) MySQL 特定的错误码，数字类型，不通用；B) SQLSTATE，为 5 个字符的字符串，采用 ANSI SQL 和 ODBC 的标准；C) 错误信息。
 * <p> 对于错误报文的格式可以参照参考文件，其中第二字节表示由 MySQL 定义的错误编码，服务器状态实际是 ANSI SQL 对应的编码，两者并非一一对应。
 * @author: liuzijian
 * @time: 2023-08-10 16:02
 */
@Getter
public class ErrorPacket {

    /**
     * 错误码
     */
    private int errorCode;
    /**
     * sqlState 5个字符的字符串
     */
    private String sqlState;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 调用之前 请去掉第一header字节
     *
     * @param bytes
     * @param clientCapabilities
     * @throws IOException
     */
    public ErrorPacket(byte[] bytes, int clientCapabilities) throws IOException {
        ByteArrayIndexInputStream buffer = new ByteArrayIndexInputStream(bytes);
        this.errorCode = buffer.readInt(2);
        if (CapabilitiesFlagsEnum.has(clientCapabilities, CapabilitiesFlagsEnum.CLIENT_PROTOCOL_41)) {
            if (buffer.read() == '#') {
                this.sqlState = buffer.readString(5);
            }
            this.errorMessage = "[" + this.errorCode + "]" + buffer.readString(buffer.available());
        } else {
            this.errorMessage = buffer.readString(buffer.available());
        }
    }
}
