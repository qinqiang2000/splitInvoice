package com.szhtxx.etcloud.smser.exception;

public class EtcRuleException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    private String msg;
    private int errorCode;
    
    public EtcRuleException(final Integer errorCode, final String msg) {
        super(msg);
        this.errorCode = -1;
        this.errorCode = errorCode;
        this.msg = msg;
    }
    
    public EtcRuleException(final String msg) {
        super(msg);
        this.errorCode = -1;
        this.msg = msg;
    }
    
    public String getMsg() {
        return this.msg;
    }
    
    public void setMsg(final String msg) {
        this.msg = msg;
    }
    
    public int getErrorCode() {
        return this.errorCode;
    }
    
    public void setErrorCode(final int errorCode) {
        this.errorCode = errorCode;
    }
}
