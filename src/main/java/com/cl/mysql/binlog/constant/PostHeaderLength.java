package com.cl.mysql.binlog.constant;

/**
 * @description:
 * @author: liuzijian
 * @time: 2023-09-12 15:58
 */

public interface PostHeaderLength {


    int ST_SERVER_VER_LEN = 50;
    int QUERY_HEADER_MINIMAL_LEN = 4 + 4 + 1 + 2;

    int QUERY_HEADER_LEN = QUERY_HEADER_MINIMAL_LEN + 2;

    int STOP_HEADER_LEN = 0;

    int START_V3_HEADER_LEN = 2 + ST_SERVER_VER_LEN + 4;

    int ROTATE_HEADER_LEN = 9;

    int INTVAR_HEADER_LEN = 0;

    int APPEND_BLOCK_HEADER_LEN = 4;

    int DELETE_FILE_HEADER_LEN = 4;

    int RAND_HEADER_LEN = 0;

    int USER_VAR_HEADER_LEN = 0;

    /**
     * 15值取决于：<br>
     *
     * @see BinlogEventTypeEnum.FORMAT_DESCRIPTION_EVENT.code
     */
    int FORMAT_DESCRIPTION_HEADER_LEN = START_V3_HEADER_LEN + 1 + (15 - 1);

    int XID_HEADER_LEN = 0;

    int BEGIN_LOAD_QUERY_HEADER_LEN = APPEND_BLOCK_HEADER_LEN;

    int ROWS_HEADER_LEN_V1 = 8;

    int TABLE_MAP_HEADER_LEN = 8;

    int EXECUTE_LOAD_QUERY_EXTRA_HEADER_LEN = 4 + 4 + 4 + 1;

    int EXECUTE_LOAD_QUERY_HEADER_LEN = QUERY_HEADER_LEN + EXECUTE_LOAD_QUERY_EXTRA_HEADER_LEN;

    int INCIDENT_HEADER_LEN = 2;

    int HEARTBEAT_HEADER_LEN = 0;

    int IGNORABLE_HEADER_LEN = 0;

    int ROWS_HEADER_LEN_V2 = 10;

    int TRANSACTION_CONTEXT_HEADER_LEN = 18;

    int VIEW_CHANGE_HEADER_LEN = 52;

    int XA_PREPARE_HEADER_LEN = 0;

    int TRANSACTION_PAYLOAD_HEADER_LEN = 0;

    int TRANSACTION_PAYLOAD_EVENT = 40;

    int POST_HEADER_LENGTH = 1 + 16 + 8 + 1 + 16;


}
