package com.szhtxx.etcloud.smser.constant;

import java.math.*;

public class SmserConstant
{
    public static final Integer ZERO;
    public static final Integer ONE;
    public static final Integer TWO;
    public static final Integer THREE;
    public static final String SMSER_PACKAGE = "产品/拆分组合组件服务";
    public static final BigDecimal TAXRATE_15;
    public static final int DIGIT_15 = 15;
    public static final String MAX_RATE = "1000";
    public static final String MAX_TAX_AMT = "1.27";
    public static final int SPLIT_SIGN = 1;
    
    static {
        ZERO = 0;
        ONE = 1;
        TWO = 2;
        THREE = 3;
        TAXRATE_15 = new BigDecimal("0.015");
    }
}
