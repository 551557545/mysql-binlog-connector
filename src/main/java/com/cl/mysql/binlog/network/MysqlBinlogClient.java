package com.cl.mysql.binlog.network;

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

    public void listenBinlog() {

    }
}
