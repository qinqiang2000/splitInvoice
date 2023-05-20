package kd.imc.sim.split.dto;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

// 单据
public class BillSubjectDto implements Serializable {
    // 单据编号
    private String billNO;

    // 客户编码
    private String custCode;

    // 客户名称
    private String custName;

    // 发票种类
    private Integer invKind;

    // 是否含税
    private Integer includeTax = 1;

    @JSONField(serialize = false)
    // 销售名称
    private String saleName;

    @JSONField(serialize = false)
    // 备注
    private String notes;

    @JSONField(serialize = false)
    // 票据规格
    private String rollInvSpec;

    // 单据明细
    private List<BillDetailDto> billDList;

    @JSONField(serialize = false)
    // 金额合计
    private BigDecimal sumAmtJE;

    @JSONField(serialize = false)
    // 税额合计
    private BigDecimal sumAmtSE;

    @JSONField(serialize = false)
    // 限制行数
    private Integer limitLine;

    @JSONField(serialize = false)
    // 是否检查通过
    private Boolean checkPassed;

    @JSONField(serialize = false)
    // 是否油卡
    private String isOil;

    @JSONField(serialize = false)
    // 负单据号集合
    private Set<String> negBillNoSet;

     public BillSubjectDto() {
        this.sumAmtJE = BigDecimal.ZERO;
        this.sumAmtSE = BigDecimal.ZERO;
        this.checkPassed = Boolean.TRUE;
        this.isOil = "0";
    }

    public String getBillNO() {
        return this.billNO;
    }

    public void setBillNO(String billNO) {
        this.billNO = billNO;
    }

    public String getCustCode() {
        return this.custCode;
    }

    public void setCustCode(String custCode) {
        this.custCode = custCode;
    }

    public String getCustName() {
        return this.custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public Integer getInvKind() {
        return this.invKind;
    }

    public void setInvKind(Integer invKind) {
        this.invKind = invKind;
    }

    public Integer getIncludeTax() {
        return this.includeTax;
    }

    public void setIncludeTax(Integer includeTax) {
        this.includeTax = includeTax;
    }
    
    public String getSaleName() {
        return this.saleName;
    }

    public void setSaleName(String saleName) {
        this.saleName = saleName;
    }

    public List<BillDetailDto> getBillDList() {
        return this.billDList;
    }

    public void setBillDList(List<BillDetailDto> billDList) {
        this.billDList = billDList;
    }

    public String getNotes() {
        return this.notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getSumAmtJE() {
        return this.sumAmtJE;
    }

    public void setSumAmtJE(BigDecimal sumAmtJE) {
        this.sumAmtJE = sumAmtJE;
    }

    public BigDecimal getSumAmtSE() {
        return this.sumAmtSE;
    }

    public void setSumAmtSE(BigDecimal sumAmtSE) {
        this.sumAmtSE = sumAmtSE;
    }

    public Integer getLimitLine() {
        return this.limitLine;
    }

    public void setLimitLine(Integer limitLine) {
        this.limitLine = limitLine;
    }

    public String getRollInvSpec() {
        return this.rollInvSpec;
    }

    public void setRollInvSpec(String rollInvSpec) {
        this.rollInvSpec = rollInvSpec;
    }

    public Boolean getCheckPassed() {
        return this.checkPassed;
    }

    public void setCheckPassed(Boolean checkPassed) {
        this.checkPassed = checkPassed;
    }

    public String getIsOil() {
        return this.isOil;
    }

    public void setIsOil(String isOil) {
        this.isOil = isOil;
    }

    public Set<String> getNegBillNoSet() {
        return this.negBillNoSet;
    }

    public void setNegBillNoSet(Set<String> negBillNoSet) {
        this.negBillNoSet = negBillNoSet;
    }

    public String toString() {
        return "BillSubjectDto{billNO='" + this.billNO + '\'' + ", custCode='" + this.custCode + '\'' + ", custName='" + this.custName + '\'' + ", invKind=" + this.invKind + ", includeTax=" + this.includeTax + ", saleName='" + this.saleName + '\'' + ", notes='" + this.notes + '\'' + ", rollInvSpec='" + this.rollInvSpec + '\'' + ", billDList=" + this.billDList + ", sumAmtJE=" + this.sumAmtJE + ", sumAmtSE=" + this.sumAmtSE + ", limitLine=" + this.limitLine + ", checkPassed=" + this.checkPassed + ", isOil='" + this.isOil + '\'' + ", negBillNoSet=" + this.negBillNoSet + '}';
    }
}