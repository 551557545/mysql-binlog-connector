package com.cl.mysql.binlog.binlogEvent;

import com.cl.mysql.binlog.constant.BinlogCheckSumEnum;
import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;
import lombok.Getter;

import java.io.IOException;

/**
 * @description: 8位的无符号xid：<a href="https://dev.mysql.com/doc/dev/mysql-server/latest/classbinary__log_1_1Xid__event.html">相关文档</a>
 * @author: liuzijian
 * @time: 2023-09-05 17:28
 */
@Getter
public class XIDEvent extends AbstractBinlogEvent {

    private final Integer xid;

    public XIDEvent(ByteArrayIndexInputStream in, int bodyLength, BinlogCheckSumEnum checkSum) throws IOException {
        super(in, bodyLength, checkSum);
        this.xid = in.readInt(8);
    }

}
