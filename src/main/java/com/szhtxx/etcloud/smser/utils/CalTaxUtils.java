package com.szhtxx.etcloud.smser.utils;

import java.math.*;

public class CalTaxUtils
{
    public static BigDecimal calTax(final int includeTax, final BigDecimal money, final BigDecimal balance, final BigDecimal taxRate, final int scale, final int roundingMode) {
        final boolean includeTaxFlag = includeTax != 0;
        final boolean fifteenThousandFlag = taxRate.compareTo(new BigDecimal("0.015")) == 0;
        if (includeTaxFlag) {
            if (fifteenThousandFlag) {
                return money.multiply(new BigDecimal("0.015")).divide(new BigDecimal("1.05"), scale, roundingMode);
            }
            if (balance != null && balance.compareTo(BigDecimal.ZERO) != 0) {
                return money.subtract(balance).multiply(taxRate).divide(BigDecimal.ONE.add(taxRate), scale, roundingMode);
            }
            return money.multiply(taxRate).divide(BigDecimal.ONE.add(taxRate), scale, roundingMode);
        }
        else {
            if (fifteenThousandFlag) {
                return money.multiply(new BigDecimal("0.015")).divide(new BigDecimal("1.035"), scale, roundingMode);
            }
            if (balance != null && balance.compareTo(BigDecimal.ZERO) != 0) {
                return money.subtract(balance).multiply(taxRate).setScale(scale, roundingMode);
            }
            return money.multiply(taxRate).setScale(scale, roundingMode);
        }
    }
    
    public static BigDecimal calTax(final int includeTax, final BigDecimal money, final BigDecimal taxRate) {
        return calTax(includeTax, money, null, taxRate, 2, 4);
    }
    
    public static BigDecimal calTax(final int includeTax, final BigDecimal money, final BigDecimal taxRate, final int scale) {
        return calTax(includeTax, money, null, taxRate, scale, 4);
    }
}
