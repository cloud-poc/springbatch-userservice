package org.akj.batch.configuration;

import lombok.extern.slf4j.Slf4j;
import org.akj.batch.constant.Constant;
import org.akj.batch.entity.Person;
import org.akj.batch.listener.CustomJobExcecutionListener;
import org.akj.batch.processor.PersonItemProcessor;
import org.akj.batch.repository.PeopleRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.beans.PropertyEditor;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

//@Configuration
//@EnableBatchProcessing
@Slf4j
public class SingleSourceConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    private PeopleRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${batch.job.name}")
    private String jobName;

    @Value("${batch.job.chunk.size}")
    private int chunkSize;

    @Value("${batch.job.input.path}")
    private String inputFolder;

    @Bean
    public FlatFileItemReader<Person> reader() {
        BeanWrapperFieldSetMapper<Person> beanMapper = new BeanWrapperFieldSetMapper<Person>();
        beanMapper.setTargetType(Person.class);

        Map<String, PropertyEditor> customEditors = new HashMap<String, PropertyEditor>();
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        customEditors.put("java.util.Date", new CustomDateEditor(dateFormatter, false));
        beanMapper.setCustomEditors(customEditors);
        return new FlatFileItemReaderBuilder<Person>().name("personItemReader")
                .resource(new FileSystemResource(inputFolder + File.separatorChar + Constant.FILE_SOURCE)).delimited()
                .names(new String[]{"pid", "firstName", "lastName", "age", "gender", "dateOfBirth", "height",
                        "weight"})
                .fieldSetMapper(beanMapper).build();
    }

    @Bean
    public PersonItemProcessor processor() {
        return new PersonItemProcessor();
    }

    @Bean
    public RepositoryItemWriter<Person> writer() {
        return new RepositoryItemWriterBuilder<Person>().repository(repository).methodName("save").build();
    }

    @Bean
    public Job importUserJobflow(CustomJobExcecutionListener listener) {
        return jobBuilderFactory.get(jobName).listener(listener).start(step1()).on("COMPLETED").to(step2(writer()))
                .end().build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                jdbcTemplate.execute(Constant.TRANCATE_TABLE_SQL);
                log.debug("successfully execute sql: " + Constant.TRANCATE_TABLE_SQL);
                return RepeatStatus.FINISHED;
            }
        }).build();

    }

    @Bean
    public Step step2(RepositoryItemWriter<Person> writer) {
        return stepBuilderFactory.get("step2").<Person, Person>chunk(chunkSize).reader(reader()).processor(processor())
                .writer(writer).taskExecutor(taskExecutor()).build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor("spring batch");
    }
}