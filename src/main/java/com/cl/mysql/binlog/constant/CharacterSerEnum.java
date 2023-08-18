package com.cl.mysql.binlog.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description: 一般用不上 直接在mysql 执行  SELECT id, collation_name FROM information_schema.collations ORDER BY id; 去查
 * @author: liuzijian
 * @time: 2023-08-10 16:51
 */
@Deprecated
@AllArgsConstructor
@Getter
public enum CharacterSerEnum {

    BIG5_CHINESE_CI(1, "big5_chinese_ci"),
    LATIN2_CZECH_CS(2, "latin2_czech_cs"),
    DEC8_SWEDISH_CI(3, "dec8_swedish_ci"),
    CP850_GENERAL_CI(4, "cp850_general_ci"),
    LATIN1_GERMAN1_CI(5, "latin1_german1_ci"),
    HP8_ENGLISH_CI(6, "hp8_english_ci"),
    KOI8R_GENERAL_CI(7, "koi8r_general_ci"),
    LATIN1_SWEDISH_CI(8, "latin1_swedish_ci"),
    LATIN2_GENERAL_CI(9, "latin2_general_ci"),
    SWE7_SWEDISH_CI(10, "swe7_swedish_ci"),
    UTF8MB3_GENERAL_CI(33, "utf8mb3_general_ci"),
    ;

    private final int code;

    private final String desc;

}
