package com.cl.mysql.binlog.constant;

import cn.hutool.core.collection.CollectionUtil;
import com.cl.mysql.binlog.network.protocol.packet.TextResultRowPacket;
import com.cl.mysql.binlog.network.protocol.packet.TextResultSetPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description:
 * @author: liuzijian
 * @time: 2023-08-23 10:51
 */
@Getter
@AllArgsConstructor
public enum BinlogCheckSumEnum {

    NONE(0),
    /**
     * CRC32函数返回校验值的无符号整数（32位）
     */
    CRC32(4),
    ;

    private final int length;

    public static BinlogCheckSumEnum getByOrdinal(int ordinal){
        return values()[ordinal];
    }

    public static BinlogCheckSumEnum getEnum(TextResultSetPacket packet) {
        if (CollectionUtil.isNotEmpty(packet.getResultRowPacketList())) {
            TextResultRowPacket firstRow = packet.getResultRowPacketList().get(0);
            String type = firstRow.getValues().get(1);
            for (BinlogCheckSumEnum e : values()) {
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
