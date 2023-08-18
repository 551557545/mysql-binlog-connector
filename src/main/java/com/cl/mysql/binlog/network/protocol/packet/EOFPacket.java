package com.cl.mysql.binlog.network.protocol.packet;

import com.cl.mysql.binlog.network.protocol.InitialHandshakeProtocol;
import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;
import lombok.ToString;

import java.io.IOException;

/**
 * @description: {@see https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_eof_packet.html}
 * @author: liuzijian
 * @time: 2023-08-14 16:58
 */
@ToString
public class EOFPacket {

    private final int warningNum;

    private final int statusFlags;

    private final String message;

    public EOFPacket(byte[] bytes, InitialHandshakeProtocol handshakeProtocol) throws IOException {
        ByteArrayIndexInputStream input = new ByteArrayIndexInputStream(bytes);
        this.warningNum = input.readInt(2);
        this.statusFlags = input.readInt(2);
        this.message = input.readStringTerminatedByZero();
    }

}
