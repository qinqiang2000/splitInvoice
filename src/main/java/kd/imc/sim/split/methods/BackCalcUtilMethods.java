package kd.imc.sim.split.methods;

import kd.imc.sim.common.constant.InvoiceConstant;
import kd.imc.sim.split.dto.SmruleConfigDto;

import java.math.BigDecimal;

public class BackCalcUtilMethods {
    public BigDecimal recursionAmtsCut(BigDecimal amounts, BigDecimal price, int digit, SmruleConfigDto smruleConfigDto) {
        BigDecimal lineAmountErr = smruleConfigDto.getLineAmountErr();
        int maxDigitLimit = smruleConfigDto.getAmtNumber();
        if (digit < maxDigitLimit) {
            digit = maxDigitLimit;
        }
        if (price.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal amts = amounts.divide(price, digit, 3);
        BigDecimal calcProLineErr = amounts.subtract(price.multiply(amts)).abs();
        return calcProLineErr.compareTo(lineAmountErr) > 0 ? this.recursionAmtsCut(amounts, price, digit + 1, smruleConfigDto) : amts;
    }

    /*
     * 该函数的作用是通过递归计算出给定价格和数量的总金额
     * 该函数的实现方式是通过将给定的数量和价格相乘来计算总金额，然后使用setScale方法设置精度。
     * 如果计算出的金额与给定的金额之间的差异大于lineAmountErr，则递归调用该函数，并将精度增加到digit + 1。
     * 如果计算出的金额与给定的金额之间的差异小于或等于lineAmountErr，则返回计算出的金额。
     */
    public BigDecimal recursionAmounts(BigDecimal amts, BigDecimal price, int digit, SmruleConfigDto smruleConfigDto) {
        BigDecimal lineAmountErr = smruleConfigDto.getLineAmountErr();
        // qqin: ROUND_HALF_UP == 4
        BigDecimal amounts = amts.multiply(price).setScale(digit, 4);
        BigDecimal calcProLineErr = amounts.subtract(price.multiply(amts)).abs();
        return calcProLineErr.compareTo(lineAmountErr) > 0 ? this.recursionAmounts(amts, price, digit + 1, smruleConfigDto) : amounts;
    }

    public BigDecimal recursionPrice(BigDecimal amounts, BigDecimal amts, int digit, SmruleConfigDto smruleConfigDto) {
        BigDecimal lineAmountErr = smruleConfigDto.getLineAmountErr();
        int maxLimit = smruleConfigDto.getPriceNumber();
        if (digit < maxLimit) {
            digit = maxLimit;
        }

        if (amts.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal price = amounts.divide(amts, digit, 4);
        BigDecimal calcProLineErr = amounts.subtract(price.multiply(amts)).abs();
        return calcProLineErr.compareTo(lineAmountErr) > 0 ? this.recursionPrice(amounts, amts, digit + 1, smruleConfigDto) : price;
    }

    public BigDecimal calcTaxAmt(BigDecimal amounts, BigDecimal taxRate, int digit) {
        if (InvoiceConstant.TAX_015.compareTo(taxRate) == 0) {
            return amounts.multiply(InvoiceConstant.TAX_015).divide(new BigDecimal("1.05"), 2, 4);
        }
        return amounts.multiply(taxRate).divide(BigDecimal.ONE.add(taxRate), digit, 4);
    }

    public BigDecimal calcTaxAmtByTaxMoneyDec(BigDecimal amounts, BigDecimal deduction, BigDecimal taxRate, int digit) {
        if (deduction == null) {
            deduction = BigDecimal.ZERO;
        }
        if (InvoiceConstant.TAX_015.compareTo(taxRate) == 0) {
            return amounts.subtract(deduction).multiply(InvoiceConstant.TAX_015).divide(new BigDecimal("1.05"), digit, 4);
        }
        return amounts.subtract(deduction).multiply(taxRate).divide(BigDecimal.ONE.add(taxRate), digit, 4);
    }

    public BigDecimal calcTaxAmtByNoTaxMoneyDec(BigDecimal amounts, BigDecimal deduction, BigDecimal taxRate, int digit) {
        if (deduction == null) {
            deduction = BigDecimal.ZERO;
        }
        if (InvoiceConstant.TAX_015.compareTo(taxRate) == 0) {
            return amounts.multiply(InvoiceConstant.TAX_015).divide(new BigDecimal("1.035"), 2, 4);
        }
        return amounts.subtract(deduction).multiply(taxRate).setScale(digit, 4);
    }
}