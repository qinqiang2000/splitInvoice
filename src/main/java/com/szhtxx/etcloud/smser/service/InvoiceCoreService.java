package com.szhtxx.etcloud.smser.service;

import com.szhtxx.etcloud.smser.methods.smser.*;
import org.slf4j.*;
import com.alibaba.fastjson.*;
import com.google.common.util.concurrent.*;

import java.math.*;

import com.szhtxx.etcloud.smser.exception.*;

import java.lang.reflect.*;

import org.apache.commons.collections.*;
import com.szhtxx.etcloud.smser.enums.*;
import com.szhtxx.etcloud.smser.utils.*;
import com.szhtxx.etcloud.smser.dto.*;

import java.util.*;
import java.util.regex.*;
import java.util.concurrent.atomic.*;

public class InvoiceCoreService {
    private static Logger LOG;
    private static BackCalcUtilMethods calcUtilMethods;

    static {
        InvoiceCoreService.LOG = LoggerFactory.getLogger(InvoiceCoreService.class);
        InvoiceCoreService.calcUtilMethods = new BackCalcUtilMethods();
    }

    // 生成发票数据
    public static void openInvoice(BillSubjectDto billSubjectDto, SmruleConfigDto configDto, SmsResultDto smsResultDto) {
        final String billSubjectNo = billSubjectDto.getBillNO();
        InvoiceCoreService.LOG.debug("单据编号[{}]生成发票请求报文 单据:{}，规则配置:{}", billSubjectNo, JSONObject.toJSONString(billSubjectDto), JSONObject.toJSONString(configDto));
        final BigDecimal invLimitAmt = configDto.getFinalLimitAmt();
        final BigDecimal amountIncTax = billSubjectDto.getSumAmtJE().add(billSubjectDto.getSumAmtSE());
        final AtomicDouble remarkAmountSum = new AtomicDouble(0.0);

        // 检查单据金额是否超过发票限额的1000倍，超过则抛出异常
        if (amountIncTax.divide(invLimitAmt, 0, 2).compareTo(new BigDecimal("1000")) > 0) {
            InvoiceCoreService.LOG.info("单据编号[{}]生成发票请求报文 单据:{}，规则配置:{}", billSubjectNo, JSONObject.toJSONString(billSubjectDto), JSONObject.toJSONString(configDto));
            throw new EtcRuleException("单据金额超过发票限额的1000倍，请调整单据金额");
        }

        try {
            openInvoiceDetail(billSubjectDto, configDto, smsResultDto, remarkAmountSum);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            InvoiceCoreService.LOG.info(e.getMessage());
            throw new EtcRuleException("对象备份失败");
        }
    }

    /* 
     * 该方法首先获取单据编号、单据明细、发票限额等信息，
     * 然后对单据明细进行遍历，计算每个明细的金额，并检查是否超过发票限额。
     * 如果超过，则抛出异常。如果未超过，则将明细金额加入到总金额中，并记录备注金额总和。
     * 最后，将生成的发票请求报文详细信息存入smsResultDto中。
    */
    public static void openInvoiceDetail(BillSubjectDto billSubjectDto, SmruleConfigDto configDto, SmsResultDto smsResultDto, 
                                        AtomicDouble remarkAmountSum) throws IllegalAccessException, InvocationTargetException {
        String billSubjectNo = billSubjectDto.getBillNO();
        InvoiceCoreService.LOG.debug("单据编号[{}]生成发票请求报文 单据:{}，规则配置:{}", billSubjectNo, JSONObject.toJSONString(billSubjectDto), JSONObject.toJSONString(configDto));

        BigDecimal allAmount = billSubjectDto.getSumAmtJE();
        List<BigDecimal> splitAmounts = configDto.getSplitAmounts();
        BigDecimal amountSum = BigDecimal.ZERO;
        BigDecimal taxAmtErrSum = BigDecimal.ZERO;
        String remark = "";
        int lastIndex = -1;
        List<BillDetailDto> detailDtos = billSubjectDto.getBillDList();
        if (CollectionUtils.isEmpty(detailDtos))
            return;

        for (int size = detailDtos.size(), newLineNum = 0, i = 0; i < size + newLineNum; ++i) {
            BigDecimal invLimitAmt = configDto.getFinalLimitAmt();
            if (splitAmounts != null && !splitAmounts.isEmpty()) {
                invLimitAmt = splitAmounts.get(0);
            }
            BillDetailDto detailDto = detailDtos.get(i);

            // qqin: 反编译的代码，没看懂，先注释???
            // if ("1123885409810882572".equals(detailDto.getBillDetailNO())) {
            //     System.out.print(111);
            // }
            
            // 如果下一行是折扣行，则取出到disDto中
            int j = i + 1;
            BillDetailDto disDto = null;
            if (j < size && isDisLine(detailDtos.get(j))) {
                disDto = detailDtos.get(j);
            }

            String billNo = detailDto.getBillNO();
            BigDecimal lineAmount = detailDto.getAmounts();
            BigDecimal disLineAmount = lineAmount.add(BigDecimal.ZERO);
            if (disDto != null) {
                // 将商品行金额和折扣行(如果存在)金额相加，结果存入disLineAmount
                disLineAmount = disLineAmount.add(disDto.getAmounts());
            }
            // 商品行+折扣行的金额超限，做拆分
            if (disLineAmount.compareTo(invLimitAmt) > 0) {
                // 构造toSplitDetails，包含待拆分的商品行和折扣行
                int k = i;
                final List<BillDetailDto> toSplitDetails = new ArrayList<BillDetailDto>();
                toSplitDetails.add(detailDtos.get(k));
                if (j < size && isDisLine(detailDtos.get(j))) {
                    disDto = detailDtos.get(j);
                    toSplitDetails.add(disDto);
                    detailDtos.remove(j);
                    ++i;
                }
                
                detailDtos.remove(k);

                // 拆分
                List<BillDetailDto> splitGoods = InvoiceSplitCoreService.goodsLineSplit(toSplitDetails, configDto, amountSum, toSplitDetails.size() > 1, allAmount, splitAmounts);
                
                // 将拆分后的商品行插入到detailDtos中
                detailDtos.addAll(k, splitGoods);

                // 更新size，for循环用到
                size = detailDtos.size();
                //LOG.debug("单据编号[{}] 超过最大行数 行号[{}]开票,金额累计：{},税额误差累计:{}", billNo, i + 1, amountSum, taxAmtErrSum);

                remark = "金额累计超限开票";
                lastIndex = saveInvoince(smsResultDto, configDto, billSubjectDto, lastIndex, i, remark, detailDtos.get(0).getLineNote(), remarkAmountSum);
                if (splitAmounts != null && !splitAmounts.isEmpty()) {
                    splitAmounts.remove(0);
                }

                BigDecimal invoinceAmount = smsResultDto.getInvoiceSList().get(smsResultDto.getInvoiceSList().size() - 1).getAmounts();
                allAmount = allAmount.subtract(invoinceAmount);
                amountSum = BigDecimal.ZERO;
                taxAmtErrSum = BigDecimal.ZERO;
            }
            else {
                if (isLastLine(billSubjectDto, i)) { // 最后一行开票
                    InvoiceCoreService.LOG.debug("单据编号[{}] 最后一行[{}]开票,金额累计：{},税额误差累计:{}", new Object[]{billNo, i + 1, amountSum, taxAmtErrSum});
                    remark = "最后一行开票";
                    saveInvoince(smsResultDto, configDto, billSubjectDto, lastIndex, i, remark, detailDtos.get(0).getLineNote(), remarkAmountSum);
                    if (splitAmounts != null && !splitAmounts.isEmpty()) {
                        splitAmounts.remove(0);
                        break;
                    }
                    break;
                } 
                else if (isOpenInvIndex(billSubjectDto, i, lastIndex)) { // 超过最大行数开票
                    InvoiceCoreService.LOG.debug("单据编号[{}] 超过最大行数 行号[{}]开票,金额累计：{},税额误差累计:{}", new Object[]{billNo, i + 1, amountSum, taxAmtErrSum});
                    remark = "超过最大行数开票";
                    lastIndex = saveInvoince(smsResultDto, configDto, billSubjectDto, lastIndex, i, remark, detailDtos.get(0).getLineNote(), remarkAmountSum);
                    if (splitAmounts != null && !splitAmounts.isEmpty()) {
                        splitAmounts.remove(0);
                    }
                    final BigDecimal invoinceAmount2 = smsResultDto.getInvoiceSList().get(smsResultDto.getInvoiceSList().size() - 1).getAmounts();
                    allAmount = allAmount.subtract(invoinceAmount2);
                    amountSum = BigDecimal.ZERO;
                    taxAmtErrSum = BigDecimal.ZERO;
                } 
                else if (!isDisLine(detailDto)) { // 非折扣行处理
                    amountSum = amountSum.add(lineAmount);
                    if (disDto != null && isDisLine(disDto)) {
                        amountSum = amountSum.add(disDto.getAmounts());
                        BigDecimal disAmt = disDto.getAmounts();
                        BigDecimal disTaxRate = disDto.getTaxRate();
                        BigDecimal disTaxAmt = disDto.getTaxAmt();
                        BigDecimal taxAmtErr = disAmt.multiply(disTaxRate).subtract(disTaxAmt).setScale(2, RoundingMode.HALF_UP);
                        taxAmtErrSum = taxAmtErrSum.add(taxAmtErr);
                    }
                    // 如果当前累计金额小于限额，且加上下一商品行金额也小于限额，则处理splitGoodsLine配置的逻辑
                    if (amountSum.compareTo(invLimitAmt) < 0 && addNextProLineIsExceed(detailDtos, i, amountSum, invLimitAmt)) {
                        int nextIndex;
                        if (disDto != null) {
                            nextIndex = i + 2;
                        } else {
                            nextIndex = i + 1;
                        }
                        final BillDetailDto nextDetails = detailDtos.get(nextIndex);
                        BigDecimal nextLineAmount = nextDetails.getAmounts();
                        BillDetailDto nextDisDto = null;
                        if (nextIndex + 1 < size && detailDtos.get(nextIndex + 1) != null && isDisLine(detailDtos.get(nextIndex + 1))) {
                            nextDisDto = detailDtos.get(nextIndex + 1);
                            nextLineAmount = nextDisDto.getAmounts();
                        }
                        if ((configDto.getSplitGoodsLine() != 0 || nextLineAmount.compareTo(invLimitAmt) >= 0) && 1 != nextDetails.getSplitSign()) {
                            if (disDto != null) {
                                ++i;
                            }
                            final List<BillDetailDto> toSplitDetails2 = new ArrayList<BillDetailDto>();
                            toSplitDetails2.add(nextDetails);
                            if (nextDisDto != null) {
                                toSplitDetails2.add(nextDisDto);
                                detailDtos.remove(nextIndex + 1);
                                ++i;
                            }
                            detailDtos.remove(nextIndex);
                            ++i;
                            final int toSplitNum = toSplitDetails2.size();
                            final List<BillDetailDto> splitGoods2 = InvoiceSplitCoreService.goodsLineSplit(toSplitDetails2, configDto, amountSum, toSplitDetails2.size() > 1, allAmount, splitAmounts);
                            detailDtos.addAll(nextIndex, splitGoods2);
                            size = detailDtos.size();
                            if (splitGoods2.size() > toSplitNum) {
                                if (nextDisDto != null && !isDisLine(splitGoods2.get(1))) {
                                    --i;
                                }
                                InvoiceCoreService.LOG.debug("单据编号[{}] 超过最大行数 行号[{}]开票,金额累计：{},税额误差累计:{}", billNo, i + 1, amountSum, taxAmtErrSum);
                                remark = "金额累计超限开票";
                                lastIndex = saveInvoince(smsResultDto, configDto, billSubjectDto, lastIndex, i, remark, detailDtos.get(0).getLineNote(), remarkAmountSum);
                                if (splitAmounts != null && !splitAmounts.isEmpty()) {
                                    splitAmounts.remove(0);
                                }
                                BigDecimal invoiceAmount3 = smsResultDto.getInvoiceSList().get(smsResultDto.getInvoiceSList().size() - 1).getAmounts();
                                allAmount = allAmount.subtract(invoiceAmount3);
                                amountSum = BigDecimal.ZERO;
                                taxAmtErrSum = BigDecimal.ZERO;
                                continue;
                            }
                            if (disDto != null) {
                                --i;
                            }
                            --i;
                        }
                    }

                    // 如果当前累加金额等于限额，或加上下一商品行金额超过限额，则生成发票，amountSum置零
                    if (amountSum.compareTo(invLimitAmt) == 0 || addNextProLineIsExceed(detailDtos, i, amountSum, invLimitAmt)) {
                        InvoiceCoreService.LOG.debug("单据编号[{}] 金额累计超限 行号[{}]开票,金额累计：{},税额误差累计:{}", billNo, i + 1, amountSum, taxAmtErrSum);
                        remark = "金额累计超限开票";
                        lastIndex = saveInvoince(smsResultDto, configDto, billSubjectDto, lastIndex, i, remark, detailDtos.get(0).getLineNote(), remarkAmountSum);
                        if (splitAmounts != null && !splitAmounts.isEmpty()) {
                            splitAmounts.remove(0);
                        }
                        BigDecimal invoinceAmount2 = smsResultDto.getInvoiceSList().get(smsResultDto.getInvoiceSList().size() - 1).getAmounts();
                        allAmount = allAmount.subtract(invoinceAmount2);
                        amountSum = BigDecimal.ZERO;
                        taxAmtErrSum = BigDecimal.ZERO;
                    }
                    else {
                        BigDecimal taxRate = detailDto.getTaxRate();
                        BigDecimal dec = detailDto.getTaxDeduction();
                        if (dec == null) {
                            dec = BigDecimal.ZERO;
                        }
                        BigDecimal calTaxAmt = lineAmount.subtract(dec).multiply(taxRate);
                        BigDecimal taxAmtErr = calTaxAmt.subtract(detailDto.getTaxAmt()).setScale(2, RoundingMode.HALF_UP);
                        taxAmtErrSum = taxAmtErrSum.add(taxAmtErr);
                    }
                }
            }
        }
    }

    
    // 判断当前单据明细的金额总和加上下一行商品明细的金额是否超过了发票限额。如果超过了，则返回true，否则返回false。
    private static Boolean addNextProLineIsExceed(List<BillDetailDto> detailDtos, int curIndex,
                                                  BigDecimal amountSum, BigDecimal invLimitAmt) {
        BigDecimal countNextAmount = BigDecimal.ZERO.add(amountSum);
        int j = curIndex + 1;
        if (j < detailDtos.size()) {
            BillDetailDto disLineDto = detailDtos.get(j);
            if (isDisLine(disLineDto)) {
                int k = j + 1;
                if (k < detailDtos.size()) {
                    BillDetailDto nextProDto = detailDtos.get(k);
                    countNextAmount = countNextAmount.add(nextProDto.getAmounts());
                }
                int m = k + 1;
                if (m < detailDtos.size()) {
                    BillDetailDto nextDisDto = detailDtos.get(m);
                    if (isDisLine(nextDisDto)) {
                        countNextAmount = countNextAmount.add(nextDisDto.getAmounts());
                    }
                }
            } else {
                BillDetailDto nextProDto2 = detailDtos.get(j);
                BigDecimal nextProAmt = nextProDto2.getAmounts();
                countNextAmount = countNextAmount.add(nextProAmt);
                int i = j + 1;
                if (i < detailDtos.size()) {
                    BillDetailDto nextDisDto2 = detailDtos.get(i);
                    if (isDisLine(nextDisDto2)) {
                        countNextAmount = countNextAmount.add(nextDisDto2.getAmounts());
                    }
                }
            }
        }

        return countNextAmount.compareTo(invLimitAmt) > 0;
    }

    private static BigDecimal calLineAmtTax(final BillDetailDto billDetailDto) {
        final BigDecimal taxRate = billDetailDto.getTaxRate();
        final BigDecimal amounts = billDetailDto.getAmounts();
        BigDecimal dec = billDetailDto.getTaxDeduction();
        if (dec == null) {
            dec = BigDecimal.ZERO;
        }
        final BigDecimal taxAmt = billDetailDto.getTaxAmt();
        final BigDecimal calTaxAmt = amounts.subtract(dec).multiply(taxRate);
        return calTaxAmt.subtract(taxAmt).setScale(2, 4);
    }

    public static Boolean isDisLine(final BillDetailDto detailDto) {
        final Integer lineProperty = detailDto.getLineProperty();
        final BigDecimal amounts = detailDto.getAmounts();
        Boolean isDisline = false;
        if (EnumType.LinePropertyEnum.FOUR.getValue().compareTo(lineProperty) == 0 && amounts.compareTo(BigDecimal.ZERO) < 0) {
            isDisline = true;
        }
        return isDisline;
    }

    private static boolean isOpenInvIndex(BillSubjectDto billSubjectDto, int i, int lastIndx) {
        List<BillDetailDto> detailDtos = billSubjectDto.getBillDList();
        int size = detailDtos.size();
        int limitLine = billSubjectDto.getLimitLine();
        int nextInvMaxIndex = lastIndx + limitLine;
        if (nextInvMaxIndex >= size) {
            return i == size - 1;
        }
        BillDetailDto lastInvDto = detailDtos.get(nextInvMaxIndex);
        if (isDisLine(lastInvDto)) {
            return nextInvMaxIndex == i;
        }
        int j = nextInvMaxIndex + 1;
        if (j < size) {
            BillDetailDto isDisDto = detailDtos.get(j);
            if (isDisLine(isDisDto)) {
                return nextInvMaxIndex - 1 == i;
            }
        }
        return nextInvMaxIndex == i;
    }

    public static Boolean isLastLine(final BillSubjectDto billSubjectDto, final int index) {
        final List<BillDetailDto> billDetailDtos = billSubjectDto.getBillDList();
        final int lastIndex = billDetailDtos.size() - 1;
        final BillDetailDto lastDto = billDetailDtos.get(lastIndex);
        if (isDisLine(lastDto)) {
            if (lastIndex - 1 == index) {
                return true;
            }
            return false;
        } else {
            if (lastIndex == index) {
                return true;
            }
            return false;
        }
    }

    private static int saveInvoince(SmsResultDto smsResultDto, SmruleConfigDto configDto, 
                                    BillSubjectDto billSubjectDto, Integer lastIndex, Integer index, 
                                    String remark, String fristLineNote, AtomicDouble remarkAmountSum) {
        List<InvoiceSubjectDto> invoiceSList = smsResultDto.getInvoiceSList();
        if (CollectionUtils.isEmpty(invoiceSList)) {
            invoiceSList = new ArrayList<InvoiceSubjectDto>();
        }
        Boolean isLast = false;
        if (remark.equals("最后一行开票")) {
            isLast = true;
        }
        InvoiceSubjectDto invoiceSubjectDto = saveOneInvoice(smsResultDto, configDto, billSubjectDto, lastIndex, index, fristLineNote, remarkAmountSum, isLast);
        invoiceSubjectDto.setRemark(remark);
        invoiceSList.add(invoiceSubjectDto);
        smsResultDto.setInvoiceSList(invoiceSList); // 这句多余
        return invoiceSubjectDto.getPageIndex();
    }

    // 将传入的单据数据中的明细拆分成多个发票，并将每个发票的信息保存到InvoiceSubjectDto 对象中
    private static InvoiceSubjectDto saveOneInvoice(SmsResultDto smsResultDto, SmruleConfigDto configDto,
                                                    BillSubjectDto billSubjectDto,
                                                    Integer lastIndex, Integer index,
                                                    String firstLineNote, AtomicDouble remarkAmountSum,
                                                    Boolean isLast) {
        InvoiceSubjectDto invoiceSubjectDto = new InvoiceSubjectDto();
        List<BillDetailDto> detailDtos = billSubjectDto.getBillDList();
        String invSN = StringUtilsEx.getInvSN();
        int useLineNote = configDto.getInvNoteUseLineNote();
        Boolean mccDistinct = configDto.isMccRepeat();
        int remarkSplitNum = configDto.getRemarkSplitNum();
        invoiceSubjectDto.setInvoiceNO(invSN);
        invoiceSubjectDto.setBillNO(billSubjectDto.getBillNO());
        invoiceSubjectDto.setInvoiceKind(billSubjectDto.getInvKind());
        invoiceSubjectDto.setInvoiceNote(billSubjectDto.getNotes());

        int curInvIndex = index;
        final int j = index + 1;
        if (j < detailDtos.size()) {
            if (isDisLine(detailDtos.get(j))) {
                ++curInvIndex;
            }
        }

        // qqin: 可用一行：int i = lastIndex + 1;替换下面的代码
        int i = 0;
        if (lastIndex == -1) {
            i = 0;
        } else {
            i = lastIndex + 1;
        } 
        int lineNum = 0;
        final List<InvoiceDetailDto> invoiceDetailList = new ArrayList<InvoiceDetailDto>(10);
        final List<String> billNOList = new ArrayList<String>();
        while (i <= curInvIndex) {
            final BillDetailDto billDetailDto = detailDtos.get(i);
            if (configDto.getEffectiveRange() == 1 && 1 != billDetailDto.getSplitSign() && billDetailDto.getDetailIdSet().size() <= 1 && !isDisLine(billDetailDto)) {
                InvoiceSplitCoreService.dealDetailByRule(billDetailDto, configDto, detailDtos, i);
            }

            InvoiceDetailDto invoiceDetailDto = setInvDetailDto(invoiceSubjectDto, billDetailDto, invSN, useLineNote, firstLineNote, mccDistinct, lineNum);
            invoiceDetailList.add(invoiceDetailDto);
            Set<BillDetailIdDto> detailIdSet = billDetailDto.getDetailIdSet();
            for (BillDetailIdDto detailIdDto : detailIdSet) {
                if (!billNOList.contains(detailIdDto.getBillNO())) {
                    billNOList.add(detailIdDto.getBillNO());
                }
            }
            ++lineNum;
            ++i;
        }

        /* qqin: 这段代码有问题，暂时注释掉
        final Set<String> netBillNoSet = billSubjectDto.getNegBillNoSet();
        if (CollectionUtils.isNotEmpty(netBillNoSet)) {
            final List<String> list;
            netBillNoSet.forEach(billNo -> {
                if (!list.contains(billNo)) {
                    list.add(billNo);
                }
                return;
            });
            billSubjectDto.setNegBillNoSet(null);
        }*/

        final List<String> lineNotes = invoiceSubjectDto.getLineNotes();
        final List<String> splitLineNotes = new ArrayList<String>();
        if (configDto.isMccRepeat()) {
            invoiceSubjectDto.setInvoiceNote(org.apache.commons.lang3.StringUtils.join(lineNotes.toArray(), configDto.getMccNoteStr()));
        } else {
            lineNotes.forEach(t -> {
                List<String> mergeList = Arrays.asList(t.split("\\".equals(configDto.getMccNoteStr()) ? "\\\\" : configDto.getMccNoteStr()));
                if (mergeList.size() > 0) {
                    for (String note : mergeList) {
                        if (!splitLineNotes.contains(note)) {
                            splitLineNotes.add(note);
                        }
                    }
                }
                return;
            });
            invoiceSubjectDto.setInvoiceNote(org.apache.commons.lang3.StringUtils.join(splitLineNotes.toArray(), configDto.getMccNoteStr()));
        }

        invoiceSubjectDto.setInvoiceDetailList(invoiceDetailList);
        invoiceSubjectDto.setBillNOList(billNOList);
        final Integer listType = configDto.getListType();
        if (listType == 1) {
            invoiceSubjectDto.setListFlag(1);
        } else if (listType == 2) {
            invoiceSubjectDto.setListFlag(0);
        } else {
            if (invoiceDetailList.size() > 8) {
                invoiceSubjectDto.setListFlag(1);
            } else {
                invoiceSubjectDto.setListFlag(0);
            }
            if (EnumType.InvKindEnum.ROLL.getValue().equals(billSubjectDto.getInvKind())) {
                invoiceSubjectDto.setListFlag(0);
            }
        }

        invoiceSubjectDto.setPageIndex(curInvIndex);
        if (remarkSplitNum > 0) {
            final String remark = invoiceSubjectDto.getInvoiceNote();
            final String[] remarkLines = remark.split("\n");
            if (remarkLines.length > remarkSplitNum - 1) {
                String remarkAmountLine = remarkLines[remarkSplitNum - 1];
                final String regex = "(\\-|\\+)?[0-9]+(\\.[0-9]{2})?";
                final Pattern pattern = Pattern.compile(regex);
                final Matcher matcher = pattern.matcher(remarkAmountLine);
                if (matcher.find()) {
                    final BigDecimal remarkAmount = new BigDecimal(matcher.group());
                    BigDecimal newAmount = remarkAmount.multiply(invoiceSubjectDto.getAmounts()).divide(billSubjectDto.getSumAmtJE(), 2, 4);
                    BigDecimal sum = new BigDecimal(new StringBuilder(String.valueOf(remarkAmountSum.get())).toString());
                    if (isLast) {
                        newAmount = remarkAmount.subtract(sum);
                    }
                    sum = sum.add(newAmount);
                    remarkAmountSum.set(sum.doubleValue());
                    remarkAmountLine = remarkAmountLine.replace(matcher.group(), newAmount.toString());
                }
                remarkLines[remarkSplitNum - 1] = remarkAmountLine;
                invoiceSubjectDto.setInvoiceNote(org.apache.commons.lang3.StringUtils.join((Object[]) remarkLines, "\n"));
            }
        }
        return invoiceSubjectDto;
    }

    public static void main(final String[] args) {
        final AtomicInteger i = new AtomicInteger(0);
        add(i);
        System.out.print(i);
    }

    private static void add(final AtomicInteger i) {
        i.getAndAdd(100);
        if (i.get() == 100) {
            System.out.println(true);
        }
        System.out.println(i);
    }

    private static InvoiceDetailDto setInvDetailDto(final InvoiceSubjectDto invoiceSubjectDto, final BillDetailDto billDetailDto, final String invSN, final int useLineNote, final String fristLineNote, final boolean mccDistinct, final int lineNum) {
        final BigDecimal origTaxAmt = billDetailDto.getTaxAmt();
        billDetailDto.setTaxAmt(origTaxAmt.setScale(2, 4));
        // final int includeTax = billDetailDto.getIncludeTax(); // 没用到

        final InvoiceDetailDto invoiceDETAIL = new InvoiceDetailDto();
        invoiceDETAIL.setInvoiceNO(invSN);
        invoiceDETAIL.setInvoiceDetailNO(StringUtilsEx.getInvSN());
        invoiceDETAIL.setGoodsClass(billDetailDto.getGoodsClass());
        invoiceDETAIL.setGoodsCode(billDetailDto.getGoodsCode());
        invoiceDETAIL.setGoodsName(billDetailDto.getGoodsName());
        invoiceDETAIL.setGoodsModel(billDetailDto.getGoodsModel());
        invoiceDETAIL.setGoodsUnit(billDetailDto.getGoodsUnit());
        invoiceDETAIL.setAmts(billDetailDto.getAmts());
        invoiceDETAIL.setPrice(billDetailDto.getPrice());
        invoiceDETAIL.setAmounts(billDetailDto.getAmounts());
        invoiceDETAIL.setTaxRate(billDetailDto.getTaxRate());
        invoiceDETAIL.setTaxAmt(billDetailDto.getTaxAmt());
        invoiceDETAIL.setGoodsNoVer(billDetailDto.getGoodsNoVer());
        invoiceDETAIL.setGoodsTaxNo(billDetailDto.getGoodsTaxNo());
        invoiceDETAIL.setTaxPre(billDetailDto.getTaxPre());
        invoiceDETAIL.setTaxPreCon(billDetailDto.getTaxPreCon());
        invoiceDETAIL.setZeroTax(billDetailDto.getZeroTax());
        invoiceDETAIL.setCropGoodsNo(billDetailDto.getCropGoodsNo());
        invoiceDETAIL.setLineProperty(billDetailDto.getLineProperty());
        invoiceDETAIL.setLineNum(lineNum);
        invoiceDETAIL.setIncludeTax(0);
        invoiceDETAIL.setTaxAmounts(billDetailDto.getAmountsIncTax());
        invoiceDETAIL.setAmountsIncTax(billDetailDto.getAmountsIncTax());
        invoiceDETAIL.setBillDetailNO(billDetailDto.getBillDetailNO());
        invoiceDETAIL.setBillNO(billDetailDto.getBillNO());
        invoiceDETAIL.setPriceIncTax(billDetailDto.getPriceIncTax());
        invoiceDETAIL.setBy1(billDetailDto.getBy1());
        invoiceDETAIL.setSplitSign(billDetailDto.getSplitSign());

        final BigDecimal taxDeduction = billDetailDto.getTaxDeduction();
        final BigDecimal taxRate = invoiceDETAIL.getTaxRate();
        final BigDecimal invTaxAmt = invoiceDETAIL.getTaxAmt();
        BigDecimal tmpTaxAmt = BigDecimal.ZERO;
        if (taxDeduction == null || taxDeduction.compareTo(BigDecimal.ZERO) == 0) {
            invoiceDETAIL.setTaxDeduction("");
            tmpTaxAmt = InvoiceCoreService.calcUtilMethods.calcTaxAmtByNoTaxMoneyDec(billDetailDto.getAmounts(), null, taxRate, 4);
            final BigDecimal invDetailErr = tmpTaxAmt.subtract(invTaxAmt);
            invoiceDETAIL.setInvDetailErr(invDetailErr);
        } else {
            invoiceDETAIL.setTaxDeduction(taxDeduction.toPlainString());
            tmpTaxAmt = InvoiceCoreService.calcUtilMethods.calcTaxAmtByNoTaxMoneyDec(billDetailDto.getAmounts(), taxDeduction, taxRate, 4);
            final BigDecimal invDetailErr = tmpTaxAmt.subtract(invTaxAmt);
            invoiceDETAIL.setInvDetailErr(invDetailErr);
            invoiceSubjectDto.setIsTaxDe(1);
            invoiceSubjectDto.setTaxDeduction(String.valueOf(billDetailDto.getTaxDeduction()));
        }

        BigDecimal invErr = invoiceSubjectDto.getInvErr();
        invErr = invErr.add(invoiceDETAIL.getInvDetailErr());
        invoiceSubjectDto.setInvErr(invErr);

        BigDecimal amtSum = invoiceSubjectDto.getAmounts();
        amtSum = amtSum.add(invoiceDETAIL.getAmounts());
        invoiceSubjectDto.setAmounts(amtSum);

        BigDecimal taxAmtSum = invoiceSubjectDto.getTaxAmt();
        taxAmtSum = taxAmtSum.add(invoiceDETAIL.getTaxAmt());
        invoiceSubjectDto.setTaxAmt(taxAmtSum);

        boolean existsInvTaxRate = false;
        final List<BigDecimal> invTaxRate = invoiceSubjectDto.getInvTaxRate();
        for (int i = 0; i < invTaxRate.size(); ++i) {
            if (billDetailDto.getTaxRate().compareTo(invTaxRate.get(i)) == 0) {
                existsInvTaxRate = true;
                break;
            }
        }
        
        if (!existsInvTaxRate) {
            invTaxRate.add(billDetailDto.getTaxRate());
        }

        if (useLineNote == 1) {
            if (org.apache.commons.lang3.StringUtils.isNotEmpty((CharSequence) billDetailDto.getLineNote())) {
                final List<String> lineNotes = invoiceSubjectDto.getLineNotes();
                if (mccDistinct || !lineNotes.contains(billDetailDto.getLineNote())) {
                    lineNotes.add(billDetailDto.getLineNote());
                }
                invoiceSubjectDto.setLineNotes(lineNotes);
            }
        } else if (fristLineNote != null) {
            final List<String> lineNotes = new ArrayList<String>();
            lineNotes.add(fristLineNote);
            invoiceSubjectDto.setLineNotes(lineNotes);
        }

        invoiceDETAIL.setDetailIdSet(billDetailDto.getDetailIdSet());
        if (invoiceDETAIL.getTaxAmt().compareTo(BigDecimal.ZERO) < 0 && invoiceDETAIL.getLineProperty() != 4) {
            throw new EtcRuleException("税额不能小于0");
        }

        return invoiceDETAIL;
    }

    public static void taxAmtCheck(final BillSubjectDto billSubjectDto, final SmruleConfigDto configDto, final SmsResultDto smsResultDto) {
        final List<InvoiceSubjectDto> list = smsResultDto.getInvoiceSList();
        if (configDto.getTotalTaxamtCount() == 1) {
            for (int k = 0; k < list.size(); ++k) {
                final InvoiceSubjectDto invoiceSubjectDto = list.get(k);
                BigDecimal invDetailErr = BigDecimal.ZERO;
                for (final InvoiceDetailDto invoiceDetailDto : invoiceSubjectDto.getInvoiceDetailList()) {
                    invDetailErr = invDetailErr.add(invoiceDetailDto.getInvDetailErr());
                }
                if (invDetailErr.abs().compareTo(new BigDecimal("1.27")) > 0) {
                    for (int i = 0; i < invoiceSubjectDto.getInvoiceDetailList().size(); ++i) {
                        final InvoiceDetailDto invoiceDetailDto2 = invoiceSubjectDto.getInvoiceDetailList().get(i);
                        if (EnumType.LinePropertyEnum.FOUR.getValue().compareTo(invoiceDetailDto2.getLineProperty()) != 0 || invoiceDetailDto2.getAmounts().compareTo(BigDecimal.ZERO) >= 0) {
                            if (invoiceDetailDto2.getSplitSign() == 1) {
                                invoiceDetailDto2.setTaxAmt(invoiceDetailDto2.getTaxAmt().add(invoiceDetailDto2.getInvDetailErr()));
                                invoiceSubjectDto.setTaxAmt(invoiceSubjectDto.getTaxAmt().add(invoiceDetailDto2.getInvDetailErr()));
                                invDetailErr.subtract(invoiceDetailDto2.getInvDetailErr());
                                if (billSubjectDto.getIncludeTax() == 1) {
                                    invoiceDetailDto2.setAmounts(invoiceDetailDto2.getAmounts().subtract(invoiceDetailDto2.getInvDetailErr()));
                                    invoiceSubjectDto.setAmounts(invoiceSubjectDto.getAmounts().subtract(invoiceDetailDto2.getInvDetailErr()));
                                } else {
                                    invoiceDetailDto2.setAmountsIncTax(invoiceDetailDto2.getAmounts().subtract(invoiceDetailDto2.getInvDetailErr()));
                                }
                                final int j = i + 1;
                                if (j < invoiceSubjectDto.getInvoiceDetailList().size()) {
                                    final InvoiceDetailDto disDto = invoiceSubjectDto.getInvoiceDetailList().get(j);
                                    if (EnumType.LinePropertyEnum.FOUR.getValue().compareTo(disDto.getLineProperty()) == 0 && disDto.getAmounts().compareTo(BigDecimal.ZERO) < 0) {
                                        disDto.setTaxAmt(disDto.getTaxAmt().add(disDto.getInvDetailErr()));
                                        invoiceSubjectDto.setTaxAmt(invoiceSubjectDto.getTaxAmt().add(disDto.getInvDetailErr()));
                                        invDetailErr.subtract(disDto.getInvDetailErr());
                                        if (billSubjectDto.getIncludeTax() == 1) {
                                            disDto.setAmounts(disDto.getAmounts().subtract(disDto.getInvDetailErr()));
                                            invoiceSubjectDto.setAmounts(invoiceSubjectDto.getAmounts().subtract(disDto.getInvDetailErr()));
                                        } else {
                                            disDto.setAmountsIncTax(disDto.getAmounts().subtract(disDto.getInvDetailErr()));
                                        }
                                    }
                                }
                                if (invDetailErr.abs().compareTo(new BigDecimal("1.27")) <= 0) {
                                    break;
                                }
                            }
                        }
                    }
                }
                if (invDetailErr.abs().compareTo(new BigDecimal("1.27")) > 0) {
                    for (int i = 0; i < invoiceSubjectDto.getInvoiceDetailList().size(); ++i) {
                        final InvoiceDetailDto invoiceDetailDto2 = invoiceSubjectDto.getInvoiceDetailList().get(i);
                        if (EnumType.LinePropertyEnum.FOUR.getValue().compareTo(invoiceDetailDto2.getLineProperty()) != 0 || invoiceDetailDto2.getAmounts().compareTo(BigDecimal.ZERO) >= 0) {
                            if (invoiceDetailDto2.getSplitSign() != 1) {
                                invoiceDetailDto2.setTaxAmt(invoiceDetailDto2.getTaxAmt().add(invoiceDetailDto2.getInvDetailErr()));
                                invoiceSubjectDto.setTaxAmt(invoiceSubjectDto.getTaxAmt().add(invoiceDetailDto2.getInvDetailErr()));
                                invDetailErr.subtract(invoiceDetailDto2.getInvDetailErr());
                                if (billSubjectDto.getIncludeTax() == 1) {
                                    invoiceDetailDto2.setAmounts(invoiceDetailDto2.getAmounts().subtract(invoiceDetailDto2.getInvDetailErr()));
                                    invoiceSubjectDto.setAmounts(invoiceSubjectDto.getAmounts().subtract(invoiceDetailDto2.getInvDetailErr()));
                                } else {
                                    invoiceDetailDto2.setAmountsIncTax(invoiceDetailDto2.getAmounts().subtract(invoiceDetailDto2.getInvDetailErr()));
                                }
                                final int j = i + 1;
                                if (j < invoiceSubjectDto.getInvoiceDetailList().size()) {
                                    final InvoiceDetailDto disDto = invoiceSubjectDto.getInvoiceDetailList().get(j);
                                    if (EnumType.LinePropertyEnum.FOUR.getValue().compareTo(disDto.getLineProperty()) == 0 && disDto.getAmounts().compareTo(BigDecimal.ZERO) < 0) {
                                        disDto.setTaxAmt(disDto.getTaxAmt().add(disDto.getInvDetailErr()));
                                        invoiceSubjectDto.setTaxAmt(invoiceSubjectDto.getTaxAmt().add(disDto.getInvDetailErr()));
                                        invDetailErr.subtract(disDto.getInvDetailErr());
                                        if (billSubjectDto.getIncludeTax() == 1) {
                                            disDto.setAmounts(disDto.getAmounts().subtract(disDto.getInvDetailErr()));
                                            invoiceSubjectDto.setAmounts(invoiceSubjectDto.getAmounts().subtract(disDto.getInvDetailErr()));
                                        } else {
                                            disDto.setAmountsIncTax(disDto.getAmounts().subtract(disDto.getInvDetailErr()));
                                        }
                                    }
                                }
                                if (invDetailErr.abs().compareTo(new BigDecimal("1.27")) <= 0) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            for (int k = 0; k < list.size(); ++k) {
                final InvoiceSubjectDto invoiceSubjectDto = list.get(k);
                BigDecimal invDetailErrs = BigDecimal.ZERO;
                for (final InvoiceDetailDto invoiceDetailDto : invoiceSubjectDto.getInvoiceDetailList()) {
                    invDetailErrs = invDetailErrs.add(invoiceDetailDto.getInvDetailErr());
                }
                if (invDetailErrs.abs().compareTo(new BigDecimal("1.27")) > 0) {
                    BigDecimal invDetailErr2 = BigDecimal.ZERO;
                    for (int l = 0; l < invoiceSubjectDto.getInvoiceDetailList().size(); ++l) {
                        final InvoiceDetailDto invoiceDetailDto3 = invoiceSubjectDto.getInvoiceDetailList().get(l);
                        if (EnumType.LinePropertyEnum.FOUR.getValue().compareTo(invoiceDetailDto3.getLineProperty()) != 0 || invoiceDetailDto3.getAmounts().compareTo(BigDecimal.ZERO) >= 0) {
                            invDetailErr2 = invDetailErr2.add(invoiceDetailDto3.getInvDetailErr());
                            final int m = l + 1;
                            if (m < invoiceSubjectDto.getInvoiceDetailList().size()) {
                                final InvoiceDetailDto disDto2 = invoiceSubjectDto.getInvoiceDetailList().get(m);
                                if (EnumType.LinePropertyEnum.FOUR.getValue().compareTo(disDto2.getLineProperty()) == 0 && disDto2.getAmounts().compareTo(BigDecimal.ZERO) < 0) {
                                    invDetailErr2 = invDetailErr2.add(disDto2.getInvDetailErr());
                                }
                            }
                            if (invDetailErr2.abs().compareTo(new BigDecimal("1.27")) > 0) {
                                throw new EtcRuleException("整张发票明细行税额误差之和超过1.27，不允许提交开票，请调整税额数值或调整拆合规则！");
                            }
                        }
                    }
                }
            }
        }
    }

    public static void invoiceAverageLimit(final BillSubjectDto subjectDto, final SmruleConfigDto configDto, final SmsResultDto smsResultDto) {
        subjectDto.getBillDList().stream().map(BillDetailDto::getAmounts).reduce(BigDecimal::add).ifPresent(subjectDto::setSumAmtJE);
        subjectDto.getBillDList().stream().map(BillDetailDto::getTaxAmt).reduce(BigDecimal::add).ifPresent(subjectDto::setSumAmtSE);
        final BigDecimal invLimitAmt = configDto.getInvLimitAmt();
        configDto.setFinalLimitAmt(invLimitAmt);
    }
}
