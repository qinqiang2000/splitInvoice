package com.szhtxx.etcloud.smser.service;

import org.slf4j.*;
import com.szhtxx.etcloud.smser.dto.*;
import java.math.*;
import java.util.function.*;
import com.szhtxx.etcloud.smser.utils.*;
import com.szhtxx.etcloud.smser.exception.*;
import org.apache.commons.collections.*;
import java.util.*;

public class NegativeRowService
{
    private static final Logger LOG;
    
    static {
        LOG = LoggerFactory.getLogger((Class)NegativeRowService.class);
    }
    
    // 处理负数行
    public static BillDealResultDto deal(final BillSubjectDto billSubjectDto, final SmruleConfigDto smruleConfig) {
        merge(billSubjectDto, smruleConfig);
        return doAgainst(billSubjectDto, smruleConfig);
    }
    
    // 处理负数行，将负数行与正数行进行合并，返回处理结果
    public static BillDealResultDto merge(final BillSubjectDto billSubjectDto, final SmruleConfigDto smruleConfig) {
        final BillDealResultDto resultDto = new BillDealResultDto();
        resultDto.setBillNO(billSubjectDto.getBillNO());
        resultDto.setSuccess(true);
        final List<BillDetailDto> detailList = billSubjectDto.getBillDList();
        if (smruleConfig.getAgainstTyp() == 0) {
            return resultDto;
        }
        final Set<String> hasDealNegSet = new HashSet<String>();
        for (int negIndex = 0; negIndex < detailList.size(); ++negIndex) {
            final BillDetailDto negDetail = detailList.get(negIndex);
            if (negDetail.getAmounts().compareTo(BigDecimal.ZERO) <= 0) {
                if (negDetail.getLineProperty() != 4) {
                    if (!hasDealNegSet.contains(negDetail.getBillDetailNO())) {
                        while (true) {
                            final Optional<BillDetailDto> maxMoneyDetailOptional = detailList.stream().filter(billDetailDto -> isFitMergeCondition(negDetail, billDetailDto)).max(Comparator.comparing((Function<? super BillDetailDto, ? extends Comparable>)BillDetailDto::getAmountsByTax));
                            final BillDetailDto maxMoneyDetail = maxMoneyDetailOptional.isPresent() ? maxMoneyDetailOptional.get() : null;
                            if (maxMoneyDetail == null) {
                                break;
                            }
                            final int maxMoneyIndex = detailList.indexOf(maxMoneyDetail);
                            BillDetailDto discountDetail = new BillDetailDto();
                            if (maxMoneyIndex < detailList.size() - 1 && detailList.get(maxMoneyIndex + 1).getLineProperty() == 4) {
                                discountDetail = detailList.get(maxMoneyIndex + 1);
                            }
                            final BigDecimal actualMoney = BigDecimalUtils.add(maxMoneyDetail.getAmountsByTax(), discountDetail.getAmountsByTax(), BigDecimal.ZERO);
                            final BigDecimal actualTax = BigDecimalUtils.add(maxMoneyDetail.getTaxAmt(), discountDetail.getTaxAmt(), BigDecimal.ZERO);
                            final BigDecimal remainMoney = actualMoney.add(negDetail.getAmountsByTax());
                            final BigDecimal remainTax = actualTax.add(negDetail.getTaxAmt());
                            final BigDecimal remainAtms = BigDecimalUtils.add(negDetail.getAmts(), maxMoneyDetail.getAmts(), BigDecimal.ZERO);
                            if (remainMoney.compareTo(BigDecimal.ZERO) > 0) {
                                dealRemaimMoneyGTZero(smruleConfig, detailList, negDetail, maxMoneyDetail, remainAtms);
                                break;
                            }
                            if (remainMoney.compareTo(BigDecimal.ZERO) == 0) {
                                detailList.remove(negDetail);
                                detailList.remove(maxMoneyDetail);
                                detailList.remove(discountDetail);
                                if (!detailList.isEmpty()) {
                                    detailList.get(0).addDetailId(negDetail.getDetailIdSet());
                                    detailList.get(0).addDetailId(maxMoneyDetail.getDetailIdSet());
                                    detailList.get(0).addDetailId(discountDetail.getDetailIdSet());
                                    break;
                                }
                                break;
                            }
                            else {
                                detailList.remove(maxMoneyDetail);
                                detailList.remove(discountDetail);
                                updateDetailData(negDetail, remainAtms, remainMoney, remainTax);
                                negDetail.addDetailId(maxMoneyDetail.getDetailIdSet());
                                negDetail.addDetailId(discountDetail.getDetailIdSet());
                            }
                        }
                        negIndex = -1;
                        hasDealNegSet.add(negDetail.getBillDetailNO());
                    }
                }
            }
        }
        return resultDto;
    }
    
    public static void dealRemaimMoneyGTZero(final SmruleConfigDto smruleConfig, final List<BillDetailDto> detailList, final BillDetailDto negDetail, final BillDetailDto maxMoneyDetail, final BigDecimal remainAtms) {
        final BigDecimal goodsRemainMoney = maxMoneyDetail.getAmountsByTax().add(negDetail.getAmountsByTax());
        BigDecimal goodsRemainTax = maxMoneyDetail.getTaxAmt().add(negDetail.getTaxAmt());
        final BigDecimal calTax = CalTaxUtils.calTax(maxMoneyDetail.getIncludeTax(), goodsRemainMoney, negDetail.getTaxRate());
        final boolean taxErrorFlag = goodsRemainTax.subtract(calTax).abs().compareTo(smruleConfig.getLineTaxAmtErr()) > 0;
        if (taxErrorFlag && smruleConfig.getTotalTaxamtCount() != 1) {
            return;
        }
        if (taxErrorFlag) {
            goodsRemainTax = calTax;
        }
        detailList.remove(negDetail);
        updateDetailData(maxMoneyDetail, remainAtms, goodsRemainMoney, goodsRemainTax);
        GoodsMergeService.dealAmtsAndPrice(maxMoneyDetail, smruleConfig, 1);
        remarkDeal(negDetail, smruleConfig, maxMoneyDetail);
    }
    
    public static boolean isFitMergeCondition(final BillDetailDto negDetail, final BillDetailDto posDetail) {
        return posDetail.getAmountsByTax().compareTo(BigDecimal.ZERO) > 0 && StringUtilsEx.equalIgnoreNull(negDetail.getGoodsName(), posDetail.getGoodsName()) && StringUtilsEx.equalIgnoreNull(negDetail.getGoodsModel(), posDetail.getGoodsModel()) && StringUtilsEx.equalIgnoreNull(negDetail.getGoodsUnit(), posDetail.getGoodsUnit()) && BigDecimalUtils.equals(negDetail.getPriceByTaxFlag(), posDetail.getPriceByTaxFlag()) && BigDecimalUtils.equals(negDetail.getTaxRate(), posDetail.getTaxRate());
    }
    
    public static void updateDetailData(final BillDetailDto posDetail, final BigDecimal remainAtms, final BigDecimal goodsRemainMoney, final BigDecimal goodsRemainTax) {
        posDetail.setTaxAmt(goodsRemainTax);
        if (remainAtms != null && remainAtms.compareTo(BigDecimal.ZERO) != 0) {
            posDetail.setAmts(remainAtms);
        }
        if (posDetail.getIncludeTax() == 1) {
            posDetail.setAmountsIncTax(goodsRemainMoney);
            posDetail.setOtherMoney();
        }
        else {
            posDetail.setAmounts(goodsRemainMoney);
            posDetail.setOtherMoney();
        }
    }
    
    // 对单据的负数行进行冲抵处理
    public static BillDealResultDto doAgainst(final BillSubjectDto billSubjectDto, final SmruleConfigDto smruleConfig) {
        final String billNO = billSubjectDto.getBillNO();
        final List<BillDetailDto> detailList = billSubjectDto.getBillDList();
        final BillDealResultDto resultDto = new BillDealResultDto();
        resultDto.setBillNO(billNO);
        resultDto.setSuccess(true);
        boolean noPosDetailFlag = false;
        for (int negIndex = 0; negIndex < detailList.size(); ++negIndex) {
            final BillDetailDto detail = detailList.get(negIndex);
            if (detail.getAmounts().compareTo(BigDecimal.ZERO) < 0) {
                if (detail.getLineProperty() != 4) {
                    noPosDetailFlag = doAgainst(detail, billSubjectDto, 0, smruleConfig);
                    if (noPosDetailFlag) {
                        throw new EtcRuleException("负数行冲抵时必须税率一致才允许冲抵。与负数行税率相同的商品明细金额不够冲抵，请调整冲抵金额");
                    }
                    negIndex = -1;
                }
            }
        }
        return resultDto;
    }
    
    private static boolean doAgainst(final BillDetailDto negDetail, final BillSubjectDto billSubjectDto, final int againstRule, final SmruleConfigDto smruleConfig) {
        final boolean taxIncludeFlag = negDetail.getIncludeTax() != 0;
        BigDecimal remainAmount = taxIncludeFlag ? negDetail.getAmountsIncTax() : negDetail.getAmounts();
        final List<BillDetailDto> detailList = billSubjectDto.getBillDList();
        int fitConditionMaxMoneyIndex = -1;
        for (int posIndex = 0; posIndex < detailList.size(); ++posIndex) {
            final BigDecimal negDetailAmount = remainAmount;
            BillDetailDto posDetail = detailList.get(posIndex);
            final boolean doAgainstFlag = isAgainstCondition(posDetail, negDetail, againstRule);
            if (doAgainstFlag) {
                final BigDecimal nowMoney = detailList.get(posIndex).getAmountsByTax();
                if (fitConditionMaxMoneyIndex == -1 || nowMoney.compareTo(detailList.get(fitConditionMaxMoneyIndex).getAmountsByTax()) > 0) {
                    fitConditionMaxMoneyIndex = posIndex;
                }
            }
            if (posIndex >= detailList.size() - 1) {
                if (posIndex == detailList.size() - 1 && fitConditionMaxMoneyIndex == -1) {
                    break;
                }
                posIndex = fitConditionMaxMoneyIndex;
                posDetail = detailList.get(posIndex);
                BigDecimal disDetailAmount = BigDecimal.ZERO;
                BillDetailDto disDetail = null;
                if (posIndex + 1 < detailList.size() && detailList.get(posIndex + 1).getLineProperty() == 4) {
                    disDetail = detailList.get(posIndex + 1);
                    disDetailAmount = (taxIncludeFlag ? disDetail.getAmountsIncTax() : disDetail.getAmounts());
                }
                BigDecimal posDetailAmount = taxIncludeFlag ? posDetail.getAmountsIncTax() : posDetail.getAmounts();
                remainAmount = negDetailAmount.add(posDetailAmount).add(disDetailAmount);
                NegativeRowService.LOG.debug("冲抵：正商品行【{}】，负数行【{}】，冲抵规则【{}】，冲抵后金额【{}】", new Object[] { posDetail.getBillDetailNO(), negDetail.getBillDetailNO(), againstRule, remainAmount });
                if (remainAmount.compareTo(BigDecimal.ZERO) > 0) {
                    posDetailAmount = posDetailAmount.add(negDetailAmount);
                    final BigDecimal calTax = CalTaxUtils.calTax(posDetail.getIncludeTax(), posDetailAmount, posDetail.getTaxRate());
                    posDetail.setTaxAmt(calTax);
                    if (taxIncludeFlag) {
                        posDetail.setAmountsIncTax(posDetailAmount);
                        posDetail.setAmounts(posDetail.getAmountsIncTax().subtract(posDetail.getTaxAmt()));
                    }
                    else {
                        posDetail.setAmounts(posDetailAmount);
                        posDetail.setAmountsIncTax(posDetail.getAmounts().add(posDetail.getTaxAmt()));
                    }
                    NegativeRowService.LOG.debug("正商品行部分冲抵: 设置商品行【{}】，金额【{}】，税额【{}】，价税合计【{}】", new Object[] { posDetail.getBillDetailNO(), posDetail.getAmounts(), posDetail.getTaxAmt(), posDetail.getAmountsIncTax() });
                    GoodsMergeService.dealAmtsAndPrice(posDetail, smruleConfig, smruleConfig.getSplitListType());
                    remarkDeal(negDetail, smruleConfig, posDetail);
                    NegativeRowService.LOG.debug("正商品行部分冲抵: 移除负数行【{}】，break", negDetail.getBillDetailNO());
                    detailList.remove(negDetail);
                    break;
                }
                Set<String> negBillNoSet = billSubjectDto.getNegBillNoSet();
                if (CollectionUtils.isEmpty((Collection)negBillNoSet)) {
                    negBillNoSet = new HashSet<String>(2);
                }
                final String billNo = posDetail.getBillNO();
                if (!negBillNoSet.contains(billNo)) {
                    negBillNoSet.add(billNo);
                }
                billSubjectDto.setNegBillNoSet(negBillNoSet);
                detailList.remove(posDetail);
                negDetail.addDetailId(posDetail.getDetailIdSet());
                --posIndex;
                if (disDetail != null) {
                    detailList.remove(disDetail);
                    negDetail.addDetailId(disDetail.getDetailIdSet());
                }
                if (remainAmount.compareTo(BigDecimal.ZERO) == 0) {
                    NegativeRowService.LOG.debug("正商品行全部冲抵：移除负数行【{}】,break", negDetail.getBillDetailNO());
                    detailList.remove(negDetail);
                    detailList.get(0).addDetailId(negDetail.getDetailIdSet());
                    break;
                }
                if (taxIncludeFlag) {
                    negDetail.setAmountsIncTax(remainAmount);
                }
                else {
                    negDetail.setAmounts(remainAmount);
                }
                NegativeRowService.LOG.debug("正商品行全部冲抵：设置负数行【{}】金额【{}】,移除商品行以及对应的折扣行", negDetail.getBillDetailNO(), remainAmount);
                fitConditionMaxMoneyIndex = -1;
                posIndex = -1;
            }
        }
        if (remainAmount.compareTo(BigDecimal.ZERO) >= 0) {
            return false;
        }
        if (againstRule == 2) {
            NegativeRowService.LOG.debug("任意冲抵下，还未冲抵完，则无正商品行了");
            final BigDecimal calTax2 = CalTaxUtils.calTax(negDetail.getIncludeTax(), remainAmount, negDetail.getTaxRate());
            if (calTax2.subtract(negDetail.getTaxAmt()).abs().compareTo(smruleConfig.getLineTaxAmtErr()) > 0) {
                negDetail.setTaxAmt(calTax2);
            }
            if (taxIncludeFlag) {
                negDetail.setAmounts(negDetail.getAmountsIncTax().subtract(negDetail.getTaxAmt()));
            }
            else {
                negDetail.setAmountsIncTax(negDetail.getAmounts().add(negDetail.getTaxAmt()));
            }
            return true;
        }
        return doAgainst(negDetail, billSubjectDto, againstRule + 1, smruleConfig);
    }
    
    public static void remarkDeal(final BillDetailDto negDetail, final SmruleConfigDto smruleConfig, final BillDetailDto posDetail) {
        String mergeRemark = posDetail.getLineNote();
        if (org.apache.commons.lang3.StringUtils.isNotEmpty((CharSequence)negDetail.getLineNote())) {
            if (org.apache.commons.lang3.StringUtils.isNotEmpty((CharSequence)mergeRemark) && (smruleConfig.isMccRepeat() || !mergeRemark.equals(negDetail.getLineNote()))) {
                mergeRemark = String.valueOf(mergeRemark) + smruleConfig.getMccNoteStr() + negDetail.getLineNote();
            }
            else {
                mergeRemark = negDetail.getLineNote();
            }
        }
        posDetail.setLineNote(mergeRemark);
        posDetail.addDetailId(negDetail.getDetailIdSet());
        NegativeRowService.LOG.debug("设置正商品行【{}】备注【{}】", posDetail.getBillDetailNO(), mergeRemark);
    }
    
    private static boolean isAgainstCondition(final BillDetailDto posDetail, final BillDetailDto negDetail, final int againstTyp) {
        boolean doAgainstFlag = false;
        if (posDetail.getAmounts().compareTo(BigDecimal.ZERO) <= 0) {
            return doAgainstFlag;
        }
        if (posDetail.getTaxRate().compareTo(negDetail.getTaxRate()) != 0) {
            return doAgainstFlag;
        }
        if (againstTyp == 0) {
            if (StringUtilsEx.equalIgnoreNull(posDetail.getGoodsName(), negDetail.getGoodsName()) && StringUtilsEx.equalIgnoreNull(posDetail.getGoodsModel(), negDetail.getGoodsModel()) && StringUtilsEx.equalIgnoreNull(posDetail.getGoodsUnit(), negDetail.getGoodsUnit())) {
                doAgainstFlag = true;
            }
        }
        else if (againstTyp == 1) {
            if (StringUtilsEx.equalIgnoreNull(posDetail.getGoodsName(), negDetail.getGoodsName())) {
                doAgainstFlag = true;
            }
        }
        else {
            doAgainstFlag = true;
        }
        return doAgainstFlag;
    }
}
