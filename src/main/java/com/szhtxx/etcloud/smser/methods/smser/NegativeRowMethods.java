package com.szhtxx.etcloud.smser.methods.smser;

import org.slf4j.*;
import com.szhtxx.etcloud.smser.dto.*;

public class NegativeRowMethods
{
    private static final Logger LOG;
    
    static {
        LOG = LoggerFactory.getLogger((Class)NegativeRowMethods.class);
    }
    
    public BillDealResultDto doAgainst(final BillSubjectDto billSubjectDto, final SmruleConfigDto smruleConfig) {
        return null;
    }
    
    public BillDealResultDto doAgainstIncTax(final BillSubjectDto billSubjectDto, final SmruleConfigDto smruleConfig) {
        final BillDealResultDto resultDto = this.doAgainst(billSubjectDto, smruleConfig);
        return resultDto;
    }
}
