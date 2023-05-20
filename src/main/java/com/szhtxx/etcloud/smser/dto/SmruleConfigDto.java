package com.szhtxx.etcloud.smser.dto;

import java.io.*;
import java.math.*;
import java.util.*;

public class SmruleConfigDto implements Serializable
{
    private static final long serialVersionUID = 1L;
    // 合并商品行
    private int mergeGoodsLine;
    // 合并超额错误
    private int mergeSupperError;
    // 合并订单错误
    private int mergeOrderByError;
    // 列表类型
    private int listType;
    // 金额数字类型
    private int amtNumberType;
    // 金额数字
    private int amtNumber;
    // 价格数字类型
    private int priceNumberType;
    // 价格数字
    private int priceNumber;
    // 自动调整错误
    private int autoAdjustError;
    // 调整常规错误
    private int adjustCommonFirst;
    // 发票使用行备注
    private int invNoteUseLineNote;
    // MCC备注字符串
    private String mccNoteStr;
    // MCC重复
    private boolean mccRepeat;
    // 拆分商品行
    private int splitGoodsLine;
    // 全额税率
    private BigDecimal fullRate;
    // 发票合并超限
    private int billMergeSupperLimit;
    // 超额错误拆分
    private int supperErrorSplit;
    // 商品分类拆分
    private int goodsClassSplit;
    // 启用红字发票
    private int enableRedInv;
    // 平均最后一张发票
    private int averageLastInv;
    // 允许抵扣
    private int allowsAgainst;
    // 抵扣类型
    private int againstTyp;
    // 备注显示页
    private int noteShowPage;
    // 最大销售行数
    private int maxSLine;
    // 最大采购行数
    private int maxCLine;
    // 最大费用行数
    private int maxELine;
    // 最大ESI行数
    private int maxEsiLine;
    // 拆分列表类型
    private int splitListType;
    // MCC
    private boolean mcc;
    // MCC备注
    private boolean mccNote;
    // 发票限额金额
    private BigDecimal invLimitAmt;
    // 发票磁盘限额金额
    private BigDecimal invDiskLimitAmt;
    // 最终限额金额
    private BigDecimal finalLimitAmt;
    // 磁盘类型
    private String diskType;
    // 行金额错误
    private BigDecimal lineAmountErr;
    // 行税额错误
    private BigDecimal lineTaxAmtErr;
    // 发票税额总和错误
    private BigDecimal invTaxAmtSumErr;
    // 拆分商品行数
    private int splitGoodsWithNumber;
    // 总税额计数
    private int totalTaxamtCount;
    // 已拆分
    private int splited;
    // 备注拆分数
    private int remarkSplitNum;
    // 有效范围
    private int effectiveRange;
    // 拆分金额
    private List<BigDecimal> splitAmounts;
    
    public SmruleConfigDto() {
        this.mergeGoodsLine = 0;
        this.listType = 0;
        this.amtNumberType = 2;
        this.amtNumber = 15;
        this.priceNumberType = 2;
        this.priceNumber = 15;
        this.autoAdjustError = 0;
        this.invNoteUseLineNote = 0;
        this.mccNoteStr = ",";
        this.mccRepeat = true;
        this.splitGoodsLine = 0;
        this.fullRate = new BigDecimal("0.95");
        this.goodsClassSplit = 0;
        this.allowsAgainst = 1;
        this.againstTyp = 0;
        this.noteShowPage = 0;
        this.maxSLine = 1999;
        this.maxCLine = 1999;
        this.maxELine = 1999;
        this.maxEsiLine = 1999;
        this.splitListType = 1;
        this.mcc = true;
        this.diskType = "0";
        this.lineAmountErr = new BigDecimal("0.01");
        this.lineTaxAmtErr = new BigDecimal("0.06");
        this.invTaxAmtSumErr = new BigDecimal("1.27");
        this.splitGoodsWithNumber = 2;
        this.totalTaxamtCount = 1;
        this.splited = 1;
        this.remarkSplitNum = 0;
        this.effectiveRange = 2;
    }
    
    public List<BigDecimal> getSplitAmounts() {
        return this.splitAmounts;
    }
    
    public void setSplitAmounts(final List<BigDecimal> splitAmounts) {
        this.splitAmounts = splitAmounts;
    }
    
    public int getMergeGoodsLine() {
        return this.mergeGoodsLine;
    }
    
    public void setMergeGoodsLine(final int mergeGoodsLine) {
        this.mergeGoodsLine = mergeGoodsLine;
    }
    
    public int getMergeSupperError() {
        return this.mergeSupperError;
    }
    
    public void setMergeSupperError(final int mergeSupperError) {
        this.mergeSupperError = mergeSupperError;
    }
    
    public int getMergeOrderByError() {
        return this.mergeOrderByError;
    }
    
    public void setMergeOrderByError(final int mergeOrderByError) {
        this.mergeOrderByError = mergeOrderByError;
    }
    
    public int getListType() {
        return this.listType;
    }
    
    public void setListType(final int listType) {
        this.listType = listType;
    }
    
    public int getAmtNumberType() {
        return this.amtNumberType;
    }
    
    public void setAmtNumberType(final int amtNumberType) {
        this.amtNumberType = amtNumberType;
    }
    
    public int getAmtNumber() {
        return this.amtNumber;
    }
    
    public void setAmtNumber(final int amtNumber) {
        this.amtNumber = amtNumber;
    }
    
    public int getPriceNumberType() {
        return this.priceNumberType;
    }
    
    public void setPriceNumberType(final int priceNumberType) {
        this.priceNumberType = priceNumberType;
    }
    
    public int getPriceNumber() {
        return this.priceNumber;
    }
    
    public void setPriceNumber(final int priceNumber) {
        this.priceNumber = priceNumber;
    }
    
    public int getAutoAdjustError() {
        return this.autoAdjustError;
    }
    
    public void setAutoAdjustError(final int autoAdjustError) {
        this.autoAdjustError = autoAdjustError;
    }
    
    public int getInvNoteUseLineNote() {
        return this.invNoteUseLineNote;
    }
    
    public void setInvNoteUseLineNote(final int invNoteUseLineNote) {
        this.invNoteUseLineNote = invNoteUseLineNote;
    }
    
    public String getMccNoteStr() {
        return this.mccNoteStr;
    }
    
    public void setMccNoteStr(final String mccNoteStr) {
        this.mccNoteStr = mccNoteStr;
    }
    
    public boolean isMccRepeat() {
        return this.mccRepeat;
    }
    
    public void setMccRepeat(final boolean mccRepeat) {
        this.mccRepeat = mccRepeat;
    }
    
    public int getSplitGoodsLine() {
        return this.splitGoodsLine;
    }
    
    public void setSplitGoodsLine(final int splitGoodsLine) {
        this.splitGoodsLine = splitGoodsLine;
    }
    
    public int getBillMergeSupperLimit() {
        return this.billMergeSupperLimit;
    }
    
    public void setBillMergeSupperLimit(final int billMergeSupperLimit) {
        this.billMergeSupperLimit = billMergeSupperLimit;
    }
    
    public int getSupperErrorSplit() {
        return this.supperErrorSplit;
    }
    
    public void setSupperErrorSplit(final int supperErrorSplit) {
        this.supperErrorSplit = supperErrorSplit;
    }
    
    public int getGoodsClassSplit() {
        return this.goodsClassSplit;
    }
    
    public void setGoodsClassSplit(final int goodsClassSplit) {
        this.goodsClassSplit = goodsClassSplit;
    }
    
    public int getEnableRedInv() {
        return this.enableRedInv;
    }
    
    public void setEnableRedInv(final int enableRedInv) {
        this.enableRedInv = enableRedInv;
    }
    
    public int getAllowsAgainst() {
        return this.allowsAgainst;
    }
    
    public void setAllowsAgainst(final int allowsAgainst) {
        this.allowsAgainst = allowsAgainst;
    }
    
    public int getAgainstTyp() {
        return this.againstTyp;
    }
    
    public void setAgainstTyp(final int againstTyp) {
        this.againstTyp = againstTyp;
    }
    
    public int getNoteShowPage() {
        return this.noteShowPage;
    }
    
    public void setNoteShowPage(final int noteShowPage) {
        this.noteShowPage = noteShowPage;
    }
    
    public int getMaxSLine() {
        return this.maxSLine;
    }
    
    public void setMaxSLine(final int maxSLine) {
        this.maxSLine = maxSLine;
    }
    
    public int getMaxCLine() {
        return this.maxCLine;
    }
    
    public void setMaxCLine(final int maxCLine) {
        this.maxCLine = maxCLine;
    }
    
    public int getMaxELine() {
        return this.maxELine;
    }
    
    public void setMaxELine(final int maxELine) {
        this.maxELine = maxELine;
    }
    
    public int getSplitListType() {
        return this.splitListType;
    }
    
    public void setSplitListType(final int splitListType) {
        this.splitListType = splitListType;
    }
    
    public boolean isMcc() {
        return this.mcc;
    }
    
    public void setMcc(final boolean mcc) {
        this.mcc = mcc;
    }
    
    public boolean isMccNote() {
        return this.mccNote;
    }
    
    public void setMccNote(final boolean mccNote) {
        this.mccNote = mccNote;
    }
    
    public int getAdjustCommonFirst() {
        return this.adjustCommonFirst;
    }
    
    public void setAdjustCommonFirst(final int adjustCommonFirst) {
        this.adjustCommonFirst = adjustCommonFirst;
    }
    
    public int getAverageLastInv() {
        return this.averageLastInv;
    }
    
    public void setAverageLastInv(final int averageLastInv) {
        this.averageLastInv = averageLastInv;
    }
    
    public BigDecimal getInvLimitAmt() {
        return this.invLimitAmt;
    }
    
    public void setInvLimitAmt(final BigDecimal invLimitAmt) {
        this.invLimitAmt = invLimitAmt;
    }
    
    public BigDecimal getInvDiskLimitAmt() {
        return this.invDiskLimitAmt;
    }
    
    public void setInvDiskLimitAmt(final BigDecimal invDiskLimitAmt) {
        this.invDiskLimitAmt = invDiskLimitAmt;
    }
    
    public BigDecimal getFinalLimitAmt() {
        return this.finalLimitAmt;
    }
    
    public void setFinalLimitAmt(final BigDecimal finalLimitAmt) {
        this.finalLimitAmt = finalLimitAmt;
    }
    
    public BigDecimal getLineAmountErr() {
        return this.lineAmountErr;
    }
    
    public void setLineAmountErr(final BigDecimal lineAmountErr) {
        this.lineAmountErr = lineAmountErr;
    }
    
    public BigDecimal getLineTaxAmtErr() {
        return this.lineTaxAmtErr;
    }
    
    public void setLineTaxAmtErr(final BigDecimal lineTaxAmtErr) {
        this.lineTaxAmtErr = lineTaxAmtErr;
    }
    
    public String getDiskType() {
        return this.diskType;
    }
    
    public void setDiskType(final String diskType) {
        this.diskType = diskType;
    }
    
    public BigDecimal getInvTaxAmtSumErr() {
        return this.invTaxAmtSumErr;
    }
    
    public void setInvTaxAmtSumErr(final BigDecimal invTaxAmtSumErr) {
        this.invTaxAmtSumErr = invTaxAmtSumErr;
    }
    
    public BigDecimal getFullRate() {
        return this.fullRate;
    }
    
    public void setFullRate(final BigDecimal fullRate) {
        this.fullRate = fullRate;
    }
    
    public int getSplitGoodsWithNumber() {
        return this.splitGoodsWithNumber;
    }
    
    public int getTotalTaxamtCount() {
        return this.totalTaxamtCount;
    }
    
    public void setSplitGoodsWithNumber(final int splitGoodsWithNumber) {
        this.splitGoodsWithNumber = splitGoodsWithNumber;
    }
    
    public void setTotalTaxamtCount(final int totalTaxamtCount) {
        this.totalTaxamtCount = totalTaxamtCount;
    }
    
    public int getSplited() {
        return this.splited;
    }
    
    public void setSplited(final int splited) {
        this.splited = splited;
    }
    
    public int getRemarkSplitNum() {
        return this.remarkSplitNum;
    }
    
    public void setRemarkSplitNum(final int remarkSplitNum) {
        this.remarkSplitNum = remarkSplitNum;
    }
    
    public int getMaxEsiLine() {
        return this.maxEsiLine;
    }
    
    public void setMaxEsiLine(final int maxEsiLine) {
        this.maxEsiLine = maxEsiLine;
    }
    
    public int getEffectiveRange() {
        return this.effectiveRange;
    }
    
    public void setEffectiveRange(final int effectiveRange) {
        this.effectiveRange = effectiveRange;
    }
    
    @Override
    public String toString() {
        return "SmruleConfigDto{mergeGoodsLine=" + this.mergeGoodsLine + ", mergeSupperError=" + this.mergeSupperError + ", mergeOrderByError=" + this.mergeOrderByError + ", listType=" + this.listType + ", amtNumberType=" + this.amtNumberType + ", amtNumber=" + this.amtNumber + ", priceNumberType=" + this.priceNumberType + ", priceNumber=" + this.priceNumber + ", autoAdjustError=" + this.autoAdjustError + ", adjustCommonFirst=" + this.adjustCommonFirst + ", invNoteUseLineNote=" + this.invNoteUseLineNote + ", mccNoteStr='" + this.mccNoteStr + '\'' + ", mccRepeat=" + this.mccRepeat + ", splitGoodsLine=" + this.splitGoodsLine + ", fullRate=" + this.fullRate + ", billMergeSupperLimit=" + this.billMergeSupperLimit + ", supperErrorSplit=" + this.supperErrorSplit + ", goodsClassSplit=" + this.goodsClassSplit + ", enableRedInv=" + this.enableRedInv + ", averageLastInv=" + this.averageLastInv + ", allowsAgainst=" + this.allowsAgainst + ", againstTyp=" + this.againstTyp + ", noteShowPage=" + this.noteShowPage + ", maxSLine=" + this.maxSLine + ", maxCLine=" + this.maxCLine + ", maxELine=" + this.maxELine + ", maxEsiLine=" + this.maxEsiLine + ", splitListType=" + this.splitListType + ", mcc=" + this.mcc + ", mccNote=" + this.mccNote + ", invLimitAmt=" + this.invLimitAmt + ", invDiskLimitAmt=" + this.invDiskLimitAmt + ", diskType='" + this.diskType + '\'' + ", lineAmountErr=" + this.lineAmountErr + ", lineTaxAmtErr=" + this.lineTaxAmtErr + ", invTaxAmtSumErr=" + this.invTaxAmtSumErr + ", splitGoodsWithNumber=" + this.splitGoodsWithNumber + ", totalTaxamtCount=" + this.totalTaxamtCount + ", splited=" + this.splited + '}';
    }
}
