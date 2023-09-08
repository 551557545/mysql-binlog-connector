package com.cl.mysql.binlog.util;

import java.util.Arrays;

/**
 * @description: <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_binary_resultset.html#sect_protocol_binary_resultset_row">官方文档</a>，用ctrl+f搜NULL-Bitmap
 * @author: liuzijian
 * @time: 2023-09-08 10:14
 */
public class BitMapUtil {

    /**
     *
     * 创建一个空的null-bit数组<p>
     * 算法理解：<p>
     * 加7的意思是：算法决定，要以8为一个周期 开辟一个新空间，所以需要加7。1~8个字段 = 1个空间、9~16个字段 = 2个空间<p>
     * 除8的意思是：一个字节8位，每一位代表一个字段是否为空。例如：假设有7个字段 那么 length = 1，bit数组只有一个字节的位置 = 0000 0000<p>
     * offset：偏移量，字节向左偏移。假设现在nullbytes = [0] 一个空间，offset = 0。设第一个字段是空值，则 nullbytes的值为 0000 0001。<br>
     * 在上面的基础上，如果offset = 1 则nullbytes的值为 0000 0010，如果offset = 2，则nullbytes的值为 0000 0100<p>
     * 会将整个bit向左移动。<p>
     *
     * ###########################################################################<p>
     *
     * 假设现在有8个字段，offset为0：<p>
     * 初始的nullbit数组为[0]<p>
     * 如果第一个字段是空，则bit数组填入 0000 0001，即[1]<p>
     * 如果第二个字段为空，则bit数组填入 0000 0010，即[2]<p>
     * 如此类推。<p>
     *
     * 假设现在有9个字段，offset为0：<p>
     * 初始nullbit数组为[0][0]，因为一个字节有8位，只能存8个空值标记，第九个则要开辟一个新的空间去存<p>
     * 如果第1个字段是空，则bit数组填入 0000 0001，即[1][0]<p>
     * 如果第2个字段为空，则bit数组填入 0000 0010，即[2][0]<p>
     * 如果第9个字段为空，则bit数组填入 [0000 0000][0000 0001]，即[0][1]<p>
     *
     * @param columnNum 字段数量
     * @param offset    偏移量
     * @return 空的byte数组，值默认为0
     */
    public static byte[] generateNulls(int columnNum, int offset) {
        int length = (columnNum + 7 + offset) / 8;
        return new byte[length];
    }

    /**
     * 填充空值的字段
     *
     * @param columnIndex 以0为开始 字段下标
     * @param offset      偏移量
     * @param nulls       null-bitmap
     * @return 填充好的字节数组
     */
    public static byte[] fillNull(int columnIndex, int offset, byte[] nulls) {
        // 定位当前下标的字段，在nullbit数组里的下标
        int byteArrayIndex = (columnIndex + offset) / 8;
        // 定位当前下标的字段，在对应的槽里面，二进制的位置 即：0000 0000 中的第几位
        int bitIndex = (columnIndex + offset) % 8;
        nulls[byteArrayIndex] = (byte) (nulls[byteArrayIndex] | (1 << bitIndex));
        return nulls;
    }


}
