package com.cl.mysql.binlog.binlogEvent;

import com.cl.mysql.binlog.constant.BinlogCheckSumEnum;
import com.cl.mysql.binlog.constant.BinlogEventTypeEnum;
import com.cl.mysql.binlog.network.BinlogEnvironment;
import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;
import lombok.Getter;

import java.io.IOException;

/**
 * @description: <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/classbinary__log_1_1Intvar__event.html">官方文档</a>
 * @author: liuzijian
 * @time: 2023-09-05 17:50
 */
@Getter
public class IntvarEvent extends AbstractBinlogEvent {

    /**
     * One byte identifying the type of variable stored. Currently, two identifiers are supported: LAST_INSERT_ID_EVENT == 1 and INSERT_ID_EVENT == 2.
     */
    private final Integer type;

    /**
     * The value of the variable.
     */
    private final Integer val;

    public IntvarEvent(BinlogEnvironment environment, BinlogEventTypeEnum binlogEvent, ByteArrayIndexInputStream in, int bodyLength, BinlogCheckSumEnum checkSum) throws IOException {
        super(environment, binlogEvent, in, bodyLength, checkSum);
        this.type = in.readInt(1);
        this.val = in.readInt(8);
    }
}
