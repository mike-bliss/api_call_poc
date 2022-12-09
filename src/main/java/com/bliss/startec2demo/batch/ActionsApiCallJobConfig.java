package com.bliss.startec2demo.batch;

import com.bliss.startec2demo.models.ApiCall;
import com.bliss.startec2demo.models.ApiCallResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import javax.sql.DataSource;

@Slf4j
@EnableBatchProcessing
@Configuration
public class ActionsApiCallJobConfig {

    private String sqlInsertStatement = "insert into API_CALL_RESULTS(account_name,ch_account_id,resource_id,region,status,success_message,failure_message) " +
            "values (:accountName,:chAccountId,:resourceId,:region,:status,:successMessage,:failureMessage)";

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public DataSource dataSource;

    @Bean
    public ItemReader<ApiCall> saveApiCAllItemReader() {
        return new JsonItemReaderBuilder<ApiCall>()
                .jsonObjectReader(new JacksonJsonObjectReader<>(ApiCall.class))
                .resource(new ClassPathResource("static/api_call_list_20000.json"))
                .name("apiCallJsonItemReader")
                .build();
    }

    @Bean
    public ItemProcessor<ApiCall, ApiCallResult> saveApiCallItemProcessor() {
        return new ApiCallExecutionItemProcessor();
    }

    @Bean
    public ItemWriter<ApiCallResult> saveApiCallItemWriter() {
        log.info("Writing ");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        return new JdbcBatchItemWriterBuilder<ApiCallResult>()
                .namedParametersJdbcTemplate(namedParameterJdbcTemplate)
                .sql(sqlInsertStatement)
                .beanMapped()
                .build();
    }

    @Bean
    public Step saveApiCallsToDbStep(ItemReader<ApiCall> apiCallJsonItemReader,
                                  ItemProcessor<ApiCall, ApiCallResult> apiCallExecutionItemProcessor,
                                  ItemWriter<ApiCallResult> apiCallResultItemWriter) {
        log.info("persisting json info to db");
        return this.stepBuilderFactory.get("saveApiCallsToDbStep")
                .<ApiCall, ApiCallResult>chunk(1000)
                .reader(apiCallJsonItemReader)
                .writer(apiCallResultItemWriter)
                .build();
    }

    @Bean
    public Step apiCallStep(ItemReader<ApiCall> apiCallJsonItemReader,
                            ItemProcessor<ApiCall, ApiCallResult> apiCallExecutionItemProcessor,
                            ItemWriter<ApiCallResult> apiCallResultItemWriter) {
        log.info("creating api call step");
        return this.stepBuilderFactory.get("apiCallStep")
                .<ApiCall, ApiCallResult>chunk(1000)
                .reader(apiCallJsonItemReader)
                .processor(apiCallExecutionItemProcessor)
                .writer(apiCallResultItemWriter)
//                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @Bean
    public Job apiCallJob(Step apiCallStep) {
        log.info("creating api call job");
        return this.jobBuilderFactory.get("apiCallJob")
                .start()
                .next(saveApiCallsToDbStep)
                .build();
    }

//    @Bean
//    public SimpleAsyncTaskExecutor simpleAsyncTaskExecutor() {
//        SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
//        simpleAsyncTaskExecutor.setConcurrencyLimit(10);
//        simpleAsyncTaskExecutor.setThreadNamePrefix("CloudHealthActionExecutorThread");
//        return simpleAsyncTaskExecutor;
//    }
}
