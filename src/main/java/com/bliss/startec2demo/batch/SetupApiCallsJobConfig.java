package com.bliss.startec2demo.batch;

import com.bliss.startec2demo.aws.AwsConfig;
import com.bliss.startec2demo.models.Violation;
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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;

@Slf4j
@EnableBatchProcessing
@Configuration
public class SetupApiCallsJobConfig {

    private final String VIOLATION_INSERT_STATEMENT = "insert into VIOLATIONS(request_id,delegated_action_id,resource_id,account_name,ch_account_id,region,status,status_message) " +
            "values (:requestId,:delegatedActionId,:resourceId,:accountName,:chAccountId,:region,:status,:statusMessage)";

    private final String VIOLATION_SELECT_STATEMENT = "select id,request_id,delegated_action_id,resource_id,account_name,ch_account_id,region,status,status_message";

    private final String VIOLATION_FROM_CLAUSE = "from VIOLATIONS";

    private final String VIOLATION_WHERE_CLAUSE = "where delegated_action_id=123";

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public DataSource dataSource;

    // SAVE VIOLATION INFO TO DB STEP

    @Bean
    public ItemReader<Violation> s3JsonItemReader() {
        return new JsonItemReaderBuilder<Violation>()
                .jsonObjectReader(new JacksonJsonObjectReader<>(Violation.class))
                .resource(new ClassPathResource("static/api_call_list_20000.json"))
                .name("s3JsonItemReader")
                .build();
    }

    @Bean
    public ItemProcessor<Violation, Violation> saveViolationsItemProcessor() {
        return new SaveViolationsItemProcessor();
    }

    @Bean
    public ItemWriter<Violation> saveViolationsItemWriter() {
        return new JdbcBatchItemWriterBuilder<Violation>()
                .dataSource(dataSource)
                .sql(VIOLATION_INSERT_STATEMENT)
                .beanMapped()
                .build();
    }

    @Bean
    public Step readJsonFromS3Step(ItemReader<Violation> s3JsonItemReader,
                                     ItemProcessor<Violation, Violation> saveViolationsItemProcessor,
                                     ItemWriter<Violation> saveViolationsItemWriter) {
        log.info("persisting json info to db");
        return this.stepBuilderFactory.get("readJsonFromS3Step")
                .<Violation, Violation>chunk(1000)
                .reader(s3JsonItemReader)
                .processor(saveViolationsItemProcessor)
                .writer(saveViolationsItemWriter)
                .build();
    }

    // PUBLISH API CALLS TO S3 STEP

    @Bean
	public ItemReader<Violation> violationItemReader() throws Exception {
		return new JdbcPagingItemReaderBuilder()
				.dataSource(dataSource)
				.name("violationItemReader")
				.queryProvider(violationQueryProvider())
				.rowMapper(new ViolationRowMapper())
				.pageSize(1000)
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
    public ItemProcessor<Violation, Violation> groupApiCallsItemProcessor() {
        return new GroupApiCallsItemProcessor();
    }

    @Bean
    public ItemWriter<Violation> publishApiCallsToSqsItemWriter() {
        return new NoOpItemWriter();
    }

    @Bean
    public Step publishApiCallsToSqsStep(ItemReader<Violation> violationItemReader,
                            ItemProcessor<Violation, Violation> groupApiCallsItemProcessor,
                            ItemWriter<Violation> publishApiCallsToSqsItemWriter ) {
        return this.stepBuilderFactory.get("publishApiCallsToSqsStep")
                .<Violation, Violation>chunk(1000)
                .reader(violationItemReader)
                .processor(groupApiCallsItemProcessor)
                .writer(publishApiCallsToSqsItemWriter)
                .build();
    }

    @Bean
    public Job setupApiCallsJob(Step readJsonFromS3Step,
                                Step publishApiCallsToSqsStep) {
        return this.jobBuilderFactory.get("setupApiCallsJob")
                .start(readJsonFromS3Step)
                .next(publishApiCallsToSqsStep)
                .build();
    }

}
