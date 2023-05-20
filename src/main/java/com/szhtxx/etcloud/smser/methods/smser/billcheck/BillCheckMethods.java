package com.szhtxx.etcloud.smser.methods.smser.billcheck;

import com.szhtxx.etcloud.smser.methods.smser.*;
import com.szhtxx.etcloud.smser.exception.*;
import org.apache.commons.lang3.*;
import java.math.*;
import com.szhtxx.etcloud.smser.enums.*;
import org.apache.commons.collections.*;
import com.szhtxx.etcloud.smser.utils.*;
import com.szhtxx.etcloud.smser.constant.*;
import com.szhtxx.etcloud.smser.dto.*;
import java.util.*;

public class BillCheckMethods
{
    private static BackCalcUtilMethods calcUtilMethods;
    
    static {
        BillCheckMethods.calcUtilMethods = new BackCalcUtilMethods();
    }
    
    public void checkNull(final String fieldValue, final BillDetailDto detailDto, final int index, final String errorMsg) {
        final String billNo = detailDto.getBillNO();
        final String billDetailNO = detailDto.getBillDetailNO();
        if (fieldValue != null && fieldValue.trim().length() == 0) {
            final String msg = String.format("单据号：%s 第%s行单据明细编号为:%s %s", billNo, index, billDetailNO, errorMsg);
            throw new EtcRuleException(msg);
        }
        if (StringUtils.isEmpty((CharSequence)fieldValue)) {
            final String msg = String.format("单据号：%s 第%s行单据明细编号为:%s %s", billNo, index, billDetailNO, errorMsg);
            throw new EtcRuleException(msg);
        }
    }
    
    public void checkMoneyFiled(final BigDecimal money, final BillDetailDto detailDto, final int index, final String errMsg) {
        final BigDecimal zeroBg = BigDecimal.ZERO;
        final String billNo = detailDto.getBillNO();
        final String billDetailNO = detailDto.getBillDetailNO();
        if (money == null) {
            throw new EtcRuleException("金额字段不能为空");
        }
        if (money.compareTo(zeroBg) < 0) {
            final String msg = String.format("单据号：%s 第%s行单据明细编号为:%s 单价[%s] %s", billNo, index, billDetailNO, detailDto.getPrice(), errMsg);
            throw new EtcRuleException(msg);
        }
    }
    
    public void specilNo0TaxRate(final BigDecimal taxRate, final Integer invKind, final BillDetailDto detailDto, final int index, final String errMsg) {
        final BigDecimal zeroBg = BigDecimal.ZERO;
        final String billNo = detailDto.getBillNO();
        final String billDetailNO = detailDto.getBillDetailNO();
        if (taxRate == null) {
            throw new EtcRuleException("税率不能为空");
        }
        if (invKind == null) {
            throw new EtcRuleException("发票类型不能为空");
        }
        if (taxRate.compareTo(zeroBg) == 0 && invKind.compareTo(EnumType.InvKindEnum.SPECIAL.getValue()) == 0) {
            final String msg = String.format("单据号：%s 第%s行单据明细编号为:%s 税率[%s] %s", billNo, index, billDetailNO, taxRate, errMsg);
            throw new EtcRuleException(msg);
        }
    }
    
    public void verfTaxRateNew(final BigDecimal taxRate, final BillDetailDto detailDto, final int index, final String errMsg) {
        final String billNo = detailDto.getBillNO();
        final String billDetailNO = detailDto.getBillDetailNO();
        if (taxRate.compareTo(BigDecimal.ZERO) < 0 || taxRate.compareTo(BigDecimal.ONE) > 0) {
            final String msg = String.format("单据号：%s 第%s行单据明细编号为:%s 税率[%s] %s", billNo, index, billDetailNO, taxRate, errMsg);
            throw new EtcRuleException(msg);
        }
        final int place = taxRate.scale();
        if (place > 3) {
            final String msg2 = String.format("单据号：%s 第%s行单据明细编号为:%s 税率[%s] %s", billNo, index, billDetailNO, taxRate, errMsg);
            throw new EtcRuleException(msg2);
        }
    }
    
    public void taxRate15NotMultiTax(final List<BillDetailDto> billDList, final BigDecimal taxRate, final BillDetailDto detailDto, final int index, final String errMsg) {
        final BigDecimal zeroBg = BigDecimal.ZERO;
        final String billNo = detailDto.getBillNO();
        final String billDetailNO = detailDto.getBillDetailNO();
        if (CollectionUtils.isEmpty((Collection)billDList)) {
            throw new EtcRuleException("集合不能为空");
        }
        final Set set = new HashSet(10);
        for (final BillDetailDto dto : billDList) {
            set.add(dto.getTaxRate());
        }
        if (set.size() > 1 && taxRate.compareTo(new BigDecimal("0.015")) == 0) {
            final String msg = String.format("单据号：%s 第%s行单据明细编号为:%s %s", billNo, index, billDetailNO, errMsg);
            throw new EtcRuleException(msg);
        }
    }
    
    public void decNottaxRate15(final BigDecimal dec, final BigDecimal taxRate, final BillDetailDto detailDto, final int index, final String errMsg) {
        final BigDecimal zeroBg = BigDecimal.ZERO;
        final String billNo = detailDto.getBillNO();
        final String billDetailNO = detailDto.getBillDetailNO();
        if (dec != null && dec.compareTo(zeroBg) != 0 && taxRate.compareTo(new BigDecimal("0.015")) == 0) {
            final String msg = String.format("单据号：%s 第%s行单据明细编号为:%s %s", billNo, index, billDetailNO, errMsg);
            throw new EtcRuleException(msg);
        }
    }
    
    public void disLineMoneyNotGt0(final BigDecimal amounts, final Integer lineProperty, final BillDetailDto detailDto, final int index, final String errMsg) {
        if (ComUtil.isDisLine(detailDto)) {
            final String billNo = detailDto.getBillNO();
            final String billDetailNO = detailDto.getBillDetailNO();
            final BigDecimal zeroBg = BigDecimal.ZERO;
            if (lineProperty.compareTo(detailDto.getLineProperty()) == 0 && amounts.compareTo(zeroBg) == 1) {
                final String msg = String.format("单据号：%s 第%s行单据明细编号为:%s %s", billNo, index, billDetailNO, errMsg);
                throw new EtcRuleException(msg);
            }
        }
    }
    
    public void checkDisRows(final BillDetailDto detailDto, final int index, final String errMsg) {
        final BigDecimal zeroBg = BigDecimal.ZERO;
        final String billNo = detailDto.getBillNO();
        final String billDetailNO = detailDto.getBillDetailNO();
        final Integer lineProperty = detailDto.getLineProperty();
        final Integer disRows = detailDto.getDisRows();
        if (EnumType.LinePropertyEnum.FOUR.getValue().compareTo(lineProperty) == 0 && disRows < -1) {
            final String msg = String.format("单据号：%s 第%s行单据明细编号为:%s 折扣行数[%s] %s", billNo, index, billDetailNO, disRows, errMsg);
            throw new EtcRuleException(msg);
        }
    }
    
    public void discount1LineNotExist(final List<BillDetailDto> billDList, final BillDetailDto detailDto, final int index) {
        final BigDecimal zeroBg = BigDecimal.ZERO;
        final String billNo = detailDto.getBillNO();
        final String billDetailNO = detailDto.getBillDetailNO();
        final Integer lineProperty = detailDto.getLineProperty();
        final Integer disRows = detailDto.getDisRows();
        if (EnumType.LinePropertyEnum.FOUR.getValue().compareTo(lineProperty) == 0 && (disRows == 0 || disRows == 1)) {
            final int j = ComUtil.findObjIndexInList(billDList, detailDto);
            if (j - 1 < 0) {
                final String msg = String.format("单据号：%s 第%s行单据明细编号为:%s %s", billNo, index, billDetailNO, "被折扣行不存在");
                throw new EtcRuleException(msg);
            }
            final BillDetailDto lastBillDeailDto = billDList.get(j - 1);
            if (lastBillDeailDto.getAmounts().compareTo(zeroBg) == -1) {
                final String msg2 = String.format("单据号：%s 第%s行单据明细编号为:%s %s", billNo, index, billDetailNO, "被折扣行金额非正数行");
                throw new EtcRuleException(msg2);
            }
            final BigDecimal amounts = SmruleConfConstant.ONE.equals(lastBillDeailDto.getIncludeTax()) ? lastBillDeailDto.getAmountsIncTax() : lastBillDeailDto.getAmounts();
            final BigDecimal disMoney = detailDto.getAmounts();
            if (amounts.compareTo(disMoney.abs()) == -1) {
                final String msg3 = String.format("单据号：%s 第%s行单据明细编号为:%s 折扣行金额[%s]大于被折金额[%s]", billNo, index, billDetailNO, disMoney, amounts);
                throw new EtcRuleException(msg3);
            }
        }
    }
    
    public void discountNLineNotExist(final List<BillDetailDto> billDList, final BillDetailDto detailDto, final int index) {
        final BigDecimal zeroBg = BigDecimal.ZERO;
        final String billNo = detailDto.getBillNO();
        final String billDetailNO = detailDto.getBillDetailNO();
        final Integer lineProperty = detailDto.getLineProperty();
        final Integer disRows = detailDto.getDisRows();
        if (EnumType.LinePropertyEnum.FOUR.getValue().compareTo(lineProperty) == 0 && disRows > 1) {
            final int j = ComUtil.findObjIndexInList(billDList, detailDto);
            int positiveLine = 0;
            final BigDecimal disMoney = detailDto.getAmounts();
            BigDecimal sumMoney = BigDecimal.ZERO;
            for (int i = j - 1; i >= 0; --i) {
                final BillDetailDto billDetailDto = billDList.get(i);
                final BigDecimal amounts = SmruleConfConstant.ONE.equals(billDetailDto.getIncludeTax()) ? billDetailDto.getAmountsIncTax() : billDetailDto.getAmounts();
                if (billDetailDto.getAmounts().compareTo(zeroBg) == 1) {
                    ++positiveLine;
                    sumMoney = sumMoney.add(amounts);
                }
                if (positiveLine == disRows) {
                    break;
                }
            }
            if (positiveLine < disRows) {
                final String msg = String.format("单据号：%s 第%s行单据明细编号为:%s %s", billNo, index, billDetailNO, "折扣行找不到足够的折扣行数");
                throw new EtcRuleException(msg);
            }
            if (sumMoney.compareTo(disMoney.abs()) == -1) {
                final String msg = String.format("单据号：%s 第%s行单据明细编号为:%s 折扣行金额[%s]大于被折金额[%s]", billNo, index, billDetailNO, disMoney, sumMoney);
                throw new EtcRuleException(msg);
            }
        }
    }
    
    public void checkNDline(final BillDetailDto detailDto, final SmruleConfigDto configDto, final int index) {
        final BigDecimal zeroBd = BigDecimal.ZERO;
        final BigDecimal amounts = detailDto.getAmounts();
        final BigDecimal amts = detailDto.getAmts();
        final BigDecimal price = detailDto.getPrice();
        final String billNo = detailDto.getBillNO();
        final String billDetailNO = detailDto.getBillDetailNO();
        final Integer lineProperty = detailDto.getLineProperty();
        if (EnumType.LinePropertyEnum.FOUR.getValue().compareTo(lineProperty) != 0) {
            if (amounts.compareTo(zeroBd) == 0 && amts.compareTo(zeroBd) == 0 && price.compareTo(zeroBd) == 0) {
                final String msg = String.format("单据号：%s 第%s行单据明细编号为:%s %s", billNo, index, billDetailNO, "普通商品行或被折扣行，数量、单价、金额不能同时为0");
                throw new EtcRuleException(msg);
            }
            if (amounts != null && amounts.compareTo(zeroBd) == 0 && amts != null && amts.compareTo(zeroBd) == 0 && price != null && price.compareTo(zeroBd) > 0) {
                final String msg = String.format("单据号：%s 第%s行单据明细编号为:%s %s", billNo, index, billDetailNO, "金额，数量未传入");
                throw new EtcRuleException(msg);
            }
            if (amounts != null && amounts.compareTo(zeroBd) == 0 && amts != null && amts.compareTo(zeroBd) > 0 && price != null && price.compareTo(zeroBd) == 0) {
                final String msg = String.format("单据号：%s 第%s行单据明细编号为:%s %s", billNo, index, billDetailNO, "金额，单价未传入");
                throw new EtcRuleException(msg);
            }
            if (amounts != null && amounts.compareTo(zeroBd) > 0 && amts != null && amts.compareTo(zeroBd) > 0 && price != null && price.compareTo(zeroBd) > 0) {
                final BigDecimal temAmouts = amts.multiply(price);
                final BigDecimal err = temAmouts.subtract(amounts).abs();
                if (err.compareTo(configDto.getLineAmountErr()) == 1) {
                    final String msg2 = String.format("单据号：%s 第%s行单据明细编号为:%s 数量(%s)*单价(%s)不等于金额(%s)", billNo, index, billDetailNO, amts, price, amounts);
                    throw new EtcRuleException(msg2);
                }
            }
            final BigDecimal disMoney = detailDto.getDisAmt();
            if (disMoney != null && amounts.compareTo(zeroBd) == -1 && disMoney.compareTo(zeroBd) == -1) {
                final String msg3 = String.format("单据号：%s 第%s行单据明细编号为:%s 负数行不允许有折扣", billNo, index, billDetailNO);
                throw new EtcRuleException(msg3);
            }
            if (disMoney != null && disMoney.abs().compareTo(amounts) == 1 && disMoney.compareTo(zeroBd) == -1) {
                final String msg3 = String.format("单据号：%s 第%s行单据明细编号为:%s 折扣金额大于金额", billNo, index, billDetailNO);
                throw new EtcRuleException(msg3);
            }
        }
    }
    
    public void checkTaxDecline(final List<BillDetailDto> billDList, final BillDetailDto detailDto, final int index) {
        final BigDecimal zeroBd = BigDecimal.ZERO;
        final String billNo = detailDto.getBillNO();
        final BigDecimal amounts = detailDto.getAmounts();
        final String billDetailNO = detailDto.getBillDetailNO();
        final BigDecimal taxDc = detailDto.getTaxDeduction();
        if (taxDc != null && taxDc.compareTo(zeroBd) != 0) {
            if (billDList.size() >= 2 && billDList.get(1).getTaxDeduction().compareTo(zeroBd) != 0) {
                final String msg = String.format("单据号：%s 第%s行单据明细编号为:%s 差额发票清单数不能超过两行,两行时必须有一行为折扣行", billNo, index, billDetailNO);
                throw new EtcRuleException(msg);
            }
            if (billDList.size() == 2) {
                final BillDetailDto billDetailDis = billDList.get(index);
                if (billDetailDis.getLineProperty() != 4 || (billDetailDis.getDisRows() != 0 && billDetailDis.getDisRows() != 1)) {
                    final String msg2 = String.format("单据号：%s 第%s行单据明细编号为:%s 差额发票清单数不能超过两行,两行时必须有一行为折扣行", billNo, index, billDetailNO);
                    throw new EtcRuleException(msg2);
                }
                if (billDetailDis.getAmounts().abs().compareTo(amounts) == 1) {
                    final String msg2 = String.format("单据号：%s 第%s行单据明细编号为:%s 折扣金额大于商品金额", billNo, index, billDetailNO);
                    throw new EtcRuleException(msg2);
                }
            }
        }
    }
    
    public void checkProLineDetail(final BillSubjectDto billSubjectDto, final SmruleConfigDto configDto) {
        final List<BillDetailDto> detailDtos = billSubjectDto.getBillDList();
        final BigDecimal lineAmtErr = configDto.getLineAmountErr();
        final BigDecimal lineTaxAmtErr = configDto.getLineTaxAmtErr();
        for (final BillDetailDto detailDto : detailDtos) {
            final BigDecimal amounts = detailDto.getAmounts();
            BigDecimal dec = detailDto.getTaxDeduction();
            final BigDecimal amountsInc = detailDto.getAmountsIncTax();
            final BigDecimal taxAmt = detailDto.getTaxAmt();
            final BigDecimal amts = detailDto.getAmts();
            final BigDecimal price = detailDto.getPrice();
            final BigDecimal taxRate = detailDto.getTaxRate();
            final BigDecimal priceInc = detailDto.getPriceIncTax();
            final String billNo = detailDto.getBillNO();
            final String detailNo = detailDto.getBillDetailNO();
            if (dec == null) {
                dec = BigDecimal.ZERO;
            }
            final BigDecimal taxErr = amounts.subtract(dec).multiply(taxRate).subtract(taxAmt).abs().setScale(2, 4);
            if (taxErr.compareTo(lineTaxAmtErr) > 0) {
                final String msg = String.format("单据编号[%s] 明细编号[%s] 不含税金额[%s]*税率[%s]税额[%s]超限[%s]", billNo, detailNo, amounts, taxRate, taxAmt, lineTaxAmtErr);
                throw new EtcRuleException(msg);
            }
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            final BigDecimal incAmtErr = amts.multiply(priceInc).subtract(amountsInc).abs().setScale(2, 4);
            if (incAmtErr.compareTo(lineAmtErr) > 0) {
                final String msg2 = String.format("单据编号[%s] 明细编号[%s] 含税单价[%s]*数量[%s]金额[%s]超限[%s]", billNo, detailNo, priceInc, amts, amountsInc, lineAmtErr);
                throw new EtcRuleException(msg2);
            }
            final BigDecimal amtErr = amts.multiply(price).subtract(amounts).abs().setScale(2, 4);
            if (amtErr.compareTo(lineAmtErr) > 0) {
                final String msg3 = String.format("单据编号[%s] 明细编号[%s] 不含税单价[%s]*数量[%s]金额[%s]超限[%s]", billNo, detailNo, price, amts, amounts, lineAmtErr);
                throw new EtcRuleException(msg3);
            }
            final Map<String, Integer> decimalMap = ComUtil.getDigitByRuleConfig(configDto);
            final int amtNumber = decimalMap.get("amtNumber");
            final BigDecimal invLimitAmt = configDto.getInvLimitAmt();
            BigDecimal calAmts = BillCheckMethods.calcUtilMethods.recursionAmts(invLimitAmt, price, amtNumber, configDto);
            calAmts = calAmts.setScale(amtNumber, 4);
            if (invLimitAmt.compareTo(amounts) < 0 && amtNumber == 0 && calAmts.compareTo(BigDecimal.ZERO) == 0) {
                throw new EtcRuleException("不能满足数量保留整数规则拆分单商品行，请调整规则");
            }
        }
    }
    
    public void checkBillTotal(final BillSubjectDto billSubjectDto) {
        final List<BillDetailDto> billDList = billSubjectDto.getBillDList();
        BigDecimal billAmountsSum = BigDecimal.ZERO;
        BigDecimal billTaxAmtsSum = BigDecimal.ZERO;
        for (final BillDetailDto billDetailDto : billDList) {
            billAmountsSum = billAmountsSum.add((billDetailDto.getAmounts() != null) ? billDetailDto.getAmounts() : BigDecimal.ZERO);
            billTaxAmtsSum = billTaxAmtsSum.add((billDetailDto.getTaxAmt() != null) ? billDetailDto.getTaxAmt() : BigDecimal.ZERO);
        }
        final BigDecimal billAmountsIncTaxSum = billAmountsSum.add(billTaxAmtsSum);
        if (billAmountsSum.compareTo(BigDecimal.ZERO) == 0 || billAmountsIncTaxSum.compareTo(BigDecimal.ZERO) == 0) {
            throw new EtcRuleException("销售单总金额或拆合处理后有发票的金额为0，请检查数据或调整拆合规则");
        }
    }
}
