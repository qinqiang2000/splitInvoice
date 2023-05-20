package com.szhtxx.etcloud.smser.dto;

import java.io.*;
import java.math.*;
import java.util.*;
import org.apache.commons.lang3.*;

public class BillSubjectDto implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String billNO; // 单据号码
    private String custCode; // 客户编码
    private String custName; // 客户名称
    private Integer invKind; // 发票种类
    private Integer includeTax; // 是否含税
    private String saleName; // 销售人员
    private String notes; // 备注
    private String rollInvSpec; // 票据规格
    private List<BillDetailDto> billDList; // 发票明细列表
    private String sortField; // 排序字段
    private BigDecimal sumAmtJE; // 价税合计金额
    private BigDecimal sumAmtSE; // 税额
    private Integer limitLine; // 分页查询时的每页行数
    private Boolean checkPassed; // 是否审核通过
    private String isOil; // 是否油票
    private Set<String> negBillNoSet; // 负数发票号码集合
    
    public BillSubjectDto() {
        this.includeTax = 1;
        this.sumAmtJE = BigDecimal.ZERO;
        this.sumAmtSE = BigDecimal.ZERO;
        this.checkPassed = Boolean.TRUE;
        this.isOil = "0";
    }
    
    // 将custName、invKind、includeTax和checkPassed的值，将它们拼接成一个字符串，并将其赋值给sortField属性
    public String getSortField() {
        final StringBuffer sb = new StringBuffer();
        if (StringUtils.isNotEmpty((CharSequence)this.custName)) {
            sb.append(this.custName);
        }
        if (this.invKind != null) {
            sb.append("_").append(this.invKind);
        }
        if (this.includeTax != null) {
            sb.append("_").append(this.includeTax);
        }
        if (this.checkPassed) {
            sb.append("_1");
        }
        else {
            sb.append("_0");
        }
        return this.sortField = sb.toString();
    }
    
    public String getBillNO() {
        return this.billNO;
    }
    
    public void setBillNO(final String billNO) {
        this.billNO = billNO;
    }
    
    public String getCustCode() {
        return this.custCode;
    }
    
    public void setCustCode(final String custCode) {
        this.custCode = custCode;
    }
    
    public String getCustName() {
        return this.custName;
    }
    
    public void setCustName(final String custName) {
        this.custName = custName;
    }
    
    public Integer getInvKind() {
        return this.invKind;
    }
    
    public void setInvKind(final Integer invKind) {
        this.invKind = invKind;
    }
    
    public Integer getIncludeTax() {
        return this.includeTax;
    }
    
    public void setIncludeTax(final Integer includeTax) {
        this.includeTax = includeTax;
    }
    
    public String getSaleName() {
        return this.saleName;
    }
    
    public void setSaleName(final String saleName) {
        this.saleName = saleName;
    }
    
    public List<BillDetailDto> getBillDList() {
        return this.billDList;
    }
    
    public void setBillDList(final List<BillDetailDto> billDList) {
        this.billDList = billDList;
    }
    
    public String getNotes() {
        return this.notes;
    }
    
    public void setNotes(final String notes) {
        this.notes = notes;
    }
    
    public BigDecimal getSumAmtJE() {
        return this.sumAmtJE;
    }
    
    public void setSumAmtJE(final BigDecimal sumAmtJE) {
        this.sumAmtJE = sumAmtJE;
    }
    
    public BigDecimal getSumAmtSE() {
        return this.sumAmtSE;
    }
    
    public void setSumAmtSE(final BigDecimal sumAmtSE) {
        this.sumAmtSE = sumAmtSE;
    }
    
    public Integer getLimitLine() {
        return this.limitLine;
    }
    
    public void setLimitLine(final Integer limitLine) {
        this.limitLine = limitLine;
    }
    
    public String getRollInvSpec() {
        return this.rollInvSpec;
    }
    
    public void setRollInvSpec(final String rollInvSpec) {
        this.rollInvSpec = rollInvSpec;
    }
    
    public Boolean getCheckPassed() {
        return this.checkPassed;
    }
    
    public void setCheckPassed(final Boolean checkPassed) {
        this.checkPassed = checkPassed;
    }
    
    public String getIsOil() {
        return this.isOil;
    }
    
    public void setIsOil(final String isOil) {
        this.isOil = isOil;
    }
    
    public Set<String> getNegBillNoSet() {
        return this.negBillNoSet;
    }
    
    public void setNegBillNoSet(final Set<String> negBillNoSet) {
        this.negBillNoSet = negBillNoSet;
    }
    
    @Override
    public String toString() {
        return "BillSubjectDto{billNO='" + this.billNO + '\'' + ", custCode='" + this.custCode + '\'' + ", custName='" + this.custName + '\'' + ", invKind=" + this.invKind + ", includeTax=" + this.includeTax + ", saleName='" + this.saleName + '\'' + ", notes='" + this.notes + '\'' + ", rollInvSpec='" + this.rollInvSpec + '\'' + ", billDList=" + this.billDList + ", sortField='" + this.sortField + '\'' + ", sumAmtJE=" + this.sumAmtJE + ", sumAmtSE=" + this.sumAmtSE + ", limitLine=" + this.limitLine + ", checkPassed=" + this.checkPassed + ", isOil='" + this.isOil + '\'' + ", negBillNoSet=" + this.negBillNoSet + '}';
    }
}
