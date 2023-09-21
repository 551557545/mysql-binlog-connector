package com.cl.mysql.binlog.constant;

import com.cl.mysql.binlog.binlogEvent.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/binlog__event_8h.html">官方文档</a> ctrl+f搜：
 * Enumerations
 * <p>
 * <a href="https://github.com/mysql/mysql-server/blob/8.0/libbinlogevents/src/control_events.cpp">mysql8.0源码，第95 ~ 第117行</a>
 * </p>
 * @author: liuzijian
 * @time: 2023-09-05 16:34
 */
@AllArgsConstructor
@Getter
public enum BinlogEventTypeEnum {

    UNKNOWN_EVENT(0, null, -1),
    START_EVENT_V3(1, null, PostHeaderLength.START_V3_HEADER_LEN),
    QUERY_EVENT(2, QueryEvent.class, PostHeaderLength.QUERY_HEADER_LEN),
    STOP_EVENT(3, null, PostHeaderLength.STOP_HEADER_LEN),
    ROTATE_EVENT(4, RotateEvent.class, PostHeaderLength.ROTATE_HEADER_LEN),
    INTVAR_EVENT(5, IntvarEvent.class, PostHeaderLength.INTVAR_HEADER_LEN),
    SLAVE_EVENT(7, null, 0),
    APPEND_BLOCK_EVENT(9, null, PostHeaderLength.APPEND_BLOCK_HEADER_LEN),
    DELETE_FILE_EVENT(11, null, PostHeaderLength.DELETE_FILE_HEADER_LEN),
    RAND_EVENT(13, null, PostHeaderLength.RAND_HEADER_LEN),
    USER_VAR_EVENT(14, null, PostHeaderLength.USER_VAR_HEADER_LEN),
    FORMAT_DESCRIPTION_EVENT(15, FormatDescriptionEvent.class, PostHeaderLength.FORMAT_DESCRIPTION_HEADER_LEN),
    XID_EVENT(16, XIDEvent.class, PostHeaderLength.XID_HEADER_LEN),
    BEGIN_LOAD_QUERY_EVENT(17, null, PostHeaderLength.BEGIN_LOAD_QUERY_HEADER_LEN),
    EXECUTE_LOAD_QUERY_EVENT(18, null, PostHeaderLength.EXECUTE_LOAD_QUERY_HEADER_LEN),
    TABLE_MAP_EVENT(19, TableMapEvent.class, PostHeaderLength.TABLE_MAP_HEADER_LEN),
    WRITE_ROWS_EVENT_V1(23, null, PostHeaderLength.ROWS_HEADER_LEN_V1),
    UPDATE_ROWS_EVENT_V1(24, UpdateRowsEvent.class, PostHeaderLength.ROWS_HEADER_LEN_V1),
    DELETE_ROWS_EVENT_V1(25, null, PostHeaderLength.ROWS_HEADER_LEN_V1),
    /**
     * INCIDENT_EVENT：
     * Something out of the ordinary happened on the master.
     * <p>
     * 主mysql可能发生了错误
     */
    INCIDENT_EVENT(26, IncidentEvent.class, PostHeaderLength.INCIDENT_HEADER_LEN),
    HEARTBEAT_LOG_EVENT(27, null, PostHeaderLength.HEARTBEAT_HEADER_LEN),
    IGNORABLE_LOG_EVENT(28, null, PostHeaderLength.IGNORABLE_HEADER_LEN),
    ROWS_QUERY_LOG_EVENT(29, null, PostHeaderLength.IGNORABLE_HEADER_LEN),
    WRITE_ROWS_EVENT_V2(30, WriteRowsEvent.class, PostHeaderLength.ROWS_HEADER_LEN_V2),
    UPDATE_ROWS_EVENT_V2(31, UpdateRowsEvent.class, PostHeaderLength.ROWS_HEADER_LEN_V2),
    DELETE_ROWS_EVENT_V2(32, DeleteRowsEvent.class, PostHeaderLength.ROWS_HEADER_LEN_V2),
    GTID_LOG_EVENT(33, GtidEvent.class, PostHeaderLength.POST_HEADER_LENGTH),
    ANONYMOUS_GTID_LOG_EVENT(34, null, PostHeaderLength.POST_HEADER_LENGTH),
    PREVIOUS_GTIDS_LOG_EVENT(35, null, PostHeaderLength.IGNORABLE_HEADER_LEN),
    TRANSACTION_CONTEXT_EVENT(36, null, PostHeaderLength.TRANSACTION_CONTEXT_HEADER_LEN),
    VIEW_CHANGE_EVENT(37, null, PostHeaderLength.VIEW_CHANGE_HEADER_LEN),
    XA_PREPARE_LOG_EVENT(38, null, PostHeaderLength.XA_PREPARE_HEADER_LEN),
    PARTIAL_UPDATE_ROWS_EVENT(39, null, PostHeaderLength.ROWS_HEADER_LEN_V2),
    TRANSACTION_PAYLOAD_EVENT(40, null, PostHeaderLength.TRANSACTION_PAYLOAD_EVENT),
    HEARTBEAT_LOG_EVENT_V2(41, HeartbeatLogEventV2.class, PostHeaderLength.HEARTBEAT_HEADER_LEN),
    ENUM_END_EVENT(-1, null, -1),
    ;

    private final int code;
    private final Class<? extends AbstractBinlogEvent> binlogEventClass;
    private final int postHeaderLength;

    private final static Map<Integer, BinlogEventTypeEnum> cache;

    static {
        cache = new HashMap<>(BinlogEventTypeEnum.values().length);
        for (BinlogEventTypeEnum e : values()) {
            cache.put(e.getCode(), e);
        }
    }

    public static BinlogEventTypeEnum getByCode(int code) {
        return cache.getOrDefault(code, UNKNOWN_EVENT);
    }

    public static boolean isInsertEvent(BinlogEventTypeEnum eventType) {
        switch (eventType) {
            case WRITE_ROWS_EVENT_V1:
            case WRITE_ROWS_EVENT_V2:
                return true;
            default:
                return false;
        }
    }

    public static boolean isUpdateEvent(BinlogEventTypeEnum eventType) {
        switch (eventType) {
            case UPDATE_ROWS_EVENT_V1:
            case UPDATE_ROWS_EVENT_V2:
                return true;
            default:
                return false;
        }
    }

    public static boolean isDelteEvent(BinlogEventTypeEnum eventType) {
        switch (eventType) {
            case DELETE_FILE_EVENT:
            case DELETE_ROWS_EVENT_V1:
            case DELETE_ROWS_EVENT_V2:
                return true;
            default:
                return false;
        }
    }

}
