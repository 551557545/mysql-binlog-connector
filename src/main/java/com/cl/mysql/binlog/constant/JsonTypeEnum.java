package com.cl.mysql.binlog.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: liuzijian
 * @time: 2023-09-18 14:45
 */
@AllArgsConstructor
@Getter
public enum JsonTypeEnum {

    SMALL_OBJECT(0x0),
    LARGE_OBJECT(0x1),
    SMALL_ARRAY(0x2),
    LARGE_ARRAY(0x3),
    LITERAL(0x4),
    INT16(0x5),
    UINT16(0x6),
    INT32(0x7),
    UINT32(0x8),
    INT64(0x9),
    UINT64(0xA),
    DOUBLE(0xB),
    STRING(0xC),
    OPAQUE(0xF),
    ;

    private final int code;

    private static Map<Integer, JsonTypeEnum> cache;

    static {
        cache = new HashMap<>(values().length);
        for (JsonTypeEnum e : values()) {
            cache.put(e.getCode(), e);
        }
    }

    public static JsonTypeEnum getByCode(int code) {
        return cache.get(code);
    }

}
