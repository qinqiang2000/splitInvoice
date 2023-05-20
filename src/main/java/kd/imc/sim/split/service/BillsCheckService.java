package kd.imc.sim.split.service;

import kd.imc.sim.common.constant.InvoiceConstant;
import kd.imc.sim.split.dto.BillDealResultDto;
import kd.imc.sim.split.dto.BillDetailDto;
import kd.imc.sim.split.dto.BillSubjectDto;
import kd.imc.sim.split.dto.SmruleConfigDto;
import kd.imc.sim.split.exception.EtcRuleException;
import kd.imc.sim.split.methods.BackCalcUtilMethods;
import kd.imc.sim.split.methods.BillCheckMethods;

import java.math.BigDecimal;
import java.util.List;


public class BillsCheckService {
    static BackCalcUtilMethods backCalcUtilMethods = new BackCalcUtilMethods();
    static BillCheckMethods billCheckMethods = new BillCheckMethods();

    public static void billItemsCheck(BillSubjectDto billSubjectDto, SmruleConfigDto configDto) {
        List<BillDetailDto> billDetailDtoList = billSubjectDto.getBillDList();
        for(int i = 0; i < billDetailDtoList.size(); ++i) {
            BillDetailDto detailDto = billDetailDtoList.get(i);
            Integer lineProperty = detailDto.getLineProperty();
            String billDetailNO = detailDto.getBillDetailNO();
            BigDecimal taxRate = detailDto.getTaxRate();
            BigDecimal amount = detailDto.getAmounts();
            BigDecimal price = detailDto.getPrice();
            BigDecimal dec = detailDto.getTaxDeduction();
            if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
                throw new EtcRuleException("单价不允许小于0");
            }
            if (taxRate == null) {
                throw new EtcRuleException("税率不能为空");
            }
            billCheckMethods.checkNull(billDetailNO, detailDto, i + 1, "单据明细编号未传入");
            billCheckMethods.specilNo0TaxRate(taxRate, billSubjectDto.getInvKind(), detailDto, i + 1, "税率为0不允许开专票");
            billCheckMethods.verfTaxRateNew(taxRate, detailDto, i + 1, "税率不合法");
            billCheckMethods.taxRate15NotMultiTax(billDetailDtoList, taxRate, detailDto, i + 1, "减免1.5计税时不允许开具多税率");
            billCheckMethods.decNottaxRate15(dec, taxRate, detailDto, i + 1, "差额发票不能开具1.5税率");
            billCheckMethods.disLineMoneyNotGt0(amount, lineProperty, detailDto, i + 1, "折扣行金额不允许大于0");
            billCheckMethods.discount1LineNotExist(billDetailDtoList, detailDto, i + 1);
            billCheckMethods.discountNLineNotExist(billDetailDtoList, detailDto, i + 1);
            billCheckMethods.checkDisRows(detailDto, i + 1, "折扣行数必须大于等于-1");
            billCheckMethods.checkNDline(detailDto, configDto, i + 1);
            checkPriceNumsAmt(detailDto, configDto);
        }

        billCheckMethods.checkBillTotal(billSubjectDto);
    }

    /* 
     * 该方法的作用是检查发票明细的单价、数量和金额是否合法，并根据需要进行计算和调整。
     * 该方法接收两个参数：detailDto和configDto，分别表示发票明细和计算规则。该方法没有返回值，而是直接修改了detailDto对象的属性值。
     * 具体实现：首先从detailDto对象中获取单价、数量和金额等属性值，如果这些属性值为null，则将其设置为0。然后根据这些属性值的情况，分别进行计算和调整。
     * 如果数量为0，单价和金额都不为0，则根据计算规则递归计算数量；
     * 如果单价为0，数量和金额都不为0，则根据计算规则递归计算单价；
     * 如果金额为0，数量和单价都不为0，则根据计算规则递归计算金额。
     * 接着根据是否含税的标志位，计算含税金额和不含税金额，并根据需要进行调整。最后将计算结果设置回detailDto对象中的相应属性。
     */
    public static void checkPriceNumsAmt(BillDetailDto detailDto, SmruleConfigDto configDto) {
        BigDecimal amount = detailDto.getAmounts();
        amount = amount == null ? BigDecimal.ZERO : amount;
        BigDecimal price = detailDto.getPrice();
        price = price == null ? BigDecimal.ZERO : price;
        BigDecimal amountInc;
        BigDecimal priceInc;
        BigDecimal amts = detailDto.getAmts();
        amts = amts == null ? BigDecimal.ZERO : amts;
        BigDecimal taxAmt = detailDto.getTaxAmt();
        taxAmt = taxAmt == null ? BigDecimal.ZERO : taxAmt;
        BigDecimal dec = detailDto.getTaxDeduction();
        BigDecimal taxRate = detailDto.getTaxRate();
        if (amount.compareTo(BigDecimal.ZERO) == 0 && price.compareTo(BigDecimal.ZERO) > 0 && amts.compareTo(BigDecimal.ZERO) > 0) {
            amount = backCalcUtilMethods.recursionAmounts(amts, price, 2, configDto);
        } else if (price.compareTo(BigDecimal.ZERO) == 0 && amount.compareTo(BigDecimal.ZERO) > 0 && amts.compareTo(BigDecimal.ZERO) > 0) {
            price = backCalcUtilMethods.recursionPrice(amount, amts, 2, configDto);
        } else if (amts.compareTo(BigDecimal.ZERO) == 0 && amount.compareTo(BigDecimal.ZERO) > 0 && price.compareTo(BigDecimal.ZERO) > 0) {
            amts = backCalcUtilMethods.recursionAmtsCut(amount, price, 2, configDto);
        }

        Integer isIncludeTax = detailDto.getIncludeTax();
        BigDecimal taxAmt15;
        if (InvoiceConstant.IS_TAX_YES_INT.equals(isIncludeTax)) {
            amountInc = amount;
            priceInc = price;
            taxAmt15 = taxAmt;
            if (taxAmt.compareTo(BigDecimal.ZERO) == 0) {
                taxAmt = backCalcUtilMethods.calcTaxAmtByTaxMoneyDec(amount, dec, taxRate, 2);
                taxAmt15 = backCalcUtilMethods.calcTaxAmtByTaxMoneyDec(amount, dec, taxRate, 15);
            }

            amount = amount.subtract(taxAmt15);
            price = backCalcUtilMethods.recursionPrice(amount, amts, 2, configDto);
            amount = amountInc.subtract(taxAmt);
        } else {
            taxAmt15 = taxAmt;
            if (taxAmt.compareTo(BigDecimal.ZERO) == 0) {
                taxAmt = backCalcUtilMethods.calcTaxAmtByNoTaxMoneyDec(amount, dec, taxRate, 2);
                taxAmt15 = backCalcUtilMethods.calcTaxAmtByNoTaxMoneyDec(amount, dec, taxRate, 15);
            }

            amountInc = taxAmt15.add(amount);
            priceInc = backCalcUtilMethods.recursionPrice(amountInc, amts, 2, configDto);
            amountInc = taxAmt.add(amount);
        }

        detailDto.setAmounts(amount);
        detailDto.setAmountsIncTax(amountInc);
        detailDto.setPrice(price);
        detailDto.setTaxAmt(taxAmt);
        detailDto.setPriceIncTax(priceInc);
        detailDto.setAmts(amts);
    }

    public static BillDealResultDto getBDR(String billNO, boolean success, String errorMsg) {
        BillDealResultDto bdr = new BillDealResultDto();
        bdr.setBillNO(billNO);
        bdr.setSuccess(success);
        bdr.setErrorMsg(errorMsg);
        return bdr;
    }
}