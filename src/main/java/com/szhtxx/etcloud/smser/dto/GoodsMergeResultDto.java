package com.szhtxx.etcloud.smser.dto;

import java.io.*;
import java.math.*;

public class GoodsMergeResultDto implements Serializable
{
    private boolean breakFlag;
    private int nowIndex;
    private int nextIndex;
    private BigDecimal mergeAmounts;
    private BigDecimal mergeAmountsIncTax;
    private BigDecimal mergeTaxAmt;
    
    public GoodsMergeResultDto() {
    }
    
    public GoodsMergeResultDto(final int nowIndex, final int nextIndex) {
        this.nowIndex = nowIndex;
        this.nextIndex = nextIndex;
    }
    
    public boolean isBreakFlag() {
        return this.breakFlag;
    }
    
    public void setBreakFlag(final boolean breakFlag) {
        this.breakFlag = breakFlag;
    }
    
    public int getNowIndex() {
        return this.nowIndex;
    }
    
    public void setNowIndex(final int nowIndex) {
        this.nowIndex = nowIndex;
    }
    
    public int getNextIndex() {
        return this.nextIndex;
    }
    
    public void setNextIndex(final int nextIndex) {
        this.nextIndex = nextIndex;
    }
    
    public BigDecimal getMergeAmounts() {
        return this.mergeAmounts;
    }
    
    public void setMergeAmounts(final BigDecimal mergeAmounts) {
        this.mergeAmounts = mergeAmounts;
    }
    
    public BigDecimal getMergeTaxAmt() {
        return this.mergeTaxAmt;
    }
    
    public void setMergeTaxAmt(final BigDecimal mergeTaxAmt) {
        this.mergeTaxAmt = mergeTaxAmt;
    }
    
    public BigDecimal getMergeAmountsIncTax() {
        return this.mergeAmountsIncTax;
    }
    
    public void setMergeAmountsIncTax(final BigDecimal mergeAmountsIncTax) {
        this.mergeAmountsIncTax = mergeAmountsIncTax;
    }
}
