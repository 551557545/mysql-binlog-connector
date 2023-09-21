package com.cl.mysql.binlog.network;

import com.cl.mysql.binlog.listener.EventListener;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * @description:
 * @author: liuzijian
 * @time: 2023-09-19 14:16
 */
public class MysqlBinlogClient {

    private final ClientProperties properties;

    private MysqlBinLogConnector binlogSlaver;

    private MysqlBinLogConnector sqlExecutor;

    public MysqlBinlogClient(ClientProperties properties) {
        this.properties = properties;
    }

    public void listenBinlog(EventListener eventListener) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        binlogSlaver = new MysqlBinLogConnector(this.properties);
        binlogSlaver.loginToMysql();
        binlogSlaver.registerEventListener(eventListener);
        binlogSlaver.listen();
    }
}
