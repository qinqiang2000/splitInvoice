package com.szhtxx.etcloud.smser.utils;

public class StringUtilsEx extends org.apache.commons.lang3.StringUtils
{
    public static boolean equalIgnoreNull(String s1, String s2) {
        if (s1 == null) {
            s1 = "";
        }
        if (s2 == null) {
            s2 = "";
        }
        return s1.equals(s2);
    }
    
    public static String getInvSN() {
        return BeanId.generateUUId();
    }
}
