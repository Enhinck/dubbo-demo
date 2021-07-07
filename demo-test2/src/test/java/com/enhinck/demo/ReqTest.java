package com.enhinck.demo;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
//import org.apache.commons.lang3.StringUtils;
import org.apache.commons.codec.binary.StringUtils;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

/**
 * <p>write your description here
 *
 * @author xiaomi（huenbin）
 * @since 2021-06-08 20:50
 */
public class ReqTest {


    public static void test() {
        String apiName = "";
        String appVersion = "";
        String clientId = "";
        String postData = "";
        String requestId = "";
        long UnixTimeStamp = System.currentTimeMillis();
        String apiVersion = "3.18.11";

        String willInfo = "a=${apiName}||appVersion=${appVersion}||clientId=${clientId}||et=0.0.2||postData=${md5Hex(postData)}||requestId=${requestId}||time=${UnixTimeStamp}||v=${apiVersion}";


    }

    public static String md5Hex(String data) {
        String hex = "";
        if (org.apache.commons.lang3.StringUtils.isNotBlank(data)) {
            String midden = hex.substring(8, 24);
            String temp = midden.substring(0, 8);
            temp = temp + hex.substring(0, 8);
            temp = temp + hex.substring(24, 32);
            temp = temp + midden.substring(8, 16);
            hex = temp;
        }
        return hex;
    }

    public static String hmacSha256(String message, String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] bytes = sha256_HMAC.doFinal(message.getBytes());
        return Hex.encodeHexString(bytes);
    }

    public static String hmacSha256_2(String message, String secret) {
        String secretKey = "";
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] bytes = sha256_HMAC.doFinal(message.getBytes());
            secretKey = byteArrayToHexString(bytes);
        } catch (Exception e) {
            System.out.println("Error HmacSHA256 ===========" + e.getMessage());
        }
        return secretKey;
    }


    private static String byteArrayToHexString(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; b!=null && n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1)
                hs.append('0');
            hs.append(stmp);
        }
        return hs.toString().toLowerCase();
    }

    public static String sss(String data,String secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Key key = new SecretKeySpec(secretKey.getBytes(), "AES");
        //获取加密算法
        Cipher c = Cipher.getInstance("AES");
        //初始化密钥
        c.init(Cipher.ENCRYPT_MODE, key);
        //生成加密数据
        byte[] encVal = c.doFinal(StringUtils.getBytesUtf8(data));
        //通过Base64加密生成URLSafe的加密数据，此返回值为postData中最终的值
        String encryptedValue = Base64.encodeBase64URLSafeString(encVal);
        return encryptedValue;
    }
}
