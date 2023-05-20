package com.szhtxx.etcloud.smser;

import com.alibaba.fastjson.JSON;
import com.szhtxx.etcloud.smser.dto.SmsRequestDto;
import com.szhtxx.etcloud.smser.dto.SmsResultDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

class SMSERNTest {
    @Test
    void doSMSER() throws IOException {
        String path = "src/test/java/com/szhtxx/etcloud/smser/";
        String in = "in1.json";
        String out = "out1.json";

        // set the required fields of the requestDto object
        String jsonString = new String(Files.readAllBytes(Paths.get(path+in)));
        SmsRequestDto requestDto = JSON.parseObject(jsonString, SmsRequestDto.class);

        SMSERN smsern = new SMSERN();
        SmsResultDto resultDto = smsern.doSMSER(requestDto);

        // compare the good result and what we return
        String goodRet = new String(Files.readAllBytes(Paths.get(path+out)));
        String ret = JSON.toJSONString(resultDto);
        List<String> excludeKeys = Arrays.asList("invoiceNO", "invoiceDetailNO");
        System.out.println(new CompareJson().compareJsonObject(goodRet, ret, excludeKeys));

        // save the result
        TestUtil.createJsonFile(resultDto, path + "new_" + out);
    }
}