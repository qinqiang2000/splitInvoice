package kd.imc.sim.split.methods;

import kd.imc.sim.split.dto.BillSubjectDto;
import kd.imc.sim.split.dto.SmruleConfigDto;
import kd.imc.sim.split.dto.SmsResultDto;
import kd.imc.sim.split.enums.EnumType.InvKindEnum;
import kd.imc.sim.split.enums.EnumType.NumberTypeEnum;
import kd.imc.sim.split.service.InvoiceCoreService;
import org.apache.commons.lang3.StringUtils;

import static kd.imc.sim.split.enums.EnumType.InvKindEnum;
import static kd.imc.sim.split.enums.EnumType.NumberTypeEnum;

public class InvoiceMethods {
    public void genInvoice(BillSubjectDto billSubjectDto, SmruleConfigDto configDto, SmsResultDto smsResultDto) {
        InvoiceCoreService.openInvoice(billSubjectDto, configDto, smsResultDto);
    }

    public void invoiceLimit(BillSubjectDto billSubjectDto, SmruleConfigDto configDto) {
        InvoiceCoreService.invoiceAverageLimit(billSubjectDto, configDto);
    }

    public void taxAmtCheck(BillSubjectDto billSubjectDto, SmruleConfigDto configDto, SmsResultDto smsResultDto) {
        InvoiceCoreService.taxAmtCheck(billSubjectDto, configDto, smsResultDto);
    }

    // 根据billSubjectDto和configDto中的信息计算出发票的最大行数，并将其设置到billSubjectDto中的limitLine属性中
    public void getMaxLine(BillSubjectDto billSubjectDto, SmruleConfigDto configDto) {
        int invkind = billSubjectDto.getInvKind();
        int maxLine;
        if (InvKindEnum.SPECIAL.getValue() == invkind) {
            maxLine = configDto.getMaxSLine();
        } else if (InvKindEnum.NORMAL.getValue() == invkind) {
            maxLine = configDto.getMaxCLine();
        } else if (InvKindEnum.ESINV.getValue() == invkind) {
            maxLine = configDto.getMaxEsiLine();
        } else {
            maxLine = configDto.getMaxELine();
        }

        String isOil = billSubjectDto.getIsOil();
        if (StringUtils.isNotEmpty(isOil) && isOil.equals(NumberTypeEnum.ONE.getValue().toString())) {
            maxLine = 8;
        } else if (NumberTypeEnum.TWO.getValue() == configDto.getListType() && maxLine > 8) {
            maxLine = 8;
        }

        if (InvKindEnum.ROLL.getValue() == invkind) {
            String mode = billSubjectDto.getRollInvSpec();
            if (StringUtils.isNotEmpty(mode) && mode.equals("01")) {
                maxLine = 13;
            } else {
                maxLine = 6;
            }
        }

        billSubjectDto.setLimitLine(maxLine);
    }
}