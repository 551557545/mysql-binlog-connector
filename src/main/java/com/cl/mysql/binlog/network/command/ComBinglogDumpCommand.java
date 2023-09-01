package com.cl.mysql.binlog.network.command;

import com.cl.mysql.binlog.constant.CommandTypeEnum;
import com.cl.mysql.binlog.stream.ByteArrayIndexOutputStream;

import java.io.IOException;

/**
 * @description: 要监听binlog，需要先向服务器发送COM_BINLOG_DUMP <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_binlog_dump.html">文档</a>
 * @author: liuzijian
 * @time: 2023-08-21 10:51
 */
public class ComBinglogDumpCommand implements Command {

    /**
     * binlog文件的位点
     */
    private final int binlogPos;

    /**
     * 该服务器的服务器id，应该是握手协议返回的threadId
     */
    private final int serverId;

    /**
     * 要监听的binlog文件名
     */
    private final String binlogFileName;

    public ComBinglogDumpCommand(String binlogFileName, int binlogPos, int serverId) {
        this.binlogPos = binlogPos;
        this.serverId = serverId;
        this.binlogFileName = binlogFileName;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayIndexOutputStream out = new ByteArrayIndexOutputStream();
        out.writeInt(CommandTypeEnum.COM_BINLOG_DUMP.ordinal(), 1);
        out.writeInt(this.binlogPos, 4);
        // BINLOG_DUMP_NON_BLOCK = 1  If there is no more events to send send a ERR_Packet instead of blocking the connection.
        // 当没有binlog事件的时候 ：如果填1 那么会发一个err_packet过来，如果填0，则会阻塞
        out.writeInt(0, 2);
        out.writeInt(this.serverId, 4);
        out.writeNullTerminatedString(this.binlogFileName);
        return out.toByteArray();
    }
}
