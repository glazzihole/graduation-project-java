package com.hugailei.graduation.corpus.util;

import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;

/**
 *  @author pengpeng
 *  @date 2018/2/28 下午5:29
 *  @desc
 */
@Slf4j
public class Md5EncryptUtil {

    /**
     * md5 加密
     * @param source
     * @return
     */
    public static String md5Encrypt(String source){
        String result = "";
        try{
            // 得到一个信息摘要器
            MessageDigest digest = MessageDigest.getInstance("md5");
            byte[] md5Byte = digest.digest(source.getBytes());
            StringBuffer buffer = new StringBuffer();
            // 把每一个byte 做一个与运算 0xff;
            for(byte b : md5Byte){
                //与运算，加盐
                int number = b & 0xff;
                String str = Integer.toHexString(number);
                if(str.length() == 1){
                    buffer.append("0");
                }
                buffer.append(str);
            }

            //标准的md5加密后的结果
            result = buffer.toString();
        }catch (Exception e){
            return null;
        }
        return result;
    }

    public static void main(String[] args){
        String source = "abc";
        String result = md5Encrypt(source);
        log.info("加密后的结果：" + result);
    }
}
