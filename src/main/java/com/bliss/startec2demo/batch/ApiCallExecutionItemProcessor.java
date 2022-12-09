package com.bliss.startec2demo.batch;

import com.bliss.startec2demo.models.ApiCall;
import com.bliss.startec2demo.models.ApiCallResult;
import com.bliss.startec2demo.models.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.item.ItemProcessor;

import java.time.Instant;
import java.util.*;

@Slf4j
public class ApiCallExecutionItemProcessor implements ItemProcessor<ApiCallResult, ApiCallResult> {

    private Map<String, Map<String, List<String>>> instancesByAccountAndRegion = new HashMap<>();

    private Map<String, String> accountNameByChId = new HashMap<>();

    @Override
    public ApiCallResult process(ApiCallResult apiCallResult) throws Exception {
        instancesByAccountAndRegion.computeIfAbsent(apiCallResult.getChAccountId(), regionMap -> new HashMap<>())
                .computeIfAbsent(apiCallResult.getRegion(), resourceList -> new ArrayList<>())
                .add(apiCallResult.getResourceId());
        accountNameByChId.put(apiCallResult.getChAccountId(), apiCallResult.getAccountName());

        int seed = Instant.now().getNano();
        Random random = new Random(seed);
        int randNum = random.nextInt();
        if (randNum % 2 == 0) {
            apiCallResult.setSuccessMessage(String.format("Successfully deleted %s in account %s", apiCallResult.getResourceId(), apiCallResult.getAccountName()));
        }else{
            apiCallResult.setFailureMessage(String.format("Failed to delete %s in account %s", apiCallResult.getResourceId(), apiCallResult.getAccountName()));
        }
        return apiCallResult;

    }

    @AfterStep
    public void afterStep(StepExecution stepExecution) {
        log.info(instancesByAccountAndRegion.toString());
        instancesByAccountAndRegion.forEach((accountId, instancesByRegion) -> {
            instancesByRegion.forEach((region, instances) -> {
                executeApiCalls(instances, accountId, region);
                try {
                    Thread.sleep(200L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    public void executeApiCalls(List<String> instances, String accountId, String region) {
        String accountName = accountNameByChId.get(accountId);
        log.info("Starting instances {} for account {} in region {}", instances, accountName, region);
    }

}
