package com.cl.mysql.binlog.constant;

/**
 * @description: <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/mysql__com_8h.html#aba06d1157f6dee3f20537154103c91a1ae51ed28a3ace64fa9b73e6cfb2ef0397">文档</a>
 * @author: liuzijian
 * @time: 2023-08-22 11:18
 */
public enum ResultSetMetadataEnum {

    /**
     * No metadata will be sent.
     */
    RESULTSET_METADATA_NONE,
    /**
     * The server will send all metadata.
     */
    RESULTSET_METADATA_FULL
    ;

}
