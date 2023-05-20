package com.szhtxx.etcloud.smser.dto;

import java.io.*;
import java.math.*;
import java.util.*;

public class InvoiceDetailDto implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String invoiceNO;
    private String invoiceDetailNO;
    private int lineNum;
    private String goodsClass;
    private String goodsCode;
    private String goodsName;
    private String goodsModel;
    private String goodsUnit;
    private BigDecimal amts;
    private BigDecimal price;
    private BigDecimal priceIncTax;
    private BigDecimal amounts;
    private BigDecimal amountsIncTax;
    private BigDecimal taxRate;
    private BigDecimal taxAmt;
    private BigDecimal taxAmounts;
    private String goodsNoVer;
    private String goodsTaxNo;
    private int taxPre;
    private String taxPreCon;
    private String zeroTax;
    private String cropGoodsNo;
    private String taxDeduction;
    private String taxDeductionIncTax;
    private int lineProperty;
    private int includeTax;
    private BigDecimal invDetailErr;
    private String billNO;
    private String billDetailNO;
    private String by1;
    private int splitSign;
    private Set<BillDetailIdDto> detailIdSet;
    
    public InvoiceDetailDto() {
        this.includeTax = 1;
        this.invDetailErr = BigDecimal.ZERO;
    }
    
    public Set<BillDetailIdDto> getDetailIdSet() {
        return this.detailIdSet;
    }
    
    public void setDetailIdSet(final Set<BillDetailIdDto> detailIdSet) {
        this.detailIdSet = detailIdSet;
    }
    
    public String getInvoiceNO() {
        return this.invoiceNO;
    }
    
    public void setInvoiceNO(final String invoiceNO) {
        this.invoiceNO = invoiceNO;
    }
    
    public String getInvoiceDetailNO() {
        return this.invoiceDetailNO;
    }
    
    public void setInvoiceDetailNO(final String invoiceDetailNO) {
        this.invoiceDetailNO = invoiceDetailNO;
    }
    
    public String getGoodsClass() {
        return this.goodsClass;
    }
    
    public void setGoodsClass(final String goodsClass) {
        this.goodsClass = goodsClass;
    }
    
    public String getGoodsCode() {
        return this.goodsCode;
    }
    
    public void setGoodsCode(final String goodsCode) {
        this.goodsCode = goodsCode;
    }
    
    public String getGoodsName() {
        return this.goodsName;
    }
    
    public void setGoodsName(final String goodsName) {
        this.goodsName = goodsName;
    }
    
    public String getGoodsModel() {
        return this.goodsModel;
    }
    
    public void setGoodsModel(final String goodsModel) {
        this.goodsModel = goodsModel;
    }
    
    public String getGoodsUnit() {
        return this.goodsUnit;
    }
    
    public void setGoodsUnit(final String goodsUnit) {
        this.goodsUnit = goodsUnit;
    }
    
    public BigDecimal getAmts() {
        return this.amts;
    }
    
    public void setAmts(final BigDecimal amts) {
        this.amts = amts;
    }
    
    public BigDecimal getPrice() {
        return this.price;
    }
    
    public void setPrice(final BigDecimal price) {
        this.price = price;
    }
    
    public BigDecimal getAmounts() {
        return this.amounts;
    }
    
    public void setAmounts(final BigDecimal amounts) {
        this.amounts = amounts;
    }
    
    public BigDecimal getTaxRate() {
        return this.taxRate;
    }
    
    public void setTaxRate(final BigDecimal taxRate) {
        this.taxRate = taxRate;
    }
    
    public BigDecimal getTaxAmt() {
        return this.taxAmt;
    }
    
    public void setTaxAmt(final BigDecimal taxAmt) {
        this.taxAmt = taxAmt;
    }
    
    public String getGoodsNoVer() {
        return this.goodsNoVer;
    }
    
    public void setGoodsNoVer(final String goodsNoVer) {
        this.goodsNoVer = goodsNoVer;
    }
    
    public String getGoodsTaxNo() {
        return this.goodsTaxNo;
    }
    
    public void setGoodsTaxNo(final String goodsTaxNo) {
        this.goodsTaxNo = goodsTaxNo;
    }
    
    public int getTaxPre() {
        return this.taxPre;
    }
    
    public void setTaxPre(final int taxPre) {
        this.taxPre = taxPre;
    }
    
    public String getTaxPreCon() {
        return this.taxPreCon;
    }
    
    public void setTaxPreCon(final String taxPreCon) {
        this.taxPreCon = taxPreCon;
    }
    
    public String getZeroTax() {
        return this.zeroTax;
    }
    
    public void setZeroTax(final String zeroTax) {
        this.zeroTax = zeroTax;
    }
    
    public String getCropGoodsNo() {
        return this.cropGoodsNo;
    }
    
    public void setCropGoodsNo(final String cropGoodsNo) {
        this.cropGoodsNo = cropGoodsNo;
    }
    
    public String getTaxDeduction() {
        return this.taxDeduction;
    }
    
    public void setTaxDeduction(final String taxDeduction) {
        this.taxDeduction = taxDeduction;
    }
    
    public int getLineProperty() {
        return this.lineProperty;
    }
    
    public void setLineProperty(final int lineProperty) {
        this.lineProperty = lineProperty;
    }
    
    public int getLineNum() {
        return this.lineNum;
    }
    
    public void setLineNum(final int lineNum) {
        this.lineNum = lineNum;
    }
    
    public int getIncludeTax() {
        return this.includeTax;
    }
    
    public void setIncludeTax(final int includeTax) {
        this.includeTax = includeTax;
    }
    
    public BigDecimal getTaxAmounts() {
        return this.taxAmounts;
    }
    
    public void setTaxAmounts(final BigDecimal taxAmounts) {
        this.taxAmounts = taxAmounts;
    }
    
    public BigDecimal getInvDetailErr() {
        return this.invDetailErr;
    }
    
    public void setInvDetailErr(final BigDecimal invDetailErr) {
        this.invDetailErr = invDetailErr;
    }
    
    public String getBillNO() {
        return this.billNO;
    }
    
    public void setBillNO(final String billNO) {
        this.billNO = billNO;
    }
    
    public String getBillDetailNO() {
        return this.billDetailNO;
    }
    
    public void setBillDetailNO(final String billDetailNO) {
        this.billDetailNO = billDetailNO;
    }
    
    public BigDecimal getPriceIncTax() {
        return this.priceIncTax;
    }
    
    public void setPriceIncTax(final BigDecimal priceIncTax) {
        this.priceIncTax = priceIncTax;
    }
    
    public String getTaxDeductionIncTax() {
        return this.taxDeductionIncTax;
    }
    
    public void setTaxDeductionIncTax(final String taxDeductionIncTax) {
        this.taxDeductionIncTax = taxDeductionIncTax;
    }
    
    public BigDecimal getAmountsIncTax() {
        return this.amountsIncTax;
    }
    
    public void setAmountsIncTax(final BigDecimal amountsIncTax) {
        this.amountsIncTax = amountsIncTax;
    }
    
    public String getBy1() {
        return this.by1;
    }
    
    public void setBy1(final String by1) {
        this.by1 = by1;
    }
    
    public int getSplitSign() {
        return this.splitSign;
    }
    
    public void setSplitSign(final int splitSign) {
        this.splitSign = splitSign;
    }
    
    @Override
    public String toString() {
        return "InvoiceDetailDto{invoiceNO='" + this.invoiceNO + '\'' + ", invoiceDetailNO='" + this.invoiceDetailNO + '\'' + ", lineNum=" + this.lineNum + ", goodsClass='" + this.goodsClass + '\'' + ", goodsCode='" + this.goodsCode + '\'' + ", goodsName='" + this.goodsName + '\'' + ", goodsModel='" + this.goodsModel + '\'' + ", goodsUnit='" + this.goodsUnit + '\'' + ", amts=" + this.amts + ", price=" + this.price + ", priceIncTax=" + this.priceIncTax + ", amounts=" + this.amounts + ", amountsIncTax=" + this.amountsIncTax + ", taxRate=" + this.taxRate + ", taxAmt=" + this.taxAmt + ", taxAmounts=" + this.taxAmounts + ", goodsNoVer='" + this.goodsNoVer + '\'' + ", goodsTaxNo='" + this.goodsTaxNo + '\'' + ", taxPre=" + this.taxPre + ", taxPreCon='" + this.taxPreCon + '\'' + ", zeroTax='" + this.zeroTax + '\'' + ", cropGoodsNo='" + this.cropGoodsNo + '\'' + ", taxDeduction='" + this.taxDeduction + '\'' + ", taxDeductionIncTax='" + this.taxDeductionIncTax + '\'' + ", lineProperty=" + this.lineProperty + ", includeTax=" + this.includeTax + ", invDetailErr=" + this.invDetailErr + ", billNO='" + this.billNO + '\'' + ", billDetailNO='" + this.billDetailNO + '\'' + ", by1='" + this.by1 + '\'' + ", splitSign='" + this.splitSign + '\'' + ", detailIdSet=" + this.detailIdSet + '}';
    }
}
