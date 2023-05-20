package kd.imc.sim.split.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SmsResultDto implements Serializable {
    @JSONField(serialize = false)
    private boolean success = true; // 是否成功

    @JSONField(serialize = false)
    private String errorMsg; // 错误信息

    private int totals; // 总数

    private int doSucc; // 成功数

    private int doFail; // 失败数

    private List<InvoiceSubjectDto> invoiceSList; // 发票主体列表

    private List<BillDealResultDto> bdrList; // 账单处理结果列表

    // getter和setter方法
    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMsg() {
        return this.errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public int getTotals() {
        return this.totals;
    }

    public void setTotals(int totals) {
        this.totals = totals;
    }

    public int getDoSucc() {
        return this.doSucc;
    }

    public void setDoSucc(int doSucc) {
        this.doSucc = doSucc;
    }

    public int getDoFail() {
        return this.doFail;
    }

    public void setDoFail(int doFail) {
        this.doFail = doFail;
    }

    public List<InvoiceSubjectDto> getInvoiceSList() {
        return this.invoiceSList;
    }

    public void setInvoiceSList(List<InvoiceSubjectDto> invoiceSList) {
        this.invoiceSList = invoiceSList;
    }

    public List<BillDealResultDto> getBdrList() {
        if (CollectionUtils.isEmpty(this.bdrList)) {
            this.bdrList = new ArrayList<>(0);
        }
        return this.bdrList;
    }

    public void setBdrList(List<BillDealResultDto> bdrList) {
        this.bdrList = bdrList;
    }

    public String toString() {
        return "SmsResultDto{success=" + this.success + ", errorMsg='" + this.errorMsg + '\'' + ", totals=" + this.totals + ", doSucc=" + this.doSucc + ", doFail=" + this.doFail + ", invoiceSList=" + this.invoiceSList + ", bdrList=" + this.bdrList + '}';
    }
}