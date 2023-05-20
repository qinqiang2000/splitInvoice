package kd.imc.sim.split.service;


import kd.imc.sim.common.constant.InvoiceConstant;
import kd.imc.sim.split.dto.BillDetailDto;
import kd.imc.sim.split.exception.EtcRuleException;
import kd.imc.sim.split.methods.BackCalcUtilMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class GoodsDiscountService {
    static BackCalcUtilMethods backCalcUtilMethods = new BackCalcUtilMethods();

    //private static final Log LOG = LogFactory.getLog(GoodsDiscountService.class);
    private static Logger LOG = LoggerFactory.getLogger(GoodsDiscountService.class);

    private static boolean isDisCountRow(BillDetailDto billDetailDto) {
        return billDetailDto.getLineProperty() != null && billDetailDto.getLineProperty() == 4;
    }

    private static String buildDetailMsg(BillDetailDto billDetailDto, String msg, String params) {
        return String.format("单据编号:" + billDetailDto.getBillNO() + "明细行号:" + billDetailDto.getBillDetailNO() + msg, params);
    }

    /* 
     * 对传入的detailList进行处理，如果其中有折扣行，则将折扣行的折扣金额分摊到前面的商品行中，
     * 如果没有折扣行，则直接返回原始的detailList。
     */
    public static List<BillDetailDto> doDisLine(List<BillDetailDto> detailList) throws EtcRuleException {
        BigDecimal plusSumAmt = BigDecimal.ZERO;
        BigDecimal disAmtSum = BigDecimal.ZERO;

        BillDetailDto beforeDetail;
        for (int i = 0; i < detailList.size(); ++i) {
            BillDetailDto billDetail = detailList.get(i);
            if (!isDisCountRow(billDetail) && (billDetail.getAmountsByTax() == null || billDetail.getAmountsByTax().compareTo(BigDecimal.ZERO) < 0)) {
                continue;
            }

            if (isDisCountRow(billDetail)) {
                plusSumAmt = plusSumAmt.subtract(billDetail.getAmountsByTax().abs());
                disAmtSum = disAmtSum.add(billDetail.getAmountsByTax().abs());

                for (int beforeIndex = i - 1; beforeIndex >= 0; --beforeIndex) {
                    beforeDetail = detailList.get(beforeIndex);
                    if (beforeDetail.getAmountsByTax().compareTo(BigDecimal.ZERO) >= 0) {
                        if (beforeDetail.getAmountsByTax().abs().compareTo(billDetail.getAmountsByTax().abs()) < 0) {
                            String errmsg = buildDetailMsg(billDetail, "的折扣行找不到足够的折扣金额(%s)", String.valueOf(billDetail.getAmountsByTax()));
                            throw new EtcRuleException(errmsg);
                        }

                        beforeDetail.setDisAmt(beforeDetail.getDisAmt().abs().add(billDetail.getAmountsByTax().abs()));
                        break;
                    }
                }
                continue;
            }
            if (billDetail.getDisAmt().compareTo(BigDecimal.ZERO) != 0) {
                disAmtSum = disAmtSum.add(billDetail.getDisAmt().abs());
            }

            if (billDetail.getDisAmt().compareTo(BigDecimal.ZERO) > 0) {
                plusSumAmt = plusSumAmt.add(billDetail.getAmountsByTax().subtract(billDetail.getDisAmt()));
            } else {
                plusSumAmt = plusSumAmt.add(billDetail.getAmountsByTax().add(billDetail.getDisAmt()));
            }
        }
        if (disAmtSum.compareTo(BigDecimal.ZERO) == 0) {
            LOG.debug("没有折扣处理");
            return detailList;
        }
        return generateDiscountRow(detailList);
    }

    private static List<BillDetailDto> generateDiscountRow(List<BillDetailDto> detailList) {
        List<BillDetailDto> resultList = new ArrayList<>();
        BigDecimal disAmt = BigDecimal.ZERO;
        BigDecimal amt = BigDecimal.ZERO;
        BigDecimal amtInc = BigDecimal.ZERO;

        for (int i = 0; i < detailList.size(); ++i) {
            BillDetailDto billDetailDto = detailList.get(i);
            if (!isDisCountRow(billDetailDto) && (billDetailDto.getAmountsByTax().compareTo(BigDecimal.ZERO) >= 0)) {
                resultList.add(billDetailDto);
                amt = amt.add(billDetailDto.getAmounts().abs());
                amtInc = amtInc.add(billDetailDto.getAmountsIncTax());
                if (billDetailDto.getDisAmt().compareTo(BigDecimal.ZERO) != 0) {
                    billDetailDto.setLineProperty(3);
                    BigDecimal lineDisRate = billDetailDto.getDisAmt().abs().divide(billDetailDto.getAmountsByTax(), 5, 4).multiply(new BigDecimal("100"));
                    billDetailDto.setDisRate(lineDisRate);
                    BillDetailDto discountRow = new BillDetailDto();
                    discountRow.setBillNO(billDetailDto.getBillNO());
                    discountRow.setBillDetailNO(billDetailDto.getBillDetailNO());
                    if (i < detailList.size() - 1 && isDisCountRow(detailList.get(i + 1))) {
                        discountRow.setBillNO(detailList.get(i + 1).getBillNO());
                        discountRow.setBillDetailNO(detailList.get(i + 1).getBillDetailNO());
                    }

                    discountRow.setGoodsName(billDetailDto.getGoodsName());
                    discountRow.setTaxRate(billDetailDto.getTaxRate());
                    discountRow.setIncludeTax(billDetailDto.getIncludeTax());
                    discountRow.setLineProperty(4);
                    discountRow.setDisRows(1);
                    if (InvoiceConstant.IS_TAX_YES_INT.equals(billDetailDto.getIncludeTax())) {
                        discountRow.setAmountsIncTax(billDetailDto.getDisAmt().abs().negate());
                        BigDecimal taxAmt = backCalcUtilMethods.calcTaxAmt(billDetailDto.getDisAmt().abs(), billDetailDto.getTaxRate(), 2);
                        discountRow.setTaxAmt(taxAmt.abs().negate());
                        discountRow.setAmounts(billDetailDto.getDisAmt().abs().subtract(taxAmt.abs()).negate());
                        disAmt = disAmt.add(discountRow.getAmountsIncTax().abs());
                    } else {
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

        LOG.debug("[折扣后] 折扣金额{} 正数金额{} 价税合计{}", disAmt, amt, amtInc);
        return resultList;
    }
}