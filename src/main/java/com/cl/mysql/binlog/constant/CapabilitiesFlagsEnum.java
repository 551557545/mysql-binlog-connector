package com.cl.mysql.binlog.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description: 客户端/服务端 连接能力枚举
 * <p> {@see https://dev.mysql.com/doc/dev/mysql-server/latest/group__group__cs__capabilities__flags.html}
 * @author: liuzijian
 * @time: 2023-08-10 17:20
 */
@AllArgsConstructor
@Getter
public enum CapabilitiesFlagsEnum {

    /**
     * Use the improved version of Old Password Authentication. 旧密码插件
     */
    CLIENT_LONG_PASSWORD(1),
    /**
     * Send found rows instead of affected rows in EOF_Packet.
     */
    CLIENT_FOUND_ROWS(1 << 1),

    /**
     * Get all column flags. 获取所有字段
     */
    CLIENT_LONG_FLAG(1 << 2),

    /**
     * Database (schema) name can be specified on connect in Handshake Response Packet. 是否带有 dbname
     */
    CLIENT_CONNECT_WITH_DB(1 << 3),

    /**
     * DEPRECATED: Don't allow database.table.column 已弃用(不允许使用db.table.col)
     */
    CLIENT_NO_SCHEMA(1 << 4),

    /**
     * Compression protocol supported. 是否支持压缩
     */
    CLIENT_COMPRESS(1 << 5),

    /**
     * Special handling of ODBC behavior.
     */
    CLIENT_ODBC(1 << 6),

    /**
     * <p> Server：
     * Enables the LOCAL INFILE request of LOAD DATA|XML.
     *
     * <p> Client：
     * Will handle LOCAL INFILE request.
     */
    CLIENT_LOCAL_FILES(1 << 7),

    /**
     * <p> Ignore spaces before '('.
     *
     * <p> Server：
     * Parser can ignore spaces before '('.
     *
     * <p> Client：
     * Let the parser ignore spaces before '('.
     */
    CLIENT_IGNORE_SPACE(1 << 8),

    /**
     * <p> Enable/disable multi-results.
     *
     * <p> Server：
     * Can send multiple resultsets for COM_QUERY. Error if the server needs to send them and client does not support them.
     *
     * <p> Client：
     * Can handle multiple resultsets for COM_QUERY.
     */
    CLIENT_PROTOCOL_41(1 << 9),

    /**
     * <p> This is an interactive client.
     *
     * <p> Use System_variables::net_wait_timeout versus System_variables::net_interactive_timeout.
     *
     * <p> Server：
     * Supports interactive and noninteractive clients.
     *
     * <p> Client：
     * Client is interactive.
     */
    CLIENT_INTERACTIVE(1 << 10),

    /**
     * <p> Verify server certificate.
     *
     * <p> Client only flag.
     */
    CLIENT_SSL(1 << 11),

    /**
     * <p> Client only flag.
     *
     * <p> Not used.
     *
     * <p> Client：
     * Do not issue SIGPIPE if network failures occur (libmysqlclient only).
     */
    CLIENT_IGNORE_SIGPIPE(1 << 12),

    /**
     * <p> Client knows about transactions.
     *
     * <p> Server：
     * Can send status flags in OK_Packet / EOF_Packet.
     *
     * <p> Client：
     * Expects status flags in OK_Packet / EOF_Packet.
     *
     * <p> Note：
     * This flag is optional in 3.23, but always set by the server since 4.0.
     */
    CLIENT_TRANSACTIONS(1 << 13),

    /**
     * DEPRECATED: Old flag for 4.1 protocol 已弃用
     */
    CLIENT_RESERVED(1 << 14),

    /**
     * DEPRECATED: Old flag for 4.1 authentication \ CLIENT_SECURE_CONNECTION.
     */
    CLIENT_RESERVED2(1 << 15),

    /**
     * <p> Enable/disable multi-stmt support. 是否支持multi-stmt.  COM_QUERY/COM_STMT_PREPARE中多条语句
     *
     * <p> Also sets CLIENT_MULTI_RESULTS. Currently not checked anywhere.
     *
     * <p> Server：
     * Can handle multiple statements per COM_QUERY and COM_STMT_PREPARE.
     *
     * <p> Client：
     * May send multiple statements per COM_QUERY and COM_STMT_PREPARE.
     *
     * <p> Note：
     * Was named CLIENT_MULTI_QUERIES in 4.1.0, renamed later.
     */
    CLIENT_MULTI_STATEMENTS(1 << 16),

    /**
     * <p> Enable/disable multi-results.
     *
     * <p> Server：
     * Can send multiple resultsets for COM_QUERY. Error if the server needs to send them and client does not support them.
     *
     * <p> Client：
     * Can handle multiple resultsets for COM_QUERY.
     *
     * <p> Requires：
     * CLIENT_PROTOCOL_41
     */
    CLIENT_MULTI_RESULTS(1 << 17),

    /**
     * <p> Multi-results and OUT parameters in PS-protocol.
     *
     * <p> Server：
     * Can send multiple resultsets for COM_STMT_EXECUTE.
     *
     * <p> Client：
     * Can handle multiple resultsets for COM_STMT_EXECUTE.
     *
     * <p> Requires：
     * CLIENT_PROTOCOL_41
     */
    CLIENT_PS_MULTI_RESULTS(1 << 18),

    /**
     * <p> Client supports plugin authentication. 是否支持密码插件
     *
     * <p> Server：
     * Sends extra data in Initial Handshake Packet and supports the pluggable authentication protocol.
     *
     * <p> Client：
     * Supports authentication plugins.
     *
     * <p> Requires：
     * CLIENT_PROTOCOL_41
     */
    CLIENT_PLUGIN_AUTH(1 << 19),

    /**
     * <p> Client supports connection attributes. 是否支持连接属性
     *
     * <p> Server：
     * Permits connection attributes in Protocol::HandshakeResponse41.
     *
     * <p> Client：
     * Sends connection attributes in Protocol::HandshakeResponse41.
     */
    CLIENT_CONNECT_ATTRS(1 << 20),

    /**
     * <p> Enable authentication response packet to be larger than 255 bytes.
     *
     * <p> When the ability to change default plugin require that the initial password field in the Protocol::HandshakeResponse41 paclet can be of arbitrary size. However, the 4.1 client-server protocol limits the length of the auth-data-field sent from client to server to 255 bytes. The solution is to change the type of the field to a true length encoded string and indicate the protocol change with this client capability flag.
     *
     * <p> 判断是否允许发送返回大于255字节的HasdShakeResponse报文
     *
     * <p> Server：
     * Understands length-encoded integer for auth response data in Protocol::HandshakeResponse41.
     *
     * <p> Client：
     * Length of auth response data in Protocol::HandshakeResponse41 is a length-encoded integer.
     *
     * <p> Note：
     * The flag was introduced in 5.6.6, but had the wrong value.
     */
    CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA(1 << 21),

    /**
     * <p> Don't close the connection for a user account with expired password.
     *
     * <p> Server：
     * Announces support for expired password extension.
     *
     * <p> Client：
     * Can handle expired passwords.
     */
    CLIENT_CAN_HANDLE_EXPIRED_PASSWORDS(1 << 22),

    /**
     * <p> Capable of handling server state change information.
     *
     * <p> Its a hint to the server to include the state change information in OK_Packet.
     *
     * <p> Server：
     * Can set SERVER_SESSION_STATE_CHANGED in the SERVER_STATUS_flags_enum and send Session State Information in a OK_Packet.
     *
     * <p> Client：
     * Expects the server to send Session State Information in a OK_Packet.
     */
    CLIENT_SESSION_TRACK(1 << 23),

    /**
     * <p> Server：
     * Can send OK after a Text Resultset.
     *
     * <p> Client：
     * Expects an OK_Packet (instead of EOF_Packet) after the resultset rows of a Text Resultset.
     *
     * <p> Background：
     * To support CLIENT_SESSION_TRACK, additional information must be sent after all successful commands. Although the OK_Packet is extensible, the EOF_Packet is not due to the overlap of its bytes with the content of the Text Resultset Row.
     * <p>
     * Therefore, the EOF_Packet in the Text Resultset is replaced with an OK_Packet. EOF_Packet is deprecated as of MySQL 5.7.5.
     */
    CLIENT_DEPRECATE_EOF(1 << 24),

    /**
     * <p> Server：
     * Sends extra data in Initial Handshake Packet and supports the pluggable authentication protocol.
     *
     * <p> Client：
     * Supports authentication plugins.
     *
     * <p> Requires：
     * CLIENT_PROTOCOL_41
     */
    CLIENT_OPTIONAL_RESULTSET_METADATA(1 << 25),

    /**
     * <p> Compression protocol extended to support zstd compression method.
     *
     * <p> This capability flag is used to send zstd compression level between client and server provided both client and server are enabled with this flag.
     *
     * <p> Server：
     * Server sets this flag when global variable protocol-compression-algorithms has zstd in its list of supported values.
     *
     * <p> Client：
     * Client sets this flag when it is configured to use zstd compression method.
     */
    CLIENT_ZSTD_COMPRESSION_ALGORITHM(1 << 26),

    /**
     * <p> Support optional extension for query parameters into the COM_QUERY and COM_STMT_EXECUTE packets.
     *
     * <p> Server：
     * Expects an optional part containing the query parameter set(s). Executes the query for each set of parameters or returns an error if more than 1 set of parameters is sent and the server can't execute it.
     *
     * <p> Client：
     * Can send the optional part containing the query parameter set(s).
     */
    CLIENT_QUERY_ATTRIBUTES(1 << 27),

    /**
     * <p> Support Multi factor authentication.
     *
     * <p> Server：
     * Server sends AuthNextFactor packet after every nth factor authentication method succeeds, except the last factor authentication.
     *
     * <p> Client：
     * Client reads AuthNextFactor packet sent by server and initiates next factor authentication method.
     */
    MULTI_FACTOR_AUTHENTICATION(1 << 28),

    /**
     * This flag will be reserved to extend the 32bit capabilities structure to 64bits.
     */
    CLIENT_CAPABILITY_EXTENSION(1 << 29),

    /**
     * <p> Verify server certificate. 已弃用
     *
     * <p> Client only flag.
     *
     * <p> Deprecated:
     * in favor of –ssl-mode.
     */
    CLIENT_SSL_VERIFY_SERVER_CERT(1 << 30),

    /**
     * <p> Don't reset the options after an unsuccessful connect.
     *
     * <p> Client only flag.
     *
     * <p> Typically passed via mysql_real_connect() 's client_flag parameter.
     */
    CLIENT_REMEMBER_OPTIONS(1 << 31),
    ;

    private final int code;


    /**
     * 删除能力
     *
     * @param capabilities
     * @param e
     * @return
     */
    public static int canncel(int capabilities, CapabilitiesFlagsEnum e) {
        if ((capabilities & e.getCode()) != 0) {
            return ~capabilities ^ ~e.getCode();
        }
        return capabilities;
    }

    public static boolean has(int capabilities, CapabilitiesFlagsEnum e) {
        return (capabilities & e.getCode()) != 0;
    }

    public static int add(int capabilities, CapabilitiesFlagsEnum... es) {
        for (CapabilitiesFlagsEnum item : es) {
            capabilities |= item.getCode();
        }
        return capabilities;
    }
}
