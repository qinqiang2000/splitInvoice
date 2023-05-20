package com.szhtxx.etcloud.smser;

import com.szhtxx.etcloud.smser.methods.smser.invoice.*;
import org.slf4j.*;
import org.apache.commons.lang3.*;
import java.math.*;
import com.szhtxx.etcloud.smser.dto.*;
import java.lang.reflect.*;
import java.util.*;
import org.apache.commons.beanutils.*;
import org.apache.commons.collections.*;
import com.szhtxx.etcloud.smser.service.*;
import com.szhtxx.etcloud.smser.exception.*;
import com.szhtxx.etcloud.smser.enums.*;

public class SMSERN {
    private static Logger logger;
    static InvoiceMethods invoiceMethods;

    static {
        SMSERN.logger = LoggerFactory.getLogger(SMSERN.class);
        SMSERN.invoiceMethods = new InvoiceMethods();
    }

    public SmsResultDto doSMSER(final SmsRequestDto requestDto) {
        if (requestDto.getSmr().getSplitGoodsWithNumber() == 1) {
            requestDto.getSmr().setSplitGoodsWithNumber(2);
        }
        requestDto.getSmr().setSplitListType(1);

        SmsResultDto resultDto = new SmsResultDto();

        int totals = 0;
        int failNums = 0;
        try {
            this.checkSms(requestDto);

            if (requestDto.getSmr() == null) {
                requestDto.setSmr(new SmruleConfigDto());
            }

            totals = requestDto.getBillSList().size();

            // 校验requestDto的数据
            MultiMergeCoreService.checkMerge(requestDto, resultDto);

            SmruleConfigDto smruleConfigDto = requestDto.getSmr();
            List<BillSubjectDto> billSList = requestDto.getBillSList();
            if (smruleConfigDto.isMcc()) {
                // 对单据明细进行排序
                MultiMergeCoreService.sortBillSubjects(billSList);
            }

            failNums = this.ruleBillSubjects(requestDto, resultDto);
        } catch (SmserException e) {
            SMSERN.logger.error("入参校验检查异常 e={}", (Throwable) e);
            resultDto.setSuccess(false);
            resultDto.setErrorMsg(e.getMessage());
            return resultDto;
        } catch (Exception e2) {
            SMSERN.logger.error("单据处理异常", (Throwable) e2);
            resultDto.setSuccess(false);
            resultDto.setErrorMsg("异常" + e2.getMessage());
            System.currentTimeMillis();
        }

        resultDto.setTotals(totals);
        resultDto.setDoSucc(totals - failNums);
        resultDto.setDoFail(failNums);
        return resultDto;
    }

    /*
     * 对requestDto中的单据进行规则处理，并将处理结果存储在resultDto中。
     * 在处理单据之前，该方法会先对requestDto进行校验，如果校验不通过，则会将错误信息存储在resultDto中。
     * 在处理单据时，该方法会根据requestDto中的规则对单据进行排序，并将排序后的单据存储在billSList中。
     * 然后，该方法会遍历billSList中的每个单据，对每个单据进行处理，并将处理结果存储在resultDto中。
     * 最后，该方法会返回处理失败的单据数量。
     */
    private int ruleBillSubjects(final SmsRequestDto requestDto, final SmsResultDto resultDto)
            throws IllegalAccessException, InvocationTargetException {
        final SmruleConfigDto smruleConfigDto = requestDto.getSmr();
        final List<BillSubjectDto> billSList = requestDto.getBillSList();
        final List<BillDetailDto> billDList = new ArrayList<BillDetailDto>();
        final String isOil = requestDto.getIsOil();
        int failNums = 0;
        String mccNote = "";

        final int size = billSList.size();
        int i = 0;
        while (i < size) {
            int billNums = 0;
            final BillSubjectDto curBillSubject = billSList.get(i);
            curBillSubject.setIsOil(isOil);
            final String curBillNo = curBillSubject.getBillNO();
            
            if (!curBillSubject.getCheckPassed()) {
                ++failNums;
                ++i;
                continue;
            }

            // 如果配置MCC，则将连续的MCC单据合并（MCC是？）
            if (smruleConfigDto.isMcc()) {
                final int lastLineIndex = this.findLastSameClassIndex(billSList, i);
                int j = i;
                while (j <= lastLineIndex) {
                    final BillSubjectDto jdto = billSList.get(j);
                    billDList.addAll(jdto.getBillDList());
                    ++j;
                    if (smruleConfigDto.isMccNote()) {
                        final String curNotes = jdto.getNotes();
                        mccNote = mccNote + (StringUtils.isEmpty(curNotes) ?
                            "" : (curNotes + smruleConfigDto.getMccNoteStr()));
                    }
                }
                billNums = lastLineIndex - i + 1;
                i = lastLineIndex + 1;
            } else {
                billNums = 1;
                ++i;
            }
            
            // 根据发票种类，设置限额
            final int curInvKind = curBillSubject.getInvKind();
            final BigDecimal invLimitAmt = this.getInvLimitAmt(curInvKind, requestDto);
            smruleConfigDto.setInvLimitAmt(invLimitAmt);

            if (StringUtils.isNotEmpty((CharSequence) mccNote)) {
                curBillSubject.setNotes(mccNote.substring(0, mccNote.length() - 1));
            }

            final BillSubjectDto newBillSubjectDto = this.buildBillSubject(curBillSubject, billDList);
            final List<BillSubjectDto> billSubjectDtos = new ArrayList<BillSubjectDto>(10);
            billSubjectDtos.add(newBillSubjectDto);
            try {
                for (final BillSubjectDto subjectDto : billSubjectDtos) {
                    this.tranferRule(subjectDto, smruleConfigDto, resultDto);
                }
            } catch (Exception e) {
                SMSERN.logger.error("拆合异常", (Throwable) e);
                failNums += billNums;
                final BillDealResultDto dealResultDto = MultiMergeCoreService.getBDR(curBillNo, false, e.getMessage());
                resultDto.getBdrList().add(dealResultDto);
                continue;
            } finally {
                billDList.clear();
            }
            billDList.clear();
        }
        return failNums;
    }

    public List<BillSubjectDto> splitBillSubject(final BillSubjectDto billSubjectDto)
            throws IllegalAccessException, InvocationTargetException {
        final List<BillSubjectDto> retList = new ArrayList<BillSubjectDto>(10);
        final Map<String, List<BillDetailDto>> map = new HashMap<String, List<BillDetailDto>>(10);
        final List<BillDetailDto> billDetailDtos = billSubjectDto.getBillDList();
        for (final BillDetailDto billDetailDto : billDetailDtos) {
            final BigDecimal taxRate = billDetailDto.getTaxRate();
            final String taxRateStr = taxRate.toPlainString();
            if (map.containsKey(taxRateStr)) {
                final List<BillDetailDto> detailDtos = map.get(taxRateStr);
                detailDtos.add(billDetailDto);
            } else {
                final List<BillDetailDto> dtos = new ArrayList<BillDetailDto>(10);
                dtos.add(billDetailDto);
                map.put(taxRateStr, dtos);
            }
        }
        for (final Map.Entry<String, List<BillDetailDto>> entry : map.entrySet()) {
            final List<BillDetailDto> mapValue = entry.getValue();
            final BillSubjectDto newBillSubjectDto = new BillSubjectDto();
            BeanUtils.copyProperties((Object) newBillSubjectDto, (Object) billSubjectDto);
            newBillSubjectDto.setBillDList(mapValue);
            retList.add(newBillSubjectDto);
        }
        return retList;
    }

    // 查找billSList列表中从curIndex开始的，最后一个与当前元素类别相同的元素的下标
    private int findLastSameClassIndex(final List<BillSubjectDto> billSList, final int curIndex) {
        final int size = billSList.size();
        final BillSubjectDto curDto = billSList.get(curIndex);
        final String custName = curDto.getCustName();
        final Integer includeTax = curDto.getIncludeTax();
        final int invKind = curDto.getInvKind();
        if (StringUtils.isEmpty(custName)) {
            return curIndex;
        }
        for (int i = curIndex; i < size; ++i) {
            final int j = i + 1;
            if (j >= size) {
                return i;
            }
            final BillSubjectDto item = billSList.get(j);
            final String itemCustName = item.getCustName();
            final Integer itemInvKind = item.getInvKind();
            final Integer itemIncludeTax = item.getIncludeTax();
            if (!StringUtils.isNotEmpty(itemCustName) || !itemCustName.equals(custName)
                    || itemInvKind != invKind || itemIncludeTax != (int) includeTax) {
                return i;
            }
        }
        return -1;
    }

    private BillSubjectDto buildBillSubject(final BillSubjectDto preBillSubjectDto, final List<BillDetailDto> billDList)
            throws IllegalAccessException, InvocationTargetException {
        final BillSubjectDto subjectDto = new BillSubjectDto();
        BeanUtils.copyProperties(subjectDto, preBillSubjectDto);
        final List<BillDetailDto> newBillDList = new ArrayList<BillDetailDto>();
        for (final BillDetailDto billDetail : billDList) {
            final BillDetailDto newBillDetail = new BillDetailDto();
            BeanUtils.copyProperties(newBillDetail, billDetail);
            newBillDList.add(newBillDetail);
        }
        if (CollectionUtils.isNotEmpty(newBillDList)) {
            subjectDto.setBillDList(newBillDList);
        }
        return subjectDto;
    }

    private void tranferRule(final BillSubjectDto subjectDto, final SmruleConfigDto smruleConfigDto,
            final SmsResultDto resultDto) throws EtcRuleException {
        // 校验单据明细
        BillsCheckService.billsCheck(subjectDto, smruleConfigDto);
        // 获取单张发票最大行数
        SMSERN.invoiceMethods.getMaxLine(subjectDto, smruleConfigDto);
        // 处理折扣行
        final List<BillDetailDto> billDetailDtos = GoodsDiscountService.doDisLine(subjectDto.getBillDList());
        subjectDto.setBillDList(billDetailDtos);
        // 合并商品行（如果配置了mergeGoodsLine）
        GoodsMergeService.mergeGoods(subjectDto, smruleConfigDto);
        // 处理负数行（如果配置了againstTyp）
        NegativeRowService.deal(subjectDto, smruleConfigDto);
        // 计算总金额、税额；设置最终限额金额
        SMSERN.invoiceMethods.invoiceLimit(subjectDto, smruleConfigDto, resultDto);
        // 拆分发票
        SMSERN.invoiceMethods.genInvoice(subjectDto, smruleConfigDto, resultDto);
        // 校验拆分后的发票
        SMSERN.invoiceMethods.taxAmtCheck(subjectDto, smruleConfigDto, resultDto);
    }

    private void checkSms(final SmsRequestDto requestDto) throws SmserException {
        if (requestDto == null) {
            throw new SmserException("传入数据为空");
        }
        if (requestDto.getBillSList() == null || requestDto.getBillSList().size() == 0) {
            throw new SmserException("未传入需处理单据");
        }
    }

    private BigDecimal getInvLimitAmt(final int preInvKind, final SmsRequestDto requestDto) {
        BigDecimal invLimitAmt = null;
        if (EnumType.InvKindEnum.SPECIAL.getValue() == preInvKind) {
            invLimitAmt = requestDto.getSiAmt();
        } else if (EnumType.InvKindEnum.NORMAL.getValue() == preInvKind) {
            invLimitAmt = requestDto.getCiAmt();
        } else if (EnumType.InvKindEnum.ROLL.getValue() == preInvKind) {
            invLimitAmt = requestDto.getVlAmt();
        } else if (EnumType.InvKindEnum.ESINV.getValue() == preInvKind) {
            invLimitAmt = requestDto.getEsiAmt();
        } else {
            invLimitAmt = requestDto.getEiAmt();
        }
        return invLimitAmt;
    }

    private BigDecimal getInvDiskLimitAmt(final int curInvKind, final SmsRequestDto requestDto) {
        BigDecimal invDiskLimitAmt = null;
        if (EnumType.InvKindEnum.SPECIAL.getValue() == curInvKind) {
            invDiskLimitAmt = requestDto.getDiskSiAmt();
        } else if (EnumType.InvKindEnum.NORMAL.getValue() == curInvKind) {
            invDiskLimitAmt = requestDto.getDiskCiAmt();
        } else if (EnumType.InvKindEnum.ROLL.getValue() == curInvKind) {
            invDiskLimitAmt = requestDto.getDiskVlAmt();
        } else if (EnumType.InvKindEnum.ESINV.getValue() == curInvKind) {
            invDiskLimitAmt = requestDto.getDiskEsiAmt();
        } else {
            invDiskLimitAmt = requestDto.getDiskEiAmt();
        }
        return invDiskLimitAmt;
    }
}
