package com.cl.mysql.binlog.exception;

import java.io.IOException;

/**
 * @description:
 * @author: liuzijian
 * @time: 2023-08-10 16:06
 */
public class ServerException extends IOException {
    private int errorCode;
    private String sqlState;

    public ServerException(String message, int errorCode, String sqlState) {
        super(message);
        this.errorCode = errorCode;
        this.sqlState = sqlState;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getSqlState() {
        return sqlState;
    }
}
