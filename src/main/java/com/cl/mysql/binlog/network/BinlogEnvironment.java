package com.cl.mysql.binlog.network;

import com.cl.mysql.binlog.binlogEvent.TableMapEvent;
import com.cl.mysql.binlog.constant.BinlogCheckSumEnum;
import lombok.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: binlog环境配置
 * @author: liuzijian
 * @time: 2023-09-19 14:20
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter(value = AccessLevel.PROTECTED)
public class BinlogEnvironment extends ClientProperties {

    private Map<Long, TableMapEvent> tableInfo = new ConcurrentHashMap<>();

    private boolean successLogin;

    private BinlogCheckSumEnum checkSum;
}
