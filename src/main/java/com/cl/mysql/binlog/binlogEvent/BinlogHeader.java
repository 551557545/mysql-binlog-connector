package com.cl.mysql.binlog.binlogEvent;

import com.cl.mysql.binlog.constant.BinlogEventTypeEnum;
import lombok.Data;

import java.util.Date;

/**
 * @description: binlogEventCommonHeader实体 <p>
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_replication_binlog_event.html#sect_protocol_replication_event_format_desc">官方文档，最下面</a>
 * @author: liuzijian
 * @time: 2023-09-05 16:30
 */
@Data
public class BinlogHeader {

    private Date timeStamp;

    /**
     * See binary_log::Log_event_type
     * <p>
     * binlog的事件类型
     */
    private BinlogEventTypeEnum event;

    private int serverId;

    /**
     * size of the event (header, post-header, body)
     * <p>
     * event报文长度，包含（header, post-header, body）
     */
    private int eventSize;

    /**
     * 下一个binlog事件的位点
     */
    private Integer logPos;

    private Integer flags;

}
