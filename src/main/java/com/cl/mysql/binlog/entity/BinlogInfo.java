package com.cl.mysql.binlog.entity;

import cn.hutool.core.collection.CollectionUtil;
import com.cl.mysql.binlog.network.protocol.packet.TextResultRowPacket;
import com.cl.mysql.binlog.network.protocol.packet.TextResultSetPacket;
import lombok.Getter;

/**
 * @description:
 * @author: liuzijian
 * @time: 2023-08-22 15:50
 */
@Getter
public class BinlogInfo {

    private String fileName;

    private Integer position;

    public BinlogInfo(TextResultSetPacket packet) {
        if (CollectionUtil.isNotEmpty(packet.getResultRowPacketList())) {
            TextResultRowPacket firstRow = packet.getResultRowPacketList().get(0);
            this.fileName = firstRow.getValues().get(0);
            this.position = Integer.parseInt(firstRow.getValues().get(1));
        } else {
            throw new RuntimeException("结果行为空");
        }
    }
}
