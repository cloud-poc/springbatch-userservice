package org.akj.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.akj.batch.constant.Constant;
import org.apache.commons.io.FileUtils;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileUrlResource;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Slf4j
public class CustomStepExecutionListener extends StepExecutionListenerSupport {
    @Value("${batch.job.ouput.path}")
    private String targetFolder;

    @Value("${batch.job.failure.path}")
    private String failtureFolder;

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        String fileName = stepExecution.getExecutionContext().getString("fileName");
        log.info("Step finshed, file name is " + fileName + ", status:" + stepExecution.getStatus());
        try {
            String filePath = new FileUrlResource(fileName).getFile().getPath();
            filePath = filePathExtractor(filePath);

            log.info("[file-archive-task] started file archive, file name: " + filePath);
            if (stepExecution.getStatus() == BatchStatus.COMPLETED) {
                FileUtils.moveFileToDirectory(new File(filePath), new File(targetFolder), true);
            } else {
                FileUtils.moveFileToDirectory(new File(filePath), new File(failtureFolder), true);
            }
            log.info("[file-archive-task] finshed file archive, file name: " + filePath);

        } catch (Exception e) {
            log.error("system error, archive file failed," + fileName + ", root cause:" + e.getMessage());
        }

        return super.afterStep(stepExecution);
    }

    private String filePathExtractor(String filePath) {
        if (filePath.startsWith(Constant.FILE_PREFIX)) {
            filePath = filePath.substring(filePath.indexOf(Constant.FILE_PREFIX) + Constant.FILE_PREFIX.length() + 1);
        }

        return filePath;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Step started, file name is " + stepExecution.getExecutionContext().getString("fileName"));
        super.beforeStep(stepExecution);
    }
}
