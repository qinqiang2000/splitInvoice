package kd.imc.sim.split.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class InvoiceSubjectDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String invoiceNO;

    private int invoiceKind;

    private int invoiceProp;

    private BigDecimal amounts;

    private BigDecimal taxRate;

    private BigDecimal taxAmt;

    private BigDecimal invErr;

    private String taxDeduction;

    private int isTaxDe;

    private int listFlag;

    private String billNO;

    private int pageIndex;

    private List<InvoiceDetailDto> invoiceDetailList;

    private List<BigDecimal> invTaxRate;

    List<String> lineNotes;

    private String invoiceNote;

    private List<String> billNOList;

    private String remark;

    public InvoiceSubjectDto() {
        this.amounts = BigDecimal.ZERO;
        this.taxAmt = BigDecimal.ZERO;
        this.invErr = BigDecimal.ZERO;
        this.invTaxRate = new ArrayList<>();
        this.lineNotes = new ArrayList<>();
    }

    public String getInvoiceNO() {
        return this.invoiceNO;
    }

    public void setInvoiceNO(String invoiceNO) {
        this.invoiceNO = invoiceNO;
    }

    public int getInvoiceKind() {
        return this.invoiceKind;
    }

    public void setInvoiceKind(int invoiceKind) {
        this.invoiceKind = invoiceKind;
    }

    public int getInvoiceProp() {
        return this.invoiceProp;
    }

    public void setInvoiceProp(int invoiceProp) {
        this.invoiceProp = invoiceProp;
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

    public String getTaxDeduction() {
        return this.taxDeduction;
    }

    public void setTaxDeduction(String taxDeduction) {
        this.taxDeduction = taxDeduction;
    }

    public int getIsTaxDe() {
        return this.isTaxDe;
    }

    public void setIsTaxDe(int isTaxDe) {
        this.isTaxDe = isTaxDe;
    }

    public String getInvoiceNote() {
        return this.invoiceNote;
    }

    public void setInvoiceNote(String invoiceNote) {
        this.invoiceNote = invoiceNote;
    }

    public int getListFlag() {
        return this.listFlag;
    }

    public void setListFlag(int listFlag) {
        this.listFlag = listFlag;
    }

    public List<InvoiceDetailDto> getInvoiceDetailList() {
        return this.invoiceDetailList;
    }

    public void setInvoiceDetailList(List<InvoiceDetailDto> invoiceDetailList) {
        this.invoiceDetailList = invoiceDetailList;
    }

    public String getBillNO() {
        return this.billNO;
    }

    public void setBillNO(String billNO) {
        this.billNO = billNO;
    }

    public BigDecimal getInvErr() {
        return this.invErr;
    }

    public void setInvErr(BigDecimal invErr) {
        this.invErr = invErr;
    }

    public int getPageIndex() {
        return this.pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public List<String> getBillNOList() {
        return this.billNOList;
    }

    public void setBillNOList(List<String> billNOList) {
        this.billNOList = billNOList;
    }

    public List<BigDecimal> getInvTaxRate() {
        return this.invTaxRate;
    }

    public void setInvTaxRate(List<BigDecimal> invTaxRate) {
        this.invTaxRate = invTaxRate;
    }

    public List<String> getLineNotes() {
        return this.lineNotes;
    }

    public void setLineNotes(List<String> lineNotes) {
        this.lineNotes = lineNotes;
    }

    public String getRemark() {
        return this.remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String toString() {
        return "InvoiceSubjectDto{invoiceNO='" + this.invoiceNO + '\'' + ", invoiceKind=" + this.invoiceKind + ", invoiceProp=" + this.invoiceProp + ", amounts=" + this.amounts + ", taxRate=" + this.taxRate + ", taxAmt=" + this.taxAmt + ", invErr=" + this.invErr + ", taxDeduction='" + this.taxDeduction + '\'' + ", isTaxDe=" + this.isTaxDe + ", listFlag=" + this.listFlag + ", billNO='" + this.billNO + '\'' + ", pageIndex=" + this.pageIndex + ", invoiceDetailList=" + this.invoiceDetailList + ", invTaxRate=" + this.invTaxRate + ", lineNotes=" + this.lineNotes + ", invoiceNote='" + this.invoiceNote + '\'' + ", billNOList=" + this.billNOList + ", remark='" + this.remark + '\'' + '}';
    }
}
