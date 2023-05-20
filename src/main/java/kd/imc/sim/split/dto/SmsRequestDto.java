package kd.imc.sim.split.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class SmsRequestDto implements Serializable {

  private List<BillSubjectDto> billSList; // 单据列表

  private SmruleConfigDto smr; // 规则配置

  // 纸质专票拆分金额
  private BigDecimal siAmt;

  // 纸质普票拆分金额
  private BigDecimal ciAmt;
  
  // 电子普票拆分金额
  private BigDecimal eiAmt;

  // 电子专票拆分金额
  private BigDecimal esiAmt;

  // 卷票拆分金额
  private BigDecimal vlAmt;

  private int operationType = 0; // 操作类型：没用上

  private BigDecimal diskSiAmt; // 纸质专票税盘限额

  private BigDecimal diskCiAmt; // 纸质普票税盘限额

  private BigDecimal diskEiAmt; // 电子普票税盘限额

  private BigDecimal diskEsiAmt; // 电子专票税盘限额

  private BigDecimal diskVlAmt; // 卷票税盘限额

  private String isOil = "0"; // 是否为油票

  public List<BillSubjectDto> getBillSList() {
    return this.billSList;
  }

  public void setBillSList(List<BillSubjectDto> billSList) {
    this.billSList = billSList;
  }

  public SmruleConfigDto getSmr() {
    return this.smr;
  }

  public void setSmr(SmruleConfigDto smr) {
    this.smr = smr;
  }

  public BigDecimal getSiAmt() {
    return this.siAmt;
  }

  public void setSiAmt(BigDecimal siAmt) {
    this.siAmt = siAmt;
  }

  public BigDecimal getCiAmt() {
    return this.ciAmt;
  }

  public void setCiAmt(BigDecimal ciAmt) {
    this.ciAmt = ciAmt;
  }

  public BigDecimal getEiAmt() {
    return this.eiAmt;
  }

  public void setEiAmt(BigDecimal eiAmt) {
    this.eiAmt = eiAmt;
  }

  public BigDecimal getVlAmt() {
    return this.vlAmt;
  }

  public void setVlAmt(BigDecimal vlAmt) {
    this.vlAmt = vlAmt;
  }

  public int getOperationType() {
    return this.operationType;
  }

  public void setOperationType(int operationType) {
    this.operationType = operationType;
  }

  public String getIsOil() {
    return this.isOil;
  }

  public void setIsOil(String isOil) {
    this.isOil = isOil;
  }

  public BigDecimal getDiskSiAmt() {
    return this.diskSiAmt;
  }

  public void setDiskSiAmt(BigDecimal diskSiAmt) {
    this.diskSiAmt = diskSiAmt;
  }

  public BigDecimal getDiskCiAmt() {
    return this.diskCiAmt;
  }

  public void setDiskCiAmt(BigDecimal diskCiAmt) {
    this.diskCiAmt = diskCiAmt;
  }

  public BigDecimal getDiskEiAmt() {
    return this.diskEiAmt;
  }

  public void setDiskEiAmt(BigDecimal diskEiAmt) {
    this.diskEiAmt = diskEiAmt;
  }

  public BigDecimal getDiskVlAmt() {
    return this.diskVlAmt;
  }

  public void setDiskVlAmt(BigDecimal diskVlAmt) {
    this.diskVlAmt = diskVlAmt;
  }

  public BigDecimal getEsiAmt() {
    return this.esiAmt;
  }

  public void setEsiAmt(BigDecimal esiAmt) {
    this.esiAmt = esiAmt;
  }

  public BigDecimal getDiskEsiAmt() {
    return this.diskEsiAmt;
  }

  public void setDiskEsiAmt(BigDecimal diskEsiAmt) {
    this.diskEsiAmt = diskEsiAmt;
  }

  public String toString() {
    return "SmsRequestDto{billSList=" + this.billSList + ", smr=" + this.smr + ", siAmt=" + this.siAmt + ", ciAmt="
        + this.ciAmt + ", eiAmt=" + this.eiAmt + ", esiAmt=" + this.esiAmt + ", vlAmt=" + this.vlAmt
        + ", operationType=" + this.operationType + ", diskSiAmt=" + this.diskSiAmt + ", diskCiAmt=" + this.diskCiAmt
        + ", diskEiAmt=" + this.diskEiAmt + ", diskEsiAmt=" + this.diskEsiAmt + ", diskVlAmt=" + this.diskVlAmt
        + ", isOil='" + this.isOil + '\'' + '}';
  }
}