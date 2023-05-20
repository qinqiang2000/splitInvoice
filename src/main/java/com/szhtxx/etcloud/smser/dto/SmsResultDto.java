package com.szhtxx.etcloud.smser.dto;

import java.io.*;
import com.alibaba.fastjson.annotation.*;
import org.apache.commons.collections.*;
import java.util.*;

public class SmsResultDto implements Serializable
{
    private static final long serialVersionUID = 1L;
    @JSONField(serialize = false)
    private boolean success;
    @JSONField(serialize = false)
    private String errorMsg;
    private int totals;
    private int doSucc;
    private int doFail;
    private List<InvoiceSubjectDto> invoiceSList;
    private List<BillDealResultDto> bdrList;
    
    public SmsResultDto() {
        this.success = true;
    }
    
    public boolean isSuccess() {
        return this.success;
    }
    
    public void setSuccess(final boolean success) {
        this.success = success;
    }
    
    public String getErrorMsg() {
        return this.errorMsg;
    }
    
    public void setErrorMsg(final String errorMsg) {
        this.errorMsg = errorMsg;
    }
    
    public int getTotals() {
        return this.totals;
    }
    
    public void setTotals(final int totals) {
        this.totals = totals;
    }
    
    public int getDoSucc() {
        return this.doSucc;
    }
    
    public void setDoSucc(final int doSucc) {
        this.doSucc = doSucc;
    }
    
    public int getDoFail() {
        return this.doFail;
    }
    
    public void setDoFail(final int doFail) {
        this.doFail = doFail;
    }
    
    public List<InvoiceSubjectDto> getInvoiceSList() {
        return this.invoiceSList;
    }
    
    public void setInvoiceSList(final List<InvoiceSubjectDto> invoiceSList) {
        this.invoiceSList = invoiceSList;
    }
    
    public List<BillDealResultDto> getBdrList() {
        if (CollectionUtils.isEmpty(this.bdrList)) {
            this.bdrList = new ArrayList<BillDealResultDto>(0);
        }
        return this.bdrList;
    }
    
    public void setBdrList(final List<BillDealResultDto> bdrList) {
        this.bdrList = bdrList;
    }
    
    @Override
    public String toString() {
        return "SmsResultDto{success=" + this.success + ", errorMsg='" + this.errorMsg + '\'' + ", totals=" + this.totals + ", doSucc=" + this.doSucc + ", doFail=" + this.doFail + ", invoiceSList=" + this.invoiceSList + ", bdrList=" + this.bdrList + '}';
    }
}
