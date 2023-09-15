package com.cl.mysql.binlog.binlogEvent;

import com.cl.mysql.binlog.constant.BinlogCheckSumEnum;
import com.cl.mysql.binlog.constant.BinlogEventEnum;
import com.cl.mysql.binlog.entity.Row;
import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;
import com.cl.mysql.binlog.util.BitMapUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.BitSet;

/**
 * @description: <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/classbinary__log_1_1Update__rows__event.html">官方文档update_row_event</a>
 * 父类：
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/classbinary__log_1_1Rows__event.html">row_event</a>
 * @author: liuzijian
 * @time: 2023-09-11 16:34
 */
@Slf4j
@Getter
public class UpdateRowsEvent extends AbstractBinlogEvent {

    private final Long tableId;
    private final Integer flags;
    /**
     * Represents the number of columns in the table
     * <p>
     * 表示表中的列数
     * </p>
     */
    private final Integer width;
    /**
     * Indicates whether each column is used, one bit per column.<br>
     * For this field, the amount of storage required is INT((width + 7) / 8) bytes.
     * <p>
     * 是否每列使用，每列1位。<br>
     * 对于这个字段，所需的存储量是INT((width + 7) / 8)字节。
     * </p>
     */
    private final BitSet columnsBeforeImage;

    private final BitSet columnsAfterImage;

    private final Row beforeRow;

    private final Row afterRow;

    /**
     * @param in
     * @param bodyLength eventSize 减去 checkSum之后的值
     * @param checkSum
     */
    public UpdateRowsEvent(BinlogEventEnum binlogEvent, ByteArrayIndexInputStream in, int bodyLength, BinlogCheckSumEnum checkSum) throws IOException {
        super(binlogEvent, in, bodyLength, checkSum);

        this.tableId = in.readLong(6);
        this.flags = in.readInt(2);

        /**
         * 根据源码 https://github.com/mysql/mysql-server/blob/8.0/libbinlogevents/src/rows_event.cpp  的方法
         * Rows_event::Rows_event(const char *buf, const Format_description_event *fde) : Binary_log_event(&buf, fde)
         * 得出一个结论。extractInfo应该是不会传过来，所以按照他的处理逻辑
         * extractInfo 长度 读取两个字节的int 在减去 2
         */
        int extractInfoLength = in.readInt(2) - 2;
        if (extractInfoLength > 0) {
            //TODO 解析extractInfo 现阶段先跳过
            in.skip(extractInfoLength);
        }
        this.width = in.readLenencInteger().intValue();
        // 根据源码 https://github.com/mysql/mysql-server/blob/8.0/libbinlogevents/src/rows_event.cpp 第469到473行得知，updateRowsEvent才有两个columnsImage
        this.columnsBeforeImage = BitMapUtil.convertByBigEndianArray(this.width, 0, in.readBytes((this.width + 7) / 8));
        /**
         *  +-------------------------------------------------------+
         *  | Event Type | Cols_before_image | Cols_after_image     |
         *  +-------------------------------------------------------+
         *  |  DELETE    |   Deleted row     |    NULL              |
         *  |  INSERT    |   NULL            |    Inserted row      |
         *  |  UPDATE    |   Old     row     |    Updated row       |
         *  +-------------------------------------------------------+
         */
        this.columnsAfterImage = BitMapUtil.convertByBigEndianArray(this.width, 0, in.readBytes((this.width + 7) / 8));


        this.beforeRow = new Row(this.tableId, in);
        this.afterRow = new Row(this.tableId, in);
        if (in.available() != checkSum.getLength()) {
            throw new RuntimeException("解析updateRowEvent错误");
        }
    }


}
