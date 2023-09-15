package com.cl.mysql.binlog.binlogEvent;

import com.cl.mysql.binlog.constant.BinlogCheckSumEnum;
import com.cl.mysql.binlog.constant.BinlogEventEnum;
import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;
import lombok.Getter;

import java.io.IOException;

/**
 * @description: <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/classbinary__log_1_1Rotate__event.html">文档</a>
 * <p>
 * The rotate event is added to the binlog as last event to tell the reader what binlog to request next.
 * <p>
 * 旋转事件：告诉监听者下一步请求的binlog位点信息
 * @author: liuzijian
 * @time: 2023-09-05 17:57
 */
@Getter
public class RotateEvent extends AbstractBinlogEvent {

    /**
     * 8 byte integer
     * <p>
     * The position within the binary log to rotate to.
     * <p>
     * binlog位点位置
     */
    private final int position;

    /**
     * variable length string without trailing zero, extending to the end of the event (determined by the length field of the Common-Header)
     * <p>
     * 当前binlog文件名
     */
    private final String newLogIdent;

    public RotateEvent(BinlogEventEnum binlogEvent, ByteArrayIndexInputStream in, int bodyLength, BinlogCheckSumEnum checkSum) throws IOException {
        super(binlogEvent, in, bodyLength, checkSum);
        this.position = in.readInt(8);
        this.newLogIdent = in.readString(bodyLength - 8);
    }
}
