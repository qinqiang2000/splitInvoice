package kd.imc.sim.split.dto;

import java.io.Serializable;
import java.math.BigDecimal;

// 拆分/合并规则
public class SmruleConfigDto implements Serializable {

    private int mergeGoodsLine = 0;

    /**
     * 合并超额误差
     */
    private int mergeSupperError;

    /**
     * 合并订单误差
     */
    private int mergeOrderByError;

    /**
     * 列表类型
     */
    private int listType = 0;

    /**
     * 金额数字类型
     */
    private int amtNumberType = 2;

    /**
     * 金额数字
     */
    private int amtNumber = 15;

    /**
     * 价格数字类型
     */
    private int priceNumberType = 2;

    /**
     * 价格数字
     */
    private int priceNumber = 15;

    /**
     * 自动调整误差
     */
    private int autoAdjustError = 0;

    /**
     * 调整常规优先
     */
    private int adjustCommonFirst;

    /**
     * 发票使用行备注
     */
    private int invNoteUseLineNote = 0;

    /**
     * MCC备注字符串
     */
    private String mccNoteStr = ",";

    /**
     * MCC重复
     */
    private boolean mccRepeat = true;

    /**
     * 拆分商品行
     */
    private int splitGoodsLine = 0;

    /**
     * 全额税率
     */
    private BigDecimal fullRate = new BigDecimal("0.95");

    /**
     * 账单合并超限
     */
    private int billMergeSupperLimit;

    /**
     * 超额误差拆分
     */
    private int supperErrorSplit;

    /**
     * 商品分类拆分
     */
    private int goodsClassSplit = 0;

    /**
     * 启用红字发票
     */
    private int enableRedInv;

    /**
     * 平均最后一张发票
     */
    private int averageLastInv;

    /**
     * 允许抵扣
     */
    private int allowsAgainst = 1;

    /**
     * 抵扣类型
     */
    private int againstTyp = 0;

    /**
     * 备注显示页
     */
    private int noteShowPage = 0;

    /**
     * 最大销售行
     */
    private int maxSLine = 1999;

    /**
     * 最大成本行
     */
    private int maxCLine = 1999;

    /**
     * 最大费用行
     */
    private int maxELine = 1999;

    /**
     * 最大费用明细行
     */
    private int maxEsiLine = 1999;

    /**
     * 拆分列表类型
     */
    private int splitListType = 1;

    /**
     * MCC
     */
    private boolean mcc = true;

    /**
     * MCC备注
     */
    private boolean mccNote;

    /**
     * 发票限额金额
     */
    private BigDecimal invLimitAmt;

    /**
     * 发票磁盘限额金额
     */
    private BigDecimal invDiskLimitAmt;

    /**
     * 最终限额金额
     */
    private BigDecimal finalLimitAmt;

    /**
     * 磁盘类型
     */
    private String diskType = "0";

    /**
     * 行金额误差
     */
    private BigDecimal lineAmountErr = new BigDecimal("0.01");

    /**
     * 行税额误差
     */
    private BigDecimal lineTaxAmtErr = new BigDecimal("0.06");

    /**
     * 发票税额总和误差
     */
    private BigDecimal invTaxAmtSumErr = new BigDecimal("1.27");

    /**
     * 拆分商品行时的拆分方式
     * 1调整金额（弃用） 2 单价不变调整数量 3 总数量不变调整单价
     */
    private int splitGoodsWithNumber = 2;

    /**
     * 总税额计数
     */
    private int totalTaxamtCount = 1;

    /**
     * 是否已拆分
     */
    private int splited = 1;

    /**
     * 备注拆分数量
     */
    private int remarkSplitNum = 0;

    /**
     * 生效范围
     */
    private int effectiveRange = 2;

    public int getMergeGoodsLine() {
        return this.mergeGoodsLine;
    }

    public void setMergeGoodsLine(int mergeGoodsLine) {
        this.mergeGoodsLine = mergeGoodsLine;
    }

    public int getMergeSupperError() {
        return this.mergeSupperError;
    }

    public void setMergeSupperError(int mergeSupperError) {
        this.mergeSupperError = mergeSupperError;
    }

    public int getMergeOrderByError() {
        return this.mergeOrderByError;
    }

    public void setMergeOrderByError(int mergeOrderByError) {
        this.mergeOrderByError = mergeOrderByError;
    }

    public int getListType() {
        return this.listType;
    }

    public void setListType(int listType) {
        this.listType = listType;
    }

    public int getAmtNumberType() {
        return this.amtNumberType;
    }

    public void setAmtNumberType(int amtNumberType) {
        this.amtNumberType = amtNumberType;
    }

    public int getAmtNumber() {
        return this.amtNumber;
    }

    public void setAmtNumber(int amtNumber) {
        this.amtNumber = amtNumber;
    }

    public int getPriceNumberType() {
        return this.priceNumberType;
    }

    public void setPriceNumberType(int priceNumberType) {
        this.priceNumberType = priceNumberType;
    }

    public int getPriceNumber() {
        return this.priceNumber;
    }

    public void setPriceNumber(int priceNumber) {
        this.priceNumber = priceNumber;
    }

    public int getAutoAdjustError() {
        return this.autoAdjustError;
    }

    public void setAutoAdjustError(int autoAdjustError) {
        this.autoAdjustError = autoAdjustError;
    }

    public int getInvNoteUseLineNote() {
        return this.invNoteUseLineNote;
    }

    public void setInvNoteUseLineNote(int invNoteUseLineNote) {
        this.invNoteUseLineNote = invNoteUseLineNote;
    }

    public String getMccNoteStr() {
        return this.mccNoteStr;
    }

    public void setMccNoteStr(String mccNoteStr) {
        this.mccNoteStr = mccNoteStr;
    }

    public boolean isMccRepeat() {
        return this.mccRepeat;
    }

    public void setMccRepeat(boolean mccRepeat) {
        this.mccRepeat = mccRepeat;
    }

    public int getSplitGoodsLine() {
        return this.splitGoodsLine;
    }

    public void setSplitGoodsLine(int splitGoodsLine) {
        this.splitGoodsLine = splitGoodsLine;
    }

    public int getBillMergeSupperLimit() {
        return this.billMergeSupperLimit;
    }

    public void setBillMergeSupperLimit(int billMergeSupperLimit) {
        this.billMergeSupperLimit = billMergeSupperLimit;
    }

    public int getSupperErrorSplit() {
        return this.supperErrorSplit;
    }

    public void setSupperErrorSplit(int supperErrorSplit) {
        this.supperErrorSplit = supperErrorSplit;
    }

    public int getGoodsClassSplit() {
        return this.goodsClassSplit;
    }

    public void setGoodsClassSplit(int goodsClassSplit) {
        this.goodsClassSplit = goodsClassSplit;
    }

    public int getEnableRedInv() {
        return this.enableRedInv;
    }

    public void setEnableRedInv(int enableRedInv) {
        this.enableRedInv = enableRedInv;
    }

    public int getAllowsAgainst() {
        return this.allowsAgainst;
    }

    public void setAllowsAgainst(int allowsAgainst) {
        this.allowsAgainst = allowsAgainst;
    }

    public int getAgainstTyp() {
        return this.againstTyp;
    }

    public void setAgainstTyp(int againstTyp) {
        this.againstTyp = againstTyp;
    }

    public int getNoteShowPage() {
        return this.noteShowPage;
    }

    public void setNoteShowPage(int noteShowPage) {
        this.noteShowPage = noteShowPage;
    }

    public int getMaxSLine() {
        return this.maxSLine;
    }

    public void setMaxSLine(int maxSLine) {
        this.maxSLine = maxSLine;
    }

    public int getMaxCLine() {
        return this.maxCLine;
    }

    public void setMaxCLine(int maxCLine) {
        this.maxCLine = maxCLine;
    }

    public int getMaxELine() {
        return this.maxELine;
    }

    public void setMaxELine(int maxELine) {
        this.maxELine = maxELine;
    }

    public int getSplitListType() {
        return this.splitListType;
    }

    public void setSplitListType(int splitListType) {
        this.splitListType = splitListType;
    }

    public boolean isMcc() {
        return this.mcc;
    }

    public void setMcc(boolean mcc) {
        this.mcc = mcc;
    }

    public boolean isMccNote() {
        return this.mccNote;
    }

    public void setMccNote(boolean mccNote) {
        this.mccNote = mccNote;
    }

    public int getAdjustCommonFirst() {
        return this.adjustCommonFirst;
    }

    public void setAdjustCommonFirst(int adjustCommonFirst) {
        this.adjustCommonFirst = adjustCommonFirst;
    }

    public int getAverageLastInv() {
        return this.averageLastInv;
    }

    public void setAverageLastInv(int averageLastInv) {
        this.averageLastInv = averageLastInv;
    }

    public BigDecimal getInvLimitAmt() {
        return this.invLimitAmt;
    }

    public void setInvLimitAmt(BigDecimal invLimitAmt) {
        this.invLimitAmt = invLimitAmt;
    }

    public BigDecimal getInvDiskLimitAmt() {
        return this.invDiskLimitAmt;
    }

    public void setInvDiskLimitAmt(BigDecimal invDiskLimitAmt) {
        this.invDiskLimitAmt = invDiskLimitAmt;
    }

    public BigDecimal getFinalLimitAmt() {
        return this.finalLimitAmt;
    }

    public void setFinalLimitAmt(BigDecimal finalLimitAmt) {
        this.finalLimitAmt = finalLimitAmt;
    }

    public BigDecimal getLineAmountErr() {
        return this.lineAmountErr;
    }

    public void setLineAmountErr(BigDecimal lineAmountErr) {
        this.lineAmountErr = lineAmountErr;
    }

    public BigDecimal getLineTaxAmtErr() {
        return this.lineTaxAmtErr;
    }

    public void setLineTaxAmtErr(BigDecimal lineTaxAmtErr) {
        this.lineTaxAmtErr = lineTaxAmtErr;
    }

    public String getDiskType() {
        return this.diskType;
    }

    public void setDiskType(String diskType) {
        this.diskType = diskType;
    }

    public BigDecimal getInvTaxAmtSumErr() {
        return this.invTaxAmtSumErr;
    }

    public void setInvTaxAmtSumErr(BigDecimal invTaxAmtSumErr) {
        this.invTaxAmtSumErr = invTaxAmtSumErr;
    }

    public BigDecimal getFullRate() {
        return this.fullRate;
    }

    public void setFullRate(BigDecimal fullRate) {
        this.fullRate = fullRate;
    }

    public int getSplitGoodsWithNumber() {
        return this.splitGoodsWithNumber;
    }

    public int getTotalTaxamtCount() {
        return this.totalTaxamtCount;
    }

    public void setSplitGoodsWithNumber(int splitGoodsWithNumber) {
        this.splitGoodsWithNumber = splitGoodsWithNumber;
    }

    public void setTotalTaxamtCount(int totalTaxamtCount) {
        this.totalTaxamtCount = totalTaxamtCount;
    }

    public int getSplited() {
        return this.splited;
    }

    public void setSplited(int splited) {
        this.splited = splited;
    }

    public int getRemarkSplitNum() {
        return this.remarkSplitNum;
    }

    public void setRemarkSplitNum(int remarkSplitNum) {
        this.remarkSplitNum = remarkSplitNum;
    }

    public int getMaxEsiLine() {
        return this.maxEsiLine;
    }

    public void setMaxEsiLine(int maxEsiLine) {
        this.maxEsiLine = maxEsiLine;
    }

    public int getEffectiveRange() {
        return this.effectiveRange;
    }

    public void setEffectiveRange(int effectiveRange) {
        this.effectiveRange = effectiveRange;
    }

    public String toString() {
        return "SmruleConfigDto{mergeGoodsLine=" + this.mergeGoodsLine + ", mergeSupperError=" + this.mergeSupperError + ", mergeOrderByError=" + this.mergeOrderByError + ", listType=" + this.listType + ", amtNumberType=" + this.amtNumberType + ", amtNumber=" + this.amtNumber + ", priceNumberType=" + this.priceNumberType + ", priceNumber=" + this.priceNumber + ", autoAdjustError=" + this.autoAdjustError + ", adjustCommonFirst=" + this.adjustCommonFirst + ", invNoteUseLineNote=" + this.invNoteUseLineNote + ", mccNoteStr='" + this.mccNoteStr + '\'' + ", mccRepeat=" + this.mccRepeat + ", splitGoodsLine=" + this.splitGoodsLine + ", fullRate=" + this.fullRate + ", billMergeSupperLimit=" + this.billMergeSupperLimit + ", supperErrorSplit=" + this.supperErrorSplit + ", goodsClassSplit=" + this.goodsClassSplit + ", enableRedInv=" + this.enableRedInv + ", averageLastInv=" + this.averageLastInv + ", allowsAgainst=" + this.allowsAgainst + ", againstTyp=" + this.againstTyp + ", noteShowPage=" + this.noteShowPage + ", maxSLine=" + this.maxSLine + ", maxCLine=" + this.maxCLine + ", maxELine=" + this.maxELine + ", maxEsiLine=" + this.maxEsiLine + ", splitListType=" + this.splitListType + ", mcc=" + this.mcc + ", mccNote=" + this.mccNote + ", invLimitAmt=" + this.invLimitAmt + ", invDiskLimitAmt=" + this.invDiskLimitAmt + ", diskType='" + this.diskType + '\'' + ", lineAmountErr=" + this.lineAmountErr + ", lineTaxAmtErr=" + this.lineTaxAmtErr + ", invTaxAmtSumErr=" + this.invTaxAmtSumErr + ", splitGoodsWithNumber=" + this.splitGoodsWithNumber + ", totalTaxamtCount=" + this.totalTaxamtCount + ", splited=" + this.splited + '}';
    }
}