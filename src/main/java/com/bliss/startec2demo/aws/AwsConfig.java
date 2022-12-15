package com.bliss.startec2demo.aws;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

@Data
@Configuration
public class AwsConfig {

    private URI endpointUri;

    private URI endpointForLocalstack = URI.create("http://localhost:4566");

    private String awsAccessKeyIdForLocalstack = "test";

    private String awsSecretKeyForLocalstack = "test";

    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(awsAccessKeyIdForLocalstack, awsSecretKeyForLocalstack)
                )).region(Region.US_EAST_1)
                .endpointOverride(endpointForLocalstack)
                .build();
    }

}

