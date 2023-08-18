package com.cl.mysql.binlog.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description: {@see https://dev.mysql.com/doc/dev/mysql-server/latest/mysql__com_8h.html#a1d854e841086925be1883e4d7b4e8cad}
 * @author: liuzijian
 * @time: 2023-08-14 16:26
 */
@AllArgsConstructor
@Getter
public enum ServerStatusEnum {

    /**
     * Is raised when a multi-statement transaction has been started, either explicitly, by means of BEGIN or COMMIT AND CHAIN, or implicitly, by the first transactional statement, when autocommit=off.
     * <p>在多语句事务启动时引发，通过BEGIN或COMMIT AND CHAIN显式启动，或者在autocommit=off时由第一个事务语句隐式启动。</p>
     */
    SERVER_STATUS_IN_TRANS(1),
    /**
     * Server in auto_commit mode.
     * <p>服务器为自动提交事务模式</p>
     */
    SERVER_STATUS_AUTOCOMMIT(1 << 1),
    /**
     * Multi query - next query exists.
     * <p>复合查询</p>
     */
    SERVER_MORE_RESULTS_EXISTS(1 << 2),
    SERVER_QUERY_NO_GOOD_INDEX_USED(1 << 3),
    SERVER_QUERY_NO_INDEX_USED(1 << 4),
    /**
     * <p> The server was able to fulfill the clients request and opened a read-only non-scrollable cursor for a query.
     * <p>服务器能够满足客户端的请求，并为查询打开一个只读的不可滚动游标。</p>
     * <p> This flag comes in reply to COM_STMT_EXECUTE and COM_STMT_FETCH commands. Used by Binary Protocol Resultset to signal that COM_STMT_FETCH must be used to fetch the row-data.
     * <p>这个标志是对COM_STMT_EXECUTE和COM_STMT_FETCH命令的响应。由二进制协议Resultset使用，表示必须使用COM_STMT_FETCH来获取行数据。</p>
     */
    SERVER_STATUS_CURSOR_EXISTS(1 << 5),
    /**
     * This flag is sent when a read-only cursor is exhausted, in reply to COM_STMT_FETCH command.
     * <p>当只读游标耗尽时发送此标志，以响应COM_STMT_FETCH命令。</p>
     */
    SERVER_STATUS_LAST_ROW_SENT(1 << 6),
    /**
     * A database was dropped.
     * <p>数据库被丢弃</p>
     */
    SERVER_STATUS_DB_DROPPED(1 << 7),
    SERVER_STATUS_NO_BACKSLASH_ESCAPES(1 << 8),
    /**
     * Sent to the client if after a prepared statement reprepare we discovered that the new statement returns a different number of result set columns.
     * <p>如果在准备好的语句重新准备之后，我们发现新语句返回的结果集列数不同，则发送到客户端。</p>
     */
    SERVER_STATUS_METADATA_CHANGED(1 << 9),
    SERVER_QUERY_WAS_SLOW(1 << 10),
    /**
     * To mark ResultSet containing output parameter values.
     * <p>标记包含输出参数值的结果集。</p>
     */
    SERVER_PS_OUT_PARAMS(1 << 11),
    /**
     * <p> Set at the same time as SERVER_STATUS_IN_TRANS if the started multi-statement transaction is a read-only transaction.
     * <p>如果启动的多语句事务是只读事务，则与SERVER_STATUS_IN_TRANS同时设置。</p>
     *
     * <p> Cleared when the transaction commits or aborts. Since this flag is sent to clients in OK and EOF packets, the flag indicates the transaction status at the end of command execution.
     * <p> 事务提交或终止时清除。由于该标志以OK和EOF包的形式发送给客户端，因此该标志指示命令执行结束时的事务状态。
     */
    SERVER_STATUS_IN_TRANS_READONLY(1 << 12),
    /**
     * This status flag, when on, implies that one of the state information has changed on the server because of the execution of the last statement.
     * <p> 如果状态标志为on，则表示由于执行了最后一条语句，服务器上的某个状态信息发生了更改。
     */
    SERVER_SESSION_STATE_CHANGED(1 << 13);

    private final int code;

}
