package com.cl.mysql.binlog.network.protocol.packet;

import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: 结果行包，<a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_query_response_text_resultset_row.html">文档</a>
 * @author: liuzijian
 * @time: 2023-08-21 17:37
 */
@Getter
public class TextResultRowPacket {

    /**
     * 列数
     */
    private final int columnNum;

    /**
     * 列值
     */
    private final List<String> values;


    public TextResultRowPacket(byte[] bytes, int columnNum) throws IOException {
        this.columnNum = columnNum;
        this.values = new ArrayList<>(columnNum);

        ByteArrayIndexInputStream in = new ByteArrayIndexInputStream(bytes);
        while (in.available() > 0){
            this.values.add(in.readLenencString());
        }
    }
}
