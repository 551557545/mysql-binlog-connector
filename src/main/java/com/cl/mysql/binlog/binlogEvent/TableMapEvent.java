package com.cl.mysql.binlog.binlogEvent;

import com.cl.mysql.binlog.constant.BinlogCheckSumEnum;
import com.cl.mysql.binlog.constant.BinlogEventTypeEnum;
import com.cl.mysql.binlog.constant.TableMapColumnTypeEnum;
import com.cl.mysql.binlog.constant.TableMapOptMetadaTypeEnum;
import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;
import com.cl.mysql.binlog.util.BitMapUtil;
import com.cl.mysql.binlog.util.CommonUtil;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/classbinary__log_1_1Table__map__event.html">官方文档</a>
 * @author: liuzijian
 * @time: 2023-09-07 18:09
 */
@Getter
public class TableMapEvent extends AbstractBinlogEvent {

    private static Map<Long, TableMapEvent> tableInfo = new ConcurrentHashMap<>();

    private final Long tableId;

    /**
     * Reserved for future use; currently always 0.
     * <p>
     * 留作将来使用的;目前总是0。
     */
    private final Integer flags;

    private final String dataBaseName;

    private final String tableName;

    private final Integer columnCount;

    private final List<TableMapColumnTypeEnum> columnType;

    private final Integer metadataLength;

    /**
     * list of metadata for each column
     * <p>
     * 每个列的元数据列表
     * </p>
     */
    private final List<Integer> metadata;

    /**
     * 记录了哪些字段是非空的bitmap数组
     * bitmap算法请看：{@link com.cl.mysql.binlog.util.BitMapUtil}
     */
    private final BitSet nullBits;

    /**
     * optional metadata fields are stored in Type, Length, Value(TLV) format. <br>
     * Type takes 1 byte. Length is a packed integer value. Values takes Length bytes.
     * <p>
     * 可选元数据字段以TLV (Type, Length, Value)格式存储。Type占用1个字节。<br>
     * 长度是一个打包的整数值。Values占用Length字节。
     * </p>
     */
    private final OptionalMetadata optionalMetadataFields;

    /**
     * @param in
     * @param bodyLength eventSize 减去 checkSum之后的值
     */
    public TableMapEvent(BinlogEventTypeEnum binlogEvent, ByteArrayIndexInputStream in, int bodyLength, BinlogCheckSumEnum checkSum) throws IOException {
        super(binlogEvent, in, bodyLength, checkSum);
        this.tableId = in.readLong(6);
        this.flags = in.readInt(2);

        int dataBaseNameLenth = in.readInt(1);
        this.dataBaseName = in.readString(dataBaseNameLenth);
        in.skip(1);// 去掉dataBaseName跟着的0

        int tableNameLength = in.readInt(1);
        this.tableName = in.readString(tableNameLength);
        in.skip(1);// 去掉tableName跟着的0

        this.columnCount = in.readLenencInteger().intValue();
        this.columnType = new ArrayList<>();
        for (int i = 0; i < columnCount; i++) {
            columnType.add(TableMapColumnTypeEnum.getByIdentifier(in.readInt(1)));
        }

        this.metadataLength = in.readLenencInteger().intValue();
        this.metadata = new ArrayList<>(this.columnCount);
        /**
         * 关于sizeOfMetadataInBytes的长度请查看<a href="https://github.com/mysql/mysql-server/blob/8.0/sql/rpl_utility.h">第864行 ~ 第926行</a>
         */
        for (int i = 0; i < this.columnCount; i++) {
            TableMapColumnTypeEnum columnTypeEnum = columnType.get(i);
            switch (columnTypeEnum) {
                case MYSQL_TYPE_FLOAT:
                case MYSQL_TYPE_DOUBLE:
                case MYSQL_TYPE_BLOB:
                case MYSQL_TYPE_JSON:
                case MYSQL_TYPE_GEOMETRY:
                case MYSQL_TYPE_BIT:
                case MYSQL_TYPE_VARCHAR:
                case MYSQL_TYPE_NEWDECIMAL:
                case MYSQL_TYPE_TIME2:
                case MYSQL_TYPE_DATETIME2:
                case MYSQL_TYPE_TIMESTAMP2:
                    metadata.add(in.readInt(columnTypeEnum.getSizeOfMetadataInBytes()));
                    break;
                case MYSQL_TYPE_STRING:
                    metadata.add(CommonUtil.bigEndianInteger(in.readBytes(columnTypeEnum.getSizeOfMetadataInBytes()), 0, columnTypeEnum.getSizeOfMetadataInBytes()));
                    break;
                default:
                    metadata.add(0);
            }
        }
        this.nullBits = BitMapUtil.convertByBigEndianArray(this.columnCount, 0, in.readBytes((this.columnCount + 7) / 8));
        if (bodyLength - (in.available() - checkSum.getLength()) > 0) {// 一般来说columnCount > 0也可以
            this.optionalMetadataFields = new OptionalMetadata(in, checkSum, this.columnType);
        } else {
            this.optionalMetadataFields = null;
        }

        // 缓存起来
        tableInfo.put(this.tableId, this);
    }

    public static TableMapEvent getTableInfo(Long tableId) {
        return tableInfo.get(tableId);
    }

    @Getter
    public static class OptionalMetadata {

        /**
         * For each numeric column, a bit indicates whether the numeric colunm has unsigned flag. 1 means it is unsigned.<br>
         * The number of bytes needed for this is int((column_count + 7) / 8). <br>
         * The order is the same as the order of column_type field.<br>
         * <p>
         * 对于每个数字列，一个位表示该数字列是否具有unsigned标志。<br>
         * 1表示无符号。<br>
         * 需要的字节数为int((column_count + 7) / 8)，顺序与column_type字段的顺序相同。<br>
         * </p>
         */
        private BitSet signedness;

        /**
         * 如果表内字符类型的字段的字符集都一致的话，则这里有数据
         */
        private byte[] defaultCharset;

        /**
         * 如果表内字符类型的字段的字符集存在不一致的话，则这里有数据
         */
        private byte[] columnCharset;

        /**
         * 字段名称，只有当binlog_row_metadata=FULL的时候才有值
         */
        private List<String> columnName;

        /**
         * The string values of SET columns.<br>
         * This is only included if binlog_row_metadata=FULL.
         * <p>
         * 用来记录set类型字段里面的set值，假设某个字段类型是set，存着 "1","2","3"，那么返回的是[["1","2","3"]]<br>
         * 如果存在2个set字段，字段一存着"1","2","3"，字段二存着 "4","5","6"，那么返回的就是[["1","2","3"],["4","5","6"]]
         * </p>
         */
        private List<String[]> setStrValue;

        /**
         * 与setStrValue同理
         */
        private List<String[]> enumStrValue;

        private Object geometryType;

        /**
         * A sequence of column indexes. The indexes are stored in pack_length format.
         * <p>
         * 列索引的序列。索引以pack_length格式存储。（假设，表中第一个字段和第九个字段都是主键，则返回[0,8]）
         * </p>
         */
        private byte[] simplePrimaryKey;

        /**
         * The primary key with some prefix. <br>
         * It doesn't appear together with SIMPLE_PRIMARY_KEY. This is only included if binlog_row_metadata=FULL and there is a primary key where some key part covers a prefix of the column.
         * <p>
         * 带前缀的主键。<br>
         * 它不与SIMPLE_PRIMARY_KEY一起出现。只有当binlog_row_metadata=FULL并且存在一个主键，其中某些键部分覆盖了列的前缀时，才会包含此选项。
         * </p>
         */
        private Object primaryKeyWithPrefix;

        private byte[] enumAndSetDefaultCharset;

        private byte[] enumAndSetColumnCharset;

        public OptionalMetadata(ByteArrayIndexInputStream in, BinlogCheckSumEnum checkSum, List<TableMapColumnTypeEnum> columnType) throws IOException {
            int currentSurplusLength = in.available() - checkSum.getLength();// 减去checkSum的长度
            while (currentSurplusLength != 0) {
                TableMapOptMetadaTypeEnum type = TableMapOptMetadaTypeEnum.getByCode(in.readInt(1));
                if (type == null) {
                    break;
                }
                int length = in.readLenencInteger().intValue();
                switch (type) {
                    case SIGNEDNESS:
                        this.signedness = this.parseSignedness(in, length, columnType);
                        break;
                    case DEFAULT_CHARSET:
                        this.defaultCharset = this.parseDefaultCharset(in, length);
                        break;
                    case COLUMN_CHARSET:
                        this.columnCharset = this.parseColumnCharset(in, length);
                        break;
                    case COLUMN_NAME:
                        this.columnName = this.parseColumnName(in, length);
                        break;
                    case SET_STR_VALUE:
                        this.setStrValue = this.parseSetStrValue(in, length);
                        break;
                    case ENUM_STR_VALUE:
                        this.enumStrValue = this.parseEnumStrValue(in, length, columnType);
                        break;
                    case GEOMETRY_TYPE:
                        this.geometryType = this.parseGeometryType(in, length);
                        break;
                    case SIMPLE_PRIMARY_KEY:
                        this.simplePrimaryKey = this.parseSimplePrimaryKey(in, length);
                        break;
                    case PRIMARY_KEY_WITH_PREFIX:
                        this.primaryKeyWithPrefix = this.parsePrimaryKeyWithPrefix(in, length);
                        break;
                    case ENUM_AND_SET_DEFAULT_CHARSET:
                        this.enumAndSetDefaultCharset = this.parseEnumAndSetDefaultCharset(in, length);
                        break;
                    case ENUM_AND_SET_COLUMN_CHARSET:
                        this.enumAndSetColumnCharset = this.parseEnumAndSetColumnCharset(in, length);
                        break;
                }
                currentSurplusLength -= (1 + length);
            }
        }

        public BitSet parseSignedness(ByteArrayIndexInputStream in, int length, List<TableMapColumnTypeEnum> columnType) throws IOException {
            // 这里取出来的值是个小端值（即：本来是0000 0001 小端-> 1000 0000），所以要转成大端值（1000 0000 -> 0000 0001）。（测试之后知道的）
            return BitMapUtil.convertByLittleEndianArray(
                    (int) columnType.stream().filter(TableMapColumnTypeEnum::isNumberColumn).count(),
                    0,
                    in.readBytes(length)
            );
        }

        public byte[] parseDefaultCharset(ByteArrayIndexInputStream in, int length) throws IOException {
            return in.readBytes(length);
        }

        public byte[] parseColumnCharset(ByteArrayIndexInputStream in, int length) throws IOException {
            return in.readBytes(length);
        }

        public List<String> parseColumnName(ByteArrayIndexInputStream in, int length) throws IOException {
            byte[] bytes = in.readBytes(length);
            ByteArrayIndexInputStream inputStream = new ByteArrayIndexInputStream(bytes);
            List<String> result = new ArrayList<>();
            while (inputStream.available() != 0) {
                int strLength = inputStream.readInt(1);
                result.add(inputStream.readString(strLength));
            }
            return result;
        }

        public List<String[]> parseSetStrValue(ByteArrayIndexInputStream in, int length) throws IOException {
            byte[] bytes = in.readBytes(length);
            ByteArrayIndexInputStream inputStream = new ByteArrayIndexInputStream(bytes);
            List<String[]> result = new ArrayList<>();
            while (inputStream.available() != 0) {
                int strLength = inputStream.readLenencInteger().intValue();
                String[] value = new String[strLength];
                for (int i = 0; i < strLength; i++) {
                    int valuleLength = inputStream.readLenencInteger().intValue();
                    value[i] = inputStream.readString(valuleLength);
                }
                result.add(value);
            }
            return result;
        }

        public List<String[]> parseEnumStrValue(ByteArrayIndexInputStream in, int length, List<TableMapColumnTypeEnum> columnType) throws IOException {
            byte[] bytes = in.readBytes(length);
            ByteArrayIndexInputStream inputStream = new ByteArrayIndexInputStream(bytes);
            List<String[]> result = new ArrayList<>();
            while (inputStream.available() != 0) {
                int strLength = inputStream.readLenencInteger().intValue();
                String[] value = new String[strLength];
                for (int i = 0; i < strLength; i++) {
                    int valuleLength = inputStream.readLenencInteger().intValue();
                    value[i] = inputStream.readString(valuleLength);
                }
                result.add(value);
            }
            return result;
        }

        public Object parseGeometryType(ByteArrayIndexInputStream in, int length) throws IOException {
            byte[] bytes = in.readBytes(length);
            return null;
        }

        public byte[] parseSimplePrimaryKey(ByteArrayIndexInputStream in, int length) throws IOException {
            return in.readBytes(length);
        }

        public Object parsePrimaryKeyWithPrefix(ByteArrayIndexInputStream in, int length) throws IOException {
            byte[] bytes = in.readBytes(length);
            return null;
        }

        public byte[] parseEnumAndSetDefaultCharset(ByteArrayIndexInputStream in, int length) throws IOException {
            return in.readBytes(length);
        }

        public byte[] parseEnumAndSetColumnCharset(ByteArrayIndexInputStream in, int length) throws IOException {
            return in.readBytes(length);
        }
    }

}
