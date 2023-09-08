package com.cl.mysql.binlog.binlogEvent;

import com.cl.mysql.binlog.constant.BinlogCheckSumEnum;
import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;

import java.io.IOException;

/**
 * @description: <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/classHeartbeat__log__event__v2.html">官方文档</a>
 * @author: liuzijian
 * @time: 2023-09-07 17:31
 */
public class HeartbeatLogEventV2 extends AbstractBinlogEvent {

    /**
     * @param in
     * @param bodyLength eventSize 减去 checkSum之后的值
     */
    public HeartbeatLogEventV2(ByteArrayIndexInputStream in, int bodyLength, BinlogCheckSumEnum checkSum) throws IOException {
        super(in, bodyLength, checkSum);
    }
}
