package com.cl.mysql.binlog.network;

import cn.hutool.core.bean.BeanUtil;
import com.cl.mysql.binlog.constant.CapabilitiesFlagsEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

/**
 * @description: 客户端参数配置
 * @author: liuzijian
 * @time: 2023-09-19 14:04
 */
@Data
public class ClientProperties {

    private String host;

    private int port;

    private String userName;

    private String password;

    private boolean ssl;

    private String dataBaseScram;

    @Setter(value = AccessLevel.PROTECTED)
    private int clientCapabilities;

    public void addClientCapabilities(CapabilitiesFlagsEnum e) {
        this.clientCapabilities = CapabilitiesFlagsEnum.add(this.clientCapabilities, e);
    }

    protected BinlogEnvironment convertToEnvironment() {
        BinlogEnvironment e = new BinlogEnvironment();
        BeanUtil.copyProperties(this,e);
        e.checkValue();
        return e;
    }
}
