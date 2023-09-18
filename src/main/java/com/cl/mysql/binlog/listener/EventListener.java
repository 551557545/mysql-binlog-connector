package com.cl.mysql.binlog.listener;

import com.cl.mysql.binlog.binlogEvent.Event;

/**
 * @description:
 * @author: liuzijian
 * @time: 2023-09-18 15:53
 */
public interface EventListener {

    void listenAll(Event event);

    void listenUpdateEvent(Event event);

    void listenDeleteEvent(Event event);

    void listenInsertEvent(Event event);

}
