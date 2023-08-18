package com.cl.mysql.binlog.network.command;

import com.cl.mysql.binlog.constant.CapabilitiesFlagsEnum;
import com.cl.mysql.binlog.network.protocol.InitialHandshakeProtocol;
import com.cl.mysql.binlog.stream.ByteArrayIndexOutputStream;

import java.io.IOException;

/**
 * @description: {@see https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_packets_protocol_ssl_request.html}
 * @author: liuzijian
 * @time: 2023-08-11 09:22
 */
public class SSLProtocol41Command implements Command {

    private int clientCapabilities;

    private final int charsetNum;

    public SSLProtocol41Command(InitialHandshakeProtocol handshakeProtocol) {
        this.clientCapabilities = handshakeProtocol.getClientCapabilities();
        this.charsetNum = handshakeProtocol.getCharsetNum();
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayIndexOutputStream out = new ByteArrayIndexOutputStream();
        if (this.clientCapabilities != 0) {
            this.clientCapabilities = 0;
            // 查询所有字段权限
            this.clientCapabilities |= CapabilitiesFlagsEnum.CLIENT_LONG_FLAG.getCode();
            // 基于41 ssl Request协议
            this.clientCapabilities |= CapabilitiesFlagsEnum.CLIENT_PROTOCOL_41.getCode();
            this.clientCapabilities |= CapabilitiesFlagsEnum.CLIENT_RESERVED2.getCode();
            // 支持密码插件
            this.clientCapabilities |= CapabilitiesFlagsEnum.CLIENT_PLUGIN_AUTH.getCode();
        }
        this.clientCapabilities |= CapabilitiesFlagsEnum.CLIENT_SSL.getCode();
        out.writeInt(this.clientCapabilities, 4);
        out.writeInt(0, 4);
        out.writeInt(this.charsetNum, 1);
        // 写23个0
        out.write(new byte[23]);
        return out.toByteArray();
    }
}
