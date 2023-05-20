package com.szhtxx.etcloud.smser.dto;

import java.io.*;
import java.util.*;

public class SameRateListDto implements Serializable
{
    private List<SameRateTotalDto> totalDtoList;
    
    public List<SameRateTotalDto> getTotalDtoList() {
        if (this.totalDtoList == null) {
            this.totalDtoList = new ArrayList<SameRateTotalDto>(5);
        }
        return this.totalDtoList;
    }
    
    public void setTotalDtoList(final List<SameRateTotalDto> totalDtoList) {
        this.totalDtoList = totalDtoList;
    }
}
