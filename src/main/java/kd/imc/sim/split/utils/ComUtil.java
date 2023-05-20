package kd.imc.sim.split.utils;


import kd.imc.sim.split.dto.BillDetailDto;
import kd.imc.sim.split.enums.EnumType;
import kd.imc.sim.split.exception.EtcRuleException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.List;


public class ComUtil {
    public static Boolean isDisLine(BillDetailDto detailDto) {
        return EnumType.LinePropertyEnum.FOUR.getValue().compareTo(detailDto.getLineProperty()) == 0 && detailDto.getAmounts().compareTo(BigDecimal.ZERO) < 0;
    }

    public static int findObjIndexInList(List<BillDetailDto> billDList, BillDetailDto detailDto) {
        String billNoP = detailDto.getBillNO();
        String billDetailNoP = detailDto.getBillDetailNO();
        if (StringUtils.isEmpty(billDetailNoP)) {
            throw new EtcRuleException(String.format("单据编号[%s] 单据明细编号不能为空", billNoP));
        }
        if (CollectionUtils.isNotEmpty(billDList)) {
            int i = 0;
            for (BillDetailDto dto : billDList) {
                String billNo = dto.getBillNO();
                String billDetailNo = dto.getBillDetailNO();
                if (billNo.equals(billNoP) && billDetailNo.equals(billDetailNoP)) {
                    break;
                }
                i++;
            }
            return i;
        }
        return -1;
    }
}