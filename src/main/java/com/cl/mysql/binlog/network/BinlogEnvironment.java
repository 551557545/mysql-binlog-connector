package com.cl.mysql.binlog.network;

import cn.hutool.core.util.StrUtil;
import com.cl.mysql.binlog.binlogEvent.TableMapEvent;
import com.cl.mysql.binlog.constant.BinlogCheckSumEnum;
import com.cl.mysql.binlog.exception.EnvironmentException;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

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

    public void checkValue() {
        if (StrUtil.isBlank(this.getHost())) {
            throw new EnvironmentException("host cannot be blank");
        } else if (this.getPort() > 65535 | this.getPort() == 0) {
            throw new EnvironmentException("port must between 1 and 65535");
        } else if (StrUtil.isBlank(this.getUserName())) {
            throw new EnvironmentException("userName cannot be blank");
        } else if (StrUtil.isBlank(this.getPassword())) {
            throw new EnvironmentException("password cannot be blank");
        }
    }
}
