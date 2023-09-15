package com.cl.mysql.binlog.entity;

import com.cl.mysql.binlog.binlogEvent.TableMapEvent;
import com.cl.mysql.binlog.constant.TableMapColumnTypeEnum;
import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;
import com.cl.mysql.binlog.util.BitMapUtil;
import com.cl.mysql.binlog.util.CommonUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.List;

/**
 * @description: 行值
 * @author: liuzijian
 * @time: 2023-09-15 15:42
 */
public class Row {
    /**
     * <a href="https://github.com/mysql/mysql-server/blob/8.0/strings/decimal.cc">源码：搜dig2bytes</a>
     */
    private final static int[] dig2bytes = {0, 1, 1, 2, 2, 3, 3, 4, 4, 4};

    private final List<Object> rowValue;


    public Row(Long tableId, ByteArrayIndexInputStream in) throws IOException {
        TableMapEvent tableMapEvent = TableMapEvent.getTableInfo(tableId);
        List<Integer> metaDataList = tableMapEvent.getMetadata();
        int columnCount = tableMapEvent.getColumnCount();
        rowValue = new ArrayList<>(columnCount);
        int nullBitMaskLength = (columnCount + 7) / 8;


        BitSet nullBitMask = BitMapUtil.convertByBigEndianArray(columnCount, 0, in.readBytes(nullBitMaskLength));
        for (int i = 0; i < columnCount; i++) {
            if (!nullBitMask.get(i)) {
                int meta = metaDataList.get(i);
                Object value = deserializeField(tableMapEvent.getColumnType().get(i), in, meta);
                rowValue.add(value);
            }
        }
    }

    /**
     * <a href="https://github.com/mysql/mysql-server/blob/8.0/sql/log_event.cc">源码第1813行 ~ 2145行，用来读取不同类型的长度</a><br>
     * <a href="https://github.com/mysql/mysql-server/blob/8.0/storage/ndb/clusterj/clusterj-tie/src/main/java/com/mysql/clusterj/tie/Utility.java">参考源码，去解析各种类型的实际值</a>
     *
     * @param columnType 字段类型
     * @param in         输入流
     * @return 对应字段的值
     * @throws IOException
     */
    public Object deserializeField(TableMapColumnTypeEnum columnType, ByteArrayIndexInputStream in, Integer meta) throws IOException {
        switch (columnType) {
            case MYSQL_TYPE_TINY:
                return this.parseTiny(in);
            case MYSQL_TYPE_SHORT:
                return this.parseShort(in);
            case MYSQL_TYPE_LONG:
                return this.parseLong(in);
            case MYSQL_TYPE_FLOAT:
                return this.parseFloat(in);
            case MYSQL_TYPE_DOUBLE:
                return this.parseDouble(in);
            case MYSQL_TYPE_NULL:
                break;
            case MYSQL_TYPE_TIMESTAMP:
                return this.parseTimestamp(in);
            case MYSQL_TYPE_TIMESTAMP2:
                return this.parseTimestamp2(meta, in);
            case MYSQL_TYPE_LONGLONG:
                return this.parseLongLong(in);
            case MYSQL_TYPE_INT24:
                return this.parseInt24(in);
            case MYSQL_TYPE_DATE:
                return this.parseDate(in);
            case MYSQL_TYPE_TIME:
                return this.parseTime(in);
            case MYSQL_TYPE_TIME2:
                return this.parseTime2(meta, in);
            case MYSQL_TYPE_DATETIME:
                return this.parseDateTime(in);
            case MYSQL_TYPE_DATETIME2:
                return this.parseDateTime2(meta, in);
            case MYSQL_TYPE_YEAR:
                return this.parseYear(in);
            case MYSQL_TYPE_VAR_STRING:
            case MYSQL_TYPE_VARCHAR:
            case MYSQL_TYPE_STRING:
                return this.parseStr(meta, in);
            case MYSQL_TYPE_BIT:
            case MYSQL_TYPE_NEWDECIMAL:
                return this.parseNewdecimal(meta, in);
            case MYSQL_TYPE_BLOB:
                return this.parseBlob(meta, in);
        }
        return null;
    }


    /**
     * <a href="https://github.com/mysql/mysql-server/blob/8.0/strings/decimal.cc">mysql8.0源码，搜int decimal_bin_size_inline，来计算字节长度</a>
     *
     * @param in
     * @return
     * @throws IOException
     */
    public int parseTiny(ByteArrayIndexInputStream in) throws IOException {
        return in.readInt(1);
    }

    /**
     * <a href="https://github.com/mysql/mysql-server/blob/8.0/strings/decimal.cc">mysql8.0源码，搜int decimal_bin_size_inline，来计算字节长度</a>
     *
     * @param in
     * @return
     * @throws IOException
     */
    public int parseShort(ByteArrayIndexInputStream in) throws IOException {
        return in.readInt(2);
    }

    /**
     * <a href="https://github.com/mysql/mysql-server/blob/8.0/strings/decimal.cc">mysql8.0源码，搜int decimal_bin_size_inline，来计算字节长度</a>
     *
     * @param in
     * @return
     * @throws IOException
     */
    private int parseLong(ByteArrayIndexInputStream in) throws IOException {
        return in.readInt(4);
    }

    /**
     * <a href="https://github.com/mysql/mysql-server/blob/8.0/strings/decimal.cc">mysql8.0源码，搜int decimal_bin_size_inline，来计算字节长度</a>
     *
     * @param in
     * @return
     * @throws IOException
     */
    private double parseDouble(ByteArrayIndexInputStream in) throws IOException {
        return Double.longBitsToDouble(in.readLong(8));
    }

    /**
     * <a href="https://github.com/mysql/mysql-server/blob/8.0/strings/decimal.cc">mysql8.0源码，搜int decimal_bin_size_inline，来计算字节长度</a>
     *
     * @param in
     * @return
     * @throws IOException
     */
    private long parseTimestamp(ByteArrayIndexInputStream in) throws IOException {
        return in.readInt(4) * 1000L;
    }

    /**
     * <a href="https://github.com/mysql/mysql-server/blob/8.0/strings/decimal.cc">mysql8.0源码，搜int decimal_bin_size_inline，来计算字节长度</a>
     *
     * @param in
     * @return
     * @throws IOException
     */
    private java.sql.Timestamp parseTimestamp2(int meta, ByteArrayIndexInputStream in) throws IOException {
        int timestampValueLength = 4;
        int millisecondsBytesLength = (meta + 1) / 2;// 纳秒长度
        long timestampValue = CommonUtil.bigEndianLong(in.readBytes(timestampValueLength), 0, timestampValueLength);
        int millisecondsValue = CommonUtil.bigEndianInteger(in.readBytes(millisecondsBytesLength), 0, millisecondsBytesLength);// 一共占24位
        millisecondsValue = this.unpackFractionalSeconds(meta, millisecondsValue);
        java.sql.Timestamp result = new java.sql.Timestamp(timestampValue * 1000);
        result.setNanos(millisecondsValue);
        return result;
    }

    /**
     * <a href="https://github.com/mysql/mysql-server/blob/8.0/strings/decimal.cc">mysql8.0源码，搜int decimal_bin_size_inline，来计算字节长度</a>
     *
     * @param in
     * @return
     * @throws IOException
     */
    private float parseFloat(ByteArrayIndexInputStream in) throws IOException {
        return Float.intBitsToFloat(in.readInt(4));
    }

    /**
     * <a href="https://github.com/mysql/mysql-server/blob/8.0/strings/decimal.cc">mysql8.0源码，搜int decimal_bin_size_inline，来计算字节长度</a>
     *
     * @param in
     * @return
     * @throws IOException
     */
    private long parseLongLong(ByteArrayIndexInputStream in) throws IOException {
        return in.readLong(8);
    }

    /**
     * <a href="https://github.com/mysql/mysql-server/blob/8.0/strings/decimal.cc">mysql8.0源码，搜int decimal_bin_size_inline，来计算字节长度</a>
     *
     * @param in
     * @return
     * @throws IOException
     */
    private int parseInt24(ByteArrayIndexInputStream in) throws IOException {
        return in.readInt(3);
    }


    /**
     * 格式：yyyy-MM-dd<br>
     * 参考{@link com.google.code.or.common.util.MySQLUtils#toDate(int)}
     *
     * @param in
     * @return
     */
    private Date parseDate(ByteArrayIndexInputStream in) throws IOException {
        int value = in.readInt(3);
        final int d = value % 32;
        value >>>= 5;
        final int m = value % 16;
        final int y = value >> 4;
        final Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(y, m - 1, d);
        return new Date(cal.getTimeInMillis());
    }

    /**
     * <a href="https://github.com/mysql/mysql-server/blob/8.0/strings/decimal.cc">mysql8.0源码，搜int decimal_bin_size_inline，来计算字节长度</a><br>
     * 格式：HH:mm:ss<br>
     * 参考{@link com.google.code.or.common.util.MySQLUtils#toTime(int)}
     *
     * @param in
     * @return
     */
    private Time parseTime(ByteArrayIndexInputStream in) throws IOException {
        int value = in.readInt(3);
        final int s = (int) (value % 100);
        value /= 100;
        final int m = (int) (value % 100);
        final int h = (int) (value / 100);
        final Calendar c = Calendar.getInstance();
        c.set(70, 0, 1, h, m, s);
        return new Time(c.getTimeInMillis());
    }

    /**
     * <a href="https://github.com/mysql/mysql-server/blob/8.0/strings/decimal.cc">mysql8.0源码，搜int decimal_bin_size_inline，来计算字节长度</a><br>
     * <a href="https://github.com/mysql/mysql-server/blob/8.0/storage/ndb/clusterj/clusterj-tie/src/main/java/com/mysql/clusterj/tie/Utility.java">搜unpackTime2来获取真实值</a>
     *
     * @param meta
     * @param in
     * @return
     * @throws IOException
     */
    private long parseTime2(int meta, ByteArrayIndexInputStream in) throws IOException {
        int timeValueLength = 3;
        int millisecondsBytesLength = (meta + 1) / 2;// 纳秒长度
        long timestampValue = CommonUtil.bigEndianLong(in.readBytes(timeValueLength), 0, timeValueLength);
        int millisecondsValue = CommonUtil.bigEndianInteger(in.readBytes(millisecondsBytesLength), 0, millisecondsBytesLength);// 一共占24位

        int milliseconds = this.unpackFractionalSeconds(meta, millisecondsValue) / 1000;

        timestampValue -= 0x800000L;// 减去一个sign位
        int hour = (int) ((timestampValue >> 10) & 0b11_1111_1111);// 占10位
        int minute = (int) ((timestampValue >> 8) & 0b1111_1100);// 占8位
        int second = (int) (timestampValue & 0b11_1111);// 占6位
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.HOUR, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, milliseconds);
        return calendar.getTimeInMillis();
    }

    /**
     * <a href="https://github.com/mysql/mysql-server/blob/8.0/strings/decimal.cc">mysql8.0源码，搜int decimal_bin_size_inline，来计算字节长度</a><br>
     * 格式：yyyy-MM-dd HH:mm:ss<br>
     * 参考{@link com.google.code.or.common.util.MySQLUtils#toDatetime(long)}<br>
     * 或者
     * <a href="https://github.com/mysql/mysql-server/blob/8.0/storage/ndb/clusterj/clusterj-tie/src/main/java/com/mysql/clusterj/tie/Utility.java">搜unpackDatetime来获取真实值</a>
     *
     * @param in
     * @return
     * @throws IOException
     */
    private java.util.Date parseDateTime(ByteArrayIndexInputStream in) throws IOException {
        long value = in.readLong(8);
        final int sec = (int) (value % 100);
        value /= 100;
        final int min = (int) (value % 100);
        value /= 100;
        final int hour = (int) (value % 100);
        value /= 100;
        final int day = (int) (value % 100);
        value /= 100;
        final int mon = (int) (value % 100);
        final int year = (int) (value / 100);
        final Calendar c = Calendar.getInstance();
        c.set(year, mon - 1, day, hour, min, sec);
        return c.getTime();
    }

    private java.util.Date parseDateTime2(int meta, ByteArrayIndexInputStream in) throws IOException {
        int packedDatetime2BytesLength = 5;// 不包括纳秒的长度
        int millisecondsBytesLength = (meta + 1) / 2;// 纳秒长度
        long packedDatetime2Value = CommonUtil.bigEndianLong(in.readBytes(packedDatetime2BytesLength), 0, packedDatetime2BytesLength);// 一共占40位
        int millisecondsValue = CommonUtil.bigEndianInteger(in.readBytes(millisecondsBytesLength), 0, millisecondsBytesLength);// 一共占24位

        packedDatetime2Value -= 0x8000000000L;// 减去一个sign位

        int yearMonth = (int) ((packedDatetime2Value & 0b111_1111_1111_1111_11_00_0000_0000_0000_0000_0000L) >> 22); // 17 bits year * 13 + month
        int year = yearMonth / 13;
        int month = (yearMonth % 13) - 1; // calendar month is 0-11
        int day = (int) ((packedDatetime2Value & 0b11111_0_0000_0000_0000_0000L) >> 17); // 5 bits day
        int hour = (int) ((packedDatetime2Value & 0b11111_0000_0000_0000L) >> 12); // 5 bits hour
        int minute = (int) ((packedDatetime2Value & 0b111111_00_0000L) >> 6); // 6 bits minute
        int second = (int) ((packedDatetime2Value & 0b111111L)); // 6 bits second
        int milliseconds = this.unpackFractionalSeconds(meta, millisecondsValue) / 1000;
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DATE, day);
        calendar.set(Calendar.HOUR, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, milliseconds);
        return calendar.getTime();
    }

    private Object parseYear(ByteArrayIndexInputStream in) throws IOException {
        int value = in.readInt(1);
        return value + 1900;
    }

    private String parseStr(int meta, ByteArrayIndexInputStream in) throws IOException {
        return meta < 256 ? in.readString(in.readInt(1)) : in.readString(in.readInt(2));
    }

    /**
     * <a href="https://github.com/mysql/mysql-server/blob/8.0/strings/decimal.cc">mysql8.0源码，搜int decimal_bin_size_inline，来计算字节长度</a>
     * <p>
     * 剩下的解析参考{@link com.google.code.or.common.util.MySQLUtils#toDecimal(int, int, byte[])}<br>
     * 和<a href="https://github.com/mysql/mysql-server/blob/8.0/strings/decimal.cc">mysql8.0 decimal对象源码</a>
     * </p>
     *
     * @param meta
     * @param in
     * @return 返回BigDecimal
     */
    private BigDecimal parseNewdecimal(int meta, ByteArrayIndexInputStream in) throws IOException {
        // 长度
        int precision = meta & 0b11111111;
        // 小数点
        int scale = (meta >> 8) & 0b11111111;
        final int DIG_PER_DEC1 = 9;
        int intg = precision - scale;
        int intg0 = intg / DIG_PER_DEC1;
        int frac0 = scale / DIG_PER_DEC1;
        int intg0x = intg - intg0 * DIG_PER_DEC1;
        int frac0x = scale - frac0 * DIG_PER_DEC1;
        int byteSize = intg0 * 4 + dig2bytes[intg0x] + frac0 * 4 + dig2bytes[frac0x];
        byte[] value = in.readBytes(byteSize);
        boolean positive = (value[0] & 0x80) == 0x80;
        value[0] ^= 0x80;
        if (!positive) {
            for (int i = 0; i < value.length; i++) {
                value[i] ^= 0xFF;
            }
        }
        int x = precision - scale;
        int ipDigits = x / DIG_PER_DEC1;
        int ipDigitsX = x - ipDigits * DIG_PER_DEC1;
        int ipSize = (ipDigits << 2) + dig2bytes[ipDigitsX];
        int offset = dig2bytes[ipDigitsX];
        BigDecimal ip = offset > 0 ? BigDecimal.valueOf(CommonUtil.bigEndianInteger(value, 0, offset)) : BigDecimal.ZERO;
        for (; offset < ipSize; offset += 4) {
            int i = CommonUtil.bigEndianInteger(value, offset, 4);
            ip = ip.movePointRight(DIG_PER_DEC1).add(BigDecimal.valueOf(i));
        }
        int shift = 0;
        BigDecimal fp = BigDecimal.ZERO;
        for (; shift + DIG_PER_DEC1 <= scale; shift += DIG_PER_DEC1, offset += 4) {
            int i = CommonUtil.bigEndianInteger(value, offset, 4);
            fp = fp.add(BigDecimal.valueOf(i).movePointLeft(shift + DIG_PER_DEC1));
        }
        if (shift < scale) {
            int i = CommonUtil.bigEndianInteger(value, offset, dig2bytes[scale - shift]);
            fp = fp.add(BigDecimal.valueOf(i).movePointLeft(scale));
        }
        BigDecimal result = ip.add(fp);
        return positive ? result : result.negate();
    }

    /**
     * <a href="https://github.com/mysql/mysql-server/blob/8.0/strings/decimal.cc">mysql8.0源码，搜int decimal_bin_size_inline里的case MYSQL_TYPE_BLOB，来计算字节长度</a>
     *
     * @param meta
     * @param in
     * @return
     * @throws IOException
     */
    private byte[] parseBlob(int meta, ByteArrayIndexInputStream in) throws IOException {
        int blobLength = in.readInt(meta);
        return in.readBytes(blobLength);
    }


    /**
     * 解包fs，指mysql datetime、timestamp 后面的6位bit的精度
     *
     * @param meta           number of digits of precision, 0 to 6
     * @param bigEndianValue 大端的纳秒值
     * @return number of milliseconds
     */
    private int unpackFractionalSeconds(int meta, int bigEndianValue) {
        switch (meta) {
            case 0:
                return 0;
            case 1:
            case 2:
                return (bigEndianValue & 0xFF) * 10000;
            case 3:
            case 4:
                return (bigEndianValue & 0xFFFF) * 1000;
            case 5:
            case 6:
                return bigEndianValue & 0xFFFFFF;
            default:
                return 0;
        }
    }
}
