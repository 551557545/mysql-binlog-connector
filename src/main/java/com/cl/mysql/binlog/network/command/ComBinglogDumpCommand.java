package com.cl.mysql.binlog.network.command;

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
     *
     */
    private final int flags;

    /**
     * 该服务器的服务器id，应该是握手协议返回的threadId
     */
    private final int serverId;

    /**
     * 要监听的binlog文件名
     */
    private final String binlogFileName;

    public ComBinglogDumpCommand(int binlogPos, int flags, int serverId, String binlogFileName) {
        this.binlogPos = binlogPos;
        this.flags = flags;
        this.serverId = serverId;
        this.binlogFileName = binlogFileName;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayIndexOutputStream out = new ByteArrayIndexOutputStream();
        out.writeInt(0x12, 1);
        out.writeInt(this.binlogPos, 4);
        out.writeInt(this.flags,2);
        out.writeInt(this.serverId,4);
        out.writeNullTerminatedString(this.binlogFileName);
        return out.toByteArray();
    }
}
