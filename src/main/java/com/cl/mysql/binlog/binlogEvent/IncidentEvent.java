package com.cl.mysql.binlog.binlogEvent;

import com.cl.mysql.binlog.constant.BinlogCheckSumEnum;
import com.cl.mysql.binlog.constant.BinlogEventTypeEnum;
import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;
import lombok.Getter;

import java.io.IOException;

/**
 * @description: <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/classbinary__log_1_1Incident__event.html">官方文档</a>
 * @author: liuzijian
 * @time: 2023-09-07 17:39
 */
@Getter
public class IncidentEvent extends AbstractBinlogEvent {

    private final Integer incident;

    private final Integer msglen;

    private final String message;

    /**
     * @param in
     * @param bodyLength eventSize 减去 checkSum之后的值
     */
    public IncidentEvent(BinlogEventTypeEnum binlogEvent, ByteArrayIndexInputStream in, int bodyLength, BinlogCheckSumEnum checkSum) throws IOException {
        super(binlogEvent, in, bodyLength, checkSum);
        this.incident = in.readInt(2);
        this.msglen = in.readInt(1);
        this.message = in.readString(msglen);
    }
}
