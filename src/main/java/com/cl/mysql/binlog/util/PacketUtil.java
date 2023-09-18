package com.cl.mysql.binlog.util;

/**
 * @description:
 * @author: liuzijian
 * @time: 2023-08-21 18:05
 */
public class PacketUtil {

    public static boolean isOkPacket(byte firstByte) {
        return (firstByte & 0xff) == 0x0;
    }

    public static boolean isEOFPacket(byte firstByte) {
        return (firstByte & 0xff) == 0xfe;
    }

    public static boolean isErrorPacket(byte firstByte) {
        return (firstByte & 0xff) == 0xff;
    }

}
