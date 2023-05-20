package kd.imc.sim.split.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class BillDetailIdDto implements Serializable {

    private String billNO;

    private String billDetailNO;

    private BigDecimal amounts;

    private BigDecimal price;

    private BigDecimal amts;

    public BillDetailIdDto(String billNO, String billDetailNO, BigDecimal amounts, BigDecimal price, BigDecimal amts) {
        this.billNO = billNO;
        this.billDetailNO = billDetailNO;
        this.amounts = amounts;
        this.price = price;
        this.amts = amts;
    }

    public String getBillNO() {
        return this.billNO;
    }

    public void setBillNO(String billNO) {
        this.billNO = billNO;
    }

    public String getBillDetailNO() {
        return this.billDetailNO;
    }

    public void setBillDetailNO(String billDetailNO) {
        this.billDetailNO = billDetailNO;
    }

    public BigDecimal getAmounts() {
        return this.amounts;
    }

    public void setAmounts(BigDecimal amounts) {
        this.amounts = amounts;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public BigDecimal getAmts() {
        return this.amts;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setAmts(BigDecimal amts) {
        this.amts = amts;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof BillDetailIdDto)) {
            return false;
        } else {
            BillDetailIdDto dtailId = (BillDetailIdDto)obj;
            return this.getBillNO().equals(dtailId.getBillNO()) && this.getBillDetailNO().equals(dtailId.getBillDetailNO());
        }
    }

    public int hashCode() {
        return Objects.hash(this.billNO, this.billDetailNO);
    }
}