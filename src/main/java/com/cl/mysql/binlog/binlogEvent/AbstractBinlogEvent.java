package com.cl.mysql.binlog.binlogEvent;

import com.cl.mysql.binlog.constant.BinlogCheckSumEnum;
import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;

import java.io.IOException;

/**
 * @description:
 * @author: liuzijian
 * @time: 2023-09-05 17:28
 */
public abstract class AbstractBinlogEvent {

    /**
     * @param in
     * @param bodyLength eventSize 减去 checkSum之后的值
     */
    public AbstractBinlogEvent(ByteArrayIndexInputStream in, int bodyLength, BinlogCheckSumEnum checkSum) throws IOException {

    }

}