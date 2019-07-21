package org.akj.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CustomJobExcecutionListener extends JobExecutionListenerSupport {

	private static final Logger log = LoggerFactory.getLogger(CustomJobExcecutionListener.class);

	private final JdbcTemplate jdbcTemplate;

	@Value("${batch.job.ouput.path}")
	private String targetFolder;

	@Value("${batch.job.failure.path}")
	private String failtureFolder;

	@Autowired
	public CustomJobExcecutionListener(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
			log.info("!!! JOB FINISHED! Time to verify the results");
			Integer count = jdbcTemplate.queryForObject("SELECT count(1) FROM person", null, Integer.class);
			log.info("Found <" + count + "> person records in the database.");

		} else if (jobExecution.getStatus() == BatchStatus.FAILED) {
			log.error("!!! JOB FAILED, please check details for more information");
		}
	}

}