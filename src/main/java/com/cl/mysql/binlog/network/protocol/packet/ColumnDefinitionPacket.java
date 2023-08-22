package com.cl.mysql.binlog.network.protocol.packet;

import com.cl.mysql.binlog.constant.CapabilitiesFlagsEnum;
import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;
import lombok.Getter;

import java.io.IOException;

/**
 * @description: 表字段描述，<a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_query_response_text_resultset_column_definition.html">文档</a>
 * @author: liuzijian
 * @time: 2023-08-22 11:39
 */
@Getter
public class ColumnDefinitionPacket {

    /**
     * The catalog used. Currently always "def"
     */
    private String cataLog;

    /**
     * schema name
     */
    private String schema;

    /**
     * virtual table name
     */
    private String table;

    /**
     * physical table name
     */
    private String orgTable;

    /**
     * virtual column name
     */
    private String name;

    /**
     * physical column name
     */
    private String orgName;

    /**
     * the column character set as defined in <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_character_set.html">Character Set</a>
     */
    private int characterSet;

    /**
     * maximum length of the field
     */
    private int columnLength;

    /**
     * type of the column as defined in enum_field_types
     */
    private int type;

    /**
     * Flags as defined in <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/group__group__cs__column__definition__flags.html">Column Definition Flags</a>
     */
    private int flags;

    /**
     * max shown decimal digits:
     * <li>0x00 for integers and static strings</li>
     * <li>0x1f for dynamic strings, double, float</li>
     * <li>0x00 to 0x51 for decimals</li>
     */
    private int decimals;


    public ColumnDefinitionPacket(byte[] bytes, int clientCapabilities) throws IOException {
        if (CapabilitiesFlagsEnum.has(clientCapabilities, CapabilitiesFlagsEnum.CLIENT_PROTOCOL_41)) {
            this.parseAs41(bytes);
        } else {
            this.parseAs320(bytes, clientCapabilities);
        }
    }

    public void parseAs41(byte[] bytes) throws IOException {
        ByteArrayIndexInputStream in = new ByteArrayIndexInputStream(bytes);
        this.cataLog = in.readLenencString();
        this.schema = in.readLenencString();
        this.table = in.readLenencString();
        this.orgTable = in.readLenencString();
        this.name = in.readLenencString();
        this.orgName = in.readLenencString();
        int fieldsLength = in.readLenencInteger().intValue();// length of fixed length fields
        this.characterSet = in.readInt(2);
        this.columnLength = in.readInt(4);
        this.type = in.readInt(1);
        this.flags = in.readInt(2);
        this.decimals = in.readInt(1);
    }

    public void parseAs320(byte[] bytes, int clientCapabilities) throws IOException {
        ByteArrayIndexInputStream in = new ByteArrayIndexInputStream(bytes);
        this.table = in.readLenencString();
        this.name = in.readLenencString();
        int lengthOfTypeField = in.readLenencInteger().intValue();
        this.type = in.readInt(1);
        int lengthOfFlagsAndDecimalsFields = in.readLenencInteger().intValue();
        this.flags = in.readInt(2);
        this.decimals = in.readInt(1);
    }

}
