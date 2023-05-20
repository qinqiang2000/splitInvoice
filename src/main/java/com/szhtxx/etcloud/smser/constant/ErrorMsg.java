package com.szhtxx.etcloud.smser.constant;

public interface ErrorMsg
{
    public static final String MONETY_IS_NULL = "金额字段不能为空";
    public static final String TAXRATE_IS_NULL = "税率不能为空";
    public static final String INVKIND_IS_NULL = "发票类型不能为空";
    public static final String TAXRATE_LIST_NULL = "税率列表不能为空";
    public static final String SET_IS_NULL = "集合不能为空";
    public static final String DISCOUNTLINE_NOTEXIST = "被折扣行不存在";
    public static final String PROLINE_MONEY_LT0 = "被折扣行金额非正数行";
    public static final String DISCOUNTLINE_NOT_ENOUGH_LINES = "折扣行找不到足够的折扣行数";
    public static final String MONEY_NUM_PRICE_ALL0 = "普通商品行或被折扣行，数量、单价、金额不能同时为0";
    public static final String ADJUST_RULE_ERROR = "不能满足数量保留整数规则拆分单商品行，请调整规则";
    public static final String SIG_LINE_AMOUT_RATE_ROOR = "单行金额/限额必须在[0-1000]之内，请调整限额";
    public static final String INVOICE_TAX_AMT_ERROR = "整张发票明细行税额误差之和超过1.27，不允许提交开票，请调整税额数值或调整拆合规则！";
    public static final String SYS_SPLIT_INVOICE_MAX_ERROR = "单据金额超过发票限额的1000倍，请调整单据金额";
    public static final String TAXAMT_NOT_ZERO = "税额不能小于0";
    public static final String BEAN_COPY_ERROR = "对象备份失败";
}
