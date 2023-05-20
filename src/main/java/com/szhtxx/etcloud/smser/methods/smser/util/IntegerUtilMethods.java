package com.szhtxx.etcloud.smser.methods.smser.util;

public class IntegerUtilMethods
{
    public Integer add(final Integer m, final Integer n) {
        return m + n;
    }
    
    public Integer subtraction(final Integer m, final Integer n) {
        return m - n;
    }
    
    public Integer multiplication(final Integer m, final Integer n) {
        return m * n;
    }
    
    public Integer delete(final Integer m, final Integer n) {
        return m / n;
    }
    
    public Integer division(final Integer m, final Integer n) {
        return m.compareTo(n);
    }
    
    public int division(final String m, final String n) {
        final Integer x = new Integer(m);
        final Integer y = new Integer(n);
        return x.compareTo(y);
    }
}
