package com.szhtxx.etcloud.smser.dto;

import java.io.*;

public class BillDealResultDto implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String billNO;
    private boolean success;
    private String errorMsg;
    
    public String getBillNO() {
        return this.billNO;
    }
    
    public void setBillNO(final String billNO) {
        this.billNO = billNO;
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
    
    @Override
    public String toString() {
        return "BillDealResultDto{billNO='" + this.billNO + '\'' + ", success=" + this.success + ", errorMsg='" + this.errorMsg + '\'' + '}';
    }
}
