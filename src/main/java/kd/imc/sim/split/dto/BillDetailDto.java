package kd.imc.sim.split.dto;

import kd.imc.sim.common.constant.InvoiceConstant;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

// 单据明细
public class BillDetailDto implements Serializable {

    private String billNO; // 单据编号

    // 单据明细编号
    private String billDetailNO;

    private String goodsName; // 商品名称

    // 税率
    private BigDecimal taxRate;

    private String goodsTaxNo; // 商品税目

    private Integer lineProperty = 0; // 行属性；4: 折扣行

    // 折扣行数
    private Integer disRows;

    private Integer includeTax; // 是否含税

    private String goodsNoVer; // 商品编码版本号

    // 数量 
    private BigDecimal amts;

    private BigDecimal originalAmts; // 原数量

    // 单价
    private BigDecimal price;

    // 金额
    private BigDecimal amounts;

    // 减按税率
    private BigDecimal taxDeduction;

    // 折扣金额
    private BigDecimal disAmt;

    private BigDecimal taxAmt; // 税额

    private BigDecimal disRate; // 折扣率

    private BigDecimal amountsIncTax; // 含税金额

    private BigDecimal priceIncTax; // 含税单价

    private String goodsModel; // 商品型号

    private String goodsCode; // 商品编码

    private String goodsClass; // 商品分类

    private String goodsUnit; // 商品单位

    private BigDecimal disAmtIncTax; // 含税折扣金额

    private Integer taxPre = 0; // 是否享受税收优惠政策

    private String taxPreCon; // 税收优惠政策内容

    private BigDecimal disTaxAmt; // 折扣税额

    private String zeroTax; // 零税率标识

    private String cropGoodsNo; // 粮油编码

    private String lineNote; // 行备注

    private int splitSign; // 拆分标志

    private String by1; // 备用字段1

    private Set<BillDetailIdDto> detailIdSet; // 明细ID集合

    public BigDecimal getAmountsByTax() {
        return InvoiceConstant.IS_TAX_YES_INT.equals(this.includeTax) ? this.getAmountsIncTax() : this.getAmounts();
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

    public String getGoodsName() {
        return this.goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public BigDecimal getTaxRate() {
        return this.taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public String getGoodsTaxNo() {
        return this.goodsTaxNo;
    }

    public void setGoodsTaxNo(String goodsTaxNo) {
        this.goodsTaxNo = goodsTaxNo;
    }

    public Integer getLineProperty() {
        return this.lineProperty;
    }

    public void setLineProperty(Integer lineProperty) {
        this.lineProperty = lineProperty;
    }

    public Integer getDisRows() {
        return this.disRows;
    }

    public void setDisRows(Integer disRows) {
        this.disRows = disRows;
    }

    public Integer getIncludeTax() {
        return this.includeTax;
    }

    public void setIncludeTax(Integer includeTax) {
        this.includeTax = includeTax;
    }

    public String getGoodsNoVer() {
        return this.goodsNoVer;
    }

    public void setGoodsNoVer(String goodsNoVer) {
        this.goodsNoVer = goodsNoVer;
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

    public BigDecimal getTaxDeduction() {
        return this.taxDeduction == null ? BigDecimal.ZERO : this.taxDeduction;
    }

    public void setTaxDeduction(BigDecimal taxDeduction) {
        this.taxDeduction = taxDeduction;
    }

    public BigDecimal getDisAmt() {
        return this.disAmt == null ? BigDecimal.ZERO : this.disAmt;
    }

    public void setDisAmt(BigDecimal disAmt) {
        this.disAmt = disAmt;
    }

    public BigDecimal getTaxAmt() {
        return this.taxAmt;
    }

    public void setTaxAmt(BigDecimal taxAmt) {
        this.taxAmt = taxAmt;
    }

    public BigDecimal getDisRate() {
        return this.disRate;
    }

    public void setDisRate(BigDecimal disRate) {
        this.disRate = disRate;
    }

    public BigDecimal getAmountsIncTax() {
        return this.amountsIncTax;
    }

    public void setAmountsIncTax(BigDecimal amountsIncTax) {
        this.amountsIncTax = amountsIncTax;
    }

    public BigDecimal getPriceIncTax() {
        return this.priceIncTax;
    }

    public void setPriceIncTax(BigDecimal priceIncTax) {
        this.priceIncTax = priceIncTax;
    }

    public String getGoodsModel() {
        return this.goodsModel;
    }

    public void setGoodsModel(String goodsModel) {
        this.goodsModel = goodsModel;
    }

    public String getGoodsCode() {
        return this.goodsCode;
    }

    public void setGoodsCode(String goodsCode) {
        this.goodsCode = goodsCode;
    }

    public String getGoodsClass() {
        return this.goodsClass;
    }

    public void setGoodsClass(String goodsClass) {
        this.goodsClass = goodsClass;
    }

    public String getGoodsUnit() {
        return this.goodsUnit;
    }

    public void setGoodsUnit(String goodsUnit) {
        this.goodsUnit = goodsUnit;
    }

    public BigDecimal getDisAmtIncTax() {
        return this.disAmtIncTax;
    }

    public void setDisAmtIncTax(BigDecimal disAmtIncTax) {
        this.disAmtIncTax = disAmtIncTax;
    }

    public Integer getTaxPre() {
        return this.taxPre;
    }

    public void setTaxPre(Integer taxPre) {
        this.taxPre = taxPre;
    }

    public String getTaxPreCon() {
        return this.taxPreCon;
    }

    public void setTaxPreCon(String taxPreCon) {
        this.taxPreCon = taxPreCon;
    }

    public BigDecimal getDisTaxAmt() {
        return this.disTaxAmt;
    }

    public void setDisTaxAmt(BigDecimal disTaxAmt) {
        this.disTaxAmt = disTaxAmt;
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

    public String getLineNote() {
        return this.lineNote;
    }

    public void setLineNote(String lineNote) {
        this.lineNote = lineNote;
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

    public BigDecimal getOriginalAmts() {
        return this.originalAmts;
    }

    public void setOriginalAmts(BigDecimal originalAmts) {
        this.originalAmts = originalAmts;
    }

    public void setSplitSign(int splitSign) {
        this.splitSign = splitSign;
    }

    public Set<BillDetailIdDto> getDetailIdSet() {
        if (this.detailIdSet == null) {
            this.detailIdSet = new LinkedHashSet<>();
            if (this.billNO != null) {
                BillDetailIdDto detail = new BillDetailIdDto(this.billNO, this.getBillDetailNO(), this.amounts, this.price, this.amts);
                this.detailIdSet.add(detail);
            }
        }
        return this.detailIdSet;
    }

    public void setDetailIdSet(Set<BillDetailIdDto> detailIdSet) {
        this.detailIdSet = detailIdSet;
    }

    public String toString() {
        return "BillDetailDto{billNO='" + this.billNO + '\'' + ", billDetailNO='" + this.billDetailNO + '\'' + ", goodsName='" + this.goodsName + '\'' + ", taxRate=" + this.taxRate + ", goodsTaxNo='" + this.goodsTaxNo + '\'' + ", lineProperty=" + this.lineProperty + ", disRows=" + this.disRows + ", includeTax=" + this.includeTax + ", goodsNoVer='" + this.goodsNoVer + '\'' + ", amts=" + this.amts + ", price=" + this.price + ", amounts=" + this.amounts + ", taxDeduction=" + this.taxDeduction + ", disAmt=" + this.disAmt + ", taxAmt=" + this.taxAmt + ", disRate=" + this.disRate + ", amountsIncTax=" + this.amountsIncTax + ", priceIncTax=" + this.priceIncTax + ", goodsModel='" + this.goodsModel + '\'' + ", goodsCode='" + this.goodsCode + '\'' + ", goodsClass=" + this.goodsClass + ", goodsUnit='" + this.goodsUnit + '\'' + ", disAmtIncTax=" + this.disAmtIncTax + ", taxPre=" + this.taxPre + ", taxPreCon=" + this.taxPreCon + ", disTaxAmt=" + this.disTaxAmt + ", zeroTax=" + this.zeroTax + ", cropGoodsNo='" + this.cropGoodsNo + '\'' + ", lineNote='" + this.lineNote + '\'' + ", by1='" + this.by1 + '\'' + ", splitSign='" + this.splitSign + '\'' + ", detailIdSet=" + this.detailIdSet + '}';
    }
}