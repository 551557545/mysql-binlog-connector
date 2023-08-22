package com.cl.mysql.binlog.network.protocol.packet;

import lombok.Getter;

import java.util.List;

/**
 * @description: <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_query_response_text_resultset.html">相关文档</a>
 * @author: liuzijian
 * @time: 2023-08-22 13:51
 */
@Getter
public class TextResultSetPacket {

    /**
     * 字段描述
     */
    private List<ColumnDefinitionPacket> columnDefinitionPacketList;

    /**
     * 结果行
     */
    private List<TextResultRowPacket> resultRowPacketList;

    public TextResultSetPacket(List<ColumnDefinitionPacket> columnDefinitionPacketList, List<TextResultRowPacket> resultRowPacketList) {
        this.columnDefinitionPacketList = columnDefinitionPacketList;
        this.resultRowPacketList = resultRowPacketList;
    }

}
