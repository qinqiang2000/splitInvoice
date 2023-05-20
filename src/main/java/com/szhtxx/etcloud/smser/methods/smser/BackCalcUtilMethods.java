package com.szhtxx.etcloud.smser.methods.smser;

import java.math.*;
import com.szhtxx.etcloud.smser.utils.*;
import com.szhtxx.etcloud.smser.exception.*;
import com.szhtxx.etcloud.smser.constant.*;
import com.szhtxx.etcloud.smser.dto.*;
import com.szhtxx.etcloud.smser.enums.*;
import java.util.*;

public class BackCalcUtilMethods
{
    public void preLineErrExceedException(final List<BillDetailDto> billDList, final BigDecimal err, final BigDecimal limit, final BillDetailDto detailDto, final String errorMsg) {
        final int j = ComUtil.findObjIndexInList(billDList, detailDto);
        if (err.abs().compareTo(limit) == 1) {
            final String billNo = detailDto.getBillNO();
            final String billDetailNO = detailDto.getBillDetailNO();
            final String msg = String.format("单据号：%s 第%s单据明细编号为:%s 误差[%s] 限额[%s] %s", billNo, j, billDetailNO, err, limit, errorMsg);
            throw new EtcRuleException(msg);
        }
    }
    
    public void preLineDisExceedException(final List<BillDetailDto> billDList, BigDecimal disTaxAmt, BigDecimal taxAmt, final BillDetailDto detailDto) {
        final int j = ComUtil.findObjIndexInList(billDList, detailDto);
        if (disTaxAmt == null) {
            disTaxAmt = BigDecimal.ZERO;
        }
        if (taxAmt == null) {
            taxAmt = BigDecimal.ZERO;
        }
        if (disTaxAmt.abs().compareTo(taxAmt) == 1) {
            final String billNo = detailDto.getBillNO();
            final String billDetailNO = detailDto.getBillDetailNO();
            final String msg = String.format("单据号：%s 第%s行单据明细编号为:%s 折扣税额[%s]大于税额[%s]", billNo, j, billDetailNO, disTaxAmt, taxAmt);
            throw new EtcRuleException(msg);
        }
    }
    
    public BigDecimal recursionAmts(final BigDecimal amounts, final BigDecimal price, int digit, final SmruleConfigDto smruleConfigDto) {
        final BigDecimal lineAmountErr = smruleConfigDto.getLineAmountErr();
        final int maxDigitLimit = smruleConfigDto.getAmtNumber();
        if (digit < maxDigitLimit) {
            digit = maxDigitLimit;
        }
        if (price.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        final BigDecimal amts = amounts.divide(price, digit, 4);
        final BigDecimal calcProLineErr = amounts.subtract(price.multiply(amts)).abs();
        if (calcProLineErr.compareTo(lineAmountErr) > 0) {
            return this.recursionAmts(amounts, price, digit + 1, smruleConfigDto);
        }
        return amts;
    }
    
    public BigDecimal recursionAmtsCut(final BigDecimal amounts, final BigDecimal price, int digit, final SmruleConfigDto smruleConfigDto) {
        final BigDecimal lineAmountErr = smruleConfigDto.getLineAmountErr();
        final int maxDigitLimit = smruleConfigDto.getAmtNumber();
        if (digit < maxDigitLimit) {
            digit = maxDigitLimit;
        }
        if (price.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        final BigDecimal amts = amounts.divide(price, digit, 3);
        final BigDecimal calcProLineErr = amounts.subtract(price.multiply(amts)).abs();
        if (calcProLineErr.compareTo(lineAmountErr) > 0) {
            return this.recursionAmtsCut(amounts, price, digit + 1, smruleConfigDto);
        }
        return amts;
    }
    
    public BigDecimal recursionAmounts(final BigDecimal amts, final BigDecimal price, final int digit, final SmruleConfigDto smruleConfigDto) {
        final BigDecimal lineAmountErr = smruleConfigDto.getLineAmountErr();
        final BigDecimal amounts = amts.multiply(price).setScale(digit, 4);
        final BigDecimal calcProLineErr = amounts.subtract(price.multiply(amts)).abs();
        if (calcProLineErr.compareTo(lineAmountErr) > 0) {
            return this.recursionAmounts(amts, price, digit + 1, smruleConfigDto);
        }
        return amounts;
    }
    
    public BigDecimal recursionPrice(final BigDecimal amounts, final BigDecimal amts, int digit, final SmruleConfigDto smruleConfigDto) {
        final BigDecimal lineAmountErr = smruleConfigDto.getLineAmountErr();
        final int maxLimit = smruleConfigDto.getPriceNumber();
        if (digit < maxLimit) {
            digit = maxLimit;
        }
        if (amts.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        final BigDecimal price = amounts.divide(amts, digit, 4);
        final BigDecimal calcProLineErr = amounts.subtract(price.multiply(amts)).abs();
        if (calcProLineErr.compareTo(lineAmountErr) > 0) {
            return this.recursionPrice(amounts, amts, digit + 1, smruleConfigDto);
        }
        return price;
    }
    
    public BigDecimal calcTaxAmt(final BigDecimal amounts, final BigDecimal taxRate, final int digit) {
        if (SmruleConfConstant.TAXRATE_15.compareTo(taxRate) == 0) {
            final BigDecimal bd105 = new BigDecimal("1.05");
            return amounts.multiply(SmruleConfConstant.TAXRATE_15).divide(bd105, 2, 4);
        }
        final BigDecimal one = new BigDecimal("1");
        final BigDecimal taxAmt = amounts.multiply(taxRate).divide(one.add(taxRate), digit, 4);
        return taxAmt;
    }
    
    public BigDecimal calcNoTaxMoney(final BigDecimal amounts, final BigDecimal taxRate, final int digit) {
        if (SmruleConfConstant.TAXRATE_15.compareTo(taxRate) == 0) {
            final BigDecimal bd105 = new BigDecimal("1.05");
            final BigDecimal taxAmt = amounts.multiply(SmruleConfConstant.TAXRATE_15).divide(bd105, 2, 4);
            return amounts.subtract(taxAmt);
        }
        final BigDecimal one = new BigDecimal("1");
        final BigDecimal taxAmt = amounts.multiply(taxRate).divide(one.add(taxRate), digit, 4);
        return amounts.subtract(taxAmt);
    }
    
    public BigDecimal calcTaxAmtByNoTaxMoney(final BigDecimal amounts, final BigDecimal taxRate, final int digit) {
        if (SmruleConfConstant.TAXRATE_15.compareTo(taxRate) == 0) {
            final BigDecimal bd1035 = new BigDecimal("1.035");
            return amounts.multiply(SmruleConfConstant.TAXRATE_15).divide(bd1035, 2, 4);
        }
        final BigDecimal taxAmt = amounts.multiply(taxRate).setScale(digit, 4);
        return taxAmt;
    }
    
    public BigDecimal calcTaxAmtByTaxMoneyDec(final BigDecimal amounts, BigDecimal dec, final BigDecimal taxRate, final int digit) {
        if (dec == null) {
            dec = BigDecimal.ZERO;
        }
        if (SmruleConfConstant.TAXRATE_15.compareTo(taxRate) == 0) {
            final BigDecimal bd105 = new BigDecimal("1.05");
            return amounts.subtract(dec).multiply(SmruleConfConstant.TAXRATE_15).divide(bd105, digit, 4);
        }
        final BigDecimal tmpRate = BigDecimal.ONE.add(taxRate);
        final BigDecimal taxAmt = amounts.subtract(dec).multiply(taxRate).divide(tmpRate, digit, 4);
        return taxAmt;
    }
    
    public BigDecimal calcAmtByTaxMoneyDec(final BigDecimal amounts, BigDecimal dec, final BigDecimal taxRate, final int digit) {
        if (dec == null) {
            dec = BigDecimal.ZERO;
        }
        if (SmruleConfConstant.TAXRATE_15.compareTo(taxRate) == 0) {
            final BigDecimal bd105 = new BigDecimal("1.05");
            final BigDecimal taxAmt = amounts.subtract(dec).multiply(SmruleConfConstant.TAXRATE_15).divide(bd105, 2, 4);
            return amounts.subtract(taxAmt);
        }
        final BigDecimal tmpRate = BigDecimal.ONE.add(taxRate);
        final BigDecimal taxAmt = amounts.subtract(dec).multiply(taxRate).divide(tmpRate, 2, 4);
        return amounts.subtract(taxAmt);
    }
    
    public BigDecimal calcTaxAmtByNoTaxMoneyDec(final BigDecimal amounts, BigDecimal dec, final BigDecimal taxRate, final int digit) {
        if (dec == null) {
            dec = BigDecimal.ZERO;
        }
        if (SmruleConfConstant.TAXRATE_15.compareTo(taxRate) == 0) {
            final BigDecimal bd1035 = new BigDecimal("1.035");
            return amounts.multiply(SmruleConfConstant.TAXRATE_15).divide(bd1035, 2, 4);
        }
        final BigDecimal taxAmt = amounts.subtract(dec).multiply(taxRate).setScale(digit, 4);
        return taxAmt;
    }
    
    public BigDecimal calcDisTaxAmtByTaxMoney(final BigDecimal disAmt, final BigDecimal taxRate, final int digit) {
        return disAmt.multiply(taxRate).divide(BigDecimal.ONE.add(taxRate), digit, 4);
    }
    
    public BigDecimal calcDisTaxAmtByNoTaxMoney(final BigDecimal disAmt, final BigDecimal taxRate, final int digit) {
        return disAmt.multiply(taxRate).setScale(digit, 4);
    }
    
    public Integer exSingleAmtErr(final BigDecimal price, final BigDecimal amts, final BigDecimal amounts, final SmruleConfigDto configDto) {
        final BigDecimal tmpAmounts = price.multiply(amts);
        final BigDecimal lineAmtErr = configDto.getLineAmountErr();
        if (amounts.subtract(tmpAmounts).abs().compareTo(lineAmtErr) > 0) {
            return 1;
        }
        return 0;
    }
    
    public void keepDecimalPlace(final BillSubjectDto billSubjectDto, final SmruleConfigDto configDto) {
        final List<BillDetailDto> billDetailDtos = billSubjectDto.getBillDList();
        for (final BillDetailDto detailDto : billDetailDtos) {
            final int priceNumberType = configDto.getPriceNumberType();
            int defaultValue = SmruleConfConstant.ONE;
            if (EnumType.NumberTypeEnum.ONE.getValue() == priceNumberType) {
                defaultValue = 0;
            }
            else if (EnumType.NumberTypeEnum.TWO.getValue() == priceNumberType) {
                defaultValue = configDto.getPriceNumber();
            }
            final BigDecimal price = detailDto.getPrice().setScale(defaultValue, 4);
            final BigDecimal priceInc = detailDto.getPriceIncTax().setScale(defaultValue, 4);
            detailDto.setPrice(price);
            detailDto.setPriceIncTax(priceInc);
            final int amtNumberType = configDto.getAmtNumberType();
            if (EnumType.NumberTypeEnum.ONE.getValue() == amtNumberType) {
                defaultValue = 0;
            }
            else if (EnumType.NumberTypeEnum.TWO.getValue() == amtNumberType) {
                defaultValue = configDto.getPriceNumber();
            }
            final BigDecimal amts = detailDto.getAmts().setScale(defaultValue, 4);
            detailDto.setAmts(amts);
        }
    }
}
