package com.szhtxx.etcloud.smser.service;

import org.apache.commons.collections.*;
import java.math.*;
import org.apache.commons.lang3.*;
import com.szhtxx.etcloud.smser.enums.*;
import com.szhtxx.etcloud.smser.dto.*;
import java.util.*;

public class MultiMergeCoreService
{
    // 对传入的billSList列表中的元素进行排序，排序规则是按照每个元素的sortField属性进行升序排序。
    public static void sortBillSubjects(final List<BillSubjectDto> billSList) {
        if (CollectionUtils.isNotEmpty(billSList)) {
            Collections.sort(billSList, new Comparator<BillSubjectDto>() {
                @Override
                public int compare(final BillSubjectDto o1, final BillSubjectDto o2) {
                    return o1.getSortField().compareTo(o2.getSortField());
                }
            });
        }
    }
    
    // 检查传入的requestDto对象中的发票信息是否符合要求，如果不符合要求，则将错误信息添加到resultDto对象中
    public static void checkMerge(final SmsRequestDto requestDto, final SmsResultDto resultDto) {
        final BigDecimal zeroBd = BigDecimal.ZERO;
        final List<BillSubjectDto> billSList = requestDto.getBillSList();
        for (int i = 0; i < billSList.size(); ++i) {
            final BillSubjectDto billSubjectDto = billSList.get(i);
            final String billNo = billSubjectDto.getBillNO();
            final int invKind = billSubjectDto.getInvKind();
            if (StringUtils.isEmpty((CharSequence)billNo) || StringUtils.isEmpty((CharSequence)billNo.trim())) {
                final BillDealResultDto dealResultDto = getBDR(billNo, false, "传入单据编号为空");
                resultDto.getBdrList().add(dealResultDto);
                billSubjectDto.setCheckPassed(false);
            }
            else if (CollectionUtils.isEmpty(billSubjectDto.getBillDList())) {
                final BillDealResultDto dealResultDto = getBDR(billNo, false, "传入单据明细为空");
                resultDto.getBdrList().add(dealResultDto);
                billSubjectDto.setCheckPassed(false);
            }
            else if (EnumType.InvKindEnum.SPECIAL.getValue() == invKind && requestDto.getSiAmt().compareTo(zeroBd) <= 0) {
                final BillDealResultDto dealResultDto = getBDR(billNo, false, "传入纸质专用发票拆分限额为0");
                resultDto.getBdrList().add(dealResultDto);
                billSubjectDto.setCheckPassed(false);
            }
            else if (EnumType.InvKindEnum.NORMAL.getValue() == invKind && requestDto.getCiAmt().compareTo(zeroBd) <= 0) {
                final BillDealResultDto dealResultDto = getBDR(billNo, false, "传入纸质普通发票拆分限额为0");
                resultDto.getBdrList().add(dealResultDto);
                billSubjectDto.setCheckPassed(false);
            }
            else if (EnumType.InvKindEnum.EINV.getValue() == invKind && requestDto.getEiAmt().compareTo(zeroBd) <= 0) {
                final BillDealResultDto dealResultDto = getBDR(billNo, false, "传入电子普通发票拆分限额为0");
                resultDto.getBdrList().add(dealResultDto);
                billSubjectDto.setCheckPassed(false);
            }
            else if (EnumType.InvKindEnum.ESINV.getValue() == invKind && requestDto.getEsiAmt().compareTo(zeroBd) <= 0) {
                final BillDealResultDto dealResultDto = getBDR(billNo, false, "传入电子专用发票拆分限额为0");
                resultDto.getBdrList().add(dealResultDto);
                billSubjectDto.setCheckPassed(false);
            }
            else if (EnumType.InvKindEnum.ROLL.getValue() == invKind && requestDto.getVlAmt().compareTo(zeroBd) <= 0) {
                final BillDealResultDto dealResultDto = getBDR(billNo, false, "传入卷票票拆分限额为0");
                resultDto.getBdrList().add(dealResultDto);
                billSubjectDto.setCheckPassed(false);
            }
            else if (EnumType.InvKindEnum.notExisted(invKind)) {
                final BillDealResultDto dealResultDto = getBDR(billNo, false, "传入的发票种类:" + invKind + "不正确。");
                resultDto.getBdrList().add(dealResultDto);
                billSubjectDto.setCheckPassed(false);
            }
            else if (!includeTaxIsSame(billSubjectDto, resultDto)) {
                final BillDealResultDto dealResultDto = getBDR(billNo, false, "单据的含税标识要跟单据明细含税标识保持一致");
                resultDto.getBdrList().add(dealResultDto);
                billSubjectDto.setCheckPassed(false);
            }
        }
    }
    
    public static BillDealResultDto getBDR(final String billNO, final boolean success, final String errorMsg) {
        final BillDealResultDto bdr = new BillDealResultDto();
        bdr.setBillNO(billNO);
        bdr.setSuccess(success);
        bdr.setErrorMsg(errorMsg);
        return bdr;
    }
    
    private static Boolean includeTaxIsSame(final BillSubjectDto billSubjectDto, final SmsResultDto resultDto) {
        Boolean flag = true;
        final List<BillDetailDto> billDetailDtos = billSubjectDto.getBillDList();
        final int includeTaxTmp = billSubjectDto.getIncludeTax();
        if (CollectionUtils.isNotEmpty(billDetailDtos)) {
            for (final BillDetailDto detailDto : billDetailDtos) {
                final int includeTax = detailDto.getIncludeTax();
                if (includeTaxTmp != includeTax) {
                    flag = false;
                    break;
                }
            }
        }
        return flag;
    }
}
