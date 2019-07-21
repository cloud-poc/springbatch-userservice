package org.akj.batch.configuration.partitioner;

import lombok.extern.slf4j.Slf4j;
import org.akj.batch.util.PathMatchingResourcePatternResolver;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class CustomMultiResourcePartitioner implements Partitioner {

	@Value("${batch.job.input.path}")
	private String inputFolder;

	@Value("${batch.job.input.filter}")
	private String fileFilter;

	@Value("${batch.partitioner.partition}")
	private int partition;

	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {
		Map<String, ExecutionContext> map = new HashMap<>(gridSize);
		AtomicInteger partitionNumber = new AtomicInteger(1);
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		try {
			String[] resources = resolver.getResources(inputFolder, fileFilter);
			log.info("started partitioning, " + resources.length + " resources been found as input");
			Arrays.stream(resources).forEach(filePath -> {
					ExecutionContext context = new ExecutionContext();
					context.putString("fileName", filePath);
					map.put("partition" + partitionNumber.getAndIncrement(), context);
			});
		} catch (IOException e) {
			log.error("system error, invalid input resource: " + inputFolder + File.separatorChar + fileFilter);
		}

		return map;
	}
}
