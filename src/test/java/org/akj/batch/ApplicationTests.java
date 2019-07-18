package org.akj.batch;

import org.akj.batch.constant.Constant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@SpringBatchTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class ApplicationTests {

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	public void launchJob() throws Exception {

		// testing a job
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();

		// Testing a individual step
		// JobExecution jobExecution = jobLauncherTestUtils.launchStep("step1");

		Assertions.assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

		int[] count = jdbcTemplate.batchUpdate(Constant.TRANCATE_TABLE_SQL);
		System.out.println(count);
	}

}
