package com.cl.mysql.binlog.constant;

/**
 * @description: sql命令
 * @author: liuzijian
 * @time: 2023-08-22 14:03
 */
public interface Sql {

    /**
     * 查询当前binlog文件与位点信息
     */
    String show_master_status = "show master status";

    /**
     * 查询当前mysql数据库checksum信息<br>
     * checkSum实际是mysql为了保证binlog报文的完整性而引入的，NONE---不校验  CRC32---校验<br>
     * <a href="https://dev.mysql.com/doc/refman/8.0/en/binary-log.html">官方文档</a>
     * <br>
     * By default, the server logs the length of the event as well as the event itself and uses this to verify that the event was written correctly. You can also cause the server to write checksums for the events by setting the binlog_checksum system variable. When reading back from the binary log, the source uses the event length by default, but can be made to use checksums if available by enabling the system variable source_verify_checksum (from MySQL 8.0.26) or master_verify_checksum (before MySQL 8.0.26). The replication I/O (receiver) thread on the replica also verifies events received from the source. You can cause the replication SQL (applier) thread to use checksums if available when reading from the relay log by enabling the system variable replica_sql_verify_checksum (from MySQL 8.0.26) or slave_sql_verify_checksum (before MySQL 8.0.26).
     */
    String show_global_variables_like_binlog_checksum = "show global variables like 'binlog_checksum'";

    String set_master_binlog_checksum_global_binlog_checksum = "set @master_binlog_checksum = @@global.binlog_checksum";

    String set_slave_uuid = "set @slave_uuid = UUID()";

    String show_global_variables_like_binlog_row_metadata = "show global variables like 'binlog_row_metadata'";

}
