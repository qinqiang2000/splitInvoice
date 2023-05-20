package com.szhtxx.etcloud.smser.exception;

public class SmserException extends Exception
{
    private static final long serialVersionUID = 1L;
    protected int errorCode;
    
    public SmserException(final Integer errorCode, final String message) {
        super(message);
        this.errorCode = -1;
        this.errorCode = errorCode;
    }
    
    public SmserException(final String message) {
        super(message);
        this.errorCode = -1;
    }
}
