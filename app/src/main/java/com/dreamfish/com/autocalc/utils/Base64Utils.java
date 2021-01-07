package com.dreamfish.com.autocalc.utils;

import java.util.Base64;

/**
 *  Base64是网络上最常见的用于传输8Bit字节代码的编码方式之一。
 *
 * @author zhenye 2018/8/22
 */
public class Base64Utils {

    /**
     * Base64编码
     * @param message 待Base64编码的字符串
     * @return 编码后的字符串
     */
    public static String encode(String message){
        if (message == null){
            return null;
        }
        byte[] bytes = message.getBytes();
        byte[] result = Base64.getEncoder().encode(bytes);
        return new String(result);
    }

    /**
     * Base64编码
     * @param bytes 待Base64编码的数据
     * @return 编码后的字符串
     */
    public static byte[] encode(byte[] bytes){
        return Base64.getEncoder().encode(bytes);
    }

    /**
     * Base64解码
     * @param message 待Base64解码的字符串
     * @return 解码后的数据
     */
    public static String decode(String message){
        if (message == null){
            return null;
        }
        byte[] bytes = message.getBytes();
        byte[] result = Base64.getDecoder().decode(bytes);
        return new String(result);
    }

    /**
     * Base64解码
     * @param bytes 待Base64解码的数据
     * @return 解码后的数据
     */
    public static byte[] decode(byte[] bytes){
        return Base64.getDecoder().decode(bytes);
    }
}
