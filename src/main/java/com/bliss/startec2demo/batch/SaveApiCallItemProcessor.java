package com.bliss.startec2demo.batch;

import com.bliss.startec2demo.models.ApiCall;
import com.bliss.startec2demo.models.ApiCallResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class SaveApiCallItemProcessor implements ItemProcessor<ApiCall, ApiCallResult> {

    @Override
    public ApiCallResult process(ApiCall apiCall) throws Exception {
        ApiCallResult apiCallResult = new ApiCallResult(apiCall);
        return apiCallResult;
    }

}
