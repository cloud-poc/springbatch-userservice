package org.akj.batch.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
@Configuration
@ConditionalOnProperty("batch.scheduling.enabled")
@Profile({"master"})
public class ScheduledJobLancherConfiguration {
    @Autowired
    Job job;

    @Value("${batch.job.name}")
    private String jobName;

    @Autowired
    private JobRepository repository;

    @Bean("scheduledJobLauncher")
    public JobLauncher jobLauncher(JobRepository jobRepo) {
        SimpleJobLauncher simpleJobLauncher = new SimpleJobLauncher();
        simpleJobLauncher.setJobRepository(jobRepo);
        return simpleJobLauncher;
    }

    @Scheduled(cron = "${batch.job.scheduler.cron}")
    public void perform() throws JobExecutionAlreadyRunningException, JobRestartException,
            JobInstanceAlreadyCompleteException, JobParametersInvalidException {
        JobParameters params = new JobParametersBuilder().addString(jobName, String.valueOf(System.currentTimeMillis()))
                .toJobParameters();
        jobLauncher(repository).run(job, params);
    }
}
