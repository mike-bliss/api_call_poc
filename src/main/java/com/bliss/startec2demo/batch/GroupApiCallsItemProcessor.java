package com.bliss.startec2demo.batch;

import com.bliss.startec2demo.aws.AwsConfig;
import com.bliss.startec2demo.models.Violation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.*;

@Slf4j
public class GroupApiCallsItemProcessor implements ItemProcessor<Violation, Violation> {

    private Map<Long, Map<String, List<Long>>> violationsByAccountAndRegion = new HashMap<>();

    @Autowired
    public SqsClient sqsClient;

    @Override
    public Violation process(Violation violation) throws Exception {
        violationsByAccountAndRegion.computeIfAbsent(violation.getChAccountId(), regionMap -> new HashMap<>())
                .computeIfAbsent(violation.getRegion(), resourceList -> new ArrayList<>())
                .add(violation.getId());

        return violation;
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        stepExecution.getExecutionContext().put("request_id", 1);

//        CreateQueueRequest createQueueRequest = CreateQueueRequest.builder()
//                .queueName("apiCallsQueue")
//                .build();
//        sqsClient.createQueue(createQueueRequest);
    }

    @AfterStep
    public void afterStep(StepExecution stepExecution) {
        violationsByAccountAndRegion.forEach((chAccountId, violationsByRegion) -> {
            violationsByRegion.forEach((region, violationIds) -> {
                String messageBody = String.format("{\"violatorIds\": %s, \"requestId\": %s }", violationIds, stepExecution.getExecutionContext().get("request_id"));
                GetQueueUrlRequest getQueueUrlRequest = GetQueueUrlRequest.builder()
                        .queueName("api-calls-queue")
                        .build();
                GetQueueUrlResponse getQueueUrlResponse = sqsClient.getQueueUrl(getQueueUrlRequest);
                SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                        .queueUrl(getQueueUrlResponse.queueUrl())
                        .messageBody(messageBody)
                        .build();
                sqsClient.sendMessage(sendMessageRequest);
                log.info("Sending message to SQS: {}", messageBody);
            });
        });
    }

}
