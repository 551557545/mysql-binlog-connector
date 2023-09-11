package com.cl.mysql.binlog.constant;

import cn.hutool.core.collection.CollectionUtil;
import com.cl.mysql.binlog.network.protocol.packet.TextResultRowPacket;
import com.cl.mysql.binlog.network.protocol.packet.TextResultSetPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description:
 * @author: liuzijian
 * @time: 2023-09-11 13:39
 */
@Getter
@AllArgsConstructor
public enum BinlogRowMetadataEnum {

    MINIMAL,
    FULL,
    ;

    public static BinlogRowMetadataEnum getEnum(TextResultSetPacket packet) {
        if (CollectionUtil.isNotEmpty(packet.getResultRowPacketList())) {
            TextResultRowPacket firstRow = packet.getResultRowPacketList().get(0);
            String type = firstRow.getValues().get(1);
            for (BinlogRowMetadataEnum e : values()) {
                if (e.name().equals(type)) {
                    return e;
                }
            }
            throw new RuntimeException("未找到合适的枚举");
        } else {
            throw new RuntimeException("结果行为空");
        }
    }

}
