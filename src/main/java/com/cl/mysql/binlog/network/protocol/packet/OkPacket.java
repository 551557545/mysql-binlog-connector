package com.cl.mysql.binlog.network.protocol.packet;

import com.cl.mysql.binlog.constant.CapabilitiesFlagsEnum;
import com.cl.mysql.binlog.constant.ServerStatusEnum;
import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;
import lombok.Getter;
import lombok.ToString;

import java.io.IOException;

/**
 * @description: {@see https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_ok_packet.html}
 * @author: liuzijian
 * @time: 2023-08-14 15:37
 */
@Getter
@ToString
public class OkPacket {

    private final Number affectedRow;
    private final Number insertId;
    private final int statusFlags;
    private final int numberOfWarnings;
    private final String message;

    public OkPacket(byte[] bytes, int clientCapabilities) throws IOException {
        ByteArrayIndexInputStream buffer = new ByteArrayIndexInputStream(bytes);
        affectedRow = buffer.readLenencInteger();// affected_rows
        insertId = buffer.readLenencInteger();// last insert-id
        statusFlags = buffer.readInt(2); // status_flags
        if (CapabilitiesFlagsEnum.has(clientCapabilities, CapabilitiesFlagsEnum.CLIENT_PROTOCOL_41)) {
            numberOfWarnings = buffer.readInt(2);// number of warnings
        } else {
            numberOfWarnings = 0;
        }
        if (CapabilitiesFlagsEnum.has(clientCapabilities, CapabilitiesFlagsEnum.CLIENT_SESSION_TRACK)) {
            message = buffer.readStringTerminatedByZero();// message
        } else {
            message = "";
        }

        if ((statusFlags & ServerStatusEnum.SERVER_SESSION_STATE_CHANGED.getCode()) > 0) {
            // TODO 说明服务器状态有变化 对于只监听binlog来说 可以不处理
        } else {

        }
    }

}
