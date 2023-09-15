package com.cl.mysql.binlog.binlogEvent;

import com.cl.mysql.binlog.constant.BinlogCheckSumEnum;
import com.cl.mysql.binlog.constant.BinlogEventEnum;
import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;
import lombok.Getter;

import java.io.IOException;

/**
 * @description: <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/classbinary__log_1_1Query__event.html">官方文档</a>
 * @author: liuzijian
 * @time: 2023-09-07 17:13
 */
@Getter
public class QueryEvent extends AbstractBinlogEvent {

    /**
     * The ID of the thread that issued this statement. It is needed for temporary tables.
     * <p>
     * 发出该语句的线程的ID。
     */
    private final Integer threadId;

    /**
     * The time from when the query started to when it was logged in the binlog, in seconds.
     * <p>
     * 从查询开始到它被记录在binlog中的时间，以秒为单位
     */
    private final Long queryExecTime;

    /**
     * The length of the name of the currently selected database.
     * <p>
     * 当前选择的数据库名称的长度。
     */
    private final Integer dbLen;

    /**
     * Error code generated by the master.  If the master fails, the slave will fail with the same error code.
     * <p>
     * 主机产生的错误码。如果主服务器失败，从服务器也会失败，并产生相同的错误码。
     */
    private final Integer errorCode;

    /**
     * The length of the status_vars block of the Body, in bytes. This is not present for binlog version 1 and 3
     * <p>
     * Body的status_vars块的长度，以字节为单位。
     */
    private final Integer statusVarsLen;

    /**
     * Zero or more status variables.  Each status variable consists of one byte identifying the variable stored, followed by the value of the variable.  The possible variables are listed separately in the table below.  MySQL always writes events in the order defined below;  however, it is capable of reading them in any order.
     * <p>
     * 零个或多个状态变量。每个状态变量由一个字节组成，该字节标识所存储的变量，后跟该变量的值。可能的变量在下表中分别列出。MySQL总是按照下面定义的顺序写事件;然而，它能够以任何顺序读取它们。
     */
    private final byte[] statusVars;

    /**
     * The currently selected database, as a null-terminated string.(The trailing zero is redundant since the length is already known;  it is db_len from Post-Header.)
     * <p>
     * 当前选择的数据库，作为以空结尾的字符串。(后面的零是多余的，因为长度是已知的;它是db_len from Post-Header。)
     */
    private final String mDb;

    /**
     * SQL查询
     */
    private final String mQuery;

    /**
     * @param in
     * @param bodyLength eventSize 减去 checkSum之后的值
     */
    public QueryEvent(BinlogEventEnum binlogEvent, ByteArrayIndexInputStream in, int bodyLength, BinlogCheckSumEnum checkSum) throws IOException {
        super(binlogEvent, in, bodyLength, checkSum);
        this.threadId = in.readInt(4);
        this.queryExecTime = in.readInt(4) * 1000L;
        this.dbLen = in.readInt(1);
        this.errorCode = in.readInt(2);
        this.statusVarsLen = in.readInt(2);

        this.statusVars = in.readBytes(this.statusVarsLen);
        this.mDb = in.readString(dbLen);
        in.skip(1);// 去掉数据库名的0
        this.mQuery = in.readString(bodyLength - 4 - 4 - 1 - 2 - 2 - statusVarsLen - dbLen - 1);
    }
}
