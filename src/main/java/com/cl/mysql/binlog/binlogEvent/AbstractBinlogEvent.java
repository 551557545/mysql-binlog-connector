package com.cl.mysql.binlog.binlogEvent;

import com.cl.mysql.binlog.constant.BinlogCheckSumEnum;
import com.cl.mysql.binlog.constant.BinlogEventTypeEnum;
import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;
import lombok.Getter;

import java.io.IOException;

/**
 * @description:
 * @author: liuzijian
 * @time: 2023-09-05 17:28
 */
@Getter
public abstract class AbstractBinlogEvent {

    private final BinlogEventTypeEnum eventType;

    /**
     * @param in
     * @param bodyLength eventSize 减去 checkSum之后的值，而FormatDescriptionEvent事件会多减去一个1
     */
    public AbstractBinlogEvent(BinlogEventTypeEnum binlogEvent, ByteArrayIndexInputStream in, int bodyLength, BinlogCheckSumEnum checkSum) throws IOException {
        this.eventType = binlogEvent;
    }

}
