package com.cl.mysql.binlog.network.command;

import com.cl.mysql.binlog.constant.CapabilitiesFlagsEnum;
import com.cl.mysql.binlog.network.protocol.InitialHandshakeProtocol;
import com.cl.mysql.binlog.stream.ByteArrayIndexOutputStream;

import java.io.IOException;

/**
 * TODO 这个非41协议的指令还未测试
 *
 * @description: {@see https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_packets_protocol_ssl_request.html}
 * @author: liuzijian
 * @time: 2023-08-11 09:37
 */
public class SSLCommand implements Command {

    private int clientCapabilities;

    private final InitialHandshakeProtocol handshakeProtocol;

    public SSLCommand(InitialHandshakeProtocol handshakeProtocol) {
        this.clientCapabilities = handshakeProtocol.getClientCapabilities();
        this.handshakeProtocol = handshakeProtocol;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayIndexOutputStream out = new ByteArrayIndexOutputStream();
        if (this.clientCapabilities == 0) {
            // 查询所有字段权限
            this.clientCapabilities |= CapabilitiesFlagsEnum.CLIENT_LONG_FLAG.getCode();
            this.clientCapabilities |= CapabilitiesFlagsEnum.CLIENT_RESERVED.getCode();
        }
        this.clientCapabilities |= CapabilitiesFlagsEnum.CLIENT_SSL.getCode();
        out.writeInt(this.clientCapabilities, 2);
        out.writeInt(0, 3);
        return out.toByteArray();
    }
}
