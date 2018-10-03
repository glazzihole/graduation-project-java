package com.bfsuolframework.core.utils;

public class ObjectUtils {
	
	
	/**
	 * 判断所获取的对象转换成字符串后是否等于指定字符串
	 * @param obj 要判断的对象
	 * @param str 期望的字符串
	 * @return
	 */
	public static boolean checkToStringEquals(Object obj, String str) {
        if(obj==null) {
            return false;
        }
        else {
            if(obj.toString().equals( str )) {
                return true;
            }
            else {
                return false;
            }
        }
	    
	}
	
	
	/**
	 * 判断所接收到的对象是否均不为空
	 * @param object
	 * @return
	 */
	public static boolean checkAllNotNull(Object... obj) {
	    for (int i = 0; i <  obj.length; i++) { 
	        if(obj[i]==null||StringUtils.isBlank( obj[i].toString() )) {
	            return false;
	        }
	    } 
	    return true;
	}

}
