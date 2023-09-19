package com.cl.mysql.binlog.listener;

import com.cl.mysql.binlog.binlogEvent.Event;

/**
 * @description: 事件监听者
 * @author: liuzijian
 * @time: 2023-09-18 15:53
 */
public interface EventListener {

    /**
     * 监听所有事件
     *
     * @param event
     */
    void listenAll(Event event);

    /**
     * 监听更新事件
     *
     * @param event
     */
    void listenUpdateEvent(Event event);

    /**
     * 监听删除事件
     *
     * @param event
     */
    void listenDeleteEvent(Event event);

    /**
     * 监听新增事件
     *
     * @param event
     */
    void listenInsertEvent(Event event);

}
