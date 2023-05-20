package com.szhtxx.etcloud.smser.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.*;
import com.alibaba.fastjson.*;
import org.apache.commons.beanutils.*;
import java.lang.reflect.*;
import org.apache.commons.collections.*;
import java.math.*;
import com.szhtxx.etcloud.smser.utils.*;
import com.szhtxx.etcloud.smser.dto.*;
import java.util.function.*;
import java.util.*;
import java.util.stream.*;

public class GoodsMergeService
{
    private static final Logger LOG;
    
    static {
        LOG = LoggerFactory.getLogger((Class)GoodsMergeService.class);
    }
    
    // 合并商品行，将相同商品的行合并为一行，并计算合并后的行的金额、税额等信息
    public static BillDealResultDto mergeGoods(final BillSubjectDto billSubjectDto, final SmruleConfigDto smruleConfig) {
        final List<BillDetailDto> detailList = billSubjectDto.getBillDList();
        GoodsMergeService.LOG.debug("合并商品行，明细行:{},拆合规则:{} ", JSON.toJSONString(detailList), JSON.toJSONString(smruleConfig));
        final BillDealResultDto resultDto = new BillDealResultDto();
        resultDto.setBillNO(detailList.get(0).getBillNO());
        resultDto.setSuccess(true);
        final int mergeGoodsLine = smruleConfig.getMergeGoodsLine();
        if (mergeGoodsLine == 0) {
            return resultDto;
        }
        final List<BillDetailDto> originalDetailList = new ArrayList<BillDetailDto>(detailList.size() * 2);
        for (int index = 0; index < detailList.size(); ++index) {
            final BillDetailDto billDetailDto = new BillDetailDto();
            try {
                BeanUtils.copyProperties(billDetailDto, detailList.get(index));
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            originalDetailList.add(billDetailDto);
        }
        dealMergeIgnoreError(detailList, mergeGoodsLine);
        GoodsMergeService.LOG.debug("预合并(忽略误差)，明细行结果:{}", JSON.toJSONString(detailList));
        for (int nowIndex = 0; nowIndex < detailList.size(); ++nowIndex) {
            final BillDetailDto nowDetail = detailList.get(nowIndex);
            if (nowDetail.getLineProperty() != 4) {
                if (nowDetail.getDetailIdSet().size() > 1) {
                    final List<BillDetailDto> preRemoveList = new ArrayList<BillDetailDto>(4);
                    dealGoodsTax(smruleConfig, originalDetailList, detailList, nowDetail, preRemoveList);
                    if (nowDetail.getDetailIdSet().size() > 1) {
                        if (nowIndex + 1 < detailList.size()) {
                            final BillDetailDto nowDiscountDetail = detailList.get(nowIndex + 1);
                            dealDiscountTax(nowDiscountDetail, smruleConfig.getLineTaxAmtErr());
                        }
                        final int calType = (mergeGoodsLine == 2) ? 1 : 0;
                        if (nowDetail.getDetailIdSet().size() > 1) {
                            dealAmtsAndPrice(nowDetail, smruleConfig, calType);
                        }
                        dealRemark(smruleConfig, originalDetailList, nowDetail);
                    }
                }
            }
        }
        GoodsMergeService.LOG.debug("合并商品行，最终结果:{}", JSON.toJSONString(detailList));
        return resultDto;
    }
    
    public static void dealGoodsTax(final SmruleConfigDto smruleConfig, final List<BillDetailDto> originalBillDetailList, final List<BillDetailDto> billDetailList, final BillDetailDto nowDetail, final List<BillDetailDto> preRemoveList) {
        BigDecimal calTax = CalTaxUtils.calTax(0, nowDetail.getAmounts(), nowDetail.getTaxRate());
        final BigDecimal taxError = calTax.subtract(nowDetail.getTaxAmt()).setScale(2, 4);
        if (taxError.abs().compareTo(smruleConfig.getLineTaxAmtErr()) <= 0) {
            if (CollectionUtils.isNotEmpty((Collection)preRemoveList)) {
                billDetailList.addAll(preRemoveList);
            }
            return;
        }
        if (smruleConfig.getTotalTaxamtCount() == 1) {
            calTax = CalTaxUtils.calTax(nowDetail.getIncludeTax(), nowDetail.getAmountsByTax(), nowDetail.getTaxRate());
            GoodsMergeService.LOG.debug("商品行合并超误差，税额重新计算={}", calTax);
            nowDetail.setTaxAmt(calTax);
            nowDetail.setOtherMoney();
        }
        else {
            final BillDetailDto removeDetail = findAbsMaxTaxError(nowDetail, originalBillDetailList);
            GoodsMergeService.LOG.debug("预合并超误差，移除的最大误差商品行:【{}】", removeDetail.getBillDetailNO());
            final List<BillDetailDto> removeList = removeBillDetail(nowDetail, removeDetail, billDetailList, originalBillDetailList);
            dealRemoveList(preRemoveList, removeList, billDetailList, smruleConfig);
            dealGoodsTax(smruleConfig, originalBillDetailList, billDetailList, nowDetail, preRemoveList);
        }
    }
    
    public static void dealDiscountTax(final BillDetailDto nowDiscountDetail, final BigDecimal lineTaxAmtErr) {
        if (nowDiscountDetail.getLineProperty() == 4) {
            final BigDecimal money = (nowDiscountDetail.getIncludeTax() == 0) ? nowDiscountDetail.getAmounts() : nowDiscountDetail.getAmountsIncTax();
            final BigDecimal calDiscountTax = CalTaxUtils.calTax(nowDiscountDetail.getIncludeTax(), money, nowDiscountDetail.getTaxRate());
            final BigDecimal dicountTaxError = calDiscountTax.subtract(nowDiscountDetail.getTaxAmt()).abs().setScale(2, 4);
            if (dicountTaxError.compareTo(lineTaxAmtErr) > 0) {
                nowDiscountDetail.setTaxAmt(calDiscountTax);
                if (nowDiscountDetail.getIncludeTax() == 0) {
                    nowDiscountDetail.setAmountsIncTax(money.add(calDiscountTax));
                }
                else {
                    nowDiscountDetail.setAmounts(money.subtract(calDiscountTax));
                }
            }
        }
    }
    
    public static void dealMergeIgnoreError(final List<BillDetailDto> detailList, final int mergeGoodsLine) {
        for (int nowIndex = 0; nowIndex < detailList.size(); ++nowIndex) {
            final BillDetailDto nowDetail = detailList.get(nowIndex);
            if (nowDetail.getLineProperty() != 4) {
                if (nowDetail.getAmounts().compareTo(BigDecimal.ZERO) > 0) {
                    for (int nextIndex = nowIndex + 1; nextIndex < detailList.size(); ++nextIndex) {
                        final BillDetailDto nextDetail = detailList.get(nextIndex);
                        if (isMergeCondition(nowDetail, nextDetail, mergeGoodsLine)) {
                            final BigDecimal mergeAmounts = nowDetail.getAmounts().add(nextDetail.getAmounts()).setScale(2, 4);
                            final BigDecimal mergeAmountsIncTax = nowDetail.getAmountsIncTax().add(nextDetail.getAmountsIncTax()).setScale(2, 4);
                            final BigDecimal mergeTaxAmt = nowDetail.getTaxAmt().add(nextDetail.getTaxAmt()).setScale(2, 4);
                            final BigDecimal mergeAmts = BigDecimalUtils.add(nowDetail.getAmts(), nextDetail.getAmts(), BigDecimal.ZERO);
                            nowDetail.setAmounts(mergeAmounts);
                            nowDetail.setAmountsIncTax(mergeAmountsIncTax);
                            nowDetail.setTaxAmt(mergeTaxAmt);
                            if (mergeAmts != null && mergeAmts.compareTo(BigDecimal.ZERO) != 0) {
                                nowDetail.setAmts(mergeAmts);
                            }
                            final GoodsMergeResultDto discountDealResult = dealDiscountMerge(detailList, nowIndex, nextIndex);
                            nowIndex = discountDealResult.getNowIndex();
                            nextIndex = discountDealResult.getNextIndex();
                            nowDetail.addDetailId(nextDetail.getDetailIdSet());
                            detailList.remove(nextIndex);
                            --nextIndex;
                        }
                    }
                }
            }
        }
    }
    
    public static GoodsMergeResultDto dealDiscountMerge(final List<BillDetailDto> detailList, final int nowIndex, int nextIndex) {
        BillDetailDto nextDiscountDetail = null;
        BillDetailDto nowDiscountDetail = null;
        final BillDetailDto nowDetail = detailList.get(nowIndex);
        boolean hasNextDiscount = false;
        int discountIndex = nextIndex + 1;
        if (discountIndex < detailList.size() && detailList.get(discountIndex).getLineProperty() == 4) {
            hasNextDiscount = true;
            nextDiscountDetail = detailList.get(discountIndex);
        }
        if (!hasNextDiscount) {
            return new GoodsMergeResultDto(nowIndex, nextIndex);
        }
        boolean hasNowDiscount = false;
        discountIndex = nowIndex + 1;
        if (discountIndex < detailList.size() && detailList.get(discountIndex).getLineProperty() == 4) {
            hasNowDiscount = true;
            nowDiscountDetail = detailList.get(discountIndex);
        }
        if (hasNowDiscount) {
            final BigDecimal disAmounts = nextDiscountDetail.getAmounts().add(nowDiscountDetail.getAmounts()).setScale(2, 4);
            final BigDecimal disAmountsIncTax = nextDiscountDetail.getAmountsIncTax().add(nowDiscountDetail.getAmountsIncTax()).setScale(2, 4);
            final BigDecimal disTaxAmt = nextDiscountDetail.getTaxAmt().add(nowDiscountDetail.getTaxAmt()).setScale(2, 4);
            nowDiscountDetail.setAmounts(disAmounts);
            nowDiscountDetail.setAmountsIncTax(disAmountsIncTax);
            nowDiscountDetail.setTaxAmt(disTaxAmt);
            nowDiscountDetail.addDetailId(nextDiscountDetail.getDetailIdSet());
        }
        else {
            nowDiscountDetail = new BillDetailDto();
            try {
                BeanUtils.copyProperties(nowDiscountDetail, nextDiscountDetail);
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            detailList.add(nowIndex + 1, nowDiscountDetail);
            ++nextIndex;
        }
        detailList.remove(nextDiscountDetail);
        nowDetail.setLineProperty(3);
        final BigDecimal disRate = detailList.get(nowIndex + 1).getAmounts().multiply(new BigDecimal("100")).divide(detailList.get(nowIndex).getAmounts(), 3, 4).abs();
        nowDetail.setDisRate(disRate);
        return new GoodsMergeResultDto(nowIndex, nextIndex);
    }
    
    public static void dealAmtsAndPrice(final BillDetailDto nowDetail, final SmruleConfigDto smruleConfigDto, final int calType) {
        final BigDecimal lineAmountErr = smruleConfigDto.getLineAmountErr();
        BigDecimal amts = nowDetail.getAmts();
        BigDecimal price = nowDetail.getPrice();
        BigDecimal priceIncTax = nowDetail.getPriceIncTax();
        final BigDecimal amounts = nowDetail.getAmounts();
        final BigDecimal amountsIncTax = nowDetail.getAmountsIncTax();
        if (amts == null || amts.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        final Map<String, Integer> digitByRuleConfig = ComUtil.getDigitByRuleConfig(smruleConfigDto);
        final int amtNumber = digitByRuleConfig.get("amtNumber");
        final int priceNumber = digitByRuleConfig.get("priceNumber");
        if (amts.setScale(amtNumber, 4).compareTo(BigDecimal.ZERO) != 0) {
            amts = amts.setScale(amtNumber, 4);
            GoodsMergeService.LOG.debug("数量：保留【{}】位小数后为【{}】", amtNumber, amts);
        }
        BigDecimal amountsError = amts.multiply(price).setScale(2, 4).subtract(amounts).abs();
        BigDecimal amountsIncludeTaxError = amts.multiply(priceIncTax).setScale(2, 4).subtract(amountsIncTax).abs();
        if (nowDetail.getIncludeTax() == 0) {
            if (amountsError.compareTo(lineAmountErr) > 0) {
                amts = BigDecimalUtils.recursionDivision(amounts, price, amtNumber, lineAmountErr);
                amountsIncludeTaxError = amts.multiply(priceIncTax).setScale(2, 4).subtract(amountsIncTax).abs();
            }
            if (amountsIncludeTaxError.compareTo(lineAmountErr) > 0) {
                GoodsMergeService.LOG.debug("反算另一个单价（含税单价）=价税合计 / 数量");
                priceIncTax = BigDecimalUtils.recursionDivision(amountsIncTax, amts, priceNumber, lineAmountErr);
            }
        }
        if (1 == nowDetail.getIncludeTax()) {
            if (amountsIncludeTaxError.compareTo(lineAmountErr) > 0) {
                amts = BigDecimalUtils.recursionDivision(amountsIncTax, priceIncTax, amtNumber, lineAmountErr);
                amountsError = amts.multiply(price).setScale(2, 4).subtract(amountsIncTax).abs();
            }
            if (amountsError.compareTo(lineAmountErr) > 0) {
                GoodsMergeService.LOG.debug("反算另一个单价（不含税单价）=不含税金额 / 数量");
                price = BigDecimalUtils.recursionDivision(amounts, amts, priceNumber, lineAmountErr);
            }
        }
        GoodsMergeService.LOG.debug("设置商品行【{}】：单价=【{}】，含税单价=【{}】，数量=【{}】", new Object[] { nowDetail.getBillDetailNO(), price, priceIncTax, amts });
        nowDetail.setPrice(price);
        nowDetail.setPriceIncTax(priceIncTax);
        nowDetail.setAmts(amts);
    }
    
    public static void dealRemark(final SmruleConfigDto smruleConfig, final List<BillDetailDto> detailList, final BillDetailDto nowDetail) {
        String mergeRemark = nowDetail.getLineNote();
        final Set<BillDetailIdDto> detailIdSet = nowDetail.getDetailIdSet();
        final List<BillDetailIdDto> sort = new ArrayList<BillDetailIdDto>(detailIdSet);
        Collections.sort(sort, Comparator.comparing((Function<? super BillDetailIdDto, ? extends Comparable>)BillDetailIdDto::getBillDetailNO));
        for (final BillDetailIdDto next : sort) {
            if (next.getBillDetailNO().equals(nowDetail.getBillDetailNO())) {
                continue;
            }
            BillDetailDto nextDetail = null;
            for (final BillDetailDto billDetailDto : detailList) {
                if (billDetailDto.getBillDetailNO().equals(next.getBillDetailNO())) {
                    nextDetail = billDetailDto;
                    break;
                }
            }
            if (!StringUtils.isNotEmpty((CharSequence)nextDetail.getLineNote())) {
                continue;
            }
            if (StringUtils.isNotEmpty((CharSequence)mergeRemark) && (smruleConfig.isMccRepeat() || !mergeRemark.equals(nextDetail.getLineNote()))) {
                mergeRemark = String.valueOf(mergeRemark) + smruleConfig.getMccNoteStr() + nextDetail.getLineNote();
            }
            else {
                mergeRemark = nextDetail.getLineNote();
            }
        }
        GoodsMergeService.LOG.debug("设置商品行【{}】：备注=【{}】", nowDetail.getBillDetailNO(), mergeRemark);
        nowDetail.setLineNote(mergeRemark);
    }
    
    public static BillDetailDto findAbsMaxTaxError(final BillDetailDto nowDetail, final List<BillDetailDto> originalBillDetailList) {
        final Set<BillDetailIdDto> detailIdSet = nowDetail.getDetailIdSet();
        final Map<String, BillDetailDto> billDetailMap = originalBillDetailList.stream().collect(Collectors.toMap(BillDetailDto::getBillDetailNO, billDetailDto -> billDetailDto));
        final List<BillDetailDto> mergeDetailList = new ArrayList<BillDetailDto>(detailIdSet.size() * 2);
        for (final BillDetailIdDto billDetailIdDto : detailIdSet) {
            if (billDetailIdDto.getBillDetailNO().equals(nowDetail.getBillDetailNO())) {
                continue;
            }
            mergeDetailList.add(billDetailMap.get(billDetailIdDto.getBillDetailNO()));
        }
        BillDetailDto removeDetail;
        if (nowDetail.queryLineTaxError().compareTo(BigDecimal.ZERO) > 0) {
            removeDetail = mergeDetailList.stream().max(Comparator.comparing(BillDetailDto::queryLineTaxError)).get();
        }
        else {
            removeDetail = mergeDetailList.stream().min(Comparator.comparing(BillDetailDto::queryLineTaxError)).get();
        }
        return removeDetail;
    }
    
    public static List<BillDetailDto> removeBillDetail(final BillDetailDto mergeDetail, final BillDetailDto removeDetail, final List<BillDetailDto> mergeBillDetailList, final List<BillDetailDto> originalBillDetailList) {
        final List<BillDetailDto> removeDetailList = new ArrayList<BillDetailDto>();
        removeDetailList.add(removeDetail);
        final Set<BillDetailIdDto> detailIdSet = mergeDetail.getDetailIdSet();
        final Iterator<BillDetailIdDto> iterator = detailIdSet.iterator();
        while (iterator.hasNext()) {
            final BillDetailIdDto next = iterator.next();
            if (removeDetail.getBillDetailNO().equals(next.getBillDetailNO())) {
                iterator.remove();
                break;
            }
        }
        mergeDetail.setAmounts(mergeDetail.getAmounts().subtract(removeDetail.getAmounts()));
        mergeDetail.setTaxAmt(mergeDetail.getTaxAmt().subtract(removeDetail.getTaxAmt()));
        mergeDetail.setAmountsIncTax(mergeDetail.getAmountsIncTax().subtract(removeDetail.getAmountsIncTax()));
        if (mergeDetail.getAmts() != null && removeDetail.getAmts() != null) {
            mergeDetail.setAmts(mergeDetail.getAmts().subtract(removeDetail.getAmts()));
        }
        final int removeIndex = originalBillDetailList.indexOf(removeDetail);
        if (removeIndex + 1 < originalBillDetailList.size() && originalBillDetailList.get(removeIndex + 1).getLineProperty() == 4) {
            final BillDetailDto removeDisconutDetail = originalBillDetailList.get(removeIndex + 1);
            final int nowIndex = mergeBillDetailList.indexOf(mergeDetail);
            final BillDetailDto nowDiscountDetail = mergeBillDetailList.get(nowIndex + 1);
            nowDiscountDetail.setAmounts(nowDiscountDetail.getAmounts().subtract(removeDisconutDetail.getAmounts()));
            nowDiscountDetail.setTaxAmt(nowDiscountDetail.getTaxAmt().subtract(removeDisconutDetail.getTaxAmt()));
            nowDiscountDetail.setAmountsIncTax(nowDiscountDetail.getAmountsIncTax().subtract(removeDisconutDetail.getAmountsIncTax()));
            final BigDecimal amountsByTax = nowDiscountDetail.getAmountsByTax();
            if (amountsByTax.compareTo(BigDecimal.ZERO) == 0) {
                mergeBillDetailList.remove(nowDiscountDetail);
            }
            final Set<BillDetailIdDto> discountDetailIdSet = nowDiscountDetail.getDetailIdSet();
            final Iterator<BillDetailIdDto> discountIterator = discountDetailIdSet.iterator();
            while (discountIterator.hasNext()) {
                final BillDetailIdDto next2 = discountIterator.next();
                if (removeDisconutDetail.getBillDetailNO().equals(next2.getBillDetailNO())) {
                    discountIterator.remove();
                    break;
                }
            }
            removeDetailList.add(removeDisconutDetail);
        }
        return removeDetailList;
    }
    
    public static void dealRemoveList(final List<BillDetailDto> preRemoveList, final List<BillDetailDto> removeList, final List<BillDetailDto> mergeList, final SmruleConfigDto smruleConfig) {
        final BillDetailDto removeDetail = removeList.get(0);
        if (CollectionUtils.isEmpty((Collection)preRemoveList)) {
            preRemoveList.addAll(removeList);
        }
        else {
            final List<BillDetailDto> removeMergeList = new ArrayList<BillDetailDto>(8);
            final BillDetailDto preRemoveDetail = preRemoveList.get(0);
            if (preRemoveDetail.getBillDetailNO().compareTo(removeDetail.getBillDetailNO()) > 0) {
                for (final BillDetailDto billDetailDto : removeList) {
                    final BillDetailDto newDetail = new BillDetailDto();
                    try {
                        BeanUtils.copyProperties(newDetail, billDetailDto);
                    }
                    catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    removeMergeList.add(newDetail);
                }
                for (final BillDetailDto billDetailDto : preRemoveList) {
                    final BillDetailDto newDetail = new BillDetailDto();
                    try {
                        BeanUtils.copyProperties(newDetail, billDetailDto);
                    }
                    catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    removeMergeList.add(newDetail);
                }
            }
            else {
                for (final BillDetailDto billDetailDto : preRemoveList) {
                    final BillDetailDto newDetail = new BillDetailDto();
                    try {
                        BeanUtils.copyProperties(newDetail, billDetailDto);
                    }
                    catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    removeMergeList.add(newDetail);
                }
                for (final BillDetailDto billDetailDto : removeList) {
                    final BillDetailDto newDetail = new BillDetailDto();
                    try {
                        BeanUtils.copyProperties(newDetail, billDetailDto);
                    }
                    catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    removeMergeList.add(newDetail);
                }
            }
            dealMergeIgnoreError(removeMergeList, smruleConfig.getMergeGoodsLine());
            final BillDetailDto removeMergeDetail = removeMergeList.get(0);
            final BigDecimal removeMergeTaxError = removeMergeDetail.queryLineTaxError().setScale(2, 4);
            if (removeMergeTaxError.abs().compareTo(smruleConfig.getLineTaxAmtErr()) > 0) {
                mergeList.addAll(preRemoveList);
                preRemoveList.clear();
                preRemoveList.addAll(removeList);
            }
            else {
                preRemoveList.clear();
                preRemoveList.addAll(removeMergeList);
            }
        }
    }
    
    public static boolean isMergeCondition(final BillDetailDto nowDetail, final BillDetailDto nextDetail, final int mergeGoodsLine) {
        if (nextDetail.getLineProperty() == 4 || nextDetail.getAmounts().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (BigDecimalUtils.equals(nowDetail.getTaxRate(), nextDetail.getTaxRate()) && StringUtilsEx.equalIgnoreNull(nowDetail.getGoodsName(), nextDetail.getGoodsName()) && StringUtilsEx.equalIgnoreNull(nowDetail.getGoodsModel(), nextDetail.getGoodsModel()) && StringUtilsEx.equalIgnoreNull(nowDetail.getGoodsUnit(), nextDetail.getGoodsUnit())) {
            final boolean incTaxFlag = nowDetail.getIncludeTax() != 0;
            final BigDecimal nowDetailPrice = incTaxFlag ? nowDetail.getPriceIncTax() : nowDetail.getPrice();
            final BigDecimal nextDetailPrice = incTaxFlag ? nextDetail.getPriceIncTax() : nextDetail.getPrice();
            if (BigDecimalUtils.equals(nowDetailPrice, nextDetailPrice) && mergeGoodsLine == 2) {
                return true;
            }
        }
        return false;
    }
}
