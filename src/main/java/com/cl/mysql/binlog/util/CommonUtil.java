package com.cl.mysql.binlog.util;

import lombok.experimental.UtilityClass;

import java.util.Collections;

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

}
