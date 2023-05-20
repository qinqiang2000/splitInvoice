package kd.imc.sim.split.exception;

public class EtcRuleException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private String msg;
    private int errorCode = -1;

    public EtcRuleException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}