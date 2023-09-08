package com.cl.mysql.binlog.binlogEvent;

import com.cl.mysql.binlog.constant.BinlogCheckSumEnum;
import com.cl.mysql.binlog.constant.TableMapColumnTypeEnum;
import com.cl.mysql.binlog.constant.TableMapOptMetadaTypeEnum;
import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
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
    private final byte[] nullBits;

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
    public TableMapEvent(ByteArrayIndexInputStream in, int bodyLength, BinlogCheckSumEnum checkSum) throws IOException {
        super(in, bodyLength, checkSum);
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
        for (int i = 0; i < this.columnCount; i++) {
            TableMapColumnTypeEnum columnTypeEnum = columnType.get(i);
            if (columnTypeEnum.getSizeOfMetadataInBytes() != 0 && columnTypeEnum.getSizeOfMetadataInBytes() != -1) {
                switch (columnTypeEnum) {
                    case MYSQL_TYPE_TYPED_ARRAY:
                        columnTypeEnum = TableMapColumnTypeEnum.getByIdentifier(in.readInt(1));
                        metadata.add(in.readInt(columnTypeEnum.getSizeOfMetadataInBytes()));
                        break;
                    default:
                        metadata.add(in.readInt(columnTypeEnum.getSizeOfMetadataInBytes()));
                }
            } else {
                metadata.add(0);
            }
        }
        this.nullBits = in.readBytes((this.columnCount + 7) / 8);
        if (this.metadataLength != 0) {
            this.optionalMetadataFields = new OptionalMetadata(in, checkSum);
        } else {
            this.optionalMetadataFields = null;
        }

        // 缓存起来
        tableInfo.putIfAbsent(this.tableId, this);
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
        private byte[] signedness;

        public OptionalMetadata(ByteArrayIndexInputStream in, BinlogCheckSumEnum checkSum) throws IOException {
            int currentSurplusLength = in.available() - checkSum.getLength();// 减去checkSum的长度
            while (currentSurplusLength != 0) {
                TableMapOptMetadaTypeEnum type = TableMapOptMetadaTypeEnum.getByCode(in.readInt(1));
                if (type == null) {
                    break;
                }
                int length = in.readLenencInteger().intValue();
                switch (type) {
                    case SIGNEDNESS:
                        signedness = parseSIGNEDNESS(in, length);
                        break;
                    case DEFAULT_CHARSET:
                        break;
                    case COLUMN_CHARSET:
                        break;
                    case COLUMN_NAME:
                        break;
                    case SET_STR_VALUE:
                        break;
                    case ENUM_STR_VALUE:
                        break;
                    case GEOMETRY_TYPE:
                        break;
                    case SIMPLE_PRIMARY_KEY:
                        break;
                    case PRIMARY_KEY_WITH_PREFIX:
                        break;
                    case ENUM_AND_SET_DEFAULT_CHARSET:
                        break;
                    case ENUM_AND_SET_COLUMN_CHARSET:
                        break;
                }
                currentSurplusLength -= (1 + length);
            }
        }

        public byte[] parseSIGNEDNESS(ByteArrayIndexInputStream in, int length) throws IOException {
            return in.readBytes(length);
        }
    }

}
