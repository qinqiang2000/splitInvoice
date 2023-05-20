package com.szhtxx.etcloud.smser.methods.smser.invoice;

import com.szhtxx.etcloud.smser.utils.*;
import com.szhtxx.etcloud.smser.dto.*;
import com.szhtxx.etcloud.smser.service.*;
import com.szhtxx.etcloud.smser.enums.*;
import org.apache.commons.lang3.*;

public class InvoiceMethods
{
    public void splitProdLineForFullInv(final BillSubjectDto billSubjectDto, final SmruleConfigDto configDto) {
    }
    
    public void splitProductLineForSingLine(final BillSubjectDto billSubjectDto, final SmruleConfigDto configDto) {
    }
    
    public void productLineSort(final BillSubjectDto subjectDto) {
        ComUtil.bubbleSort(subjectDto);
    }
    
    public void genInvoice(final BillSubjectDto billSubjectDto, final SmruleConfigDto configDto, final SmsResultDto smsResultDto) {
        InvoiceCoreService.openInvoice(billSubjectDto, configDto, smsResultDto);
    }
    
    public void invoiceLimit(final BillSubjectDto billSubjectDto, final SmruleConfigDto configDto, final SmsResultDto smsResultDto) {
        InvoiceCoreService.invoiceAverageLimit(billSubjectDto, configDto, smsResultDto);
    }
    
    public void taxAmtCheck(final BillSubjectDto billSubjectDto, final SmruleConfigDto configDto, final SmsResultDto smsResultDto) {
        InvoiceCoreService.taxAmtCheck(billSubjectDto, configDto, smsResultDto);
    }
    
    public void getMaxLine(BillSubjectDto billSubjectDto, SmruleConfigDto configDto) {
        int invkind = billSubjectDto.getInvKind();
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
        if (StringUtils.isNotEmpty((CharSequence)isOil) && isOil.equals(EnumType.NumberTypeEnum.ONE.getValue().toString())) {
            maxLine = 8;
        }
        else if (EnumType.NumberTypeEnum.TWO.getValue() == configDto.getListType() && maxLine > 8) {
            maxLine = 8;
        }
        if (EnumType.InvKindEnum.ROLL.getValue() == invkind) {
            final String mode = billSubjectDto.getRollInvSpec();
            if (StringUtils.isNotEmpty((CharSequence)mode) && mode.equals("01")) {
                maxLine = 13;
            }
            else {
                maxLine = 6;
            }
        }
        billSubjectDto.setLimitLine(maxLine);
    }
}
