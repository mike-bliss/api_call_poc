package com.bliss.startec2demo.batch;

import com.bliss.startec2demo.models.SqsMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PublishToSqsWriter implements ItemWriter<SqsMessage>, ItemStream {

    @Autowired
    private SqsClient sqsClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void write(List<? extends SqsMessage> sqsMessages) throws Exception {
        log.info("Inside PublishToSqsWriter. Sending {} messages to queue.", sqsMessages.size());
        List<SendMessageBatchRequestEntry> batchMessages = new ArrayList<>();
        sqsMessages.forEach(sqsMessage -> {
            String messageBody;
            try {
                messageBody = objectMapper.writeValueAsString(sqsMessage);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            SendMessageBatchRequestEntry batchRequestEntry = SendMessageBatchRequestEntry.builder()
                    .id(sqsMessage.getViolatorId().get(0))
                    .messageBody(messageBody)
                    .build();
            batchMessages.add(batchRequestEntry);
        });
        SendMessageBatchRequest sendMessageBatchRequest = SendMessageBatchRequest.builder()
                .queueUrl("http://localhost:4566/000000000000/api-calls-queue")
                .entries(batchMessages)
                .build();
        SendMessageBatchResponse response = sqsClient.sendMessageBatch(sendMessageBatchRequest);
        log.info("Send result: {} for queue.", response);
    }

    /**
     * Open the stream for the provided {@link ExecutionContext}.
     *
     * @param executionContext current step's {@link ExecutionContext}.  Will be the
     *                         executionContext from the last run of the step on a restart.
     * @throws IllegalArgumentException if context is null
     */
    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {

    }

    /**
     * Indicates that the execution context provided during open is about to be saved. If any state is remaining, but
     * has not been put in the context, it should be added here.
     *
     * @param executionContext to be updated
     * @throws IllegalArgumentException if executionContext is null.
     */
    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {

    }

    /**
     * If any resources are needed for the stream to operate they need to be destroyed here. Once this method has been
     * called all other methods (except open) may throw an exception.
     */
    @Override
    public void close() throws ItemStreamException {

    }
}
