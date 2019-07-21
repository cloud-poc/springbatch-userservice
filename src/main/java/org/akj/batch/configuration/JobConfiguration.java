package org.akj.batch.configuration;

import org.akj.batch.listener.CustomJobExcecutionListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableBatchProcessing
@Import(value = { MasterStepConfiguration.class, SlaveStepConfiguation.class })
public class JobConfiguration {
	@Value("${batch.job.name}")
	private String jobName;

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	@Qualifier("masterStep")
	private Step masterStep;

	@Bean
	@Profile({ "master"})
	public Job job(CustomJobExcecutionListener listener) {
		return jobBuilderFactory.get(jobName).listener(listener).incrementer(new RunIdIncrementer()).flow(masterStep)
				.end().build();
	}
}
