package com.cl.mysql.binlog.constant;

/**
 * @description: <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/my__command_8h.html#ae2ff1badf13d2b8099af8b47831281e1a753ea4f670b155bf3f6f2bdf85431fc8">文档</a>
 * @author: liuzijian
 * @time: 2023-08-21 16:56
 */
public enum CommandTypeEnum {

    /**
     * Currently refused by the server.
     */
    COM_SLEEP,
    COM_QUIT,
    COM_INIT_DB,
    COM_QUERY,
    COM_FIELD_LIST,
    COM_CREATE_DB,
    COM_DROP_DB,
    COM_REFRESH,
    COM_DEPRECATED_1,
    COM_STATISTICS,
    COM_PROCESS_INFO,
    COM_CONNECT,
    COM_PROCESS_KILL,
    COM_DEBUG,
    COM_PING,
    COM_TIME,
    COM_DELAYED_INSERT,
    COM_CHANGE_USER,
    COM_BINLOG_DUMP,
    ;

}
