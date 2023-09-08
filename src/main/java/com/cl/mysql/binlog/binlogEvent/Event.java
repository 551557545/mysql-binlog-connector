package com.cl.mysql.binlog.binlogEvent;

import cn.hutool.core.util.ReflectUtil;
import com.cl.mysql.binlog.constant.BinlogCheckSumEnum;
import com.cl.mysql.binlog.constant.BinlogEventEnum;
import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;
import lombok.Getter;

import java.io.IOException;
import java.util.Date;

/**
 * @description:
 * @author: liuzijian
 * @time: 2023-09-05 17:36
 */
@Getter
public class Event {

    private final BinlogHeader header;

    private final AbstractBinlogEvent body;

    public Event(BinlogHeader header, AbstractBinlogEvent body) {
        this.header = header;
        this.body = body;
    }

    public static Event V4Deserialization(ByteArrayIndexInputStream indexInputStream, BinlogCheckSumEnum checkSum) throws IOException {
        // Network streams are requested with COM_BINLOG_DUMP and prepend each Binlog Event with 00 OK-byte.
        // 这个success 是文档上说 每一个binlog报文都会在前面带上一个字节的 ok byte
        // 文档：https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_replication.html#sect_protocol_replication_binlog_stream #Binlog Network Stream
        int success = indexInputStream.readInt(1);

        int timeStamp = indexInputStream.readInt(4); // seconds since unix epoch
        int eventType = indexInputStream.readInt(1); // See binary_log::Log_event_type
        int serverId = indexInputStream.readInt(4); // server-id of the originating mysql-server. Used to filter out events in circular replication
        int eventSize = indexInputStream.readInt(4); //size of the event (header, post-header, body)
        int logPos = indexInputStream.readInt(4); // position of the next event
        int flags = indexInputStream.readInt(2); // See Binlog Event Header Flags

        BinlogEventEnum eventEnum = BinlogEventEnum.getByCode(eventType);

        BinlogHeader header = new BinlogHeader();
        header.setTimeStamp(new Date(timeStamp * 1000L));
        header.setEvent(eventEnum);
        header.setServerId(serverId);
        header.setEventSize(eventSize);
        header.setLogPos(logPos);
        header.setFlags(flags);
        AbstractBinlogEvent body = null;
        if (eventEnum.getBinlogEventClass() != null) {
            /**
             * 这个是一般事件的结构
             * CheckSum is attached as the last item to follow the event data.
             * Typical structure of the event with the checksum (CS) is as
             * the following
             * +-----------+------------+------------+--------------+
             * |           |            |            |              |
             * | Common Hdr| SubHeader  | Payload    | Checksum (V) |
             * +-----------+------------+------------+--------------+
             *
             * 这里是FormatDescriptor (FD) event的结构描述
             * https://dev.mysql.com/worklog/task/?id=2540#tabs-2540-4
             *  +-----------+------------+------------+------+------------+
             *  | Common Hdr|            |            |      |            |
             *  |   (F)     | SubHeader  | Payload    |  (A) | Checksum(V)|
             *  +-----------+------------+------------+------+------------+
             *  其中 （A） 位代表：
             *  0         no checksum
             *  1         CRC32 algorithm
             *  [2 - 127] extensible range for new algorithm
             *  [128-254] reserved range
             *  255       Event that contains (A) == 255 is generated by checksum-unaware server
             *  实际上是checksum的类型，占用1个字节
             *
             */
            /**
             * 文档：https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_replication_binlog_event.html
             * 说了V4版本的binlog的header长度固定为19 （Length of the Binlog Event Header of next events. Should always be 19.）
             */
            int commonHdrLength = 19; // （F）
            int checkSumLength = checkSum.getLength();   // FormatDescriptionEvent：（A）+ (V) 其他事件：（V）
            if (eventEnum == BinlogEventEnum.FORMAT_DESCRIPTION_EVENT) {
                checkSumLength += 1;// 加上（A）位长度
            }
            body = ReflectUtil.newInstance(eventEnum.getBinlogEventClass(), indexInputStream, eventSize - commonHdrLength - checkSumLength, checkSum);
        }
        return new Event(header, body);
    }

}
