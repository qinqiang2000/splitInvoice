package kd.imc.sim.common.constant;

import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.util.Map;

public class InvoiceConstant {
    //含税标志- 0不含税 1含税
    String IS_TAX_NO = "0";
    String IS_TAX_YES = "1";
    public static Integer IS_TAX_YES_INT = 1;

    // 商品行性质- 0正常行 1折扣行 2被折后行
    String ITME_TYPE_DISCOUNT = "1";

    // 1开红票 0开蓝票
    String ISSUE_TYPE_RED = "1";
    String ISSUE_TYPE_BLUE = "0";

    // 特殊校验值
    public static BigDecimal TAX_015 = new BigDecimal("0.015");
    BigDecimal DIFF_01 = new BigDecimal("0.01");
    BigDecimal DIFFF_06 = new BigDecimal("0.06");
    public static BigDecimal DIFF_127 = new BigDecimal("1.27");

    // 是否享受优惠政策
    String TAXPREMARK_USE = "1";
    String TAXPREMARK_NO_USE = "0";

    // 0 未打印 1 已打印 2 打印失败
    String PRINT_FLAG_YES = "1";

    // 0 蓝票  1红票  2 红字信息表
    String BLUE_INVOICE = "0";

    // 清单标志 1有清单  0无清单
    String IS_INVENTORY_YES = "1";
    String IS_INVENTORY_NO = "0";

    // 折扣行 0 明细行   1 折扣行   2被折扣行
    String ROWTYPE_NOMAR = "0";
    String ROWTYPE_DISCOUNT = "1";
    String ROWTYPE_BE_DISCOUNT = "2";

    Map<String, String> HSBZ_MAP = new ImmutableMap.Builder<String, String>().
        put("含税", IS_TAX_YES).
        put("不含税", IS_TAX_NO).build();
/*
    Map<String, String> BUYER_PROPERTY_MAP = new ImmutableMap.Builder<String, String>().
        put("个人", OriginalBillConstant.BuyerPropertyEnum.PERSONAL).
        put("企业", OriginalBillConstant.BuyerPropertyEnum.ENTERPRISE).build();

    Map<String, String> ROWTYPE_MAP = new ImmutableMap.Builder<String, String>().
        put("整单折扣", OriginalBillConstant.RowTypeEnum.ALL).
        put("折扣行", OriginalBillConstant.RowTypeEnum.DISCOUNT).
        put("普通商品行", OriginalBillConstant.RowTypeEnum.NORMAL).build();

    Map<String, String> TAXPREMARK_MAP = new ImmutableMap.Builder<String, String>().
        put("享受", TAXPREMARK_USE).
        put("不享受", TAXPREMARK_NO_USE).build();

    Map<String, String> TAXEDTYPE_MAP = new ImmutableMap.Builder<String, String>().
        put("差额征税-差额开票", TaxedTypeEnum.all_e_deduction.getValue()).
        put("差额征税-全额开票", TaxedTypeEnum.all_e_deduction_full.getValue()).build();

    Map<String, String> ALL_E_DEDUCTION_MAP = new ImmutableMap.Builder<String, String>().
        put("01 全电发票", AllEDeductionConstant.EvidenceTypeEnum.ALL_E).
        put("02 增值税专用发票", AllEDeductionConstant.EvidenceTypeEnum.VAT_S_INV).
        put("03 增值税普通发票", AllEDeductionConstant.EvidenceTypeEnum.VAT_C_INV).
        put("04 营业税发票", AllEDeductionConstant.EvidenceTypeEnum.BUSINESS_TAX_INV).
        put("05 财政票据", AllEDeductionConstant.EvidenceTypeEnum.FISCAL_BILLS).
        put("06 法院裁决书", AllEDeductionConstant.EvidenceTypeEnum.COURT_RULING).
        put("07 契税完税凭证", AllEDeductionConstant.EvidenceTypeEnum.DEED_TAX_PAYMENT_VOUCHER).
        put("08 其他发票类", AllEDeductionConstant.EvidenceTypeEnum.OTHER_INV).
        put("09 其他扣除凭证", AllEDeductionConstant.EvidenceTypeEnum.OTHER_DEDUCTION_VOUCHERS).build();*/
}
