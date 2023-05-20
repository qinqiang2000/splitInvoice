package com.szhtxx.etcloud.smser.methods.smser.util;

import java.math.*;

public class BigdecimalUtilMethods
{
    public BigDecimal add(final BigDecimal m, final BigDecimal n, final int digit) {
        return m.add(n).setScale(digit, 4);
    }
    
    public BigDecimal subtraction(final BigDecimal m, final BigDecimal n, final int digit) {
        return m.subtract(n).setScale(digit, 4);
    }
    
    public BigDecimal multiplication(final BigDecimal m, final BigDecimal n, final int digit) {
        return m.multiply(n).setScale(digit, 4);
    }
    
    public BigDecimal division(final BigDecimal m, final BigDecimal n, final int digit) {
        return m.divide(n, digit, 4);
    }
    
    public Integer compareTo(final BigDecimal m, final BigDecimal n) {
        return m.compareTo(n);
    }
    
    public Integer compareTo(final String m, final String n) {
        final BigDecimal x = new BigDecimal(m);
        final BigDecimal y = new BigDecimal(n);
        return x.compareTo(y);
    }
}
