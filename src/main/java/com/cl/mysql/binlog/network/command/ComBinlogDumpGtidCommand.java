package com.cl.mysql.binlog.network.command;

import com.cl.mysql.binlog.constant.CommandTypeEnum;
import com.cl.mysql.binlog.stream.ByteArrayIndexOutputStream;

import java.io.IOException;

/**
 * <a href="https://github.com/mysql/mysql-server/blob/8.0/sql/rpl_source.cc">源码第984行~1046行</a>
 *
 * @description: TODO 未完成 主要是gtidSet的数据结构没搞清楚
 * @author: liuzijian
 * @time: 2023-09-20 14:09
 */
public class ComBinlogDumpGtidCommand implements Command {

    private final Integer serverId;

    private final String binlogFileName;

    private final Long binlogPos;

    private Object gtidSet;

    public ComBinlogDumpGtidCommand(Integer serverId, String binlogFileName, long binlogPos, Object gtidSet) {
        this.serverId = serverId;
        this.binlogFileName = binlogFileName;
        this.binlogPos = binlogPos;
        this.gtidSet = gtidSet;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayIndexOutputStream out = new ByteArrayIndexOutputStream();
        out.writeInt(CommandTypeEnum.COM_BINLOG_DUMP_GTID.ordinal(), 1);
        out.writeInt(0, 2);// flags always 0
        out.writeInt(this.serverId, 4);
        out.writeInt(this.binlogFileName.length(), 4);
        out.write(this.binlogFileName.getBytes());
        out.writeLong(this.binlogPos, 8);
        return new byte[0];
    }
}
