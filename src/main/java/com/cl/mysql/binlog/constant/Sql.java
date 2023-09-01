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

    String show_global_variables_like_binlog_checksum = "show global variables like 'binlog_checksum'";

    String set_master_binlog_checksum_global_binlog_checksum = "set @master_binlog_checksum = @@global.binlog_checksum";

}
