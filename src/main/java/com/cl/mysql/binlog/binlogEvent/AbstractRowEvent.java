package com.cl.mysql.binlog.binlogEvent;

import com.cl.mysql.binlog.constant.BinlogCheckSumEnum;
import com.cl.mysql.binlog.constant.BinlogEventTypeEnum;
import com.cl.mysql.binlog.entity.Row;
import com.cl.mysql.binlog.network.BinlogEnvironment;
import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;

/**
 * @description:
 * @author: liuzijian
 * @time: 2023-09-15 15:51
 */
@Getter
public abstract class AbstractRowEvent extends AbstractBinlogEvent {


    protected final Long tableId;
    protected final Integer flags;
    /**
     * Represents the number of columns in the table
     * <p>
     * 表示表中的列数
     * </p>
     */
    protected final Integer width;

    /**
     * @param binlogEvent
     * @param in
     * @param bodyLength  eventSize 减去 checkSum之后的值，而FormatDescriptionEvent事件会多减去一个1
     * @param checkSum
     */
    public AbstractRowEvent(BinlogEnvironment environment, BinlogEventTypeEnum binlogEvent, ByteArrayIndexInputStream in, int bodyLength, BinlogCheckSumEnum checkSum) throws IOException {
        super(environment, binlogEvent, in, bodyLength, checkSum);

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

        parseColumnImageAndRows(new ByteArrayIndexInputStream(in.readBytes(in.available() - checkSum.getLength())));

        if (in.available() != checkSum.getLength()) {
            throw new RuntimeException("解析updateRowEvent错误");
        }
    }

    /**
     * 解析 columnImages 和 rows
     *
     * @param in socket输入流
     * @throws IOException
     */
    public abstract void parseColumnImageAndRows(ByteArrayIndexInputStream in) throws IOException;

    /**
     * +-------------------------------------------------------+
     * | Event Type | Cols_before_image | Cols_after_image     |
     * +-------------------------------------------------------+
     * |  DELETE    |   Deleted row     |    NULL              |
     * |  INSERT    |   NULL            |    Inserted row      |
     * |  UPDATE    |   Old     row     |    Updated row       |
     * +-------------------------------------------------------+
     */
    @Getter
    @AllArgsConstructor
    public static class RowEntry {
        private final Row before;
        private final Row after;
    }
}
