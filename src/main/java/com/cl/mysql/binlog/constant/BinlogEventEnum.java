package com.cl.mysql.binlog.constant;

import com.cl.mysql.binlog.binlogEvent.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description:
 * @author: liuzijian
 * @time: 2023-09-05 16:34
 */
@AllArgsConstructor
@Getter
public enum BinlogEventEnum {

    BINLOG_EVENT_HEADER(19, 19, 13, null),
    START_EVENT_V3(19, 19, 13, null),
    QUERY_EVENT(56, 56, 56, null),
    STOP_EVENT(0, 0, 0, null),
    ROTATE_EVENT(8, 8, 0, RotateEvent.class),
    INTVAR_EVENT(0, 0, 0, IntvarEvent.class),
    LOAD_EVENT(18, 18, 18, null),
    SLAVE_EVENT(0, 0, 0, null),
    CREATE_FILE_EVENT(4, 4, 4, null),
    APPEND_BLOCK_EVENT(4, 4, 4, null),
    EXEC_LOAD_EVENT(4, 4, 4, null),
    DELETE_FILE_EVENT(4, 4, 4, null),
    NEW_LOAD_EVENT(18, 18, 18, null),
    RAND_EVENT(0, 0, 0, null),
    USER_VAR_EVENT(0, 0, 0, null),
    FORMAT_DESCRIPTION_EVENT(84, -1, -1, FormatDescriptionEvent.class),
    XID_EVENT(0, -1, -1, XIDEvent.class),
    BEGIN_LOAD_QUERY_EVENT(4, -1, -1, null),
    EXECUTE_LOAD_QUERY_EVENT(26, -1, -1, null),
    TABLE_MAP_EVENT(8, -1, -1, null),
    DELETE_ROWS_EVENTv0(0, -1, -1, null),
    UPDATE_ROWS_EVENTv0(0, -1, -1, null),
    WRITE_ROWS_EVENTv0(0, -1, -1, null),
    DELETE_ROWS_EVENTv1(8, -1, -1, null),
    UPDATE_ROWS_EVENTv1(8, -1, -1, null),
    WRITE_ROWS_EVENTv1(8, -1, -1, null),
    INCIDENT_EVENT(2, -1, -1, null),
    HEARTBEAT_EVENT(0, -1, -1, null),
    DELETE_ROWS_EVENTv2(10, -1, -1, null),
    UPDATE_ROWS_EVENTv2(10, -1, -1, null),
    WRITE_ROWS_EVENTv2(10, -1, -1, null),
    ;

    /**
     * header长度
     */
    private final int v4_length;
    private final int v3_length;
    private final int v1_length;
    private final Class<? extends AbstractBinlogEvent> binlogEventClass;

    public static BinlogEventEnum getByOrdinal(int ordinal) {
        return values()[ordinal];
    }

}
