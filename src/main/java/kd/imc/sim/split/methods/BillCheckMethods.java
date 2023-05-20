package kd.imc.sim.split.methods;

import kd.imc.sim.common.constant.InvoiceConstant;
import kd.imc.sim.split.dto.*;
import kd.imc.sim.split.enums.EnumType.InvKindEnum;
import kd.imc.sim.split.enums.EnumType.LinePropertyEnum;
import kd.imc.sim.split.exception.EtcRuleException;
import kd.imc.sim.split.service.BillsCheckService;
import kd.imc.sim.split.utils.ComUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static kd.imc.sim.split.enums.EnumType.InvKindEnum;
import static kd.imc.sim.split.enums.EnumType.LinePropertyEnum;


public class BillCheckMethods {

    public void checkBill(SmsRequestDto requestDto, SmsResultDto resultDto) {
        BillSubjectDto billSubjectDto = requestDto.getBillSList().get(0);

        String billNo = billSubjectDto.getBillNO();
        int invKind = billSubjectDto.getInvKind();
        BillDealResultDto dealResultDto;
        if (StringUtils.isBlank(billNo)) {
            dealResultDto = BillsCheckService.getBDR(billNo, false, "传入单据编号为空");
            resultDto.getBdrList().add(dealResultDto);
            billSubjectDto.setCheckPassed(Boolean.FALSE);
            return;
        }
        if (CollectionUtils.isEmpty(billSubjectDto.getBillDList())) {
            dealResultDto = BillsCheckService.getBDR(billNo, false, "传入单据明细为空");
            resultDto.getBdrList().add(dealResultDto);
            billSubjectDto.setCheckPassed(Boolean.FALSE);
            return;
        }
        if (InvKindEnum.notExisted(invKind)) {
            dealResultDto = BillsCheckService.getBDR(billNo, false, "传入的发票种类:" + invKind + "不正确。");
            resultDto.getBdrList().add(dealResultDto);
            billSubjectDto.setCheckPassed(Boolean.FALSE);
            return;
        }
        if (InvKindEnum.SPECIAL.getValue() == invKind && requestDto.getSiAmt().compareTo(BigDecimal.ZERO) <= 0) {
            dealResultDto = BillsCheckService.getBDR(billNo, false, "传入纸质专用发票拆分限额为0");
            resultDto.getBdrList().add(dealResultDto);
            billSubjectDto.setCheckPassed(Boolean.FALSE);
        } else if (InvKindEnum.NORMAL.getValue() == invKind && requestDto.getCiAmt().compareTo(BigDecimal.ZERO) <= 0) {
            dealResultDto = BillsCheckService.getBDR(billNo, false, "传入纸质普通发票拆分限额为0");
            resultDto.getBdrList().add(dealResultDto);
            billSubjectDto.setCheckPassed(Boolean.FALSE);
        } else if (InvKindEnum.EINV.getValue() == invKind && requestDto.getEiAmt().compareTo(BigDecimal.ZERO) <= 0) {
            dealResultDto = BillsCheckService.getBDR(billNo, false, "传入电子普通发票拆分限额为0");
            resultDto.getBdrList().add(dealResultDto);
            billSubjectDto.setCheckPassed(Boolean.FALSE);
        } else if (InvKindEnum.ESINV.getValue() == invKind && requestDto.getEsiAmt().compareTo(BigDecimal.ZERO) <= 0) {
            dealResultDto = BillsCheckService.getBDR(billNo, false, "传入电子专用发票拆分限额为0");
            resultDto.getBdrList().add(dealResultDto);
            billSubjectDto.setCheckPassed(Boolean.FALSE);
        } else if (InvKindEnum.ROLL.getValue() == invKind && requestDto.getVlAmt().compareTo(BigDecimal.ZERO) <= 0) {
            dealResultDto = BillsCheckService.getBDR(billNo, false, "传入卷票票拆分限额为0");
            resultDto.getBdrList().add(dealResultDto);
            billSubjectDto.setCheckPassed(Boolean.FALSE);
        }
    }

    public void checkNull(String fieldValue, BillDetailDto detailDto, int index, String errorMsg) {
        String billNo = detailDto.getBillNO();
        String billDetailNO = detailDto.getBillDetailNO();
        String msg;
        if (fieldValue != null && fieldValue.trim().length() == 0) {
            msg = String.format("单据号：%s 第%s行单据明细编号为:%s %s", billNo, index, billDetailNO, errorMsg);
            throw new EtcRuleException(msg);
        } else if (StringUtils.isEmpty(fieldValue)) {
            msg = String.format("单据号：%s 第%s行单据明细编号为:%s %s", billNo, index, billDetailNO, errorMsg);
            throw new EtcRuleException(msg);
        }
    }

    public void specilNo0TaxRate(BigDecimal taxRate, Integer invKind, BillDetailDto detailDto, int index, String errMsg) {
        BigDecimal zeroBg = BigDecimal.ZERO;
        String billNo = detailDto.getBillNO();
        String billDetailNO = detailDto.getBillDetailNO();
        if (taxRate == null) {
            throw new EtcRuleException("税率不能为空");
        } else if (invKind == null) {
            throw new EtcRuleException("发票类型不能为空");
        } else if (taxRate.compareTo(zeroBg) == 0 && invKind.compareTo(InvKindEnum.SPECIAL.getValue()) == 0) {
            String msg = String.format("单据号：%s 第%s行单据明细编号为:%s 税率[%s] %s", billNo, index, billDetailNO, taxRate, errMsg);
            throw new EtcRuleException(msg);
        }
    }

    public void verfTaxRateNew(BigDecimal taxRate, BillDetailDto detailDto, int index, String errMsg) {
        String billNo = detailDto.getBillNO();
        String billDetailNO = detailDto.getBillDetailNO();
        if (taxRate.compareTo(BigDecimal.ZERO) >= 0 && taxRate.compareTo(BigDecimal.ONE) <= 0) {
            int place = taxRate.scale();
            if (place > 3) {
                String msg = String.format("单据号：%s 第%s行单据明细编号为:%s 税率[%s] %s", billNo, index, billDetailNO, taxRate, errMsg);
                throw new EtcRuleException(msg);
            }
        } else {
            String msg = String.format("单据号：%s 第%s行单据明细编号为:%s 税率[%s] %s", billNo, index, billDetailNO, taxRate, errMsg);
            throw new EtcRuleException(msg);
        }
    }

    public void taxRate15NotMultiTax(List<BillDetailDto> billDList, BigDecimal taxRate, BillDetailDto detailDto, int index, String errMsg) {
        String billNo = detailDto.getBillNO();
        String billDetailNO = detailDto.getBillDetailNO();
        if (CollectionUtils.isEmpty(billDList)) {
            throw new EtcRuleException("集合不能为空");
        } else {
            Set<BigDecimal> set = new HashSet<>(10);

            for (BillDetailDto dto : billDList) {
                set.add(dto.getTaxRate());
            }

            if (set.size() > 1 && taxRate.compareTo(new BigDecimal("0.015")) == 0) {
                String msg = String.format("单据号：%s 第%s行单据明细编号为:%s %s", billNo, index, billDetailNO, errMsg);
                throw new EtcRuleException(msg);
            }
        }
    }

    public void decNottaxRate15(BigDecimal dec, BigDecimal taxRate, BillDetailDto detailDto, int index, String errMsg) {
        BigDecimal zeroBg = BigDecimal.ZERO;
        String billNo = detailDto.getBillNO();
        String billDetailNO = detailDto.getBillDetailNO();
        if (dec != null && dec.compareTo(zeroBg) != 0 && taxRate.compareTo(new BigDecimal("0.015")) == 0) {
            String msg = String.format("单据号：%s 第%s行单据明细编号为:%s %s", billNo, index, billDetailNO, errMsg);
            throw new EtcRuleException(msg);
        }
    }

    public void disLineMoneyNotGt0(BigDecimal amounts, Integer lineProperty, BillDetailDto detailDto, int index, String errMsg) {
        if (ComUtil.isDisLine(detailDto)) {
            String billNo = detailDto.getBillNO();
            String billDetailNO = detailDto.getBillDetailNO();
            BigDecimal zeroBg = BigDecimal.ZERO;
            if (lineProperty.compareTo(detailDto.getLineProperty()) == 0 && amounts.compareTo(zeroBg) > 0) {
                String msg = String.format("单据号：%s 第%s行单据明细编号为:%s %s", billNo, index, billDetailNO, errMsg);
                throw new EtcRuleException(msg);
            }
        }

    }

    public void checkDisRows(BillDetailDto detailDto, int index, String errMsg) {
        String billNo = detailDto.getBillNO();
        String billDetailNO = detailDto.getBillDetailNO();
        Integer lineProperty = detailDto.getLineProperty();
        Integer disRows = detailDto.getDisRows();
        if (LinePropertyEnum.FOUR.getValue().compareTo(lineProperty) == 0 && disRows < -1) {
            String msg = String.format("单据号：%s 第%s行单据明细编号为:%s 折扣行数[%s] %s", billNo, index, billDetailNO, disRows, errMsg);
            throw new EtcRuleException(msg);
        }
    }

    public void discount1LineNotExist(List<BillDetailDto> billDList, BillDetailDto detailDto, int index) {
        BigDecimal zeroBg = BigDecimal.ZERO;
        String billNo = detailDto.getBillNO();
        String billDetailNO = detailDto.getBillDetailNO();
        Integer lineProperty = detailDto.getLineProperty();
        Integer disRows = detailDto.getDisRows();
        if (LinePropertyEnum.FOUR.getValue().compareTo(lineProperty) == 0 && (disRows == 0 || disRows == 1)) {
            int j = ComUtil.findObjIndexInList(billDList, detailDto);
            if (j - 1 < 0) {
                String msg = String.format("单据号：%s 第%s行单据明细编号为:%s %s", billNo, index, billDetailNO, "被折扣行不存在");
                throw new EtcRuleException(msg);
            }

            BillDetailDto lastBillDeailDto = billDList.get(j - 1);
            if (lastBillDeailDto.getAmounts().compareTo(zeroBg) < 0) {
                String msg = String.format("单据号：%s 第%s行单据明细编号为:%s %s", billNo, index, billDetailNO, "被折扣行金额非正数行");
                throw new EtcRuleException(msg);
            }

            BigDecimal amounts = InvoiceConstant.IS_TAX_YES_INT.equals(lastBillDeailDto.getIncludeTax()) ? lastBillDeailDto.getAmountsIncTax() : lastBillDeailDto.getAmounts();
            BigDecimal disMoney = detailDto.getAmounts();
            if (amounts.compareTo(disMoney.abs()) < 0) {
                String msg = String.format("单据号：%s 第%s行单据明细编号为:%s 折扣行金额[%s]大于被折金额[%s]", billNo, index, billDetailNO, disMoney, amounts);
                throw new EtcRuleException(msg);
            }
        }
    }

    public void discountNLineNotExist(List<BillDetailDto> billDList, BillDetailDto detailDto, int index) {
        BigDecimal zeroBg = BigDecimal.ZERO;
        String billNo = detailDto.getBillNO();
        String billDetailNO = detailDto.getBillDetailNO();
        Integer lineProperty = detailDto.getLineProperty();
        Integer disRows = detailDto.getDisRows();
        if (LinePropertyEnum.FOUR.getValue().compareTo(lineProperty) == 0 && disRows > 1) {
            int j = ComUtil.findObjIndexInList(billDList, detailDto);
            int positiveLine = 0;
            BigDecimal disMoney = detailDto.getAmounts();
            BigDecimal sumMoney = BigDecimal.ZERO;

            for (int i = j - 1; i >= 0; --i) {
                BillDetailDto billDetailDto = billDList.get(i);
                BigDecimal amounts = InvoiceConstant.IS_TAX_YES_INT.equals(billDetailDto.getIncludeTax()) ? billDetailDto.getAmountsIncTax() : billDetailDto.getAmounts();
                if (billDetailDto.getAmounts().compareTo(zeroBg) > 0) {
                    ++positiveLine;
                    sumMoney = sumMoney.add(amounts);
                }

                if (positiveLine == disRows) {
                    break;
                }
            }

            String msg;
            if (positiveLine < disRows) {
                msg = String.format("单据号：%s 第%s行单据明细编号为:%s %s", billNo, index, billDetailNO, "折扣行找不到足够的折扣行数");
                throw new EtcRuleException(msg);
            }

            if (sumMoney.compareTo(disMoney.abs()) < 0) {
                msg = String.format("单据号：%s 第%s行单据明细编号为:%s 折扣行金额[%s]大于被折金额[%s]", billNo, index, billDetailNO, disMoney, sumMoney);
                throw new EtcRuleException(msg);
            }
        }
    }

    public void checkNDline(BillDetailDto detailDto, SmruleConfigDto configDto, int index) {
        BigDecimal zeroBd = BigDecimal.ZERO;
        BigDecimal amounts = detailDto.getAmounts();
        BigDecimal amts = detailDto.getAmts();
        BigDecimal price = detailDto.getPrice();
        String billNo = detailDto.getBillNO();
        String billDetailNO = detailDto.getBillDetailNO();
        Integer lineProperty = detailDto.getLineProperty();
        if (LinePropertyEnum.FOUR.getValue().compareTo(lineProperty) != 0) {
            String msg;
            if (amounts.compareTo(zeroBd) == 0 && amts.compareTo(zeroBd) == 0 && price.compareTo(zeroBd) == 0) {
                msg = String.format("单据号：%s 第%s行单据明细编号为:%s %s", billNo, index, billDetailNO, "普通商品行或被折扣行，数量、单价、金额不能同时为0");
                throw new EtcRuleException(msg);
            }

            if (amts != null && price != null && amts.compareTo(zeroBd) == 0 && price.compareTo(zeroBd) > 0 && amounts.compareTo(zeroBd) == 0) {
                msg = String.format("单据号：%s 第%s行单据明细编号为:%s %s", billNo, index, billDetailNO, "金额，数量未传入");
                throw new EtcRuleException(msg);
            }

            if (amts != null && price != null && amts.compareTo(zeroBd) > 0 && price.compareTo(zeroBd) == 0 && amounts.compareTo(zeroBd) == 0) {
                msg = String.format("单据号：%s 第%s行单据明细编号为:%s %s", billNo, index, billDetailNO, "金额，单价未传入");
                throw new EtcRuleException(msg);
            }

            BigDecimal disMoney;
            if (amts != null && price != null && amts.compareTo(zeroBd) > 0 && price.compareTo(zeroBd) > 0 && amounts.compareTo(zeroBd) > 0) {
                disMoney = amts.multiply(price);
                BigDecimal err = disMoney.subtract(amounts).abs();
                if (err.compareTo(configDto.getLineAmountErr()) > 0) {
                    msg = String.format("单据号：%s 第%s行单据明细编号为:%s 数量(%s)*单价(%s)不等于金额(%s)", billNo, index, billDetailNO, amts, price, amounts);
                    throw new EtcRuleException(msg);
                }
            }

            disMoney = detailDto.getDisAmt();
            if (disMoney != null && amounts.compareTo(zeroBd) < 0 && disMoney.compareTo(zeroBd) < 0) {
                msg = String.format("单据号：%s 第%s行单据明细编号为:%s 负数行不允许有折扣", billNo, index, billDetailNO);
                throw new EtcRuleException(msg);
            }

            if (disMoney != null && disMoney.abs().compareTo(amounts) > 0 && disMoney.compareTo(zeroBd) < 0) {
                msg = String.format("单据号：%s 第%s行单据明细编号为:%s 折扣金额大于金额", billNo, index, billDetailNO);
                throw new EtcRuleException(msg);
            }
        }
    }

    /* 
     * 该函数的作用是检查销售单的总金额是否为0，如果是则抛出一个EtcRuleException异常。
     * 函数内部首先获取销售单的所有明细，然后计算销售单的总金额、总税额和含税总金额。如果总金额或含税总金额为0，则抛出异常。
     */
    public void checkBillTotal(BillSubjectDto billSubjectDto) {
        List<BillDetailDto> billDList = billSubjectDto.getBillDList();
        BigDecimal billAmountsSum = BigDecimal.ZERO;
        BigDecimal billTaxAmtsSum = BigDecimal.ZERO;

        for (BillDetailDto billDetailDto : billDList) {
            billAmountsSum = billAmountsSum.add(billDetailDto.getAmounts() != null ? billDetailDto.getAmounts() : BigDecimal.ZERO);
            billTaxAmtsSum = billTaxAmtsSum.add(billDetailDto.getTaxAmt() != null ? billDetailDto.getTaxAmt() : BigDecimal.ZERO);
        }
        BigDecimal billAmountsIncTaxSum = billAmountsSum.add(billTaxAmtsSum);
        if (billAmountsSum.compareTo(BigDecimal.ZERO) == 0 || billAmountsIncTaxSum.compareTo(BigDecimal.ZERO) == 0) {
            throw new EtcRuleException("销售单总金额或拆合处理后有发票的金额为0，请检查数据或调整拆合规则");
        }
    }
}