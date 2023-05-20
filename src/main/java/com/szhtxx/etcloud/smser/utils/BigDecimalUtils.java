package com.szhtxx.etcloud.smser.utils;

import java.math.*;
import com.szhtxx.etcloud.smser.exception.*;

public class BigDecimalUtils
{
    public static boolean equals(final BigDecimal b1, final BigDecimal b2) {
        return (b1 == null && b2 == null) || (b1 != null && b2 != null && b1.compareTo(b2) == 0);
    }
    
    public static BigDecimal add(BigDecimal b1, BigDecimal b2, final BigDecimal defaultNull) {
        if (b1 == null) {
            if (defaultNull == null) {
                return null;
            }
            b1 = defaultNull;
        }
        if (b2 == null) {
            if (defaultNull == null) {
                return null;
            }
            b2 = defaultNull;
        }
        return b1.add(b2);
    }
    
    public static BigDecimal recursionDivision(final BigDecimal dividend, final BigDecimal divisor, final int digit, final BigDecimal errorValue) {
        final BigDecimal value = dividend.divide(divisor, digit, 4);
        final BigDecimal calcErrorValue = dividend.subtract(divisor.multiply(value)).abs();
        if (calcErrorValue.compareTo(errorValue) > 0) {
            return recursionDivision(dividend, divisor, digit + 1, errorValue);
        }
        return value;
    }
    
    public static BigDecimal recursionDivision(final BigDecimal dividend, final BigDecimal divisor, final int digit, final BigDecimal compareValue, final BigDecimal errorValue) {
        if (digit > 100) {
            throw new EtcRuleException("反算时小数位已超100位");
        }
        final BigDecimal value = dividend.divide(divisor, digit, 4);
        final BigDecimal calcErrorValue = compareValue.subtract(divisor.multiply(value)).abs();
        if (calcErrorValue.compareTo(errorValue) > 0) {
            return recursionDivision(dividend, divisor, digit + 1, compareValue, errorValue);
        }
        return value;
    }
}
