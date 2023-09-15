package com.cl.mysql.binlog.util;

import lombok.experimental.UtilityClass;

/**
 * @description:
 * @author: liuzijian
 * @time: 2023-08-11 17:57
 */
@UtilityClass
public class CommonUtil {

    /**
     * 合并两个数组
     *
     * @param bytes1
     * @param bytes2
     * @return
     */
    public static byte[] contract(byte[] bytes1, byte[] bytes2) {
        byte[] newBytes = new byte[bytes1.length + bytes1.length];
        System.arraycopy(bytes1, 0, newBytes, 0, bytes1.length);
        System.arraycopy(bytes2, 0, newBytes, bytes1.length, bytes2.length);
        return newBytes;
    }

    /**
     * 异或
     *
     * @param input
     * @param against
     * @return
     */
    public static byte[] xor(byte[] input, byte[] against) {
        byte[] result = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            result[i] = (byte) (input[i] ^ against[i]);
        }
        return result;
    }

    /**
     * 将小端字节转为大端
     *
     * @param bytes  字节数组
     * @param offset 偏移量
     * @param length 长度
     * @return
     */
    public static int bigEndianInteger(byte[] bytes, int offset, int length) {
        int result = 0;
        for (int i = offset; i < length + offset; i++) {
            byte b = bytes[i];
            result = result << 8;
            // 小于0的要转为无符号
            result |= (b >= 0 ? b : Byte.toUnsignedInt(b));
        }
        return result;
    }

    /**
     * 将小端字节转为大端
     *
     * @param bytes  字节数组
     * @param offset 偏移量
     * @param length 长度
     * @return
     */
    public static long bigEndianLong(byte[] bytes, int offset, int length) {
        long result = 0;
        for (int i = offset; i < length + offset; i++) {
            byte b = bytes[i];
            result = result << 8;
            // 小于0的要转为无符号
            result |= (b >= 0 ? b : Byte.toUnsignedInt(b));
        }
        return result;
    }


}
