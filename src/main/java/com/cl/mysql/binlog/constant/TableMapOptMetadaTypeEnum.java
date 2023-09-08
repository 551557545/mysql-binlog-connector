package com.cl.mysql.binlog.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/rows__event_8h_source.html">官方文档</a>，ctrl+f搜SIGNEDNESS，枚举值让我找了好久
 * @author: liuzijian
 * @time: 2023-09-08 16:50
 */
@Getter
@AllArgsConstructor
public enum TableMapOptMetadaTypeEnum {

    SIGNEDNESS(1),
    DEFAULT_CHARSET(2),
    COLUMN_CHARSET(3),
    COLUMN_NAME(4),
    SET_STR_VALUE(5),
    ENUM_STR_VALUE(6),
    GEOMETRY_TYPE(7),
    SIMPLE_PRIMARY_KEY(8),
    PRIMARY_KEY_WITH_PREFIX(9),
    ENUM_AND_SET_DEFAULT_CHARSET(10),
    ENUM_AND_SET_COLUMN_CHARSET(11);

    private final int code;

    private static final Map<Integer, TableMapOptMetadaTypeEnum> cache;

    static {
        cache = new HashMap<>();
        for (TableMapOptMetadaTypeEnum e : values()) {
            cache.put(e.code, e);
        }
    }

    public static TableMapOptMetadaTypeEnum getByCode(int code) {
        return cache.get(code);
    }

}
