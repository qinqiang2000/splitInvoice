package com.szhtxx.etcloud.smser;

import com.alibaba.fastjson.JSON;
import com.szhtxx.etcloud.smser.dto.SmsRequestDto;
import com.szhtxx.etcloud.smser.dto.SmsResultDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class SMSERNTest {
    @Test
    void doSMSER() throws IOException {
        String path = "src/test/java/com/szhtxx/etcloud/smser/";
        String in = "in1.json";

        // set the required fields of the requestDto object
        String jsonString = new String(Files.readAllBytes(Paths.get(path+in)));
        SmsRequestDto requestDto = JSON.parseObject(jsonString, SmsRequestDto.class);

        SMSERN smsern = new SMSERN();
        SmsResultDto resultDto = smsern.doSMSER(requestDto);

        // save the result
        TestUtil.createJsonFile(resultDto, path + "t_" + in);
    }
}