package com.szhtxx.etcloud.smser.methods.smser.util;

import com.szhtxx.etcloud.smser.exception.*;
import org.apache.commons.lang3.*;

public class StringUtilMethods
{
    public Boolean isEmpty(final String str, final String msg) {
        if (str != null && str.trim().length() == 0) {
            throw new EtcRuleException(msg);
        }
        if (StringUtils.isEmpty((CharSequence)str)) {
            throw new EtcRuleException(msg);
        }
        return false;
    }
}
