package com.szhtxx.etcloud.smser.dto;

import java.io.*;
import java.math.*;

public class SameRateTotalDto implements Serializable
{
    private static final long serialVersionUID = 1L;
    private BigDecimal taxRate;
    private BigDecimal amtTotal;
    private BigDecimal amtTotalIncTax;
    private BigDecimal taxAmtTotal;
    private BigDecimal taxDcTotal;
    
    public BigDecimal getTaxRate() {
        return this.taxRate;
    }
    
    public void setTaxRate(final BigDecimal taxRate) {
        this.taxRate = taxRate;
    }
    
    public BigDecimal getAmtTotal() {
        return this.amtTotal;
    }
    
    public void setAmtTotal(final BigDecimal amtTotal) {
        this.amtTotal = amtTotal;
    }
    
    public BigDecimal getTaxAmtTotal() {
        return this.taxAmtTotal;
    }
    
    public void setTaxAmtTotal(final BigDecimal taxAmtTotal) {
        this.taxAmtTotal = taxAmtTotal;
    }
    
    public BigDecimal getTaxDcTotal() {
        return this.taxDcTotal;
    }
    
    public void setTaxDcTotal(final BigDecimal taxDcTotal) {
        this.taxDcTotal = taxDcTotal;
    }
    
    public BigDecimal getAmtTotalIncTax() {
        return this.amtTotalIncTax;
    }
    
    public void setAmtTotalIncTax(final BigDecimal amtTotalIncTax) {
        this.amtTotalIncTax = amtTotalIncTax;
    }
    
    @Override
    public String toString() {
        return "SameRateTotalDto{taxRate=" + this.taxRate + ", amtTotal=" + this.amtTotal + ", amtTotalIncTax=" + this.amtTotalIncTax + ", taxAmtTotal=" + this.taxAmtTotal + ", taxDcTotal=" + this.taxDcTotal + '}';
    }
}
