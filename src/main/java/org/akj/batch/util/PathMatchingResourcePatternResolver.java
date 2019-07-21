package org.akj.batch.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class PathMatchingResourcePatternResolver {

	public String[] getResources(String directory, String filter) throws IOException {
		String[] resources = null;
		Assert.notNull(directory, "input source can not be null");
		File[] files = null;

		if (StringUtils.isEmpty(filter)) {
			files = new File(directory).listFiles();
		} else {
			files = new File(directory).listFiles(file -> file.getPath().endsWith(filter));
		}

		if (files != null) {
			resources = new String[files.length];
			resources = Arrays.asList(files).parallelStream().map(File::getPath)
					.collect(Collectors.toList()).toArray(resources);
		}
		files = null;

		return resources;
	}
}
