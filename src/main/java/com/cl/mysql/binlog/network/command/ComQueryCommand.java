package com.cl.mysql.binlog.network.command;

import com.cl.mysql.binlog.constant.CapabilitiesFlagsEnum;
import com.cl.mysql.binlog.constant.CommandTypeEnum;
import com.cl.mysql.binlog.stream.ByteBufferLittleEndianOutputStream;

import java.io.IOException;
import java.util.Map;

/**
 * @description: 查询语句 <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_query.html">文档</a>
 * @author: liuzijian
 * @time: 2023-08-21 11:13
 */
public class ComQueryCommand implements Command {

    private final String sql;

    private final int clientCapabilities;

    private Map<String, String> params;

    public ComQueryCommand(String sql) {
        this.sql = sql;
        this.clientCapabilities = 0;
    }

    public ComQueryCommand(String sql, int clientCapabilities, Map<String, String> params) {
        this.sql = sql;
        this.clientCapabilities = clientCapabilities;
        this.params = params;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteBufferLittleEndianOutputStream out = new ByteBufferLittleEndianOutputStream();
        out.writeInt(CommandTypeEnum.COM_QUERY.ordinal(), 1);
        int numParams = params != null ? params.size() : 0;
        if (CapabilitiesFlagsEnum.has(clientCapabilities, CapabilitiesFlagsEnum.CLIENT_QUERY_ATTRIBUTES)) {
            out.writeLenencInt(numParams);// parameter_count：Number of parameters
            out.writeLenencInt(1);// parameter_set_count：Number of parameter sets. Currently always 1
            /**
             *
             * {@link com.mysql.cj.protocol.a.NativeMessageBuilder#buildComStmtExecute}
             */
            if (params.size() > 0) {
                int currentPos = out.getPos();
                int nullBitMapLength = (numParams + 7) / 8;
                byte[] nullBitMap = new byte[nullBitMapLength];
                out.write(nullBitMap);// 先占位
                out.writeInt(1, 1);// new_params_bind_flag：Always 1. Malformed packet error if not 1

                int fieldPos = 1;
                for (Map.Entry<String, String> pE : params.entrySet()) {
                    //out.writeInt(, 2);// param_type_and_flag：Parameter type (2 bytes). The MSB is reserved for unsigned flag
                    //out.writeLenencString(, StandardCharsets.UTF_8);// parameter name

                    // bitmap-index：(field-pos + offset) / 8
                    // bitmap-bit: (field-post + offset) % 8
                    // 这里的offset固定为0
                    int bitMapIndex = fieldPos / 8;
                    int bitMapBit = fieldPos % 8;
                    nullBitMap[bitMapIndex] |= 1 << bitMapBit;
                    fieldPos++;
                }
                out.setPosition(currentPos);
                out.write(nullBitMap);
                out.resetPosition();
                //TODO parameter_values
            }
        }
        out.writeNullTerminatedString(this.sql);
        return out.toByteArray();
    }
}
