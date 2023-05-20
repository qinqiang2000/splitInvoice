package com.szhtxx.etcloud.smser.service;

import com.szhtxx.etcloud.smser.constant.*;
import org.slf4j.*;
import com.szhtxx.etcloud.smser.dto.*;
import com.google.common.collect.*;
import java.math.*;
import com.szhtxx.etcloud.smser.exception.*;
import java.util.*;
import com.szhtxx.etcloud.smser.methods.smser.*;

public class GoodsDiscountService implements SmruleConfConstant
{
    private static Logger LOG;
    
    static {
        GoodsDiscountService.LOG = LoggerFactory.getLogger((Class)GoodsDiscountService.class);
    }
    
    private static boolean isDisCountRow(final BillDetailDto billDetailDto) {
        return billDetailDto.getLineProperty() != null && billDetailDto.getLineProperty() == 4;
    }
    
    private static String buildDetailMsg(final BillDetailDto billDetailDto, final String msg, final String... params) {
        final StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("单据编号:").append(billDetailDto.getBillNO()).append("明细行号:").append(billDetailDto.getBillDetailNO()).append(msg);
        return String.format(msgBuilder.toString(), (Object[])params);
    }
    
    public static List<BillDetailDto> doDisLine(final List<BillDetailDto> detailList) throws EtcRuleException {
        final ArrayListMultimap<BillDetailDto, BillDetailDto> disCountMap = ArrayListMultimap.create();
        BigDecimal plusSumAmt = BigDecimal.ZERO;
        BigDecimal billDisSum = BigDecimal.ZERO;
        BigDecimal disAmtSum = BigDecimal.ZERO;
        BillDetailDto allDisDetail = null;
        for (int i = 0; i < detailList.size(); ++i) {
            final BillDetailDto billDetail = detailList.get(i);
            if (!isDisCountRow(billDetail)) {
                if (billDetail.getAmountsByTax() == null) {
                    continue;
                }
                if (billDetail.getAmountsByTax().compareTo(BigDecimal.ZERO) < 0) {
                    continue;
                }
            }
            if (isDisCountRow(billDetail) && billDetail.getDisRows() == -1) {
                allDisDetail = billDetail;
                billDisSum = billDisSum.add(billDetail.getAmountsByTax().abs());
                disAmtSum = disAmtSum.add(billDetail.getAmountsByTax().abs());
            }
            else if (isDisCountRow(billDetail) && billDetail.getDisRows() > -1) {
                plusSumAmt = plusSumAmt.subtract(billDetail.getAmountsByTax().abs());
                disAmtSum = disAmtSum.add(billDetail.getAmountsByTax().abs());
                BigDecimal tmpAmounts = BigDecimal.ZERO;
                for (int beforeIndex = i - 1; beforeIndex >= 0; --beforeIndex) {
                    if (beforeIndex >= 0) {
                        final BillDetailDto beforeDetail = detailList.get(beforeIndex);
                        if (isDisCountRow(beforeDetail)) {
                            if (beforeDetail.getDisRows() <= -1) {}
                        }
                        else if (beforeDetail.getAmountsByTax().compareTo(BigDecimal.ZERO) >= 0) {
                            if (billDetail.getDisRows() > 1) {
                                if (disCountMap.get(billDetail) != null && disCountMap.get(billDetail).size() >= billDetail.getDisRows()) {
                                    break;
                                }
                                disCountMap.put(billDetail, beforeDetail);
                                tmpAmounts = tmpAmounts.add(beforeDetail.getAmountsByTax());
                            }
                            else {
                                if (beforeDetail.getAmountsByTax().abs().compareTo(billDetail.getAmountsByTax().abs()) >= 0) {
                                    beforeDetail.setDisAmt(beforeDetail.getDisAmt().abs().add(billDetail.getAmountsByTax().abs()));
                                    break;
                                }
                                final String errmsg = buildDetailMsg(billDetail, "的折扣行找不到足够的折扣金额(%s)", String.valueOf(billDetail.getAmountsByTax()));
                                throw new EtcRuleException(errmsg);
                            }
                        }
                    }
                }
            }
            else {
                if (billDetail.getDisAmt().compareTo(BigDecimal.ZERO) != 0) {
                    disAmtSum = disAmtSum.add(billDetail.getDisAmt().abs());
                }
                if (billDetail.getDisAmt().compareTo(BigDecimal.ZERO) > 0) {
                    plusSumAmt = plusSumAmt.add(billDetail.getAmountsByTax().subtract(billDetail.getDisAmt()));
                }
                else {
                    plusSumAmt = plusSumAmt.add(billDetail.getAmountsByTax().add(billDetail.getDisAmt()));
                }
            }
        }
        GoodsDiscountService.LOG.debug(String.format("正数金额%s\t需折扣金额%s\t整单折扣金额%s", plusSumAmt, disAmtSum, billDisSum));
        if (disAmtSum.compareTo(BigDecimal.ZERO) == 0) {
            GoodsDiscountService.LOG.debug("没有折扣处理");
            return detailList;
        }
        for (final BillDetailDto disCountRow : disCountMap.keySet()) {
            final List<BillDetailDto> toDisGoodsDetailList = (List<BillDetailDto>)disCountMap.get(disCountRow);
            if (toDisGoodsDetailList.size() == 1) {
                continue;
            }
            if (toDisGoodsDetailList.size() < disCountRow.getDisRows()) {
                final String errmsg2 = buildDetailMsg(disCountRow, "的折扣行找不到足够的折扣行数(%s)", String.valueOf(disCountRow.getDisRows()));
                throw new EtcRuleException(errmsg2);
            }
            BigDecimal canDisCountAmountSum = BigDecimal.ZERO;
            for (final BillDetailDto goodDetail : toDisGoodsDetailList) {
                canDisCountAmountSum = canDisCountAmountSum.add(goodDetail.getAmountsByTax().subtract(goodDetail.getDisAmt().abs()));
            }
            if (canDisCountAmountSum.compareTo(disCountRow.getAmountsByTax().abs()) < 0) {
                final String errmsg3 = buildDetailMsg(disCountRow, "的折扣行找不到足够的折扣金额(%s)", String.valueOf(disCountRow.getAmountsByTax()));
                throw new EtcRuleException(errmsg3);
            }
            final BigDecimal disCountRate = disCountRow.getAmountsByTax().abs().divide(canDisCountAmountSum.abs(), 5, 4).multiply(new BigDecimal("100")).setScale(3, 4);
            GoodsDiscountService.LOG.debug("disRate : " + disCountRate);
            int disRows = 0;
            BigDecimal disAmted = BigDecimal.ZERO;
            for (final BillDetailDto goodDetail2 : toDisGoodsDetailList) {
                final BigDecimal canDisCountAmount = goodDetail2.getAmountsByTax().subtract(goodDetail2.getDisAmt().abs());
                BigDecimal currentRowDisCountAmount = BigDecimal.ZERO;
                currentRowDisCountAmount = canDisCountAmount.multiply(disCountRate).divide(new BigDecimal(100)).setScale(2, 4);
                if (currentRowDisCountAmount.compareTo(goodDetail2.getAmountsByTax()) >= 0) {
                    GoodsDiscountService.LOG.debug("不折这行");
                }
                else {
                    disAmted = disAmted.add(currentRowDisCountAmount);
                    ++disRows;
                    goodDetail2.setDisAmt(goodDetail2.getDisAmt().abs().add(currentRowDisCountAmount.abs()));
                }
            }
            final BigDecimal adustAmt = disAmted.subtract(disCountRow.getAmountsByTax().abs());
            for (final BillDetailDto goodDetail3 : toDisGoodsDetailList) {
                if (adustAmt.compareTo(BigDecimal.ZERO) > 0) {
                    if (goodDetail3.getDisAmt().abs().compareTo(adustAmt) > 0) {
                        goodDetail3.setDisAmt(goodDetail3.getDisAmt().abs().subtract(adustAmt));
                        break;
                    }
                    continue;
                }
                else {
                    if (goodDetail3.getDisAmt().abs().add(adustAmt.abs()).compareTo(goodDetail3.getAmountsByTax()) < 0) {
                        goodDetail3.setDisAmt(goodDetail3.getDisAmt().abs().add(adustAmt.abs()));
                        break;
                    }
                    break;
                }
            }
        }
        billDiscount(detailList, billDisSum, plusSumAmt);
        return generateDiscountRow(detailList, 0, allDisDetail);
    }
    
    private static List<BillDetailDto> billDiscount(final List<BillDetailDto> detailList, final BigDecimal billDisSum, final BigDecimal plusSumAmt) {
        BigDecimal billDisRate = BigDecimal.ZERO;
        if (billDisSum.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal disAmted = new BigDecimal("0");
            billDisRate = billDisSum.divide(plusSumAmt, 5, 4).multiply(new BigDecimal("100")).setScale(3, 4);
            for (final BillDetailDto billDetailDto : detailList) {
                if (!isDisCountRow(billDetailDto)) {
                    if (billDetailDto.getAmountsByTax().compareTo(BigDecimal.ZERO) < 0) {
                        continue;
                    }
                    final BigDecimal canDisCountAmount = billDetailDto.getAmountsByTax().subtract(billDetailDto.getDisAmt().abs());
                    final BigDecimal currentRowDisCountAmount = canDisCountAmount.multiply(billDisRate).divide(new BigDecimal(100)).setScale(2, 4);
                    if (currentRowDisCountAmount.compareTo(billDetailDto.getAmountsByTax()) >= 0) {
                        GoodsDiscountService.LOG.debug("不折这行");
                    }
                    else {
                        disAmted = disAmted.add(currentRowDisCountAmount);
                        billDetailDto.setDisAmt(billDetailDto.getDisAmt().abs().add(currentRowDisCountAmount));
                    }
                }
            }
            final BigDecimal adustAmt = disAmted.abs().subtract(billDisSum.abs());
            for (final BillDetailDto goodDetail : detailList) {
                if (!isDisCountRow(goodDetail)) {
                    if (goodDetail.getAmountsByTax().compareTo(BigDecimal.ZERO) < 0) {
                        continue;
                    }
                    if (adustAmt.compareTo(BigDecimal.ZERO) > 0) {
                        if (goodDetail.getDisAmt().abs().compareTo(adustAmt) > 0) {
                            goodDetail.setDisAmt(goodDetail.getDisAmt().abs().subtract(adustAmt));
                            break;
                        }
                        continue;
                    }
                    else {
                        if (goodDetail.getDisAmt().abs().add(adustAmt.abs()).compareTo(goodDetail.getAmountsByTax()) < 0) {
                            goodDetail.setDisAmt(goodDetail.getDisAmt().abs().add(adustAmt.abs()));
                            break;
                        }
                        break;
                    }
                }
            }
        }
        return detailList;
    }
    
    private static List<BillDetailDto> generateDiscountRow(final List<BillDetailDto> detailList, final Integer type, final BillDetailDto allDisDetail) {
        final List<BillDetailDto> resultList = new ArrayList<BillDetailDto>();
        BigDecimal disAmt = BigDecimal.ZERO;
        BigDecimal amt = BigDecimal.ZERO;
        BigDecimal amtInc = BigDecimal.ZERO;
        for (int i = 0; i < detailList.size(); ++i) {
            final BillDetailDto billDetailDto = detailList.get(i);
            if (!isDisCountRow(billDetailDto)) {
                if (type == 0 || billDetailDto.getAmountsByTax().compareTo(BigDecimal.ZERO) >= 0) {
                    resultList.add(billDetailDto);
                    amt = amt.add(billDetailDto.getAmounts().abs());
                    amtInc = amtInc.add(billDetailDto.getAmountsIncTax());
                    if (billDetailDto.getDisAmt().compareTo(BigDecimal.ZERO) != 0) {
                        billDetailDto.setLineProperty(3);
                        final BigDecimal lineDisRate = billDetailDto.getDisAmt().abs().divide(billDetailDto.getAmountsByTax(), 5, 4).multiply(new BigDecimal("100"));
                        billDetailDto.setDisRate(lineDisRate);
                        final BillDetailDto discountRow = new BillDetailDto();
                        discountRow.setBillNO(billDetailDto.getBillNO());
                        discountRow.setBillDetailNO(billDetailDto.getBillDetailNO());
                        if (i < detailList.size() - 1 && isDisCountRow(detailList.get(i + 1))) {
                            discountRow.setBillNO(detailList.get(i + 1).getBillNO());
                            discountRow.setBillDetailNO(detailList.get(i + 1).getBillDetailNO());
                        }
                        if (allDisDetail != null) {
                            discountRow.setBillNO(allDisDetail.getBillNO());
                            discountRow.setBillDetailNO(allDisDetail.getBillDetailNO());
                        }
                        discountRow.setGoodsName(billDetailDto.getGoodsName());
                        discountRow.setTaxRate(billDetailDto.getTaxRate());
                        discountRow.setIncludeTax(billDetailDto.getIncludeTax());
                        discountRow.setLineProperty(4);
                        discountRow.setDisRows(1);
                        if (SmruleConfConstant.ONE.equals(billDetailDto.getIncludeTax())) {
                            discountRow.setAmountsIncTax(billDetailDto.getDisAmt().abs().negate());
                            final BigDecimal taxAmt = new BackCalcUtilMethods().calcTaxAmt(billDetailDto.getDisAmt().abs(), billDetailDto.getTaxRate(), 2);
                            discountRow.setTaxAmt(taxAmt.abs().negate());
                            discountRow.setAmounts(billDetailDto.getDisAmt().abs().subtract(taxAmt.abs()).negate());
                            disAmt = disAmt.add(discountRow.getAmountsIncTax().abs());
                        }
                        else {
                            discountRow.setTaxAmt(billDetailDto.getDisAmt().abs().multiply(billDetailDto.getTaxRate()).negate());
                            discountRow.setAmountsIncTax(billDetailDto.getDisAmt().abs().add(discountRow.getTaxAmt().abs()).negate());
                            discountRow.setAmounts(billDetailDto.getDisAmt().abs().negate());
                            disAmt = disAmt.add(discountRow.getAmounts().abs());
                        }
                        discountRow.setGoodsTaxNo(billDetailDto.getGoodsTaxNo());
                        discountRow.setGoodsNoVer(billDetailDto.getGoodsNoVer());
                        resultList.add(discountRow);
                    }
                }
            }
        }
        GoodsDiscountService.LOG.debug("[折扣后] 折扣金额{} 正数金额{} 价税合计{}", new Object[] { disAmt, amt, amtInc });
        return resultList;
    }
    
    public static List<BillDetailDto> doBillDiscount(final List<BillDetailDto> detailList) {
        BigDecimal plusAmt = new BigDecimal("0");
        BigDecimal billDisAmt = new BigDecimal("0");
        for (int i = 0; i < detailList.size(); ++i) {
            final BillDetailDto billDetailDto = detailList.get(i);
            if (i + 1 < detailList.size()) {
                final BillDetailDto nextRow = detailList.get(i + 1);
                if (isDisCountRow(nextRow)) {
                    billDetailDto.setDisAmt(nextRow.getAmountsByTax());
                }
            }
            if (billDetailDto.getAmountsByTax().compareTo(BigDecimal.ZERO) < 0 && !isDisCountRow(billDetailDto)) {
                billDisAmt = billDisAmt.add(billDetailDto.getAmountsByTax().abs());
            }
            else if (billDetailDto.getAmountsByTax().compareTo(BigDecimal.ZERO) > 0) {
                plusAmt = plusAmt.add(billDetailDto.getAmountsByTax().subtract(billDetailDto.getDisAmt().abs()));
            }
        }
        GoodsDiscountService.LOG.debug("抵扣不完整单折 billDisAmt: " + billDisAmt + "  plusAmt: " + plusAmt);
        billDiscount(detailList, billDisAmt, plusAmt);
        return generateDiscountRow(detailList, 1, null);
    }
}
