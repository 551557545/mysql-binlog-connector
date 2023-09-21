package com.cl.mysql.binlog.binlogEvent;

import com.cl.mysql.binlog.constant.BinlogCheckSumEnum;
import com.cl.mysql.binlog.constant.BinlogEventTypeEnum;
import com.cl.mysql.binlog.network.BinlogEnvironment;
import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;

import java.io.IOException;

/**
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/classbinary__log_1_1Gtid__event.html">官方文档</a><br>
 * <a href="https://github.com/mysql/mysql-server/blob/8.0/libbinlogevents/src/control_events.cpp">mysql8.0源码</a>
 *
 * @author: liuzijian
 * @time: 2023-09-20 10:03
 */
public class GtidEvent extends AbstractBinlogEvent {

    /**
     * 00000001 = Transaction may have changes logged with SBR. <br>
     * In 5.6, 5.7.0-5.7.18, and 8.0.0-8.0.1, this flag is always set. Starting in 5.7.19 and 8.0.2, this flag is cleared if the transaction only contains row events.<br>
     * It is set if any part of the transaction is written in statement format.<br>
     * <p>
     * 00000001 =事务可能有SBR记录的更改。<br>
     * 在5.6、5.7.0-5.7.18和8.0.0-8.0.1中，这个标志总是被设置的。<br>
     * 从5.7.19和8.0.2开始，如果事务只包含行事件，则清除此标志。如果事务的任何部分以报表格式编写，则设置该参数。<br>
     * </p>
     */
    private final Integer gtidFlags;

    /**
     * UUID representing the SID
     * <p>
     * 表示SID的UUID
     * </p>
     */
    private final String SID;

    /**
     * Group number, second component of GTID.
     * <p>
     * 组号，GTID的第二部分。
     * </p>
     */
    private final Long GNO;

    /**
     * The type of logical timestamp used in the logical clock fields.
     * <p>
     *     逻辑时钟字段中使用的逻辑时间戳类型。
     * </p>
     */
    private final Integer logicalClockTimestampTypeCode;

    /**
     * Store the transaction's commit parent sequence_number
     * <p>
     *     存储事务的提交父序列
     * </p>
     */
    private final Long lastCommitted;

    /**
     * The transaction's logical timestamp assigned at prepare phase
     * <p>
     *     事务的逻辑时间戳在准备阶段分配
     * </p>
     */
    private final Long sequenceNumber;

    private final Long immediateCommitTimestamp;

    private final Long originalCommitTimestamp;

    private final Object transactionLength;

    private final Integer immediateServerVersion;

    private final Integer originalServerVersion;

    /**
     * @param environment
     * @param binlogEvent
     * @param in
     * @param bodyLength  eventSize 减去 checkSum之后的值，而FormatDescriptionEvent事件会多减去一个1
     * @param checkSum
     */
    public GtidEvent(BinlogEnvironment environment, BinlogEventTypeEnum binlogEvent, ByteArrayIndexInputStream in, int bodyLength, BinlogCheckSumEnum checkSum) throws IOException {
        super(environment, binlogEvent, in, bodyLength, checkSum);
        this.gtidFlags = in.readInt(1);
        this.SID = in.readString(16);
        this.GNO = in.readLong(8);
        this.logicalClockTimestampTypeCode = in.readInt(1);
        this.lastCommitted = in.readLong(8);
        this.sequenceNumber = in.readLong(8);
        this.immediateCommitTimestamp = in.readLong(7);
        this.originalCommitTimestamp = in.readLong(7);
        this.transactionLength = in.readLenencInteger().intValue();
        this.immediateServerVersion = in.readInt(4);
        this.originalServerVersion = in.readInt(4);
        System.out.println(1);
    }
}
