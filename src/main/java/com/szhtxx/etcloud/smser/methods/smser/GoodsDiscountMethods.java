package com.szhtxx.etcloud.smser.methods.smser;

import org.slf4j.*;
import com.szhtxx.etcloud.smser.service.*;
import java.util.*;
import com.szhtxx.etcloud.smser.dto.*;

public class GoodsDiscountMethods
{
    private static final Logger LOG;
    
    static {
        LOG = LoggerFactory.getLogger((Class)GoodsDiscountMethods.class);
    }
    
    public void doDisLine(final BillSubjectDto billSubjectDto) {
        final List<BillDetailDto> billDetailDtoList = GoodsDiscountService.doDisLine(billSubjectDto.getBillDList());
        billSubjectDto.setBillDList(billDetailDtoList);
    }
    
    public void doBillDisLine(final BillSubjectDto billSubjectDto) {
        final List<BillDetailDto> billDetailDtoList = GoodsDiscountService.doBillDiscount(billSubjectDto.getBillDList());
        billSubjectDto.setBillDList(billDetailDtoList);
    }
    
    public void doDisLineTax(final BillSubjectDto billSubjectDto) {
        this.doDisLine(billSubjectDto);
    }
    
    public void doBillDisLineTax(final BillSubjectDto billSubjectDto) {
        this.doBillDisLine(billSubjectDto);
    }
}
