package com.cl.mysql.binlog;

import com.cl.mysql.binlog.binlogEvent.Event;
import com.cl.mysql.binlog.listener.EventListener;
import com.cl.mysql.binlog.network.MysqlBinLogConnector;

public class Main {
    public static void main(String[] args) throws Exception {
//        new com.mysql.jdbc.Driver();
//        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/test", "root", "57895123");
        MysqlBinLogConnector connector = MysqlBinLogConnector.openConnect("127.0.0.1", 3307, "root", "57895123", false, null);
        connector.registerEventListener(new EventListener() {
            @Override
            public void listenAll(Event event) {
                //监听所有事件
                System.out.println(1);
            }

            @Override
            public void listenUpdateEvent(Event event) {
                //监听更新事件
            }

            @Override
            public void listenDeleteEvent(Event event) {
                //监听删除事件
            }

            @Override
            public void listenInsertEvent(Event event) {
                //监听新增事件
            }
        });
        connector.listen();
    }
}
