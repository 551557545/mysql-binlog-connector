package com.cl.mysql.binlog.entity;

/**
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/classbinary__log_1_1Rows__event.html">官方文档 搜extra_row_info</a><p>
 * The class Extra_row_info will be storing the information related to m_extra_row_ndb_info and partition info (partition_id and source_partition_id). <br>
 * At any given time a Rows_event can have both, one or none of ndb_info and partition_info present as part of Rows_event. <br>
 * In case both ndb_info and partition_info are present then below will be the order in which they will be stored.<br>
 *
 * <p>
 * 类Extra_row_info将存储与m_extra_row_ndb_info和分区信息(partition_id和source_partition_id)相关的信息。<br>
 * 在任何给定的时间，Rows_event可以同时包含ndb_info和partition_info中的一个，也可以不包含。<br>
 * 如果ndb_info和partition_info都存在，那么下面是它们存储的顺序。
 * </p>
 * +----------+--------------------------------------+<br>
 * |type_code |        extra_row_ndb_info            |<br>
 * +--- ------+--------------------------------------+<br>
 * | NDB      |Len of ndb_info |Format |ndb_data     |<br>
 * | 1 byte   |1 byte         |1 byte |len - 2 byte |<br>
 * +----------+----------------+-------+-------------+<br>
 * <p>
 * In case of INSERT/DELETE
 * </p>
 * +-----------+----------------+<br>
 * | type_code | partition_info |<br>
 * +-----------+----------------+<br>
 * |   PART    |  partition_id  |<br>
 * | (1 byte)  |     2 byte     |<br>
 * +-----------+----------------+<br>
 * <p>
 * In case of UPDATE
 * </p>
 * +-----------+------------------------------------+<br>
 * | type_code |        partition_info              |<br>
 * +-----------+--------------+---------------------+<br>
 * |   PART    | partition_id | source_partition_id |<br>
 * | (1 byte)  |    2 byte    |       2 byte        |<br>
 * +-----------+--------------+---------------------+<br>
 * <p>
 * source_partition_id is used only in the case of Update_event
 * to log the partition_id of the source partition.
 * <p>
 * This is the format for any information stored as extra_row_info. <br>
 * type_code is not a part of the class Extra_row_info as it is a constant values used at the time of serializing and decoding the event.
 * </p>
 * <p>
 *     这是存储为extra_row_info的任何信息的格式。<br>
 *     type_code不是Extra_row_info类的一部分，因为它是在序列化和解码事件时使用的常量值。
 * </p>
 *
 * @author: liuzijian
 * @time: 2023-09-12 16:36
 */
public class ExtraRowInfo {
}
