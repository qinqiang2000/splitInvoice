package com.szhtxx.etcloud.smser.methods.smser;

import org.slf4j.*;
import java.util.*;
import com.szhtxx.etcloud.smser.dto.*;

public class GoodsMergeMethods
{
    private static final Logger LOG;
    
    static {
        LOG = LoggerFactory.getLogger((Class)GoodsMergeMethods.class);
    }
    
    public BillDealResultDto mergeGoods(final BillSubjectDto billSubjectDto, final SmruleConfigDto smruleConfig) {
        final List<BillDetailDto> detailList = billSubjectDto.getBillDList();
        return null;
    }
    
    public BillDealResultDto mergeGoodsIncTax(final BillSubjectDto billSubjectDto, final SmruleConfigDto smruleConfig) {
        return this.mergeGoods(billSubjectDto, smruleConfig);
    }
}
