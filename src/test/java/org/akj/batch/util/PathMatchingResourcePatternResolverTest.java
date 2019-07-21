package org.akj.batch.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;

class PathMatchingResourcePatternResolverTest {

	private String inputFolder = "c:/temp/source";

	private String fileFilter = ".csv";

	@Test
	public void test() {
		MultiResourcePartitioner partitioner = new MultiResourcePartitioner();
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		String[] resources = null;
		try {
			resources = resolver.getResources(inputFolder, fileFilter);
		} catch (IOException e) {
			System.out
					.println("system error, invalid input resource: " + inputFolder + File.separatorChar + fileFilter);
		}

		Assertions.assertNotNull(resources);
	}

	@Test
	public void test1() throws URISyntaxException, IOException {
		FileUrlResource fileUrlResource = new FileUrlResource("file:/c:/temp/source/sample-data_3.csv");
		Assertions.assertNotNull(fileUrlResource);
		
		System.out.println(fileUrlResource.getURL());
		System.out.println(fileUrlResource.getURI().getPath());
		System.out.println(fileUrlResource.getFile().getPath());
		System.out.println(fileUrlResource.getFile().getName());
		
	}
}
