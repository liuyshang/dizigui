package com.anl.wxb.dzg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class AESHelper {

//    返回生成指定算法的秘密密钥的 KeyGenerator对象
    private static byte[] getRawKey(byte[] seed) throws Exception {
//        为指定算法生成一个KeyGenerator对象   getInstance(String algorithm , Provider provider)
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
//        返回实现指定随机数生成器RNG算法的SecureRandom对象   getInstance(String algorithm , Provider provider)
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
//        重新设置次随机对象的种子
        sr.setSeed(seed);
        kgen.init(128, sr);
//        生成一个密钥
        SecretKey skey = kgen.generateKey();
        byte[] raw = skey.getEncoded();
        return raw;
    }

//    加密String cleartext , String Seed
    public static String encrypt(String seed, String cleartext) throws Exception {
        byte[] rawKey = getRawKey(seed.getBytes());
        byte[] result = encrypt(rawKey, cleartext.getBytes());
        return toHex(result);
    }

//    解密String encrypted , String seed
    public static String decrypt(String seed, String encrypted) throws Exception {
        byte[] rawKey = getRawKey(seed.getBytes());
        byte[] enc = toByte(encrypted);
        byte[] result = decrypt(rawKey, enc);
        return new String(result);
    }

//    加密byte[] clear
    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
//        SecretKeySpec(byte[] key, int offset, int len, String algorithm)
//        根据给定的字节数组构造一个密钥，使用key中的始于且包含offset的前len个字节
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");

        /*
        * DECRYPT_MODE 用于将Cipher初始化为解密模式的常量
        * ENCRYPT_MODE 用于将Cipher初始化为加密模式的常量
        * PRIVATE_KEY 用于表示要解包的密钥为“私钥”的常量
        * PUBLIC_KEY 用于表示要解包的密钥为“公钥”的常量
        * SECRET_KEY 用于表示要解包的密钥为“秘密密钥”的常量
        * UNWRAP_MODE 用于将Cipher初始化为密钥解包模式的常量
        * WRAP_MODE 用于将Cipher初始化为密钥包装模式的常量
        * */
//        getInstance(String transformation) 返回实现指定转换的Cipher对象
        Cipher cipher = Cipher.getInstance("AES");
//        init(int opmpde, Key key , AlgorithmParameters params)  用密钥和一组算法参数初始化此Cipher
//        IvParameterSpec(byte[] iv) 使用iv中的字节作为IV来构造一个IvParameterSpec对象
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(
                new byte[cipher.getBlockSize()]));
//        doFinal(byte[] input) 按但部分操作加密或解密数据
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    private static byte[] decrypt(byte[] raw, byte[] encrypted)
            throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(
                new byte[cipher.getBlockSize()]));
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

//    转换成16进制
    private static String toHex(String txt) {
        return toHex(txt.getBytes());
    }
//     转换成字节
    private static String fromHex(String hex) {
        return new String(toByte(hex));
    }

    private static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
                    16).byteValue();
        return result;
    }

    private static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }

    private final static String HEX = "0123456789ABCDEF";

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }

}