package com.cl.mysql.binlog.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/classbinary__log_1_1Table__map__event.html#Table_table_map_event_column_types">官方文档</a>，crtl+f搜Table_map_event column types
 * @author: liuzijian
 * @time: 2023-09-08 13:44
 */
@Getter
@AllArgsConstructor
public enum TableMapColumnTypeEnum {

    MYSQL_TYPE_DECIMAL(0, 0),
    MYSQL_TYPE_TINY(1, 0),
    MYSQL_TYPE_SHORT(2, 0),
    MYSQL_TYPE_LONG(3, 0),
    /**
     * 1 byte unsigned integer, representing the "pack_length", which is equal to sizeof(float) on the server from which the event originates.
     * <p>
     * 1字节无符号整数，表示“pack_length”，等于事件起源服务器上的sizeof(double)。
     * </p>
     */
    MYSQL_TYPE_FLOAT(4, 1),
    /**
     * 1 byte unsigned integer, representing the "pack_length", which is equal to sizeof(double) on the server from which the event originates.
     * <p>
     * 1字节无符号整数，表示“pack_length”，等于事件起源服务器上的sizeof(double)。
     * </p>
     */
    MYSQL_TYPE_DOUBLE(5, 1),
    MYSQL_TYPE_NULL(6, 0),
    MYSQL_TYPE_TIMESTAMP(7, 0),
    MYSQL_TYPE_LONGLONG(8, 0),
    MYSQL_TYPE_INT24(9, 0),
    MYSQL_TYPE_DATE(10, 0),
    MYSQL_TYPE_TIME(11, 0),
    MYSQL_TYPE_DATETIME(12, 0),
    MYSQL_TYPE_YEAR(13, 0),
    /**
     * This enumeration value is only used internally and cannot exist in a binlog.
     * <p>
     * 此枚举值仅在内部使用，不能存在于binlog中。
     * </p>
     */
    @Deprecated
    MYSQL_TYPE_NEWDATE(14, -1),
    /**
     * 2 byte unsigned integer representing the maximum length of the string.
     * <p>
     * 2字节无符号整数，表示字符串的最大长度。
     * </p>
     */
    MYSQL_TYPE_VARCHAR(15, 2),
    /**
     * A 1 byte unsigned int representing the length in bits of the bitfield (0 to 64), followed by a 1 byte unsigned int representing the number of bytes occupied by the bitfield.<br>
     * The number of bytes is either int((length + 7) / 8) or int(length / 8).
     * <p>
     * 一个1字节的无符号整型，表示位域的长度(0到64)，后跟一个1字节的无符号整型，表示位域占用的字节数。字节数为int((length + 7) / 8)或int(length / 8)。
     * </p>
     */
    MYSQL_TYPE_BIT(16, 2),
    /**
     * A 1 byte unsigned int representing the precision, followed by a 1 byte unsigned int representing the number of decimals.
     * <p>
     * 一个表示精度的1字节无符号整型，后跟一个表示小数数的1字节无符号整型。
     * </p>
     */
    MYSQL_TYPE_NEWDECIMAL(246, 2),
    /**
     * This enumeration value is only used internally and cannot exist in a binlog.
     * <p>
     * 此枚举值仅在内部使用，不能存在于binlog中。
     * </p>
     */
    @Deprecated
    MYSQL_TYPE_ENUM(247, -1),
    /**
     * This enumeration value is only used internally and cannot exist in a binlog.
     * <p>
     * 此枚举值仅在内部使用，不能存在于binlog中。
     * </p>
     */
    @Deprecated
    MYSQL_TYPE_SET(248, -1),
    /**
     * This enumeration value is only used internally and cannot exist in a binlog.
     * <p>
     * 此枚举值仅在内部使用，不能存在于binlog中。
     * </p>
     */
    @Deprecated
    MYSQL_TYPE_TINY_BLOB(249, -1),
    /**
     * This enumeration value is only used internally and cannot exist in a binlog.
     * <p>
     * 此枚举值仅在内部使用，不能存在于binlog中。
     * </p>
     */
    @Deprecated
    MYSQL_TYPE_MEDIUM_BLOB(250, -1),
    /**
     * This enumeration value is only used internally and cannot exist in a binlog.
     * <p>
     * 此枚举值仅在内部使用，不能存在于binlog中。
     * </p>
     */
    @Deprecated
    MYSQL_TYPE_LONG_BLOB(251, -1),
    /**
     * The pack length, i.e., the number of bytes needed to represent the length of the blob: 1, 2, 3, or 4.
     * <p>
     * 包长度，即表示blob长度所需的字节数:1、2、3或4。
     * </p>
     */
    MYSQL_TYPE_BLOB(252, 1),
    /**
     * This is used to store both strings and enumeration values. <br>
     * The first byte is a enumeration value storing the real type, which may be either MYSQL_TYPE_VAR_STRING or MYSQL_TYPE_ENUM. The second byte is a 1 byte unsigned integer representing the field size, i.e., the number of bytes needed to store the length of the string.
     * <p>
     * 这用于存储字符串和枚举值。<br>
     * 第一个字节是存储实际类型的枚举值，可以是MYSQL_TYPE_VAR_STRING或MYSQL_TYPE_ENUM。第二个字节是一个1字节的无符号整数，表示字段大小，即存储字符串长度所需的字节数。
     * </p>
     */
    MYSQL_TYPE_VAR_STRING(253, 2),
    /**
     * The first byte is always MYSQL_TYPE_VAR_STRING (i.e., 253).<br>
     * The second byte is the field size, i.e., the number of bytes in the representation of size of the string: 3 or 4.
     * <p>
     * 第一个字节总是MYSQL_TYPE_VAR_STRING(即253)。<br>
     * 第二个字节是字段大小，即表示字符串大小的字节数:3或4。
     * </p>
     */
    MYSQL_TYPE_STRING(254, 2),
    /**
     * The pack length, i.e., the number of bytes needed to represent the length of the geometry: 1, 2, 3, or 4.
     * <p>
     * 包长度，即表示几何形状长度所需的字节数:1,2,3或4。
     * </p>
     */
    MYSQL_TYPE_GEOMETRY(255, 1),
    /**
     * <li>The first byte holds the MySQL type for the elements.</li>
     * <li>
     * The following 0, 1, 2, or 3 bytes holds the metadata for the MySQL type for the elements. <br>
     * The contents of these bytes depends on the element type, as described in the other rows of this table.
     * </li>
     * <p>
     * <li>
     * 第一个字节保存元素的MySQL类型。
     * </li>
     * <li>
     * 下面的0、1、2或3个字节保存了元素的MySQL类型的元数据。这些字节的内容取决于元素类型，如该表的其他行所述。
     * </li>
     * </p>
     */
    MYSQL_TYPE_TYPED_ARRAY(15, 4),

    ;


    private final int identifier;

    private final int sizeOfMetadataInBytes;

    private static Map<Integer, TableMapColumnTypeEnum> cache;

    static {
        cache = new HashMap<>(values().length);
        for (TableMapColumnTypeEnum e : values()) {
            cache.put(e.identifier, e);
        }
    }

    public static TableMapColumnTypeEnum getByIdentifier(int identifier) {
        return cache.get(identifier);
    }

    public boolean isNumberColumn() {
        switch (this) {
            case MYSQL_TYPE_DECIMAL:
            case MYSQL_TYPE_TINY:
            case MYSQL_TYPE_SHORT:
            case MYSQL_TYPE_LONG:
            case MYSQL_TYPE_FLOAT:
            case MYSQL_TYPE_DOUBLE:
            case MYSQL_TYPE_LONGLONG:
            case MYSQL_TYPE_INT24:
            case MYSQL_TYPE_NEWDECIMAL:
                return true;
            default:
                return false;
        }
    }
}
