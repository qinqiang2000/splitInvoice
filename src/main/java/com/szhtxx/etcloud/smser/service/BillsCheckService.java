package com.szhtxx.etcloud.smser.service;

import com.szhtxx.etcloud.smser.methods.smser.*;
import com.szhtxx.etcloud.smser.methods.smser.billcheck.*;
import com.szhtxx.etcloud.smser.dto.*;
import org.apache.commons.lang3.*;
import com.szhtxx.etcloud.smser.exception.*;
import java.math.*;
import com.szhtxx.etcloud.smser.constant.*;
import org.apache.commons.collections.*;
import java.util.*;

public class BillsCheckService
{
    static BackCalcUtilMethods backCalcUtilMethods;
    static BillCheckMethods billCheckMethods;
    
    static {
        BillsCheckService.backCalcUtilMethods = new BackCalcUtilMethods();
        BillsCheckService.billCheckMethods = new BillCheckMethods();
    }
    
    public static void billsCheck(final BillSubjectDto billSubjectDto, final SmruleConfigDto configDto) {
        final List<BillDetailDto> billDetailDtoList = billSubjectDto.getBillDList();
        final Integer invKind = billSubjectDto.getInvKind();
        for (int i = 0; i < billDetailDtoList.size(); ++i) {
            final BillDetailDto detailDto = billDetailDtoList.get(i);
            final String billNo = detailDto.getBillNO();
            final Integer lineProperty = detailDto.getLineProperty();
            final String billDetailNO = detailDto.getBillDetailNO();
            final BigDecimal taxRate = detailDto.getTaxRate();
            final BigDecimal amount = detailDto.getAmounts();
            final BigDecimal price = detailDto.getPrice();
            final BigDecimal dec = detailDto.getTaxDeduction();
            if (StringUtils.isEmpty((CharSequence)billNo)) {
                throw new EtcRuleException("单据编号不能为空！");
            }
            if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
                throw new EtcRuleException("单价不允许小于0");
            }
            if (taxRate == null) {
                throw new EtcRuleException("税率不能为空");
            }
            if (invKind == null) {
                throw new EtcRuleException("发票类型不能为空");
            }
            BillsCheckService.billCheckMethods.checkNull(billDetailNO, detailDto, i + 1, "单据明细编号未传入");
            BillsCheckService.billCheckMethods.specilNo0TaxRate(taxRate, invKind, detailDto, i + 1, "税率为0不允许开专票");
            BillsCheckService.billCheckMethods.verfTaxRateNew(taxRate, detailDto, i + 1, "税率不合法");
            BillsCheckService.billCheckMethods.taxRate15NotMultiTax(billDetailDtoList, taxRate, detailDto, i + 1, "减免1.5计税时不允许开具多税率");
            BillsCheckService.billCheckMethods.decNottaxRate15(dec, taxRate, detailDto, i + 1, "差额发票不能开具1.5税率");
            BillsCheckService.billCheckMethods.disLineMoneyNotGt0(amount, lineProperty, detailDto, i + 1, "折扣行金额不允许大于0");
            BillsCheckService.billCheckMethods.discount1LineNotExist(billDetailDtoList, detailDto, i + 1);
            BillsCheckService.billCheckMethods.discountNLineNotExist(billDetailDtoList, detailDto, i + 1);
            BillsCheckService.billCheckMethods.checkDisRows(detailDto, i + 1, "折扣行数必须大于等于-1");
            BillsCheckService.billCheckMethods.checkNDline(detailDto, configDto, i + 1);
            checkPriceNumsAmt(detailDto, configDto);
        }
        BillsCheckService.billCheckMethods.checkBillTotal(billSubjectDto);
    }
    
    public static void checkPriceNumsAmt(final BillDetailDto detailDto, final SmruleConfigDto configDto) {
        BigDecimal amount = detailDto.getAmounts();
        amount = ((amount == null) ? BigDecimal.ZERO : amount);
        BigDecimal price = detailDto.getPrice();
        price = ((price == null) ? BigDecimal.ZERO : price);
        BigDecimal amountInc = detailDto.getAmountsIncTax();
        BigDecimal priceInc = detailDto.getPriceIncTax();
        BigDecimal amts = detailDto.getAmts();
        amts = ((amts == null) ? BigDecimal.ZERO : amts);
        BigDecimal taxAmt = detailDto.getTaxAmt();
        taxAmt = ((taxAmt == null) ? BigDecimal.ZERO : taxAmt);
        final BigDecimal dec = detailDto.getTaxDeduction();
        final BigDecimal taxRate = detailDto.getTaxRate();
        if (amount.compareTo(BigDecimal.ZERO) == 0 && price.compareTo(BigDecimal.ZERO) > 0 && amts.compareTo(BigDecimal.ZERO) > 0) {
            amount = BillsCheckService.backCalcUtilMethods.recursionAmounts(amts, price, 2, configDto);
        }
        else if (price.compareTo(BigDecimal.ZERO) == 0 && amount.compareTo(BigDecimal.ZERO) > 0 && amts.compareTo(BigDecimal.ZERO) > 0) {
            price = BillsCheckService.backCalcUtilMethods.recursionPrice(amount, amts, 2, configDto);
        }
        else if (amts.compareTo(BigDecimal.ZERO) == 0 && amount.compareTo(BigDecimal.ZERO) > 0 && price.compareTo(BigDecimal.ZERO) > 0) {
            amts = BillsCheckService.backCalcUtilMethods.recursionAmtsCut(amount, price, 2, configDto);
        }
        final Integer isIncludeTax = detailDto.getIncludeTax();
        if (SmruleConfConstant.ONE.equals(isIncludeTax)) {
            amountInc = amount;
            priceInc = price;
            BigDecimal taxAmt2 = taxAmt;
            if (1 == configDto.getTotalTaxamtCount() || taxAmt.compareTo(BigDecimal.ZERO) == 0) {
                taxAmt = BillsCheckService.backCalcUtilMethods.calcTaxAmtByTaxMoneyDec(amountInc, dec, taxRate, 2);
                taxAmt2 = BillsCheckService.backCalcUtilMethods.calcTaxAmtByTaxMoneyDec(amountInc, dec, taxRate, 15);
            }
            amount = amountInc.subtract(taxAmt2);
            price = BillsCheckService.backCalcUtilMethods.recursionPrice(amount, amts, 2, configDto);
            amount = amountInc.subtract(taxAmt);
        }
        else {
            BigDecimal taxAmt2 = taxAmt;
            if (1 == configDto.getTotalTaxamtCount() || taxAmt.compareTo(BigDecimal.ZERO) == 0) {
                taxAmt = BillsCheckService.backCalcUtilMethods.calcTaxAmtByNoTaxMoneyDec(amount, dec, taxRate, 2);
                taxAmt2 = BillsCheckService.backCalcUtilMethods.calcTaxAmtByNoTaxMoneyDec(amount, dec, taxRate, 15);
            }
            amountInc = taxAmt2.add(amount);
            priceInc = BillsCheckService.backCalcUtilMethods.recursionPrice(amountInc, amts, 2, configDto);
            amountInc = taxAmt.add(amount);
        }
        detailDto.setAmounts(amount);
        detailDto.setAmountsIncTax(amountInc);
        detailDto.setPrice(price);
        detailDto.setTaxAmt(taxAmt);
        detailDto.setPriceIncTax(priceInc);
        detailDto.setAmts(amts);
    }
    
    public static int findObjIndexInList(final List<BillDetailDto> billDList, final BillDetailDto detailDto) {
        String msg = "";
        final String billNoP = detailDto.getBillNO();
        final String billDetailNoP = detailDto.getBillDetailNO();
        if (StringUtils.isEmpty((CharSequence)billDetailNoP)) {
            msg = String.format("单据编号[%s] 单据明细编号不能为空", billNoP);
            throw new EtcRuleException(msg);
        }
        if (CollectionUtils.isNotEmpty((Collection)billDList)) {
            int i = 0;
            for (final BillDetailDto dto : billDList) {
                final String billNo = dto.getBillNO();
                final String billDetailNo = dto.getBillDetailNO();
                if (billNo.equals(billNoP) && billDetailNo.equals(billDetailNoP)) {
                    break;
                }
                ++i;
            }
            return i;
        }
        return -1;
    }
}
