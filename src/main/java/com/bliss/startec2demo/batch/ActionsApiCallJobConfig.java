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
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.sql.DataSource;
import java.util.List;

@Slf4j
@EnableBatchProcessing
@Configuration
public class ActionsApiCallJobConfig {

    private final String SQL_INSERT_STATEMENT = "insert into API_CALL_RESULTS(account_name,ch_account_id,resource_id,region,status,success_message,failure_message) " +
            "values (:accountName,:chAccountId,:resourceId,:region,:status,:successMessage,:failureMessage)";

    private final String SQL_SELECT_STATEMENT = "select id,account_name,ch_account_id,resource_id,region,status,success_message,failure_message";

    private final String FROM_CLAUSE = "from API_CALL_RESULTS";

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public DataSource dataSource;

    // SAVE API CALL TO DB STEP

    @Bean
    public ItemReader<ApiCall> saveApiCallItemReader() {
        return new JsonItemReaderBuilder<ApiCall>()
                .jsonObjectReader(new JacksonJsonObjectReader<>(ApiCall.class))
                .resource(new ClassPathResource("static/api_call_list_20000.json"))
                .name("apiCallJsonItemReader")
                .build();
    }

    @Bean
    public ItemProcessor<ApiCall, ApiCallResult> saveApiCallItemProcessor() {
        return new SaveApiCallItemProcessor();
    }

    @Bean
    public ItemWriter<ApiCallResult> saveApiCallItemWriter() {
        return new JdbcBatchItemWriterBuilder<ApiCallResult>()
                .dataSource(dataSource)
                .sql(SQL_INSERT_STATEMENT)
                .beanMapped()
                .build();
    }

    @Bean
    public Step saveApiCallsToDbStep(ItemReader<ApiCall> saveApiCallItemReader,
                                     ItemProcessor<ApiCall, ApiCallResult> saveApiCallItemProcessor,
                                     ItemWriter<ApiCallResult> saveApiCallItemWriter) {
        log.info("persisting json info to db");
        return this.stepBuilderFactory.get("saveApiCallsToDbStep")
                .<ApiCall, ApiCallResult>chunk(1000)
                .reader(saveApiCallItemReader)
                .processor(saveApiCallItemProcessor)
                .writer(saveApiCallItemWriter)
                .build();
    }

    // API CALL EXECUTION STEP

    @Bean
	public ItemReader<ApiCallResult> apiCallExecutionItemReader() throws Exception {
		return new JdbcPagingItemReaderBuilder()
				.dataSource(dataSource)
				.name("apiCallExecutionItemReader")
				.queryProvider(apiCallQueryProvider())
				.rowMapper(new ApiCallResultRowMapper())
				.pageSize(1000)
				.build();
	}

	@Bean
	public PagingQueryProvider apiCallQueryProvider() throws Exception {
		SqlPagingQueryProviderFactoryBean factory = new SqlPagingQueryProviderFactoryBean();

		factory.setSelectClause(SQL_SELECT_STATEMENT);
		factory.setFromClause(FROM_CLAUSE);
		factory.setSortKey("id");
		factory.setDataSource(dataSource);

		return factory.getObject();
	}

    @Bean
    public ItemProcessor<ApiCallResult, ApiCallResult> apiCallExecutionItemProcessor() {
        return new ApiCallExecutionItemProcessor();
    }

    @Bean
    public ItemWriter<ApiCallResult> apiCallExecutionItemWriter() {
        return new JdbcBatchItemWriterBuilder<ApiCallResult>()
                .dataSource(dataSource)
                .sql(SQL_INSERT_STATEMENT)
                .beanMapped()
                .build();
    }

    @Bean
    public Step apiCallExecutionStep(ItemReader<ApiCallResult> apiCallExecutionItemReader,
                            ItemProcessor<ApiCallResult, ApiCallResult> apiCallExecutionItemProcessor,
                            ItemWriter<ApiCallResult> apiCallExecutionItemWriter,
                            SimpleAsyncTaskExecutor apiCallAsyncTaskExecutor ) {
        return this.stepBuilderFactory.get("apiCallExecutionStep")
                .<ApiCallResult, ApiCallResult>chunk(1000)
                .reader(apiCallExecutionItemReader)
                .processor(apiCallExecutionItemProcessor)
                .writer(apiCallExecutionItemWriter)
                .taskExecutor(apiCallAsyncTaskExecutor)
                .build();
    }

    @Bean
    public Job apiCallJob(Step saveApiCallsToDbStep,
                          Step apiCallExecutionStep) {
        return this.jobBuilderFactory.get("apiCallJob")
                .start(saveApiCallsToDbStep)
                .next(apiCallExecutionStep)
                .build();
    }

    @Bean
    public SimpleAsyncTaskExecutor apiCallAsyncTaskExecutor() {
        SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
        simpleAsyncTaskExecutor.setConcurrencyLimit(3);
        simpleAsyncTaskExecutor.setThreadNamePrefix("CloudHealthActionExecutorThread");
        return simpleAsyncTaskExecutor;
    }
}
