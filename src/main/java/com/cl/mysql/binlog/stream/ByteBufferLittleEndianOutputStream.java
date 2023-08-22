package com.cl.mysql.binlog.stream;

import lombok.Getter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @description: 小端缓冲输出流
 * @author: liuzijian
 * @time: 2023-08-21 15:21
 */
public class ByteBufferLittleEndianOutputStream extends OutputStream {

    private byte[] buffer;

    /**
     * 当前位
     */
    @Getter
    private int pos = 0;

    /**
     * 已使用位
     */
    private int usedPos = 0;

    public ByteBufferLittleEndianOutputStream() {
        this(32);
    }

    public ByteBufferLittleEndianOutputStream(int bufferSize) {
        this.buffer = new byte[bufferSize];
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

    public void writeString(String value) throws IOException {
        this.write(value.getBytes());
    }

    public void writeLenencString(String value, Charset charset) throws IOException {
        byte[] strBytes = value.getBytes(charset);
        this.writeInt(strBytes.length, 2);
        this.write(strBytes);
    }

    public void writeLenencInt(long value) throws IOException {
        if (value >= 0 && value <= 250) {
            this.writeLong(value, 1);
        } else if (value >= 251 && value < 65536L) {
            this.writeLong(0xFC, 1);
            this.writeLong(value, 2);
        } else if (value >= 65536L && value < 16777216L) {
            this.writeLong(0xFD, 1);
            this.writeLong(value, 3);
        } else {
            this.writeLong(0xFE, 1);
            this.writeLong(value, 8);
        }
    }

    @Override
    public void write(int b) throws IOException {
        ensureCapacity(b + 1);
        this.buffer[this.addPosition(1)] = (byte) b;
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) - b.length > 0)) {
            throw new IndexOutOfBoundsException();
        }
        ensureCapacity(len + 1);
        System.arraycopy(b, off, this.buffer, pos, len);
        this.addPosition(len);
    }

    /**
     * 扩容
     *
     * @param additionalData 准备要新增的字节长度
     */
    public final void ensureCapacity(int additionalData) {
        if ((this.pos + additionalData) > this.buffer.length) {
            //
            // Resize, and pad so we can avoid allocing again in the near future
            //
            int newLength = (int) (this.buffer.length * 1.25);

            if (newLength < (this.buffer.length + additionalData)) {
                newLength = this.buffer.length + (int) (additionalData * 1.25);
            }

            if (newLength < this.buffer.length) {
                newLength = this.buffer.length + additionalData;
            }

            byte[] newBytes = new byte[newLength];

            System.arraycopy(this.buffer, 0, newBytes, 0, this.buffer.length);
            this.buffer = newBytes;
        }
    }

    public void setPosition(int pos) {
        if (pos > this.buffer.length) {
            throw new IndexOutOfBoundsException();
        }
        if (pos > usedPos) {
            usedPos = pos;
        }
        this.pos = pos;
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(buffer, usedPos);
    }

    public void resetPosition() {
        this.pos = this.usedPos;
    }

    private int addPosition(int add) {
        int tempPos = this.pos;
        this.pos += add;
        this.usedPos += add;
        return tempPos;
    }


}
