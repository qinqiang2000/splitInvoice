package kd.imc.sim.split.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.AtomicDouble;
import kd.imc.sim.common.constant.InvoiceConstant;
import kd.imc.sim.split.dto.*;
import kd.imc.sim.split.enums.EnumType.InvKindEnum;
import kd.imc.sim.split.enums.EnumType.LinePropertyEnum;
import kd.imc.sim.split.exception.EtcRuleException;
import kd.imc.sim.split.methods.BackCalcUtilMethods;
import kd.imc.sim.split.utils.ComUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static kd.imc.sim.split.enums.EnumType.InvKindEnum;
import static kd.imc.sim.split.enums.EnumType.LinePropertyEnum;


public class InvoiceCoreService {
    private static Logger LOG = LoggerFactory.getLogger(InvoiceCoreService.class);
    private static BackCalcUtilMethods calcUtilMethods = new BackCalcUtilMethods();

    /* 生成发票请求报文；
        首先获取billSubjectDto中的单据编号，
        然后判断单据金额是否超过发票限额的1000倍，如果超过则抛出一个EtcRuleException异常。
        接着调用openInvoiceDetail函数，该函数会对单据中的每一行进行处理，如果某一行的金额超过了发票限额，则会对该行进行拆分。
        最后，函数调用saveInvoince函数，将生成的发票请求报文保存到smsResultDto中。 
    */
    public static void openInvoice(BillSubjectDto billSubjectDto, SmruleConfigDto configDto, SmsResultDto smsResultDto) {
        String billSubjectNo = billSubjectDto.getBillNO();
        LOG.debug("单据编号[{}]生成发票请求报文 单据:{}，规则配置:{}", billSubjectNo, JSONObject.toJSONString(billSubjectDto), JSONObject.toJSONString(configDto));
        BigDecimal invLimitAmt = configDto.getFinalLimitAmt();
        BigDecimal amountIncTax = billSubjectDto.getSumAmtJE().add(billSubjectDto.getSumAmtSE());
        AtomicDouble remarkAmountSum = new AtomicDouble(0.0D);
        if (amountIncTax.divide(invLimitAmt, 0, 2).compareTo(new BigDecimal("1000")) > 0) {
            LOG.info("单据编号[{}]生成发票请求报文 单据:{}，规则配置:{}", billSubjectNo, JSONObject.toJSONString(billSubjectDto), JSONObject.toJSONString(configDto));
            throw new EtcRuleException("单据金额超过发票限额的1000倍，请调整单据金额");
        }
        try {
            openInvoiceDetail(billSubjectDto, configDto, smsResultDto, remarkAmountSum);
        } catch (Exception e) {
            LOG.info(e.getMessage());
            throw new EtcRuleException("对象备份失败");
        }
    }

    /* 
     * 对单据中的每一行进行处理，如果某一行的金额超过了发票限额，则会对该行进行拆分。最后，函数调用saveInvoince函数，将生成的发票请求报文保存到smsResultDto中。
     * 函数遍历每一行明细，如果某一行的金额超过了发票限额，则会对该行进行拆分。
     * 如果拆分后的行数超过了最大行数，则会对前面的行进行开票。
     * 最后，函数调用saveInvoince函数，将生成的发票请求报文保存到smsResultDto中。
     */
    public static void openInvoiceDetail(BillSubjectDto billSubjectDto, SmruleConfigDto configDto, 
                                        SmsResultDto smsResultDto, AtomicDouble remarkAmountSum) throws Exception {
        LOG.debug("单据编号[{}]生成发票请求报文 单据:{}，规则配置:{}", billSubjectDto.getBillNO(), JSONObject.toJSONString(billSubjectDto), JSONObject.toJSONString(configDto));
        BigDecimal invLimitAmt = configDto.getFinalLimitAmt();
        List<BillDetailDto> detailDtos = billSubjectDto.getBillDList();

        BigDecimal allAmount = billSubjectDto.getSumAmtJE();
        BigDecimal amountSum = BigDecimal.ZERO;
        BigDecimal taxAmtErrSum = BigDecimal.ZERO;
        int lastIndex = -1;

        int size = detailDtos.size();
        for (int detailIndex = 0; detailIndex < size; ++detailIndex) {
            BillDetailDto detailDto = detailDtos.get(detailIndex);
            
            // 如果下一行是折扣行，则取出到disDto
            int nextDetailIndex = detailIndex + 1;
            BillDetailDto disDto = null;
            if (nextDetailIndex < size && ComUtil.isDisLine(detailDtos.get(nextDetailIndex))) {
                disDto = detailDtos.get(nextDetailIndex);
            }

            // 将商品行金额和折扣行(如果存在)金额相加，结果存入disLineAmount
            String billNo = detailDto.getBillNO();
            BigDecimal lineAmount = detailDto.getAmounts();
            BigDecimal disLineAmount = lineAmount.add(disDto != null ? disDto.getAmounts() : BigDecimal.ZERO);

            // 商品行+折扣行的金额大于限额，直接拆分
            if (disLineAmount.compareTo(invLimitAmt) > 0) {
                int currentIndex = detailIndex;
                List<BillDetailDto> toSplitDetails = new ArrayList<>();
                toSplitDetails.add(detailDtos.get(detailIndex));
                if (disDto != null) {
                    toSplitDetails.add(disDto);
                    detailDtos.remove(nextDetailIndex);
                    detailIndex++;
                }
                detailDtos.remove(currentIndex);
                List<BillDetailDto> splitGoods = InvoiceSplitCoreService.goodsLineSplit(toSplitDetails, configDto, amountSum, toSplitDetails.size() > 1, allAmount);
                detailDtos.addAll(currentIndex, splitGoods);
                size = detailDtos.size();
                LOG.debug("单据编号[{}] 超过最大行数 行号[{}]开票,金额累计：{},税额误差累计:{}", billNo, detailIndex + 1, amountSum, taxAmtErrSum);
                lastIndex = saveInvoince(smsResultDto, configDto, billSubjectDto, lastIndex, detailIndex, "金额累计超限开票", detailDtos.get(0).getLineNote(), remarkAmountSum);

                allAmount = allAmount.subtract(smsResultDto.getInvoiceSList().get(smsResultDto.getInvoiceSList().size() - 1).getAmounts());
                amountSum = BigDecimal.ZERO;
                taxAmtErrSum = BigDecimal.ZERO;
                continue;
            }

            // 最后一行
            if (isLastLine(billSubjectDto, detailIndex)) {
                LOG.debug("单据编号[{}] 最后一行[{}]开票,金额累计：{},税额误差累计:{}", billNo, detailIndex + 1, amountSum, taxAmtErrSum);
                saveInvoince(smsResultDto, configDto, billSubjectDto, lastIndex, detailIndex, "最后一行开票", detailDtos.get(0).getLineNote(), remarkAmountSum);
                break;
            }

            // 超过最大行数
            if (isOpenInvIndex(billSubjectDto, detailIndex, lastIndex)) {
                LOG.debug("单据编号[{}] 超过最大行数 行号[{}]开票,金额累计：{},税额误差累计:{}", billNo, detailIndex + 1, amountSum, taxAmtErrSum);
                lastIndex = saveInvoince(smsResultDto, configDto, billSubjectDto, lastIndex, detailIndex, "超过最大行数开票", (detailDtos.get(0)).getLineNote(), remarkAmountSum);

                BigDecimal invoinceAmount = (smsResultDto.getInvoiceSList().get(smsResultDto.getInvoiceSList().size() - 1)).getAmounts();
                allAmount = allAmount.subtract(invoinceAmount);
                amountSum = BigDecimal.ZERO;
                taxAmtErrSum = BigDecimal.ZERO;
                continue;
            }
            if (ComUtil.isDisLine(detailDto)) {
                continue;
            }
            // 非折扣行
            amountSum = amountSum.add(lineAmount);
            if (disDto != null && ComUtil.isDisLine(disDto)) {
                amountSum = amountSum.add(disDto.getAmounts());
                BigDecimal taxAmtErr = disDto.getAmounts().multiply(disDto.getTaxRate()).subtract(disDto.getTaxAmt()).setScale(2, 4);
                taxAmtErrSum = taxAmtErrSum.add(taxAmtErr);
            }

            // 判断是否需要将下一行商品行进行拆分，以保证拆分后的发票金额不超过限制。
            // 如果当前行的金额加上下一行的金额仍然小于限制金额，则不需要拆分下一行商品行。
            // 如果需要拆分，则将下一行商品行进行拆分，并将拆分后的行插入到当前行和下一行之间。
            // 如果拆分后的行数超过了最大行数，则需要将当前行和下一行商品行之后的所有行进行拆分，直到拆分后的行数不超过最大行数为止
            if (amountSum.compareTo(invLimitAmt) < 0 && addNextProLineIsExpeed(detailDtos, detailIndex, amountSum, invLimitAmt)) {
                int nextIndex = disDto != null ? detailIndex + 2 : detailIndex + 1;

                BillDetailDto nextDetails = detailDtos.get(nextIndex);
                BigDecimal nextLineAmount = nextDetails.getAmounts();
                BillDetailDto nextDisDto = null;
                if (nextIndex + 1 < size && detailDtos.get(nextIndex + 1) != null && ComUtil.isDisLine(detailDtos.get(nextIndex + 1))) {
                    nextDisDto = detailDtos.get(nextIndex + 1);
                    nextLineAmount = nextDisDto.getAmounts();
                }

                if ((configDto.getSplitGoodsLine() != 0 || nextLineAmount.compareTo(invLimitAmt) >= 0) && 1 != nextDetails.getSplitSign()) {
                    if (disDto != null) {
                        detailIndex++;
                    }

                    List<BillDetailDto> toSplitDetails = new ArrayList<>();
                    toSplitDetails.add(nextDetails);
                    if (nextDisDto != null) {
                        toSplitDetails.add(nextDisDto);
                        detailDtos.remove(nextIndex + 1);
                        detailIndex++;
                    }

                    detailDtos.remove(nextIndex);
                    detailIndex++;
                    int toSplitNum = toSplitDetails.size();
                    List<BillDetailDto> splitGoods = InvoiceSplitCoreService.goodsLineSplit(toSplitDetails, configDto, amountSum, toSplitDetails.size() > 1, allAmount);
                    detailDtos.addAll(nextIndex, splitGoods);
                    size = detailDtos.size();
                    if (splitGoods.size() > toSplitNum) {
                        if (nextDisDto != null && !ComUtil.isDisLine(splitGoods.get(1))) {
                            detailIndex--;
                        }

                        LOG.debug("单据编号[{}] 超过最大行数 行号[{}]开票,金额累计：{},税额误差累计:{}", billNo, detailIndex + 1, amountSum, taxAmtErrSum);
                        lastIndex = saveInvoince(smsResultDto, configDto, billSubjectDto, lastIndex, detailIndex, "金额累计超限开票", (detailDtos.get(0)).getLineNote(), remarkAmountSum);

                        allAmount = allAmount.subtract((smsResultDto.getInvoiceSList().get(smsResultDto.getInvoiceSList().size() - 1)).getAmounts());
                        amountSum = BigDecimal.ZERO;
                        taxAmtErrSum = BigDecimal.ZERO;
                        continue;
                    }

                    if (disDto != null) {
                        detailIndex--;
                    }
                    detailIndex--;
                }
            }

            if (amountSum.compareTo(invLimitAmt) != 0 && !addNextProLineIsExpeed(detailDtos, detailIndex, amountSum, invLimitAmt)) {
                BigDecimal nextLineAmount = lineAmount.subtract(detailDto.getTaxDeduction()).multiply(detailDto.getTaxRate());
                BigDecimal taxAmtErr = nextLineAmount.subtract(detailDto.getTaxAmt()).setScale(2, 4);
                taxAmtErrSum = taxAmtErrSum.add(taxAmtErr);
            } else {
                LOG.debug("单据编号[{}] 金额累计超限 行号[{}]开票,金额累计：{},税额误差累计:{}", billNo, detailIndex + 1, amountSum, taxAmtErrSum);
                lastIndex = saveInvoince(smsResultDto, configDto, billSubjectDto, lastIndex, detailIndex, "金额累计超限开票", (detailDtos.get(0)).getLineNote(), remarkAmountSum);

                allAmount = allAmount.subtract((smsResultDto.getInvoiceSList().get(smsResultDto.getInvoiceSList().size() - 1)).getAmounts());
                amountSum = BigDecimal.ZERO;
                taxAmtErrSum = BigDecimal.ZERO;
            }
        }
    }

    /* 
     * 该函数的作用是判断是否需要将下一行商品行进行拆分，以保证拆分后的发票金额不超过限制。
     * 如果当前行的金额加上下一行的金额仍然小于限制金额，则不需要拆分下一行商品行。
     * 如果需要拆分，则将下一行商品行进行拆分，并将拆分后的行插入到当前行和下一行之间。
     * 如果拆分后的行数超过了最大行数，则需要将当前行和下一行商品行之后的所有行进行拆分，直到拆分后的行数不超过最大行数为止。（？？）
     * 该函数的返回值为布尔类型，表示是否需要拆分下一行商品行。
     */
    private static Boolean addNextProLineIsExpeed(List<BillDetailDto> detailDtos, int curIndex, BigDecimal amountSum, BigDecimal invLimitAmt) {
        BigDecimal countNextAmount = BigDecimal.ZERO.add(amountSum);
        int j = curIndex + 1;
        if (j < detailDtos.size()) {
            BillDetailDto disLineDto = detailDtos.get(j);
            if (ComUtil.isDisLine(disLineDto)) {
                int k = j + 1;
                if (k < detailDtos.size()) {
                    BillDetailDto nextProDto = detailDtos.get(k);
                    countNextAmount = countNextAmount.add(nextProDto.getAmounts());
                }

                int m = k + 1;
                if (m < detailDtos.size()) {
                    BillDetailDto nextDisDto = detailDtos.get(m);
                    if (ComUtil.isDisLine(nextDisDto)) {
                        countNextAmount = countNextAmount.add(nextDisDto.getAmounts());
                    }
                }
            } else {
                BillDetailDto nextProDto = detailDtos.get(j);
                BigDecimal nextProAmt = nextProDto.getAmounts();
                countNextAmount = countNextAmount.add(nextProAmt);
                int k = j + 1;
                if (k < detailDtos.size()) {
                    BillDetailDto nextDisDto = detailDtos.get(k);
                    if (ComUtil.isDisLine(nextDisDto)) {
                        countNextAmount = countNextAmount.add(nextDisDto.getAmounts());
                    }
                }
            }
        }

        return countNextAmount.compareTo(invLimitAmt) > 0;
    }

    private static boolean isOpenInvIndex(BillSubjectDto billSubjectDto, int i, int lastIndx) {
        List<BillDetailDto> detailDtos = billSubjectDto.getBillDList();
        int size = detailDtos.size();
        int limitLine = billSubjectDto.getLimitLine();
        int nextInvMaxIndex = lastIndx + limitLine;
        if (nextInvMaxIndex < size) {
            BillDetailDto lastInvDto = detailDtos.get(nextInvMaxIndex);
            if (ComUtil.isDisLine(lastInvDto)) {
                return nextInvMaxIndex == i;
            } else {
                int j = nextInvMaxIndex + 1;
                if (j < size) {
                    BillDetailDto isDisDto = detailDtos.get(j);
                    if (ComUtil.isDisLine(isDisDto)) {
                        return nextInvMaxIndex - 1 == i;
                    }
                }
                return nextInvMaxIndex == i;
            }
        } else {
            return i == size - 1;
        }
    }

    public static Boolean isLastLine(BillSubjectDto billSubjectDto, int index) {
        List<BillDetailDto> billDetailDtos = billSubjectDto.getBillDList();
        int lastIndex = billDetailDtos.size() - 1;
        BillDetailDto lastDto = billDetailDtos.get(lastIndex);
        if (ComUtil.isDisLine(lastDto)) {
            return lastIndex - 1 == index;
        } else {
            return lastIndex == index;
        }
    }

    private static int saveInvoince(SmsResultDto smsResultDto, SmruleConfigDto configDto, BillSubjectDto billSubjectDto, Integer lastIndex, Integer index, String remark, String fristLineNote, AtomicDouble remarkAmountSum) {
        List<InvoiceSubjectDto> invoiceSList = smsResultDto.getInvoiceSList();
        if (CollectionUtils.isEmpty(invoiceSList)) {
            invoiceSList = new ArrayList<>();
        }

        boolean isLast = false;
        if (remark.equals("最后一行开票")) {
            isLast = true;
        }

        InvoiceSubjectDto invoiceSubjectDto = saveOneInvoice(configDto, billSubjectDto, lastIndex, index, fristLineNote, remarkAmountSum, isLast);
        invoiceSubjectDto.setRemark(remark);
        invoiceSList.add(invoiceSubjectDto);
        smsResultDto.setInvoiceSList(invoiceSList);
        return invoiceSubjectDto.getPageIndex();
    }

    private static InvoiceSubjectDto saveOneInvoice(SmruleConfigDto configDto, BillSubjectDto billSubjectDto, Integer lastIndex, Integer index, String fristLineNote, AtomicDouble remarkAmountSum, Boolean isLast) {
        InvoiceSubjectDto invoiceSubjectDto = new InvoiceSubjectDto();
        List<BillDetailDto> detailDtos = billSubjectDto.getBillDList();
        String invSN = getInvSN();
        int useLineNote = configDto.getInvNoteUseLineNote();
        Boolean mccDistinct = configDto.isMccRepeat();
        int remarkSplitNum = configDto.getRemarkSplitNum();
        invoiceSubjectDto.setInvoiceNO(invSN);
        invoiceSubjectDto.setBillNO(billSubjectDto.getBillNO());
        invoiceSubjectDto.setInvoiceKind(billSubjectDto.getInvKind());
        invoiceSubjectDto.setInvoiceNote(billSubjectDto.getNotes());
        int curInvIndex = index;
        int j = index + 1;
        if (j < detailDtos.size()) {
            BillDetailDto tmpDto = detailDtos.get(j);
            if (ComUtil.isDisLine(tmpDto)) {
                curInvIndex++;
            }
        }

        List<InvoiceDetailDto> invoiceDetailList = new ArrayList<>(10);
        int lineNum = 0;
        List<String> billNOList = new ArrayList<>();
        for (int i = lastIndex == -1 ? 0 : lastIndex + 1; i <= curInvIndex; ++i) {
            BillDetailDto billDetailDto = detailDtos.get(i);
            if (configDto.getEffectiveRange() == 1 && 1 != billDetailDto.getSplitSign() && billDetailDto.getDetailIdSet().size() <= 1 && !ComUtil.isDisLine(billDetailDto)) {
                InvoiceSplitCoreService.dealDetailByRule(billDetailDto, configDto, detailDtos, i);
            }

            InvoiceDetailDto invoiceDetailDto = setInvDetailDto(invoiceSubjectDto, billDetailDto, invSN, useLineNote, fristLineNote, mccDistinct, lineNum);
            invoiceDetailList.add(invoiceDetailDto);
            Set<BillDetailIdDto> detailIdSet = billDetailDto.getDetailIdSet();

            for (BillDetailIdDto detailIdDto : detailIdSet) {
                if (!billNOList.contains(detailIdDto.getBillNO())) {
                    billNOList.add(detailIdDto.getBillNO());
                }
            }
            ++lineNum;
        }

        List<String> lineNotes = invoiceSubjectDto.getLineNotes();
        List<String> splitLineNotes = new ArrayList<>();
        if (configDto.isMccRepeat()) {
            invoiceSubjectDto.setInvoiceNote(StringUtils.join(lineNotes.toArray(), configDto.getMccNoteStr()));
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
            });
            invoiceSubjectDto.setInvoiceNote(StringUtils.join(splitLineNotes.toArray(), configDto.getMccNoteStr()));
        }

        invoiceSubjectDto.setInvoiceDetailList(invoiceDetailList);
        invoiceSubjectDto.setBillNOList(billNOList);
        int listType = configDto.getListType();
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

            if (InvKindEnum.ROLL.getValue().equals(billSubjectDto.getInvKind())) {
                invoiceSubjectDto.setListFlag(0);
            }
        }

        invoiceSubjectDto.setPageIndex(curInvIndex);
        if (remarkSplitNum > 0) {
            String remark = invoiceSubjectDto.getInvoiceNote();
            String[] remarkLines = remark.split("\n");
            if (remarkLines.length > remarkSplitNum - 1) {
                String remarkAmountLine = remarkLines[remarkSplitNum - 1];
                String regex = "([-+])?[0-9]+(\\.[0-9]{2})?";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(remarkAmountLine);
                if (matcher.find()) {
                    BigDecimal remarkAmount = new BigDecimal(matcher.group());
                    BigDecimal newAmount = remarkAmount.multiply(invoiceSubjectDto.getAmounts()).divide(billSubjectDto.getSumAmtJE(), 2, 4);
                    BigDecimal sum = new BigDecimal(String.valueOf(remarkAmountSum.get()));
                    if (isLast) {
                        newAmount = remarkAmount.subtract(sum);
                    }

                    sum = sum.add(newAmount);
                    remarkAmountSum.set(sum.doubleValue());
                    remarkAmountLine = remarkAmountLine.replace(matcher.group(), newAmount.toString());
                }

                remarkLines[remarkSplitNum - 1] = remarkAmountLine;
                invoiceSubjectDto.setInvoiceNote(StringUtils.join(remarkLines, "\n"));
            }
        }

        return invoiceSubjectDto;
    }

    private static InvoiceDetailDto setInvDetailDto(InvoiceSubjectDto invoiceSubjectDto, BillDetailDto billDetailDto, String invSN, int useLineNote, String fristLineNote, boolean mccDistinct, int lineNum) {
        BigDecimal origTaxAmt = billDetailDto.getTaxAmt();
        billDetailDto.setTaxAmt(origTaxAmt.setScale(2, 4));
        InvoiceDetailDto invoiceDETAIL = new InvoiceDetailDto();
        invoiceDETAIL.setInvoiceNO(invSN);
        invoiceDETAIL.setInvoiceDetailNO(getInvSN());
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
        BigDecimal taxDeduction = billDetailDto.getTaxDeduction();
        BigDecimal taxRate = invoiceDETAIL.getTaxRate();
        BigDecimal invTaxAmt = invoiceDETAIL.getTaxAmt();
        BigDecimal tmpTaxAmt;
        BigDecimal invErr;
        if (taxDeduction != null && taxDeduction.compareTo(BigDecimal.ZERO) != 0) {
            invoiceDETAIL.setTaxDeduction(taxDeduction.toPlainString());
            tmpTaxAmt = calcUtilMethods.calcTaxAmtByNoTaxMoneyDec(billDetailDto.getAmounts(), taxDeduction, taxRate, 4);
            invErr = tmpTaxAmt.subtract(invTaxAmt);
            invoiceDETAIL.setInvDetailErr(invErr);
            invoiceSubjectDto.setIsTaxDe(1);
            invoiceSubjectDto.setTaxDeduction(String.valueOf(billDetailDto.getTaxDeduction()));
        } else {
            invoiceDETAIL.setTaxDeduction("");
            tmpTaxAmt = calcUtilMethods.calcTaxAmtByNoTaxMoneyDec(billDetailDto.getAmounts(), null, taxRate, 4);
            invErr = tmpTaxAmt.subtract(invTaxAmt);
            invoiceDETAIL.setInvDetailErr(invErr);
        }

        invErr = invoiceSubjectDto.getInvErr();
        invErr = invErr.add(invoiceDETAIL.getInvDetailErr());
        invoiceSubjectDto.setInvErr(invErr);
        BigDecimal amtSum = invoiceSubjectDto.getAmounts();
        amtSum = amtSum.add(invoiceDETAIL.getAmounts());
        invoiceSubjectDto.setAmounts(amtSum);
        BigDecimal taxAmtSum = invoiceSubjectDto.getTaxAmt();
        taxAmtSum = taxAmtSum.add(invoiceDETAIL.getTaxAmt());
        invoiceSubjectDto.setTaxAmt(taxAmtSum);
        boolean existsInvTaxRate = false;
        List<BigDecimal> invTaxRate = invoiceSubjectDto.getInvTaxRate();

        for (BigDecimal bigDecimal : invTaxRate) {
            if (billDetailDto.getTaxRate().compareTo(bigDecimal) == 0) {
                existsInvTaxRate = true;
                break;
            }
        }

        if (!existsInvTaxRate) {
            invTaxRate.add(billDetailDto.getTaxRate());
        }

        if (useLineNote == 1) {
            if (StringUtils.isNotEmpty(billDetailDto.getLineNote())) {
                List<String> lineNotes = invoiceSubjectDto.getLineNotes();
                if (mccDistinct || !lineNotes.contains(billDetailDto.getLineNote())) {
                    lineNotes.add(billDetailDto.getLineNote());
                }

                invoiceSubjectDto.setLineNotes(lineNotes);
            }
        } else if (fristLineNote != null) {
            List<String> lineNotes = new ArrayList<>();
            lineNotes.add(fristLineNote);
            invoiceSubjectDto.setLineNotes(lineNotes);
        }

        invoiceDETAIL.setDetailIdSet(billDetailDto.getDetailIdSet());
        if (invoiceDETAIL.getTaxAmt().compareTo(BigDecimal.ZERO) < 0 && invoiceDETAIL.getLineProperty() != 4) {
            throw new EtcRuleException("税额不能小于0");
        } else {
            return invoiceDETAIL;
        }
    }

    public static void taxAmtCheck(BillSubjectDto billSubjectDto, SmruleConfigDto configDto, SmsResultDto smsResultDto) {
//        if (configDto.getTotalTaxamtCount() == 1) {
//            for (InvoiceSubjectDto invoiceSubjectDto : list) {
//                BigDecimal invDetailErr = BigDecimal.ZERO;
//                for (InvoiceDetailDto invoiceDetailDto : invoiceSubjectDto.getInvoiceDetailList()) {
//                    invDetailErr = invDetailErr.add(invoiceDetailDto.getInvDetailErr());
//                }
//
//                if (invDetailErr.abs().compareTo(InvoiceConstant.DIFF_127) > 0) {
//                    for (int i = 0; i < invoiceSubjectDto.getInvoiceDetailList().size(); ++i) {
//                        InvoiceDetailDto invoiceDetailDto = invoiceSubjectDto.getInvoiceDetailList().get(i);
//                        if ((LinePropertyEnum.FOUR.getValue().compareTo(invoiceDetailDto.getLineProperty()) != 0 || invoiceDetailDto.getAmounts().compareTo(BigDecimal.ZERO) >= 0) && invoiceDetailDto.getSplitSign() == 1) {
//                            invoiceDetailDto.setTaxAmt(invoiceDetailDto.getTaxAmt().add(invoiceDetailDto.getInvDetailErr()));
//                            invoiceSubjectDto.setTaxAmt(invoiceSubjectDto.getTaxAmt().add(invoiceDetailDto.getInvDetailErr()));
//                            invDetailErr.subtract(invoiceDetailDto.getInvDetailErr());
//                            if (billSubjectDto.getIncludeTax() == 1) {
//                                invoiceDetailDto.setAmounts(invoiceDetailDto.getAmounts().subtract(invoiceDetailDto.getInvDetailErr()));
//                                invoiceSubjectDto.setAmounts(invoiceSubjectDto.getAmounts().subtract(invoiceDetailDto.getInvDetailErr()));
//                            } else {
//                                invoiceDetailDto.setAmountsIncTax(invoiceDetailDto.getAmounts().subtract(invoiceDetailDto.getInvDetailErr()));
//                            }
//
//                            int j = i + 1;
//                            if (j < invoiceSubjectDto.getInvoiceDetailList().size()) {
//                                InvoiceDetailDto disDto = invoiceSubjectDto.getInvoiceDetailList().get(j);
//                                if (LinePropertyEnum.FOUR.getValue().compareTo(disDto.getLineProperty()) == 0 && disDto.getAmounts().compareTo(BigDecimal.ZERO) < 0) {
//                                    disDto.setTaxAmt(disDto.getTaxAmt().add(disDto.getInvDetailErr()));
//                                    invoiceSubjectDto.setTaxAmt(invoiceSubjectDto.getTaxAmt().add(disDto.getInvDetailErr()));
//                                    invDetailErr.subtract(disDto.getInvDetailErr());
//                                    if (billSubjectDto.getIncludeTax() == 1) {
//                                        disDto.setAmounts(disDto.getAmounts().subtract(disDto.getInvDetailErr()));
//                                        invoiceSubjectDto.setAmounts(invoiceSubjectDto.getAmounts().subtract(disDto.getInvDetailErr()));
//                                    } else {
//                                        disDto.setAmountsIncTax(disDto.getAmounts().subtract(disDto.getInvDetailErr()));
//                                    }
//                                }
//                            }
//
//                            if (invDetailErr.abs().compareTo(InvoiceConstant.DIFF_127) <= 0) {
//                                break;
//                            }
//                        }
//                    }
//                }
//
//                if (invDetailErr.abs().compareTo(InvoiceConstant.DIFF_127) > 0) {
//                    for (int i = 0; i < invoiceSubjectDto.getInvoiceDetailList().size(); ++i) {
//                        InvoiceDetailDto invoiceDetailDto = invoiceSubjectDto.getInvoiceDetailList().get(i);
//                        if ((LinePropertyEnum.FOUR.getValue().compareTo(invoiceDetailDto.getLineProperty()) != 0 || invoiceDetailDto.getAmounts().compareTo(BigDecimal.ZERO) >= 0) && invoiceDetailDto.getSplitSign() != 1) {
//                            invoiceDetailDto.setTaxAmt(invoiceDetailDto.getTaxAmt().add(invoiceDetailDto.getInvDetailErr()));
//                            invoiceSubjectDto.setTaxAmt(invoiceSubjectDto.getTaxAmt().add(invoiceDetailDto.getInvDetailErr()));
//                            invDetailErr.subtract(invoiceDetailDto.getInvDetailErr());
//                            if (billSubjectDto.getIncludeTax() == 1) {
//                                invoiceDetailDto.setAmounts(invoiceDetailDto.getAmounts().subtract(invoiceDetailDto.getInvDetailErr()));
//                                invoiceSubjectDto.setAmounts(invoiceSubjectDto.getAmounts().subtract(invoiceDetailDto.getInvDetailErr()));
//                            } else {
//                                invoiceDetailDto.setAmountsIncTax(invoiceDetailDto.getAmounts().subtract(invoiceDetailDto.getInvDetailErr()));
//                            }
//
//                            int j = i + 1;
//                            if (j < invoiceSubjectDto.getInvoiceDetailList().size()) {
//                                InvoiceDetailDto disDto = invoiceSubjectDto.getInvoiceDetailList().get(j);
//                                if (LinePropertyEnum.FOUR.getValue().compareTo(disDto.getLineProperty()) == 0 && disDto.getAmounts().compareTo(BigDecimal.ZERO) < 0) {
//                                    disDto.setTaxAmt(disDto.getTaxAmt().add(disDto.getInvDetailErr()));
//                                    invoiceSubjectDto.setTaxAmt(invoiceSubjectDto.getTaxAmt().add(disDto.getInvDetailErr()));
//                                    invDetailErr.subtract(disDto.getInvDetailErr());
//                                    if (billSubjectDto.getIncludeTax() == 1) {
//                                        disDto.setAmounts(disDto.getAmounts().subtract(disDto.getInvDetailErr()));
//                                        invoiceSubjectDto.setAmounts(invoiceSubjectDto.getAmounts().subtract(disDto.getInvDetailErr()));
//                                    } else {
//                                        disDto.setAmountsIncTax(disDto.getAmounts().subtract(disDto.getInvDetailErr()));
//                                    }
//                                }
//                            }
//
//                            if (invDetailErr.abs().compareTo(InvoiceConstant.DIFF_127) <= 0) {
//                                break;
//                            }
//                        }
//                    }
//                }
//            }
//            return;
//        }
        List<InvoiceSubjectDto> list = smsResultDto.getInvoiceSList();
        for (InvoiceSubjectDto invoiceSubjectDto : list) {
            BigDecimal invDetailErrs = BigDecimal.ZERO;
            for (InvoiceDetailDto invoiceDetailDto : invoiceSubjectDto.getInvoiceDetailList()) {
                invDetailErrs = invDetailErrs.add(invoiceDetailDto.getInvDetailErr());
            }

            if (invDetailErrs.abs().compareTo(InvoiceConstant.DIFF_127) > 0) {
                BigDecimal invDetailErr = BigDecimal.ZERO;

                for (int i = 0; i < invoiceSubjectDto.getInvoiceDetailList().size(); ++i) {
                    InvoiceDetailDto invoiceDetailDto = invoiceSubjectDto.getInvoiceDetailList().get(i);
                    if (LinePropertyEnum.FOUR.getValue().compareTo(invoiceDetailDto.getLineProperty()) != 0 || invoiceDetailDto.getAmounts().compareTo(BigDecimal.ZERO) >= 0) {
                        invDetailErr = invDetailErr.add(invoiceDetailDto.getInvDetailErr());
                        int j = i + 1;
                        if (j < invoiceSubjectDto.getInvoiceDetailList().size()) {
                            InvoiceDetailDto disDto = invoiceSubjectDto.getInvoiceDetailList().get(j);
                            if (LinePropertyEnum.FOUR.getValue().compareTo(disDto.getLineProperty()) == 0 && disDto.getAmounts().compareTo(BigDecimal.ZERO) < 0) {
                                invDetailErr = invDetailErr.add(disDto.getInvDetailErr());
                            }
                        }
                        if (invDetailErr.abs().compareTo(InvoiceConstant.DIFF_127) > 0) {
                            throw new EtcRuleException("整张发票明细行税额误差之和超过1.27，不允许提交开票，请调整税额数值或调整拆合规则！");
                        }
                    }
                }
            }
        }
    }

    // 计算发票的平均限额，并将结果存储在configDto中
    public static void invoiceAverageLimit(BillSubjectDto subjectDto, SmruleConfigDto configDto) {
        subjectDto.getBillDList().stream().map(BillDetailDto::getAmounts).reduce(BigDecimal::add).ifPresent(subjectDto::setSumAmtJE);
        subjectDto.getBillDList().stream().map(BillDetailDto::getTaxAmt).reduce(BigDecimal::add).ifPresent(subjectDto::setSumAmtSE);
        BigDecimal invLimitAmt = configDto.getInvLimitAmt();
        configDto.setFinalLimitAmt(invLimitAmt);
    }

    private static String getInvSN() {
        return UUID.randomUUID().toString();
    }
}