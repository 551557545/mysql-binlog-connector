package com.cl.mysql.binlog.binlogEvent;

import com.cl.mysql.binlog.constant.BinlogCheckSumEnum;
import com.cl.mysql.binlog.constant.BinlogEventTypeEnum;
import com.cl.mysql.binlog.entity.Row;
import com.cl.mysql.binlog.network.BinlogEnvironment;
import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;
import com.cl.mysql.binlog.util.BitMapUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/classbinary__log_1_1Update__rows__event.html">官方文档update_row_event</a>
 * 父类：<a href="https://dev.mysql.com/doc/dev/mysql-server/latest/classbinary__log_1_1Rows__event.html">row_event</a>
 *
 * @description: 更新时间
 * @author: liuzijian
 * @time: 2023-09-11 16:34
 */
@Slf4j
@Getter
public class UpdateRowsEvent extends AbstractRowEvent {

    /**
     * Indicates whether each column is used, one bit per column.<br>
     * For this field, the amount of storage required is INT((width + 7) / 8) bytes.
     * <p>
     * 是否每列使用，每列1位。<br>
     * 对于这个字段，所需的存储量是INT((width + 7) / 8)字节。
     * </p>
     */
    private BitSet columnsBeforeImage;

    private BitSet columnsAfterImage;

    List<RowEntry> rows;

    /**
     * @param in
     * @param bodyLength eventSize 减去 checkSum之后的值
     * @param checkSum
     */
    public UpdateRowsEvent(BinlogEnvironment environment, BinlogEventTypeEnum binlogEvent, ByteArrayIndexInputStream in, int bodyLength, BinlogCheckSumEnum checkSum) throws IOException {
        super(environment, binlogEvent, in, bodyLength, checkSum);
    }

    @Override
    public void parseColumnImageAndRows(ByteArrayIndexInputStream in) throws IOException {
        // 根据源码 https://github.com/mysql/mysql-server/blob/8.0/libbinlogevents/src/rows_event.cpp 第469到473行得知，updateRowsEvent才有两个columnsImage
        this.columnsBeforeImage = BitMapUtil.convertByBigEndianArray(width, 0, in.readBytes((this.width + 7) / 8));
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

        rows = new ArrayList<>();
        while (in.available() > 0) {
            Row before = new Row(environment.getTableInfo().get(this.tableId), in);
            Row after = new Row(environment.getTableInfo().get(this.tableId), in);
            rows.add(new RowEntry(before, after));
        }
    }


}
