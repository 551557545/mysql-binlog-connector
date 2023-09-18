package com.cl.mysql.binlog.binlogEvent;

import com.cl.mysql.binlog.constant.BinlogCheckSumEnum;
import com.cl.mysql.binlog.constant.BinlogEventTypeEnum;
import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;
import lombok.Getter;

import java.io.IOException;

/**
 * @description: <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/classbinary__log_1_1Format__description__event.html">官方文档</a>
 * @author: liuzijian
 * @time: 2023-09-07 14:48
 */
@Getter
public class FormatDescriptionEvent extends AbstractBinlogEvent {

    /**
     * This is 1 in MySQL 3.23 and 3 in MySQL 4.0 and 4.1 (In MySQL 5.0 and up, FORMAT_DESCRIPTION_EVENT is used instead of START_EVENT_V3 and for them its 4).
     * <p>
     * 在MySQL 3.23中是1，在MySQL 4.0和4.1中是3(在MySQL 5.0及以上版本中，FORMAT_DESCRIPTION_EVENT被使用而不是START_EVENT_V3，对于它们来说是4)。
     */
    private final Integer binlogVersion;

    /**
     * The MySQL server's version (example: 4.0.14-debug-log), padded with 0x00 bytes on the right
     * <p>
     * MySQL服务器的版本号(例如:4.0.14-debug-log)，右边填充0x00字节
     */
    private final String serverVersion;

    private final long createTimestamp;

    private final Integer headerLength;

    /**
     * 指除checkNum外的报文长度
     */
    private final Integer postHeaderLength;

    private final BinlogCheckSumEnum checkSumType;

    private final Integer checkSum;


    public FormatDescriptionEvent(BinlogEventTypeEnum binlogEvent, ByteArrayIndexInputStream in, int bodyLength, BinlogCheckSumEnum checkSum) throws IOException {
        super(binlogEvent, in, bodyLength, checkSum);
        /**
         * The layout of Format_description_event data part is as follows:
         * Format_description_event数据部分的布局如下:
         *
         * The problem with this constructor is that the fixed header may have a length different from this version, but we don't know this length as we have not read the Format_description_log_event which says it, yet.
         *
         * +=====================================+
         * | event  | binlog_version   19 : 2    | = 4
         * | data   +----------------------------+
         * |        | server_version   21 : 50   |
         * |        +----------------------------+
         * |        | create_timestamp 71 : 4    |
         * |        +----------------------------+
         * |        | header_length    75 : 1    |
         * |        +----------------------------+
         * |        | post-header      76 : n    | = array of n bytes, one byte
         * |        | lengths for all            |   per event type that the
         * |        | event types                |   server knows about
         * +=====================================+
         */
        this.binlogVersion = in.readInt(2);
        this.serverVersion = in.readString(50).trim();
        this.createTimestamp = in.readInt(4);
        this.headerLength = in.readInt(1); // Length of the Binlog Event Header of next events. Should always be 19. 固定为19
        /**
         * 原文：a array indexed by binlog-event-type - 1 to extract the length of the event specific header
         * 翻译：binlog-event-type - 1作为数组下标 去提取postHeader的长度
         * 理解：实际是个下标值 根据上图eventdata的结构，这个下标值是header_length之后的第BinlogEventEnum.FORMAT_DESCRIPTION_EVENT.ordinal() - 1个开始的下标得到的值 就是 postHeader的长度
         * 举例：header_length之后的字节数组是 [0][1][2][3][4][5]...[100] 假设BinlogEventEnum.FORMAT_DESCRIPTION_EVENT = 5
         * 那么字节数组的第五位记录着postHeader的长度就是[4]，那根据io流，如果要读取[4]那就要跳过5 - 1个字节，然后读取一位
         */
        int skipIndex = BinlogEventTypeEnum.FORMAT_DESCRIPTION_EVENT.getCode() - 1;
        // 跳过下标
        in.skip(skipIndex);
        // 读取一位
        /**
         * postHeaderLength 长度 包含了binlogVersion + serverVersion + createTimestamp + headerLength + skipIndex + 1
         */
        this.postHeaderLength = in.readInt(1);

        /**
         * 可以根据这个计算公式 来确定checkSum占用的字节数 FD的checkSum比其他event多一个字节，用来记录类型，
         */
        int checkSumLength = bodyLength + checkSum.getLength() + 1 - postHeaderLength;
        in.skip(in.available() - checkSumLength);
        // 读取1字节去获取checkSum类型
        this.checkSumType = BinlogCheckSumEnum.getByOrdinal(in.readInt(1));
        if (BinlogCheckSumEnum.CRC32 == this.checkSumType) {
            this.checkSum = in.readInt(BinlogCheckSumEnum.CRC32.getLength());
        } else {
            this.checkSum = 0;
        }
    }
}
