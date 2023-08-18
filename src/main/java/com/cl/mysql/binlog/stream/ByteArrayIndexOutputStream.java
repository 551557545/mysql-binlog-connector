package com.cl.mysql.binlog.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @描述 msql协议为小端协议 输出流需要转成小端输出
 * @description:
 * @author: liuzijian
 * @time: 2023-08-10 10:33
 */
public class ByteArrayIndexOutputStream extends OutputStream {

    private final OutputStream out;

    public ByteArrayIndexOutputStream() {
        this(new ByteArrayOutputStream());
    }

    public ByteArrayIndexOutputStream(OutputStream out) {
        this.out = out;
    }

    /**
     * @param value  int值
     * @param length 长度
     * @描述 以小端的方式写入流
     * <p> 以unsigned int value = 0x12345678为例，分别看看在两种字节序下其存储情况，我们可以用unsigned char buf[4]来表示value：
     * <p> Big-Endian: 低地址存放高位，如下：
     * <p> 高地址
     * <p> ---------------
     * <p> buf[3] (0x78) – 低位
     * <p> buf[2] (0x56)
     * <p> buf[1] (0x34)
     * <p> buf[0] (0x12) – 高位
     * <p> ---------------
     * <p> 低地址
     * <p> Little-Endian: 低地址存放低位，如下：
     * <p> 高地址
     * <p> ---------------
     * <p> buf[3] (0x12) – 高位
     * <p> buf[2] (0x34)
     * <p> buf[1] (0x56)
     * <p> buf[0] (0x78) – 低位
     * <p> --------------
     * <p> 低地址
     * <p> ————————————————
     */
    public void writeInt(int value, int length) throws IOException {
        for (int i = 0; i < length; i++) {
            // 32位 一个字节 8位 以1111 1111 去做与运算  先将低位写入低地址
            this.write(0x000000FF & (value >>> (i << 3)));
        }
    }

    public void writeLong(long value, int length) throws IOException {
        for (int i = 0; i < length; i++) {
            // 32位 一个字节 8位 以1111 1111 去做与运算  先将低位写入低地址
            this.write((int) (0x00000000000000FF & (value >>> (i << 3))));
        }
    }

    /**
     * 写入以0结尾的字符串 <br>
     * 即mysql通讯协议基础数据类型：string(nul) 以0结尾的字符串
     */
    public void writeNullTerminatedString(String value) throws IOException {
        this.write(value.getBytes());
        this.writeInt(0, 1);
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    public byte[] toByteArray() {
        if (out instanceof ByteArrayOutputStream) {
            return ((ByteArrayOutputStream) out).toByteArray();
        }
        return new byte[0];
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    @Override
    public void write(byte[] b) throws IOException {
        out.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }
}
