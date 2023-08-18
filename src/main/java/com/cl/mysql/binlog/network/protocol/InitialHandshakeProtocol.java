package com.cl.mysql.binlog.network.protocol;

import com.cl.mysql.binlog.constant.CapabilitiesFlagsEnum;
import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @description: <p> server -> client  握手协议
 * <p> https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_packets_protocol_handshake_v10.html
 * <p> protocol_version（1 byte）                   服务器协议版本号
 * <p> server_version（60 byte）                    服务器的版本号
 * <p> conn_thread_id（4 byte）                     服务器为客户端分配
 * <p> before_scramble_data（8 byte）               服务器生成的随机挑战数据的前8个字节
 * <p> 0x00（1 byte）                               填充值
 * <p> client_capabilities（low 2 byte）            期望客户端访问的能力低两个字节
 * <p> charset_num（1 byte）                        字符编码号
 * <p> server_status（2 byte）                      服务器状态
 * <p> client_capabilities（up 2 byte）             期望客户端访问的能力高两个字节
 * <p> scramble_len（1 byte）                       挑战数据总长
 * <p> 0x00（10 byte）                              填充值
 * <p> after_scramble_data（scramble_len - 8 byte） 服务器生成的随机挑战数据的剩余字节
 * <p> plugin_name（null end string）               MPVIO_EXT插件的名称
 * @author: liuzijian
 * @time: 2023-08-10 14:13
 */
@Slf4j
@Getter
@ToString
public class InitialHandshakeProtocol {

    /**
     * 服务器协议版本号
     */
    private final int protocolVersion;
    /**
     * mysql服务器的版本号
     */
    private final String serverVersion;
    /**
     * 服务器为客户端分配连接线程id
     */
    private final long threadId;
    /**
     * 挑战字符串前缀
     */
    private final String scramblePrefix;
    /**
     * 挑战字符串后缀
     */
    private final String scrambleSuffix;

    /**
     * 完整挑战字节的长度 即 scramblePrefix + scrambleSuffix，一般是20个字节 传过来21的长度 结尾字节为0 所以要去掉0 减去一个长度
     */
    private final int scrambleLength;
    /**
     * 期望客户端访问的能力低两个字节
     */
    private final int lowClientCapabilities;
    /**
     * 字符编码号
     */
    private final int charsetNum;
    /**
     * 服务器状态
     */
    private final int serverStatus;
    /**
     * 期望客户端访问的能力高两个字节
     */
    private final int highClientCapabilities;

    /**
     * 完整挑战字符串
     */
    private final String scramble;

    /**
     * 插件名称
     */
    private final String pluginName;

    /**
     * 合并highClientCapabilities 和 lowClientCapabilities
     */
    private final int clientCapabilities;

    public InitialHandshakeProtocol(byte[] bytes) throws IOException {
        ByteArrayIndexInputStream buffer = new ByteArrayIndexInputStream(bytes);
        // 固定为10
        this.protocolVersion = buffer.readInt(1);
        // mysql服务器版本
        this.serverVersion = buffer.readStringTerminatedByZero();
        // mysql服务器连接分配的线程id
        this.threadId = buffer.readLong(4);
        // 挑战字符前缀
        this.scramblePrefix = buffer.readString(8);
        // 跳过一个0
        buffer.skip(1);
        // 第八位能力参数
        this.lowClientCapabilities = buffer.readInt(2);
        // 服务器编码
        this.charsetNum = buffer.readInt(1);
        // 服务器状态
        this.serverStatus = buffer.readInt(2);
        // 高八位能力参数
        this.highClientCapabilities = buffer.readInt(2);
        // 合成能力参数
        this.clientCapabilities = (highClientCapabilities << 16) | lowClientCapabilities;
        // 获取挑战字节长度
        if ((this.clientCapabilities & CapabilitiesFlagsEnum.CLIENT_PLUGIN_AUTH.getCode()) != 0) {
            this.scrambleLength = buffer.readInt(1);
        } else {
            this.scrambleLength = 0;
        }
        // 跳过10个0
        buffer.skip(10);
        // 根据挑战字节长度 获取挑战字节后缀
        if (this.scrambleLength != 0) {
            // 要减多一个1然后跳过 最后一位是0
            this.scrambleSuffix = buffer.readString(this.scrambleLength - this.scramblePrefix.getBytes().length - 1);
            buffer.skip(1);
        } else {
            this.scrambleSuffix = "";
        }
        // 拼接挑战字节
        this.scramble = this.scramblePrefix + this.scrambleSuffix;

        // 获取认证插件 mysql_native_password 或者是 caching_sha2_password
        // 在8.0服务器端和客户端中，默认的认证方式为Caching_sha2_password信息
        if (buffer.available() > 0) {
            this.pluginName = buffer.readStringTerminatedByZero();
        } else {
            this.pluginName = null;
        }
        log.info("握手协议：{}", this);
    }

    /**
     * 判断服务器是否支持ssl
     *
     * @return
     */
    public boolean supportSSL() {
        return (this.clientCapabilities & CapabilitiesFlagsEnum.CLIENT_SSL.getCode()) != 0;
    }

    /**
     * 是否支持41协议
     *
     * @return
     */
    public boolean support41Protocol() {
        return (this.clientCapabilities & CapabilitiesFlagsEnum.CLIENT_PROTOCOL_41.getCode()) != 0;
    }

}
