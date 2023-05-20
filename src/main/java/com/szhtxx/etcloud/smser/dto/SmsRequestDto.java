package com.szhtxx.etcloud.smser.dto;

import java.io.*;
import java.util.*;
import java.math.*;

public class SmsRequestDto implements Serializable
{
    private static final long serialVersionUID = 1L;
    private List<BillSubjectDto> billSList;
    private SmruleConfigDto smr;
    private BigDecimal siAmt;
    private BigDecimal ciAmt;
    private BigDecimal eiAmt;
    private BigDecimal esiAmt;
    private BigDecimal vlAmt;
    private int operationType;
    private BigDecimal diskSiAmt;
    private BigDecimal diskCiAmt;
    private BigDecimal diskEiAmt;
    private BigDecimal diskEsiAmt;
    private BigDecimal diskVlAmt;
    private String isOil;
    
    public SmsRequestDto() {
        this.operationType = 0;
        this.isOil = "0";
    }
    
    public List<BillSubjectDto> getBillSList() {
        return this.billSList;
    }
    
    public void setBillSList(final List<BillSubjectDto> billSList) {
        this.billSList = billSList;
    }
    
    public SmruleConfigDto getSmr() {
        return this.smr;
    }
    
    public void setSmr(final SmruleConfigDto smr) {
        this.smr = smr;
    }
    
    public BigDecimal getSiAmt() {
        return this.siAmt;
    }
    
    public void setSiAmt(final BigDecimal siAmt) {
        this.siAmt = siAmt;
    }
    
    public BigDecimal getCiAmt() {
        return this.ciAmt;
    }
    
    public void setCiAmt(final BigDecimal ciAmt) {
        this.ciAmt = ciAmt;
    }
    
    public BigDecimal getEiAmt() {
        return this.eiAmt;
    }
    
    public void setEiAmt(final BigDecimal eiAmt) {
        this.eiAmt = eiAmt;
    }
    
    public BigDecimal getVlAmt() {
        return this.vlAmt;
    }
    
    public void setVlAmt(final BigDecimal vlAmt) {
        this.vlAmt = vlAmt;
    }
    
    public int getOperationType() {
        return this.operationType;
    }
    
    public void setOperationType(final int operationType) {
        this.operationType = operationType;
    }
    
    public String getIsOil() {
        return this.isOil;
    }
    
    public void setIsOil(final String isOil) {
        this.isOil = isOil;
    }
    
    public BigDecimal getDiskSiAmt() {
        return this.diskSiAmt;
    }
    
    public void setDiskSiAmt(final BigDecimal diskSiAmt) {
        this.diskSiAmt = diskSiAmt;
    }
    
    public BigDecimal getDiskCiAmt() {
        return this.diskCiAmt;
    }
    
    public void setDiskCiAmt(final BigDecimal diskCiAmt) {
        this.diskCiAmt = diskCiAmt;
    }
    
    public BigDecimal getDiskEiAmt() {
        return this.diskEiAmt;
    }
    
    public void setDiskEiAmt(final BigDecimal diskEiAmt) {
        this.diskEiAmt = diskEiAmt;
    }
    
    public BigDecimal getDiskVlAmt() {
        return this.diskVlAmt;
    }
    
    public void setDiskVlAmt(final BigDecimal diskVlAmt) {
        this.diskVlAmt = diskVlAmt;
    }
    
    public BigDecimal getEsiAmt() {
        return this.esiAmt;
    }
    
    public void setEsiAmt(final BigDecimal esiAmt) {
        this.esiAmt = esiAmt;
    }
    
    public BigDecimal getDiskEsiAmt() {
        return this.diskEsiAmt;
    }
    
    public void setDiskEsiAmt(final BigDecimal diskEsiAmt) {
        this.diskEsiAmt = diskEsiAmt;
    }
    
    @Override
    public String toString() {
        return "SmsRequestDto{billSList=" + this.billSList + ", smr=" + this.smr + ", siAmt=" + this.siAmt + ", ciAmt=" + this.ciAmt + ", eiAmt=" + this.eiAmt + ", esiAmt=" + this.esiAmt + ", vlAmt=" + this.vlAmt + ", operationType=" + this.operationType + ", diskSiAmt=" + this.diskSiAmt + ", diskCiAmt=" + this.diskCiAmt + ", diskEiAmt=" + this.diskEiAmt + ", diskEsiAmt=" + this.diskEsiAmt + ", diskVlAmt=" + this.diskVlAmt + ", isOil='" + this.isOil + '\'' + '}';
    }
}
