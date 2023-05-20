package kd.imc.sim.split.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

public class InvoiceDetailDto implements Serializable {

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

    private int includeTax = 1;

    private BigDecimal invDetailErr;

    private String billNO;

    private String billDetailNO;

    private String by1;

    private int splitSign;

    private Set<BillDetailIdDto> detailIdSet;

    public InvoiceDetailDto() {
        this.invDetailErr = BigDecimal.ZERO;
    }

    public Set<BillDetailIdDto> getDetailIdSet() {
        return this.detailIdSet;
    }

    public void setDetailIdSet(Set<BillDetailIdDto> detailIdSet) {
        this.detailIdSet = detailIdSet;
    }

    public String getInvoiceNO() {
        return this.invoiceNO;
    }

    public void setInvoiceNO(String invoiceNO) {
        this.invoiceNO = invoiceNO;
    }

    public String getInvoiceDetailNO() {
        return this.invoiceDetailNO;
    }

    public void setInvoiceDetailNO(String invoiceDetailNO) {
        this.invoiceDetailNO = invoiceDetailNO;
    }

    public String getGoodsClass() {
        return this.goodsClass;
    }

    public void setGoodsClass(String goodsClass) {
        this.goodsClass = goodsClass;
    }

    public String getGoodsCode() {
        return this.goodsCode;
    }

    public void setGoodsCode(String goodsCode) {
        this.goodsCode = goodsCode;
    }

    public String getGoodsName() {
        return this.goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getGoodsModel() {
        return this.goodsModel;
    }

    public void setGoodsModel(String goodsModel) {
        this.goodsModel = goodsModel;
    }

    public String getGoodsUnit() {
        return this.goodsUnit;
    }

    public void setGoodsUnit(String goodsUnit) {
        this.goodsUnit = goodsUnit;
    }

    public BigDecimal getAmts() {
        return this.amts;
    }

    public void setAmts(BigDecimal amts) {
        this.amts = amts;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getAmounts() {
        return this.amounts;
    }

    public void setAmounts(BigDecimal amounts) {
        this.amounts = amounts;
    }

    public BigDecimal getTaxRate() {
        return this.taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getTaxAmt() {
        return this.taxAmt;
    }

    public void setTaxAmt(BigDecimal taxAmt) {
        this.taxAmt = taxAmt;
    }

    public String getGoodsNoVer() {
        return this.goodsNoVer;
    }

    public void setGoodsNoVer(String goodsNoVer) {
        this.goodsNoVer = goodsNoVer;
    }

    public String getGoodsTaxNo() {
        return this.goodsTaxNo;
    }

    public void setGoodsTaxNo(String goodsTaxNo) {
        this.goodsTaxNo = goodsTaxNo;
    }

    public int getTaxPre() {
        return this.taxPre;
    }

    public void setTaxPre(int taxPre) {
        this.taxPre = taxPre;
    }

    public String getTaxPreCon() {
        return this.taxPreCon;
    }

    public void setTaxPreCon(String taxPreCon) {
        this.taxPreCon = taxPreCon;
    }

    public String getZeroTax() {
        return this.zeroTax;
    }

    public void setZeroTax(String zeroTax) {
        this.zeroTax = zeroTax;
    }

    public String getCropGoodsNo() {
        return this.cropGoodsNo;
    }

    public void setCropGoodsNo(String cropGoodsNo) {
        this.cropGoodsNo = cropGoodsNo;
    }

    public String getTaxDeduction() {
        return this.taxDeduction;
    }

    public void setTaxDeduction(String taxDeduction) {
        this.taxDeduction = taxDeduction;
    }

    public int getLineProperty() {
        return this.lineProperty;
    }

    public void setLineProperty(int lineProperty) {
        this.lineProperty = lineProperty;
    }

    public int getLineNum() {
        return this.lineNum;
    }

    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

    public int getIncludeTax() {
        return this.includeTax;
    }

    public void setIncludeTax(int includeTax) {
        this.includeTax = includeTax;
    }

    public BigDecimal getTaxAmounts() {
        return this.taxAmounts;
    }

    public void setTaxAmounts(BigDecimal taxAmounts) {
        this.taxAmounts = taxAmounts;
    }

    public BigDecimal getInvDetailErr() {
        return this.invDetailErr;
    }

    public void setInvDetailErr(BigDecimal invDetailErr) {
        this.invDetailErr = invDetailErr;
    }

    public String getBillNO() {
        return this.billNO;
    }

    public void setBillNO(String billNO) {
        this.billNO = billNO;
    }

    public String getBillDetailNO() {
        return this.billDetailNO;
    }

    public void setBillDetailNO(String billDetailNO) {
        this.billDetailNO = billDetailNO;
    }

    public BigDecimal getPriceIncTax() {
        return this.priceIncTax;
    }

    public void setPriceIncTax(BigDecimal priceIncTax) {
        this.priceIncTax = priceIncTax;
    }

    public String getTaxDeductionIncTax() {
        return this.taxDeductionIncTax;
    }

    public void setTaxDeductionIncTax(String taxDeductionIncTax) {
        this.taxDeductionIncTax = taxDeductionIncTax;
    }

    public BigDecimal getAmountsIncTax() {
        return this.amountsIncTax;
    }

    public void setAmountsIncTax(BigDecimal amountsIncTax) {
        this.amountsIncTax = amountsIncTax;
    }

    public String getBy1() {
        return this.by1;
    }

    public void setBy1(String by1) {
        this.by1 = by1;
    }

    public int getSplitSign() {
        return this.splitSign;
    }

    public void setSplitSign(int splitSign) {
        this.splitSign = splitSign;
    }

    public String toString() {
        return "InvoiceDetailDto{invoiceNO='" + this.invoiceNO + '\'' + ", invoiceDetailNO='" + this.invoiceDetailNO + '\'' + ", lineNum=" + this.lineNum + ", goodsClass='" + this.goodsClass + '\'' + ", goodsCode='" + this.goodsCode + '\'' + ", goodsName='" + this.goodsName + '\'' + ", goodsModel='" + this.goodsModel + '\'' + ", goodsUnit='" + this.goodsUnit + '\'' + ", amts=" + this.amts + ", price=" + this.price + ", priceIncTax=" + this.priceIncTax + ", amounts=" + this.amounts + ", amountsIncTax=" + this.amountsIncTax + ", taxRate=" + this.taxRate + ", taxAmt=" + this.taxAmt + ", taxAmounts=" + this.taxAmounts + ", goodsNoVer='" + this.goodsNoVer + '\'' + ", goodsTaxNo='" + this.goodsTaxNo + '\'' + ", taxPre=" + this.taxPre + ", taxPreCon='" + this.taxPreCon + '\'' + ", zeroTax='" + this.zeroTax + '\'' + ", cropGoodsNo='" + this.cropGoodsNo + '\'' + ", taxDeduction='" + this.taxDeduction + '\'' + ", taxDeductionIncTax='" + this.taxDeductionIncTax + '\'' + ", lineProperty=" + this.lineProperty + ", includeTax=" + this.includeTax + ", invDetailErr=" + this.invDetailErr + ", billNO='" + this.billNO + '\'' + ", billDetailNO='" + this.billDetailNO + '\'' + ", by1='" + this.by1 + '\'' + ", splitSign='" + this.splitSign + '\'' + ", detailIdSet=" + this.detailIdSet + '}';
    }
}