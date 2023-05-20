package com.szhtxx.etcloud.smser.dto;

import java.io.*;
import java.math.*;
import com.szhtxx.etcloud.smser.constant.*;
import java.util.*;
import com.szhtxx.etcloud.smser.utils.*;

public class BillDetailDto implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String billNO; // 单据编号
    private String billDetailNO; // 单据明细编号
    private String goodsName; // 商品名称
    private BigDecimal taxRate; // 税率
    private String goodsTaxNo; // 商品税目
    private Integer lineProperty; // 行性质
    private Integer disRows; // 折扣行数
    private Integer includeTax; // 是否含税
    private String goodsNoVer; // 商品编码版本号
    private BigDecimal amts; // 数量
    private BigDecimal originalAmts; // 原金额
    private BigDecimal price; // 单价
    private BigDecimal amounts; // 金额
    private BigDecimal taxDeduction; // 税额抵扣
    private BigDecimal disAmt; // 折扣金额
    private BigDecimal taxAmt; // 税额
    private BigDecimal disRate; // 折扣率
    private BigDecimal amountsIncTax; // 含税金额
    private BigDecimal priceIncTax; // 含税单价
    private String goodsModel; // 商品型号
    private String goodsCode; // 商品编码
    private String goodsClass; // 商品分类
    private String goodsUnit; // 商品单位
    private BigDecimal disAmtIncTax; // 含税折扣金额
    private Integer taxPre; // 税前标志
    private String taxPreCon; // 税前折扣条件
    private BigDecimal disTaxAmt; // 折扣税额
    private String zeroTax; // 零税率标识
    private String cropGoodsNo; // 企业商品编码
    private String lineNote; // 行备注
    private int splitSign; // 拆分标志
    private String by1; // 备用字段1
    private Set<BillDetailIdDto> detailIdSet; // 单据明细ID集合
    
    public BillDetailDto() {
        this.lineProperty = 0;
        this.taxPre = 0;
    }
    
    public BigDecimal getAmountsByTax() {
        if (SmserConstant.ONE.equals(this.includeTax)) {
            return this.getAmountsIncTax();
        }
        return this.getAmounts();
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
    
    public String getGoodsName() {
        return this.goodsName;
    }
    
    public void setGoodsName(final String goodsName) {
        this.goodsName = goodsName;
    }
    
    public BigDecimal getTaxRate() {
        return this.taxRate;
    }
    
    public void setTaxRate(final BigDecimal taxRate) {
        this.taxRate = taxRate;
    }
    
    public String getGoodsTaxNo() {
        return this.goodsTaxNo;
    }
    
    public void setGoodsTaxNo(final String goodsTaxNo) {
        this.goodsTaxNo = goodsTaxNo;
    }
    
    public Integer getLineProperty() {
        return this.lineProperty;
    }
    
    public void setLineProperty(final Integer lineProperty) {
        this.lineProperty = lineProperty;
    }
    
    public Integer getDisRows() {
        return this.disRows;
    }
    
    public void setDisRows(final Integer disRows) {
        this.disRows = disRows;
    }
    
    public Integer getIncludeTax() {
        return this.includeTax;
    }
    
    public void setIncludeTax(final Integer includeTax) {
        this.includeTax = includeTax;
    }
    
    public String getGoodsNoVer() {
        return this.goodsNoVer;
    }
    
    public void setGoodsNoVer(final String goodsNoVer) {
        this.goodsNoVer = goodsNoVer;
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
    
    public BigDecimal getTaxDeduction() {
        return this.taxDeduction;
    }
    
    public void setTaxDeduction(final BigDecimal taxDeduction) {
        this.taxDeduction = taxDeduction;
    }
    
    public BigDecimal getDisAmt() {
        return (this.disAmt == null) ? BigDecimal.ZERO : this.disAmt;
    }
    
    public void setDisAmt(final BigDecimal disAmt) {
        this.disAmt = disAmt;
    }
    
    public BigDecimal getTaxAmt() {
        return this.taxAmt;
    }
    
    public void setTaxAmt(final BigDecimal taxAmt) {
        this.taxAmt = taxAmt;
    }
    
    public BigDecimal getDisRate() {
        return this.disRate;
    }
    
    public void setDisRate(final BigDecimal disRate) {
        this.disRate = disRate;
    }
    
    public BigDecimal getAmountsIncTax() {
        return this.amountsIncTax;
    }
    
    public void setAmountsIncTax(final BigDecimal amountsIncTax) {
        this.amountsIncTax = amountsIncTax;
    }
    
    public BigDecimal getPriceIncTax() {
        return this.priceIncTax;
    }
    
    public void setPriceIncTax(final BigDecimal priceIncTax) {
        this.priceIncTax = priceIncTax;
    }
    
    public String getGoodsModel() {
        return this.goodsModel;
    }
    
    public void setGoodsModel(final String goodsModel) {
        this.goodsModel = goodsModel;
    }
    
    public String getGoodsCode() {
        return this.goodsCode;
    }
    
    public void setGoodsCode(final String goodsCode) {
        this.goodsCode = goodsCode;
    }
    
    public String getGoodsClass() {
        return this.goodsClass;
    }
    
    public void setGoodsClass(final String goodsClass) {
        this.goodsClass = goodsClass;
    }
    
    public String getGoodsUnit() {
        return this.goodsUnit;
    }
    
    public void setGoodsUnit(final String goodsUnit) {
        this.goodsUnit = goodsUnit;
    }
    
    public BigDecimal getDisAmtIncTax() {
        return this.disAmtIncTax;
    }
    
    public void setDisAmtIncTax(final BigDecimal disAmtIncTax) {
        this.disAmtIncTax = disAmtIncTax;
    }
    
    public Integer getTaxPre() {
        return this.taxPre;
    }
    
    public void setTaxPre(final Integer taxPre) {
        this.taxPre = taxPre;
    }
    
    public String getTaxPreCon() {
        return this.taxPreCon;
    }
    
    public void setTaxPreCon(final String taxPreCon) {
        this.taxPreCon = taxPreCon;
    }
    
    public BigDecimal getDisTaxAmt() {
        return this.disTaxAmt;
    }
    
    public void setDisTaxAmt(final BigDecimal disTaxAmt) {
        this.disTaxAmt = disTaxAmt;
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
    
    public String getLineNote() {
        return this.lineNote;
    }
    
    public void setLineNote(final String lineNote) {
        this.lineNote = lineNote;
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
    
    public BigDecimal getOriginalAmts() {
        return this.originalAmts;
    }
    
    public void setOriginalAmts(final BigDecimal originalAmts) {
        this.originalAmts = originalAmts;
    }
    
    public void setSplitSign(final int splitSign) {
        this.splitSign = splitSign;
    }
    
    public Set<BillDetailIdDto> getDetailIdSet() {
        if (this.detailIdSet == null) {
            this.detailIdSet = new LinkedHashSet<BillDetailIdDto>();
            if (this.billNO != null) {
                final BillDetailIdDto detail = new BillDetailIdDto(this.billNO, this.getBillDetailNO(), this.amounts, this.price, this.amts);
                this.detailIdSet.add(detail);
            }
        }
        return this.detailIdSet;
    }
    
    public void setDetailIdSet(final Set<BillDetailIdDto> detailIdSet) {
        this.detailIdSet = detailIdSet;
    }
    
    public void addDetailId(final Set<BillDetailIdDto> addDetailId) {
        this.detailIdSet = this.getDetailIdSet();
        if (addDetailId != null) {
            this.detailIdSet.addAll(addDetailId);
        }
    }
    
    public BigDecimal queryLineTaxError() {
        return this.taxAmt.subtract(CalTaxUtils.calTax(this.includeTax, this.getAmountsByTax(), this.taxRate, 4));
    }
    
    public void setOtherMoney() {
        if (1 == this.includeTax) {
            this.amounts = this.amountsIncTax.subtract(this.taxAmt);
        }
        else {
            this.amountsIncTax = this.amounts.add(this.taxAmt);
        }
    }
    
    public BigDecimal getPriceByTaxFlag() {
        if (SmserConstant.ONE.equals(this.includeTax)) {
            return this.getPriceIncTax();
        }
        return this.getPrice();
    }
    
    @Override
    public String toString() {
        return "BillDetailDto{billNO='" + this.billNO + '\'' + ", billDetailNO='" + this.billDetailNO + '\'' + ", goodsName='" + this.goodsName + '\'' + ", taxRate=" + this.taxRate + ", goodsTaxNo='" + this.goodsTaxNo + '\'' + ", lineProperty=" + this.lineProperty + ", disRows=" + this.disRows + ", includeTax=" + this.includeTax + ", goodsNoVer='" + this.goodsNoVer + '\'' + ", amts=" + this.amts + ", price=" + this.price + ", amounts=" + this.amounts + ", taxDeduction=" + this.taxDeduction + ", disAmt=" + this.disAmt + ", taxAmt=" + this.taxAmt + ", disRate=" + this.disRate + ", amountsIncTax=" + this.amountsIncTax + ", priceIncTax=" + this.priceIncTax + ", goodsModel='" + this.goodsModel + '\'' + ", goodsCode='" + this.goodsCode + '\'' + ", goodsClass=" + this.goodsClass + ", goodsUnit='" + this.goodsUnit + '\'' + ", disAmtIncTax=" + this.disAmtIncTax + ", taxPre=" + this.taxPre + ", taxPreCon=" + this.taxPreCon + ", disTaxAmt=" + this.disTaxAmt + ", zeroTax=" + this.zeroTax + ", cropGoodsNo='" + this.cropGoodsNo + '\'' + ", lineNote='" + this.lineNote + '\'' + ", by1='" + this.by1 + '\'' + ", splitSign='" + this.splitSign + '\'' + ", detailIdSet=" + this.detailIdSet + '}';
    }
}
