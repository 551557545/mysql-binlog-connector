package com.cl.mysql.binlog.constant;

/**
 * @description:
 * @author: liuzijian
 * @time: 2023-08-22 14:03
 */
public interface Sql {

    /**
     * 查询主数据库的binlog信息
     */
    String show_master_status = "show master status";

}
