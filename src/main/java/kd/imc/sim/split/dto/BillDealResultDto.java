package kd.imc.sim.split.dto;

import java.io.Serializable;

public class BillDealResultDto implements Serializable {

    private String billNO;

    private boolean success;

    private String errorMsg;

    public String getBillNO() {
        return this.billNO;
    }

    public void setBillNO(String billNO) {
        this.billNO = billNO;
    }

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

    public String toString() {
        return "BillDealResultDto{billNO='" + this.billNO + '\'' + ", success=" + this.success + ", errorMsg='" + this.errorMsg + '\'' + '}';
    }
}