package com.cl.mysql.binlog.constant;

import com.cl.mysql.binlog.binlogEvent.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/binlog__event_8h.html">官方文档</a> ctrl+f搜：
 * Enumerations
 * @author: liuzijian
 * @time: 2023-09-05 16:34
 */
@AllArgsConstructor
@Getter
public enum BinlogEventEnum {

    UNKNOWN_EVENT(0, null),
    START_EVENT_V3(1, null),
    QUERY_EVENT(2, QueryEvent.class),
    STOP_EVENT(3, null),
    ROTATE_EVENT(4, RotateEvent.class),
    INTVAR_EVENT(5, IntvarEvent.class),
    SLAVE_EVENT(7, null),
    APPEND_BLOCK_EVENT(9, null),
    DELETE_FILE_EVENT(11, null),
    RAND_EVENT(13, null),
    USER_VAR_EVENT(14, null),
    FORMAT_DESCRIPTION_EVENT(15, FormatDescriptionEvent.class),
    XID_EVENT(16, XIDEvent.class),
    BEGIN_LOAD_QUERY_EVENT(17, null),
    EXECUTE_LOAD_QUERY_EVENT(18, null),
    TABLE_MAP_EVENT(19, TableMapEvent.class),
    WRITE_ROWS_EVENT_V1(23, null),
    UPDATE_ROWS_EVENT_V1(24, null),
    DELETE_ROWS_EVENT_V1(25, null),
    /**
     * INCIDENT_EVENT：
     * Something out of the ordinary happened on the master.
     * <p>
     * 主mysql可能发生了错误
     */
    INCIDENT_EVENT(26, IncidentEvent.class),
    HEARTBEAT_LOG_EVENT(27, null),
    IGNORABLE_LOG_EVENT(28, null),
    ROWS_QUERY_LOG_EVENT(29, null),
    WRITE_ROWS_EVENT(30, null),
    UPDATE_ROWS_EVENT(31, null),
    DELETE_ROWS_EVENT(32, null),
    GTID_LOG_EVENT(33, null),
    ANONYMOUS_GTID_LOG_EVENT(34, null),
    PREVIOUS_GTIDS_LOG_EVENT(35, null),
    TRANSACTION_CONTEXT_EVENT(36, null),
    VIEW_CHANGE_EVENT(37, null),
    XA_PREPARE_LOG_EVENT(38, null),
    PARTIAL_UPDATE_ROWS_EVENT(39, null),
    TRANSACTION_PAYLOAD_EVENT(40, null),
    HEARTBEAT_LOG_EVENT_V2(41, HeartbeatLogEventV2.class),
    ENUM_END_EVENT(-1, null),
    ;

    private final int code;
    private final Class<? extends AbstractBinlogEvent> binlogEventClass;

    private final static Map<Integer, BinlogEventEnum> cache;

    static {
        cache = new HashMap<>(BinlogEventEnum.values().length);
        for (BinlogEventEnum e : values()) {
            cache.put(e.getCode(), e);
        }
    }

    public static BinlogEventEnum getByCode(int code) {
        return cache.getOrDefault(code, UNKNOWN_EVENT);
    }

}
