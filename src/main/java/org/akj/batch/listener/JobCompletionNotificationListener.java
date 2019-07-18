package org.akj.batch.listener;

import java.io.File;

import org.akj.batch.constant.Constant;
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
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

	private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

	private final JdbcTemplate jdbcTemplate;

	@Value("${batch.job.ouput.target-folder}")
	private String targetFolder;

	@Value("${batch.job.failure.target-folder}")
	private String failtureFolder;

	@Value("${batch.job.input.file-source}")
	private String inputFolder;

	@Autowired
	public JobCompletionNotificationListener(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
			log.info("!!! JOB FINISHED! Time to verify the results");

			Integer count = jdbcTemplate.queryForObject("SELECT count(1) FROM person", null, Integer.class);

			log.info("Found <" + count + "> person records in the database.");

			// backup the source file to target folder
			File file = new File(inputFolder + File.separatorChar + Constant.FILE_SOURCE);
			String newPath = targetFolder + File.separatorChar + Constant.FILE_SOURCE;
			file.renameTo(new File(newPath));

			log.debug("event source file:" + file.getPath() + " been archived to: " + newPath);
		} else if (jobExecution.getStatus() == BatchStatus.FAILED) {
			log.error("!!! JOB FAILED, please check details for more information");

			// backup the source file to failure folder
			File file = new File(inputFolder + File.separatorChar + Constant.FILE_SOURCE);
			String newPath = failtureFolder + File.separatorChar + Constant.FILE_SOURCE;
			file.renameTo(new File(newPath));

			log.debug("event source file:" + file.getPath() + " been moved to: " + newPath);
		}
	}
}