package com.cl.mysql.binlog.binlogEvent;

import com.cl.mysql.binlog.constant.BinlogCheckSumEnum;
import com.cl.mysql.binlog.constant.BinlogEventTypeEnum;
import com.cl.mysql.binlog.entity.Row;
import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;
import com.cl.mysql.binlog.util.BitMapUtil;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/classbinary__log_1_1Update__rows__event.html">官方文档update_row_event</a>
 * 父类：<a href="https://dev.mysql.com/doc/dev/mysql-server/latest/classbinary__log_1_1Rows__event.html">row_event</a>
 *
 * @description: 删除事件
 * @author: liuzijian
 * @time: 2023-09-15 15:48
 */
@Getter
public class DeleteRowsEvent extends AbstractRowEvent {

    private BitSet columnsBeforeImage;
    private List<RowEntry> rows;

    /**
     * @param binlogEvent
     * @param in
     * @param bodyLength  eventSize 减去 checkSum之后的值，而FormatDescriptionEvent事件会多减去一个1
     * @param checkSum
     */
    public DeleteRowsEvent(BinlogEventTypeEnum binlogEvent, ByteArrayIndexInputStream in, int bodyLength, BinlogCheckSumEnum checkSum) throws IOException {
        super(binlogEvent, in, bodyLength, checkSum);
    }

    @Override
    public void parseColumnImageAndRows(ByteArrayIndexInputStream in) throws IOException {
        // 根据源码 https://github.com/mysql/mysql-server/blob/8.0/libbinlogevents/src/rows_event.cpp 第469到473行得知，updateRowsEvent才有两个columnsImage
        /**
         *  +-------------------------------------------------------+
         *  | Event Type | Cols_before_image | Cols_after_image     |
         *  +-------------------------------------------------------+
         *  |  DELETE    |   Deleted row     |    NULL              |
         *  |  INSERT    |   NULL            |    Inserted row      |
         *  |  UPDATE    |   Old     row     |    Updated row       |
         *  +-------------------------------------------------------+
         */
        this.columnsBeforeImage = BitMapUtil.convertByBigEndianArray(width, 0, in.readBytes((width + 7) / 8));

        rows = new ArrayList<>();
        while (in.available() > 0) {
            rows.add(new RowEntry(
                    new Row(this.tableId, in),
                    null
            ));
        }
    }
}
