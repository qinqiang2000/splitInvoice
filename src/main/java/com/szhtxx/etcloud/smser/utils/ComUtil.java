package com.szhtxx.etcloud.smser.utils;

import com.szhtxx.etcloud.smser.constant.*;
import com.szhtxx.etcloud.smser.enums.*;
import java.math.*;

import com.szhtxx.etcloud.smser.exception.*;
import org.apache.commons.collections.*;
import com.szhtxx.etcloud.smser.dto.*;
import java.util.*;

public class ComUtil
{
    public static Map<String, Integer> getDigitByRuleConfig(final SmruleConfigDto configDto) {
        final Map<String, Integer> retMap = new HashMap<String, Integer>(1);
        final Integer amtNumberType = configDto.getAmtNumberType();
        final Integer amtNumber = configDto.getAmtNumber();
        final Integer priceNumberType = configDto.getPriceNumberType();
        final Integer priceNumber = configDto.getPriceNumber();
        int digit = SmruleConfConstant.ONE;
        if (EnumType.NumberTypeEnum.ZERO.getValue() == (int)priceNumberType) {
            digit = 1;
        }
        else if (EnumType.NumberTypeEnum.ONE.getValue() == (int)priceNumberType) {
            digit = 0;
        }
        else if (EnumType.NumberTypeEnum.TWO.getValue() == (int)priceNumberType) {
            digit = priceNumber;
        }
        retMap.put("priceNumber", digit);
        if (EnumType.NumberTypeEnum.ZERO.getValue() == (int)amtNumberType) {
            digit = 1;
        }
        else if (EnumType.NumberTypeEnum.ONE.getValue() == (int)amtNumberType) {
            digit = 0;
        }
        else if (EnumType.NumberTypeEnum.TWO.getValue() == (int)amtNumberType) {
            digit = amtNumber;
        }
        retMap.put("amtNumber", digit);
        return retMap;
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
    
    public static int findObjIndexInList(final List<BillDetailDto> billDList, final BillDetailDto detailDto) {
        String msg = "";
        final String billNoP = detailDto.getBillNO();
        final String billDetailNoP = detailDto.getBillDetailNO();
        if (StringUtilsEx.isEmpty((CharSequence)billDetailNoP)) {
            msg = String.format("单据编号[%s] 单据明细编号不能为空", billNoP);
            throw new EtcRuleException(msg);
        }
        if (CollectionUtils.isNotEmpty((Collection)billDList)) {
            int i = 0;
            for (final BillDetailDto dto : billDList) {
                final String billNo = dto.getBillNO();
                final String billDetailNo = dto.getBillDetailNO();
                if (billNo.equals(billNoP) && billDetailNo.equals(billDetailNoP)) {
                    break;
                }
                ++i;
            }
            return i;
        }
        return -1;
    }
    
    public static List<BillDetailDto> bubbleSort(final BillSubjectDto subjectDto) {
        final List<BillDetailDto> list = subjectDto.getBillDList();
        final List<Map<BillDetailDto, BillDetailDto>> mapList = new ArrayList<Map<BillDetailDto, BillDetailDto>>();
        final List<BillDetailDto> billList = new ArrayList<BillDetailDto>();
        for (int i = 0; i < list.size(); ++i) {
            final BillDetailDto curDto = list.get(i);
            final BillDetailDto b = new BillDetailDto();
            final Map<BillDetailDto, BillDetailDto> map = new HashMap<BillDetailDto, BillDetailDto>();
            if (!isDisLine(curDto)) {
                final int j = i + 1;
                if (j < list.size()) {
                    final BillDetailDto disDto = list.get(j);
                    if (isDisLine(disDto)) {
                        map.put(curDto, list.get(j));
                    }
                    else {
                        map.put(curDto, b);
                    }
                }
                else {
                    map.put(curDto, b);
                }
                mapList.add(map);
            }
        }
        for (int i = 0; i < mapList.size() - 1; ++i) {
            for (int k = 0; k < mapList.size() - i - 1; ++k) {
                final Map<BillDetailDto, BillDetailDto> map2 = mapList.get(k);
                final Map<BillDetailDto, BillDetailDto> map3 = mapList.get(k + 1);
                BillDetailDto mx = new BillDetailDto();
                BigDecimal value = BigDecimal.ZERO;
                BillDetailDto mx2 = new BillDetailDto();
                BigDecimal value2 = BigDecimal.ZERO;
                final Iterator<BillDetailDto> iterator = map2.keySet().iterator();
                while (iterator.hasNext()) {
                    final BillDetailDto key = mx = iterator.next();
                    value = map2.get(mx).getAmounts();
                    if (value == null) {
                        value = BigDecimal.ZERO;
                    }
                }
                final Iterator<BillDetailDto> iterator2 = map3.keySet().iterator();
                while (iterator2.hasNext()) {
                    final BillDetailDto key = mx2 = iterator2.next();
                    value2 = map3.get(mx2).getAmounts();
                    if (value2 == null) {
                        value2 = BigDecimal.ZERO;
                    }
                }
                if (mx.getAmounts().add(value).compareTo(mx2.getAmounts().add(value2)) < 0) {
                    mapList.set(k, mapList.get(k + 1));
                    mapList.set(k + 1, map2);
                }
            }
        }
        for (int i = 0; i < mapList.size(); ++i) {
            final Map<BillDetailDto, BillDetailDto> map4 = mapList.get(i);
            for (final BillDetailDto s : map4.keySet()) {
                billList.add(s);
                final BillDetailDto curDto2 = map4.get(s);
                final BigDecimal amounts = curDto2.getAmounts();
                final BigDecimal taxAmt = curDto2.getTaxAmt();
                if (amounts == null || amounts.compareTo(BigDecimal.ZERO) == 0) {
                    if (taxAmt == null) {
                        continue;
                    }
                    if (taxAmt.compareTo(BigDecimal.ZERO) == 0) {
                        continue;
                    }
                }
                billList.add(map4.get(s));
            }
        }
        subjectDto.setBillDList(billList);
        return billList;
    }
    
    public void getMaxLine(final BillSubjectDto billSubjectDto, final SmruleConfigDto configDto) {
        final int invkind = billSubjectDto.getInvKind();
        int maxLine = 0;
        if (EnumType.InvKindEnum.SPECIAL.getValue() == invkind) {
            maxLine = configDto.getMaxSLine();
        }
        else if (EnumType.InvKindEnum.NORMAL.getValue() == invkind) {
            maxLine = configDto.getMaxCLine();
        }
        else if (EnumType.InvKindEnum.ESINV.getValue() == invkind) {
            maxLine = configDto.getMaxEsiLine();
        }
        else {
            maxLine = configDto.getMaxELine();
        }
        final String isOil = billSubjectDto.getIsOil();
        if (StringUtilsEx.isNotEmpty(isOil) && isOil.equals(EnumType.NumberTypeEnum.ONE.getValue().toString())) {
            maxLine = 8;
        }
        else if (EnumType.NumberTypeEnum.TWO.getValue() == configDto.getListType() && maxLine > 8) {
            maxLine = 8;
        }
        if (EnumType.InvKindEnum.ROLL.getValue() == invkind) {
            final String mode = billSubjectDto.getRollInvSpec();
            if (StringUtilsEx.isNotEmpty(mode) && mode.equals("01")) {
                maxLine = 13;
            }
            else {
                maxLine = 6;
            }
        }
        billSubjectDto.setLimitLine(maxLine);
    }
}
