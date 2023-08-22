package com.cl.mysql.binlog.network.command;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import com.cl.mysql.binlog.constant.CapabilitiesFlagsEnum;
import com.cl.mysql.binlog.exception.ServerException;
import com.cl.mysql.binlog.network.PacketChannel;
import com.cl.mysql.binlog.network.protocol.InitialHandshakeProtocol;
import com.cl.mysql.binlog.network.protocol.packet.ErrorPacket;
import com.cl.mysql.binlog.network.protocol.packet.OkPacket;
import com.cl.mysql.binlog.stream.ByteArrayIndexInputStream;
import com.cl.mysql.binlog.stream.ByteArrayIndexOutputStream;
import com.cl.mysql.binlog.util.CommonUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

/**
 * @description: {@see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_packets_protocol_handshake_response.html">相关文档</a>}
 * @author: liuzijian
 * @time: 2023-08-11 14:55
 */
@Slf4j
public class HandShakeResponseCommand implements Command {

    private final String SHA2_PASSWORD = "caching_sha2_password";
    private final String MYSQL_NATIVE = "mysql_native_password";

    private final String userName;

    private final InitialHandshakeProtocol handshakeProtocol;

    private final String password;

    private final boolean ssl;

    private final String dataBaseScram;

    @Getter
    @Setter
    private int clientCapabilities;

    private final PacketChannel channel;

    public HandShakeResponseCommand(InitialHandshakeProtocol handshakeProtocol, String userName, String password, boolean ssl, String dataBaseScram, PacketChannel channel) {
        this.userName = userName;
        this.handshakeProtocol = handshakeProtocol;
        this.password = password;
        this.ssl = ssl;
        this.channel = channel;
        this.dataBaseScram = dataBaseScram;
    }

    public void send() throws IOException {
        channel.sendCommand(this);
        byte[] sslCommand = channel.readDataContent();
        this.checkPacket(sslCommand);
        channel.setHasAuth(true);
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayIndexOutputStream out = new ByteArrayIndexOutputStream();
        InitialHandshakeProtocol handshakeProtocol = this.handshakeProtocol;
        int tempClientCapabilities = this.clientCapabilities;
        boolean ssl = this.ssl;
        String userName = this.userName;
        String password = this.password;
        String dataBaseScram = this.dataBaseScram;

        if (handshakeProtocol.support41Protocol()) {
            if (tempClientCapabilities == 0) {
                tempClientCapabilities = CapabilitiesFlagsEnum.add(tempClientCapabilities,
                        CapabilitiesFlagsEnum.CLIENT_LONG_FLAG,
                        CapabilitiesFlagsEnum.CLIENT_PROTOCOL_41,
                        CapabilitiesFlagsEnum.CLIENT_RESERVED2,
                        CapabilitiesFlagsEnum.CLIENT_PLUGIN_AUTH,
                        CapabilitiesFlagsEnum.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA
                );
                if (StrUtil.isNotBlank(dataBaseScram)) {
                    tempClientCapabilities = CapabilitiesFlagsEnum.add(tempClientCapabilities, CapabilitiesFlagsEnum.CLIENT_CONNECT_WITH_DB);
                }
            }
            if (!ssl) {
                tempClientCapabilities = CapabilitiesFlagsEnum.canncel(tempClientCapabilities, CapabilitiesFlagsEnum.CLIENT_SSL);
            } else {
                tempClientCapabilities = CapabilitiesFlagsEnum.add(tempClientCapabilities, CapabilitiesFlagsEnum.CLIENT_SSL);
            }
            out.writeInt(tempClientCapabilities, 4);
            out.writeInt(0, 4);
            out.writeInt(handshakeProtocol.getCharsetNum(), 1);
            // 写23个0
            out.write(new byte[23]);
            out.writeNullTerminatedString(userName);
            // 这里开始是填入密码
            // 基于mysql_native_password插件
            byte[] encodePassword = null;

            if (SHA2_PASSWORD.equals(handshakeProtocol.getPluginName())) {
                encodePassword = CacheSha2PasswordPlugin.genarateCachingSha2Password(password, this.handshakeProtocol.getScramble());
            } else {
                encodePassword = NativePasswordPlugin.genarateNativeAuthPassword(password, this.handshakeProtocol.getScramble());
            }

            /**
             * 不用判断CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA
             * mysql驱动自己写了一套outputStream的逻辑 维护一个byte数组
             * 而我这里直接使用ByteArrayOutputStream，让他自己维护byte数组
             * {@link com.mysql.cj.protocol.a.NativeAuthenticationProvider#createHandshakeResponsePacket}
             */
            out.writeInt(encodePassword.length, 1);
            out.write(encodePassword);
            if (CapabilitiesFlagsEnum.has(tempClientCapabilities, CapabilitiesFlagsEnum.CLIENT_CONNECT_WITH_DB)) {
                out.writeNullTerminatedString(dataBaseScram);
            }
            if (CapabilitiesFlagsEnum.has(tempClientCapabilities, CapabilitiesFlagsEnum.CLIENT_PLUGIN_AUTH)) {
                out.writeNullTerminatedString(handshakeProtocol.getPluginName());
            }
            if (CapabilitiesFlagsEnum.has(tempClientCapabilities, CapabilitiesFlagsEnum.CLIENT_CONNECT_ATTRS)) {

            }
            if (CapabilitiesFlagsEnum.has(tempClientCapabilities, CapabilitiesFlagsEnum.CLIENT_ZSTD_COMPRESSION_ALGORITHM)) {

            }
        } else {
            out.writeInt(handshakeProtocol.getLowClientCapabilities(), 2);
            out.writeInt(0, 3);
            out.writeNullTerminatedString(userName);
        }
        this.clientCapabilities = tempClientCapabilities;
        return out.toByteArray();
    }

    /**
     * 检查响应报文
     * <h3>情况1：</h3>
     * <p> 根据官方文档，socket开始连接的时候，服务器会返回默认的密码认证插件。但是，客户端需要登录的用户存储的密码不一定是这个默认的插件。有可能是其他的。
     * <p> 这个时候，在发送handShakeResponse去认证用户密码是否准确时，若客户端发送的密码认证插件与当前用户在mysql用户表中存储的不一致的时候，服务器会返回一个正确的插件名称给客户端
     * <p> 这个时候需要根据<a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_packets_protocol_auth_switch_request.html">Protocol::AuthSwitchRequest:结构</a>去解析
     * <h3>情况2：</h3>
     * <P> 继续走chaing_sha2的校验流程
     *
     * @param bytes
     * @throws IOException
     */
    private void checkPacket(byte[] bytes) throws IOException {
        // 如果错误的话 会返回0xff
        if ((bytes[0] & 0xff) == 0xff) {
            // 不拷贝第一个字节
            bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
            ErrorPacket errorPacket = new ErrorPacket(bytes, this.clientCapabilities);
            throw new ServerException(
                    errorPacket.getErrorMessage(),
                    errorPacket.getErrorCode(),
                    errorPacket.getSqlState()
            );
        } else if ((bytes[0] & 0xff) == 0x0) {
            // 说明登录成功
            OkPacket okPacket = new OkPacket(Arrays.copyOfRange(bytes, 1, bytes.length), this.clientCapabilities);
            log.info("【操作成功】ok_packet：{}", okPacket);
        } else if ((bytes[0] & 0xff) == 0xfe) {
            // 说明当前客户端使用的密码插件与用户密码存储在mysql里面的密码插件不一致，要做切换
            this.doSwitchAuthPlugin(bytes);
        } else {
            // 走caching_sha2_password插件的流程
            this.doCachingSha2Process(bytes);
        }
    }

    /**
     * 1、The client connects to the server <br>
     * 2、The server sends Protocol::Handshake <br>
     * 3、The client responds with Protocol::HandshakeResponse: <br>
     * 4、The server sends the Protocol::AuthSwitchRequest: to tell the client that it needs to switch to a new authentication method. <br>
     * 5、Client and server possibly exchange further packets as required by the server authentication method for the user account the client is trying to authenticate against. <br>
     * 6、The server responds with an OK_Packet or rejects with ERR_Packet <br><br>
     *
     * <h3>方法描述：</h3>切换密码控件，现在走到了第四步 这个方法后续处理就是第五步和第六步 <br>
     * 第五步可以选择不切换，断开连接，如果要切换，则返回<a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_packets_protocol_auth_switch_response.html">Protocol::AuthSwitchResponse:结构体</a>就行 <br>
     * Protocol::AuthSwitchResponse就是对应密码插件生成密码的公式算法得出的密码加密后值
     *
     * <p> 文档： <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_packets_protocol_auth_switch_request.html">Protocol::AuthSwitchRequest:结构</a>
     * 、<a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase.html#sect_protocol_connection_phase_auth_method_mismatch">更换密码流程</a>
     *
     * @param bytes
     */
    private void doSwitchAuthPlugin(byte[] bytes) throws IOException {
        ByteArrayIndexInputStream in = new ByteArrayIndexInputStream(bytes);
        in.skip(1);
        // 新的密码插件
        String pluginName = in.readStringTerminatedByZero();
        // 新的斗争码
        String scramble = in.readStringTerminatedByZero();

        if (MYSQL_NATIVE.equals(pluginName)) {
            channel.sendCommand(() -> NativePasswordPlugin.genarateNativeAuthPassword(password, scramble));
        } else if (SHA2_PASSWORD.equals(pluginName)) {
            channel.sendCommand(() -> CacheSha2PasswordPlugin.genarateCachingSha2Password(password, scramble));
        }
        byte[] dataContent = channel.readDataContent();
        this.checkPacket(dataContent);
    }

    /**
     * 根据<a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_caching_sha2_authentication_exchanges.html">caching_sha2_password流程图</a> <br>
     * 当认证成功会返回一个<a href="https://dev.mysql.com/doc/dev/mysql-server/latest/sha2__password_8cc.html#a13f76edb62a3845978ddb48a4fd3ad86">fast auth success</a>的包（是个常量，为3，当时找文档找死我） ，后续会跟着一个ok_packet，直接读一次socket就行<br>
     * 当认证失败会返回一个<a href="https://dev.mysql.com/doc/dev/mysql-server/latest/sha2__password_8cc.html#a0326821e4363bd6924a6d6379b9f858e">perform_full_authentication</a>的包（常量，为4）<br>
     *
     * @param bytes
     * @throws IOException
     */
    private void doCachingSha2Process(byte[] bytes) throws IOException {
        ByteArrayIndexInputStream in = new ByteArrayIndexInputStream(bytes);
        in.skip(1);
        int status = in.readInt(1);
        if (status == 3) {
            this.checkPacket(channel.readDataContent());
        } else if (status == 4) {
            this.doPerformFullAuthentication();
        }
    }

    /**
     * 这里是密码没认证成功，所以要走全流程认证 <br>
     * <h3>情况1</h3>
     * 如果当前socket已经是走了ssl，就不需要去服务器请求公钥
     * <h3>情况2</h3>
     * 如果当前socket没走ssl，那么就要去mysql请求一个公钥回来，所以要发一个请求报文<a href="https://dev.mysql.com/doc/dev/mysql-server/latest/sha2__password_8cc.html#aee9b36790cc292a2e60f4e5972febd25">request_public_key</a>（是个常量，值为2 找文档找死我） <br>
     * <p>
     * 然后要发送密码过去
     * <h3>情况一</h3>
     * 如果已经建立了ssl，那么可以直接发送raw password过去
     * <h3>情况2</h3>
     * 如果没有建立ssl，则要发送requet_public_key包，获取服务器公钥，然后用scramble做xor，防止重放攻击
     * <p>
     * 相关参考文档：<a href="https://blog.csdn.net/n88Lpo/article/details/128245779">csdn参考</a>
     * <a href="https://dev.mysql.com/doc/refman/8.0/en/caching-sha2-pluggable-authentication.html">mysql官方文档</a> <br>
     * <p>
     * For clients that use the caching_sha2_password plugin, passwords are never exposed as cleartext when connecting to the server. How password transmission occurs depends on whether a secure connection or RSA encryption is used:
     * <li>If the connection is secure, an RSA key pair is unnecessary and is not used. This applies to TCP connections encrypted using TLS, as well as Unix socket-file and shared-memory connections. The password is sent as cleartext but cannot be snooped because the connection is secure. <p>如果连接是安全的，可以不使用 RSA 密钥。适用于使用 TLS 加密的 TCP 连接，以及 Unix 套接字文件和共享内存连接。密码以明文格式发送，但不能被窃听，因为连接是安全的。</p></li>
     * <li>If the connection is not secure, an RSA key pair is used. This applies to TCP connections not encrypted using TLS and named-pipe connections. RSA is used only for password exchange between client and server, to prevent password snooping. When the server receives the encrypted password, it decrypts it. A scramble is used in the encryption to prevent repeat attacks.<p>如果连接不是安全的，可以使用 RSA 密钥对。适用于未使用 TLS 加密的 TCP 连接和 named-pipe 连接。RSA 仅用于客户端和服务器之间的密码交换，防止密码被截取。当服务器接收到使用公钥加密的密码后，它使用私钥解密。一个随机字符串用在加密中，防止重放攻击（repeat attacks）。</p></li>
     *
     * @throws IOException
     */
    private void doPerformFullAuthentication() throws IOException {
        ByteArrayIndexOutputStream out = new ByteArrayIndexOutputStream();
        if (channel.isSslSocket()) {
            // ssl通道
            // 为什么直接传原始密码 我没找到对应的文档说明
            out.writeNullTerminatedString(this.password);
            byte[] bytes = channel.readDataContent();
            this.checkPacket(bytes);
        } else {
            // 需要获取公钥并且加密
            channel.sendCommand(new RequestPublicKeyCommand()); // 发送request_public_key报文
            byte[] bytes = channel.readDataContent();
            RSAPublicKey puk = null;
            try {

                String pukStr = new String(bytes);
                pukStr = pukStr.replace("\u0001-----BEGIN PUBLIC KEY-----", "");
                pukStr = pukStr.replace("-----END PUBLIC KEY-----", "");
                pukStr = pukStr.replace("\n", "");

                X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.decode(pukStr.getBytes()));
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                puk = (RSAPublicKey) keyFactory.generatePublic(spec);

                /**
                 * mysql官方文档没有介绍实际的对接细节只能通过mysql驱动源码去参考
                 * {@link com.mysql.cj.protocol.a.authentication.Sha256PasswordPlugin#encryptPassword}
                 */
                Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
                cipher.init(Cipher.ENCRYPT_MODE, puk);

                ByteArrayIndexOutputStream passBuffer = new ByteArrayIndexOutputStream();
                passBuffer.writeNullTerminatedString(password);

                byte[] xorBuffer = CommonUtil.xor(passBuffer.toByteArray(), this.handshakeProtocol.getScramble().getBytes());
                out.write(cipher.doFinal(xorBuffer));
                channel.sendCommand(out::toByteArray);
                this.checkPacket(channel.readDataContent());
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException |
                     IllegalBlockSizeException | BadPaddingException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private static class NativePasswordPlugin {

        /**
         * @throws IOException
         * @描述 使用sha1做hash
         * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_authentication_methods_native_password_authentication.html">相关文档</a>
         */
        private static byte[] genarateNativeAuthPassword(String password, String scrambleStr) {
            if (StrUtil.isBlank(password)) {
                return new byte[0];
            }
            MessageDigest sha;
            try {
                sha = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            // 公式 SHA1( password ) XOR SHA1( "20-bytes random data from server" <concat> SHA1( SHA1( password ) ) )
            // 1、先做对password做两次sha1
            // 2、然后完整挑战字节与做完两次sha1的password合并，然后做一次sha1
            // 3、 然后做完一次sha1的password 与第2步异或

            // 原生密码
            byte[] raw_password = password.getBytes();
            // 完整的挑战字节
            byte[] scramble = scrambleStr.getBytes();
            // 做过一次sha1的密码
            byte[] password_sha1_1 = sha.digest(raw_password);
            // 做过两次sha1的密码
            byte[] password_sha1_2 = sha.digest(password_sha1_1);
            // 完整挑战字节 与 做完两次sha1的password 合并
            byte[] contractBytes = CommonUtil.contract(scramble, password_sha1_2);
            // 获取结果
            return CommonUtil.xor(password_sha1_1, sha.digest(contractBytes));
        }
    }

    private static class CacheSha2PasswordPlugin {

        /**
         * 生成密码
         *
         * @throws IOException
         * @描述 使用sha2做hash
         * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_caching_sha2_authentication_exchanges.html">相关文档</a>
         */
        private static byte[] genarateCachingSha2Password(String password, String scrambleStr) throws IOException {
            if (StrUtil.isBlank(password)) {
                return new byte[0];
            }

            MessageDigest sha;
            try {
                sha = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            // 公式 XOR(SHA256(password), SHA256(SHA256(SHA256(password)), Nonce))
            // 完整的挑战字节
            byte[] scramble = scrambleStr.getBytes();
            // 原生密码
            byte[] raw_password = password.getBytes();
            // 做过一次sha2的密码
            byte[] password_sha2_1 = sha.digest(raw_password);
            byte[] password_sha2_3 = sha.digest(sha.digest(password_sha2_1));
            // 获取结果
            return CommonUtil.xor(password_sha2_1, CommonUtil.xor(password_sha2_3, sha.digest(scramble)));
        }


    }
}
