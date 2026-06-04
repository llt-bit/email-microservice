package com.email.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 邮件正文 DES 加解密工具（从 OA EmailDESUtil 迁移）。
 *
 * <p>密钥通过系统属性 {@code internalmail.mailKey} 配置，原 OA 中存储在 {@code pluginProperties.xml}。
 * 剥离后改为 applicaton.yml 中的 {@code internalmail.mailKey} 配置项。</p>
 */

@Component
public class EmailDESUtil {
    private static final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(EmailDESUtil.class);

    private static final String ALGORITHM = "DES";

    private final String mailKey;

    public EmailDESUtil(@Value("${internalmail.mailKey:seeyon@inmail}") String mailKey) {
        this.mailKey = mailKey;
    }

    /**
     * DES 加密，返回 Base64 编码密文。
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) return plainText;
        try {
            SecureRandom random = new SecureRandom();
            DESKeySpec desKey = new DESKeySpec(mailKey.getBytes(StandardCharsets.UTF_8));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
            SecretKey secretKey = keyFactory.generateSecret(desKey);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, random);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.warn("DES 加密失败，返回原文", e);
            return plainText;
        }
    }

    /**
     * DES 解密。
     */
    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) return cipherText;
        try {
            SecureRandom random = new SecureRandom();
            DESKeySpec desKey = new DESKeySpec(mailKey.getBytes(StandardCharsets.UTF_8));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
            SecretKey secretKey = keyFactory.generateSecret(desKey);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, random);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("DES 解密失败，返回原文", e);
            return cipherText;
        }
    }

    /**
     * 兼容原 OA 的静态方法调用 —— InMailUtil.makeInMailSummary 中调用。
     */
    public static String getEncryptString(String plainText) {
        return EmailDESUtilHolder.INSTANCE.encrypt(plainText);
    }

    /**
     * 兼容原 OA 的静态方法 —— 自动判断是否需要解密。
     */
    public static String getDecryptString(String content) {
        return EmailDESUtilHolder.INSTANCE.decrypt(content);
    }

    /** 持有一个静态实例，用默认密钥 */
    private static class EmailDESUtilHolder {
        static final EmailDESUtil INSTANCE = new EmailDESUtil("seeyon@inmail");
    }
}
