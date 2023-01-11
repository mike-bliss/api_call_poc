package com.bliss.startec2demo.batch.launcher;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class JobExecutor {

    @Autowired
    private JobLauncher actionJobLauncher;

    @Autowired
    private Job setupApiCallsJob;

    public void launchJob() {
        long requestId = generateId();
        long delegatedActionId = generateId();
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("requestId", requestId)
                .addLong("delegatedActionId", delegatedActionId)
                .addString("s3FilePath", "static/api_call_list_20000_with_headers.json")
                .toJobParameters();
        try {
            actionJobLauncher.run(setupApiCallsJob, jobParameters);
        } catch (JobExecutionAlreadyRunningException e) {
            throw new RuntimeException(e);
        } catch (JobRestartException e) {
            throw new RuntimeException(e);
        } catch (JobInstanceAlreadyCompleteException e) {
            throw new RuntimeException(e);
        } catch (JobParametersInvalidException e) {
            throw new RuntimeException(e);
        }
    }

    public long generateId() {
        SecureRandom random = new SecureRandom();
        return Math.abs(random.nextInt());
    }
}
