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
    ;

}
