package com.cl.mysql.binlog.network.command;

import java.io.IOException;

/**
 * @description: 请求获取公钥
 * @author: liuzijian
 * @time: 2023-08-16 09:31
 */
public class RequestPublicKeyCommand implements Command {

    public RequestPublicKeyCommand() {
    }

    @Override
    public byte[] toByteArray() throws IOException {
        return new byte[]{0x02};
    }
}
