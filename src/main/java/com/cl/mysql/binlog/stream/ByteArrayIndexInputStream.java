package com.cl.mysql.binlog.stream;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @描述 msql协议为小端协议 输入流要从小端转为正常输入
 * @description:
 * @author: liuzijian
 * @time: 2023-08-10 09:54
 */
public class ByteArrayIndexInputStream extends InputStream {

    private final InputStream in;

    public ByteArrayIndexInputStream(InputStream in) {
        this.in = in;
    }

    public ByteArrayIndexInputStream(byte[] bytes) {
        this(new ByteArrayInputStream(bytes));
    }


    /**
     * @param length 读取字节数组的长度
     * @return int数字
     * @描述 mysql报文为小端模式 所以要按小端模式去读
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
     * <p> 版权声明：本文为CSDN博主「PlutoZuo」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
     * <p> 原文链接：https://blog.csdn.net/PlutoZuo/article/details/131204030
     */
    public int readInt(int length) throws IOException {
        int result = 0;
        for (int i = 0; i < length; i++) {
            //小端转换算法 转成十进制
            result |= (this.read() << (i << 3));
        }
        return result;
    }

    public byte[] readBytes() throws IOException {
        byte[] buffer = new byte[512 * 1024];
        in.read(buffer, 0, buffer.length);
        return buffer;
    }

    public long readLong(int length) throws IOException {
        long result = 0;
        for (int i = 0; i < length; i++) {
            //小端转换算法 转成十进制
            result |= (((long) this.read()) << (i << 3));
        }
        return result;
    }

    /**
     * 读取固定长度的string <br>
     * 即mysql通讯协议数据类型：string(len) 就是长度为len的字符串。
     *
     * @param length
     * @return
     * @throws IOException
     */
    public String readString(int length) throws IOException {
        byte[] b = new byte[length];
        this.fillBytes(b, 0, length);
        return new String(b);
    }

    public String readLenencString() throws IOException {
        int length = this.readLenencInteger().intValue();
        if (length == 0) {
            return "";
        }
        return this.readString(length);
    }

    /**
     * @return
     * @描述 读取字符串，结尾遇到0的时候停止 <br>
     * 即mysql通讯协议数据类型：string(NUL) 就是string+'0x00',这里的'0x00'是作为结尾符的。
     */
    public String readStringTerminatedByZero() throws IOException {
        ByteArrayIndexOutputStream outputStream = new ByteArrayIndexOutputStream();
        int num;
        int available = in.available();
        while (available > 0 && (num = this.read()) != 0) {
            outputStream.writeInt(num, 1);
        }
        return new String(outputStream.toByteArray());
    }

    /**
     * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_integers.html#sect_protocol_basic_dt_int_le">官方文档</a> <br>
     * int<lenenc>是可变长度类型的整数 <br>
     *
     * @return long or null
     * @throws IOException in case of malformed number or EOF
     */
    public Number readLenencInteger() throws IOException {
        int b = this.read();
        if (b < 251) {
            return b;
        } else if (b == 251) {
            return null;
        } else if (b == 0xfc) {
            return (long) readInt(2);
        } else if (b == 0xfd) {
            return (long) readInt(3);
        } else if (b == 0xfe) {
            return readLong(8);
        }
        throw new IOException("Unexpected packed number byte " + b);
    }


    @Override
    public int read() throws IOException {
        return in.read();
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public long skip(long n) throws IOException {
        return in.skip(n);
    }

    /**
     * @param length 读取长度
     * @return bytes
     * @throws IOException
     * @描述 读取定长数组
     */
    public byte[] readBytes(int length) throws IOException {
        if (length == 0) {
            return new byte[0];
        }
        byte[] b = new byte[length];
        this.fillBytes(b, 0, length);
        return b;
    }

    /**
     * @param b      数组
     * @param offset 数组起始位
     * @param length 读取长度
     * @return
     * @描述 读取字节并填充数组
     */
    public int fillBytes(byte[] b, int offset, int length) throws IOException {
        int result = this.read(b, offset, length);
        if (result == -1) {
            throw new EOFException();
        }
        return result;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return in.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return in.read(b, off, len);
    }

    @Override
    public synchronized void reset() throws IOException {
        in.reset();
    }

    @Override
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return in.markSupported();
    }
}
