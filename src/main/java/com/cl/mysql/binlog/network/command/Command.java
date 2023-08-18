package com.cl.mysql.binlog.network.command;

import java.io.IOException;

/**
 * @description: 命令接口
 * @author: liuzijian
 * @time: 2023-08-11 09:35
 */
public interface Command {

    byte[] toByteArray() throws IOException;

}
