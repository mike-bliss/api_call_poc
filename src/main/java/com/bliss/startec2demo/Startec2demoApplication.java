package com.bliss.startec2demo;

import com.bliss.startec2demo.batch.launcher.JobExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class Startec2demoApplication implements CommandLineRunner {

    @Autowired
    private JobExecutor jobExecutor;

    public static void main(String[] args) {
        log.info("running api calls app");
        SpringApplication.run(Startec2demoApplication.class, args);
    }

    /**
     * Callback used to run the bean.
     *
     * @param args incoming main method arguments
     * @throws Exception on error
     */
    @Override
    public void run(String... args) throws Exception {
        jobExecutor.launchJob();
    }
}
