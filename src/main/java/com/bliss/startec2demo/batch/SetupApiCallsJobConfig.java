package com.bliss.startec2demo.batch;

import com.bliss.startec2demo.models.ActionDetail;
import com.bliss.startec2demo.models.SqsMessage;
import com.bliss.startec2demo.models.Violation;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@EnableBatchProcessing
@Configuration
public class SetupApiCallsJobConfig {

    private final String VIOLATION_INSERT_STATEMENT = "insert into VIOLATIONS(request_id,delegated_action_id,resource_id,account_name,ch_account_id,region,status,status_message) " +
            "values (:requestId,:delegatedActionId,:resourceId,:accountName,:chAccountId,:region,:status,:statusMessage)";

    private final String VIOLATION_SELECT_STATEMENT = "select id,request_id,delegated_action_id,resource_id,account_name,ch_account_id,region,status,status_message";

    private final String VIOLATION_FROM_CLAUSE = "from VIOLATIONS";

    private final String VIOLATION_WHERE_CLAUSE = "where delegated_action_id=:delegatedActionId";

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public DataSource dataSource;

    // SAVE VIOLATION INFO TO DB STEP

//    @Bean
//    public ItemReader<Violation> s3JsonItemReader() {
//        return new JsonItemReaderBuilder<Violation>()
//                .jsonObjectReader(new JacksonJsonObjectReader<>(Violation.class))
//                .resource(new ClassPathResource("static/api_call_list_20000.json"))
//                .name("s3JsonItemReader")
//                .build();
//    }

    @Bean
    @StepScope
    public ItemReader<Violation> actionRequestItemReader(@Value("#{jobParameters['s3FilePath']}") String s3FilePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(s3FilePath);
        try (inputStream) {
            ActionDetail actionDetail = objectMapper.readValue(inputStream, ActionDetail.class);
            return new ListItemReader<>(actionDetail.getViolations());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    @StepScope
    public ItemProcessor<Violation, Violation> saveViolationsItemProcessor(@Value("#{jobParameters['requestId']}") Long requestId,
                                                                           @Value("#{jobParameters['delegatedActionId']}") Long delegatedActionId) {
        return new SaveViolationsItemProcessor(requestId, delegatedActionId);
    }

    @Bean
    @StepScope
    public ItemWriter<Violation> saveViolationsItemWriter() {
        return new JdbcBatchItemWriterBuilder<Violation>()
                .dataSource(dataSource)
                .sql(VIOLATION_INSERT_STATEMENT)
                .beanMapped()
                .build();
    }

    @Bean
    public Step readJsonFromS3Step() {
        log.info("persisting json info to db");
        return this.stepBuilderFactory.get("readJsonFromS3Step")
                .<Violation, Violation>chunk(5000)
                .reader(actionRequestItemReader(null))
                .processor(saveViolationsItemProcessor(null, null))
                .writer(saveViolationsItemWriter())
                .build();
    }

    // PUBLISH API CALLS TO S3 STEP

    //Reader
    @Bean
    @StepScope
    public ItemReader<Violation> violationItemReader(@Value("#{stepExecutionContext['minValue']}") Long minValue
            , @Value("#{stepExecutionContext['maxValue']}") Long maxValue
            , @Value("#{jobParameters['delegatedActionId']}") Long delegatedActionId) throws Exception {
        log.info("reading {} to {}", minValue, maxValue);
        JdbcPagingItemReader<Violation> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(this.dataSource);
        reader.setFetchSize(5000);
        reader.setRowMapper(new ViolationRowMapper());
        reader.setQueryProvider(violationQueryProvider());

        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("delegatedActionId", delegatedActionId);
        reader.setParameterValues(parameterValues);

        return reader;
    }

    //Processor
    @Bean
    @StepScope
    public ItemProcessor<Violation, SqsMessage> groupApiCallsItemProcessor(@Value("#{jobParameters['requestId']}") Long requestId) {
        return new GroupApiCallsItemProcessor(requestId);
    }

    //Writer
    @Bean
    @StepScope
    public ItemWriter<SqsMessage> publishApiCallsToSqsItemWriter() {
        return new PublishToSqsWriter();
    }

    //Step
    @Bean
    public Step publishApiCallsToSqsStep() throws Exception {
        return this.stepBuilderFactory.get("publishApiCallsToSqsStep")
                .<Violation, SqsMessage>chunk(5000)
                .reader(violationItemReader(null, null, null))
                .processor(groupApiCallsItemProcessor(null))
                .writer(publishApiCallsToSqsItemWriter())
                //.partitioner(publishApiCallsToSqsStepSlave().getName(), partitioner(null))
                //.step(publishApiCallsToSqsStepSlave())
                //.gridSize(4)
                //.taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    //Slave Step
    @Bean
    public Step publishApiCallsToSqsStepSlave() throws Exception {
        return stepBuilderFactory.get("publishApiCallsToSqsStepSlave")
                .<Violation, SqsMessage>chunk(5000)
                .reader(violationItemReader(null, null, null))
                .processor(groupApiCallsItemProcessor(null))
                .writer(publishApiCallsToSqsItemWriter())
                .build();
    }

    @Bean
    public PagingQueryProvider violationQueryProvider() throws Exception {
        SqlPagingQueryProviderFactoryBean factory = new SqlPagingQueryProviderFactoryBean();

        factory.setSelectClause(VIOLATION_SELECT_STATEMENT);
        factory.setFromClause(VIOLATION_FROM_CLAUSE);
        factory.setWhereClause(VIOLATION_WHERE_CLAUSE);
        factory.setSortKey("id");
        factory.setDataSource(dataSource);

        return factory.getObject();
    }

    @Bean
    @StepScope
    public ColumnRangePartitioner partitioner(@Value("#{jobParameters['delegatedActionId']}") Long delegatedActionId) {
        ColumnRangePartitioner columnRangePartitioner = new ColumnRangePartitioner(delegatedActionId);
        columnRangePartitioner.setDataSource(this.dataSource);

        return columnRangePartitioner;
    }

    @Bean
    public Job setupApiCallsJob() throws Exception {
        return this.jobBuilderFactory.get("setupApiCallsJob")
                .start(readJsonFromS3Step())
                .next(publishApiCallsToSqsStep())
                .build();
    }

}
