package com.bliss.startec2demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class Startec2demoApplication {

    public static void main(String[] args) {
        log.info("running api calls app");
        SpringApplication.run(Startec2demoApplication.class, args);
    }

}
