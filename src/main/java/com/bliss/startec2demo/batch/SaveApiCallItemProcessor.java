package com.bliss.startec2demo.batch;

import com.bliss.startec2demo.models.ApiCall;
import com.bliss.startec2demo.models.ApiCallResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.item.ItemProcessor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SaveApiCallItemProcessor implements ItemProcessor<ApiCall, ApiCallResult> {

    @Override
    public ApiCallResult process(ApiCall apiCall) throws Exception {
        ApiCallResult apiCallResult = new ApiCallResult(apiCall);
        return apiCallResult;
    }

}
