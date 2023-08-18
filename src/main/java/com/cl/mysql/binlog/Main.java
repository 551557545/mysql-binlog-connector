package com.cl.mysql.binlog;

import com.cl.mysql.binlog.network.MysqlBinLogConnector;

public class Main {
    public static void main(String[] args) throws Exception {
//        new com.mysql.jdbc.Driver();
//        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/test", "root", "57895123");
        MysqlBinLogConnector connector = MysqlBinLogConnector.openConnect("127.0.0.1", 3307, "root", "578951231", false, null);

    }
}
