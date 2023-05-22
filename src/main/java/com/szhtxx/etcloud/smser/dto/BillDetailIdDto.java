package com.szhtxx.etcloud.smser.dto;

import java.io.*;
import java.math.*;
import java.util.*;

public class BillDetailIdDto implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String billNO;
    private String billDetailNO;
    private BigDecimal amounts;
    private BigDecimal price;
    private BigDecimal amts;
    private BigDecimal taxAmt;

    public BillDetailIdDto(final String billNO, final String billDetailNO) {
        this.billNO = billNO;
        this.billDetailNO = billDetailNO;
    }

    public BillDetailIdDto(final String billNO, final String billDetailNO, final BigDecimal amounts, final BigDecimal price, final BigDecimal amts, final BigDecimal taxAmt) {
        this.billNO = billNO;
        this.billDetailNO = billDetailNO;
        this.amounts = amounts;
        this.price = price;
        this.amts = amts;
        this.taxAmt = taxAmt;
    }

    public String getBillNO() {
        return this.billNO;
    }

    public void setBillNO(final String billNO) {
        this.billNO = billNO;
    }

    public String getBillDetailNO() {
        return this.billDetailNO;
    }

    public void setBillDetailNO(final String billDetailNO) {
        this.billDetailNO = billDetailNO;
    }

    public BigDecimal getAmounts() {
        return this.amounts;
    }

    public void setAmounts(final BigDecimal amounts) {
        this.amounts = amounts;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public BigDecimal getAmts() {
        return this.amts;
    }

    public void setPrice(final BigDecimal price) {
        this.price = price;
    }

    public void setAmts(final BigDecimal amts) {
        this.amts = amts;
    }

    public BigDecimal getTaxAmt() {
        return this.taxAmt;
    }

    public void setTaxAmt(final BigDecimal taxAmt) {
        this.taxAmt = taxAmt;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BillDetailIdDto)) {
            return false;
        }
        final BillDetailIdDto dtailId = (BillDetailIdDto)obj;
        return this.getBillNO().equals(dtailId.getBillNO()) && this.getBillDetailNO().equals(dtailId.getBillDetailNO());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.billNO, this.billDetailNO);
    }
}
