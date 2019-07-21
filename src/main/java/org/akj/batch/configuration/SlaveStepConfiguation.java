package org.akj.batch.configuration;

import lombok.extern.slf4j.Slf4j;
import org.akj.batch.entity.Person;
import org.akj.batch.listener.CustomStepExecutionListener;
import org.akj.batch.processor.PersonItemProcessor;
import org.akj.batch.repository.PeopleRepository;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.integration.partition.BeanFactoryStepLocator;
import org.springframework.batch.integration.partition.StepExecutionRequestHandler;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileUrlResource;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.QueueChannel;

import java.beans.PropertyEditor;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SlaveStepConfiguation {

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private PeopleRepository repository;

    @Autowired
    public JobExplorer jobExplorer;

    @Autowired
    private FlatFileItemReader<Person> personItemReader;

    @Autowired
    private PersonItemProcessor personItemProcessor;

    @Value("${batch.job.chunk.size}")
    private int chunkSize;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    @Qualifier("inboundRequests")
    private QueueChannel inboundRequests;

    @Bean
    @StepScope
    @Qualifier("personItemReader")
    public FlatFileItemReader<Person> reader(@Value("#{stepExecutionContext['fileName']}") String fileName)
            throws MalformedURLException {
        BeanWrapperFieldSetMapper<Person> beanMapper = new BeanWrapperFieldSetMapper<Person>();
        beanMapper.setTargetType(Person.class);

        // registry custom date converter for string to date conversion
        Map<String, PropertyEditor> customEditors = new HashMap<String, PropertyEditor>();
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        customEditors.put("java.util.Date", new CustomDateEditor(dateFormatter, false));
        beanMapper.setCustomEditors(customEditors);

        log.info("create step reader - FlatFileItemReader<Person>, file name is: " + fileName);

        return new FlatFileItemReaderBuilder<Person>()
                .name("personItemReader").resource(new FileUrlResource(fileName)).delimited().names(new String[]{
                        "pid", "firstName", "lastName", "age", "gender", "dateOfBirth", "height", "weight"})
                .fieldSetMapper(beanMapper).build();
    }

    @Bean
    @StepScope
    public PersonItemProcessor processor() {
        return new PersonItemProcessor();
    }

    @Bean
    @StepScope
    public RepositoryItemWriter<Person> writer() {
        return new RepositoryItemWriterBuilder<Person>().repository(repository).methodName("save").build();
    }

    @Bean
    public Step slaveStep(CustomStepExecutionListener listener) {
        return stepBuilderFactory.get("slaveStep").listener(listener).<Person, Person>chunk(chunkSize)
                .reader(personItemReader).processor(personItemProcessor).writer(writer()).build();
    }

    @Bean
    @Profile({"slave"})
    @ServiceActivator(inputChannel = "inboundRequests", outputChannel = "outboundStaging", async = "true", poller = {@Poller(fixedRate = "1000")})
    public StepExecutionRequestHandler stepExecutionRequestHandler() {
        StepExecutionRequestHandler stepExecutionRequestHandler = new StepExecutionRequestHandler();
        BeanFactoryStepLocator stepLocator = new BeanFactoryStepLocator();
        stepLocator.setBeanFactory(this.applicationContext);
        stepExecutionRequestHandler.setStepLocator(stepLocator);
        stepExecutionRequestHandler.setJobExplorer(this.jobExplorer);

        return stepExecutionRequestHandler;
    }

    @Bean
    @Profile({"slave"})
    public AmqpInboundChannelAdapter inbound(SimpleMessageListenerContainer listenerContainer) {
        AmqpInboundChannelAdapter adapter = new AmqpInboundChannelAdapter(listenerContainer);
        adapter.setOutputChannel(inboundRequests);
        adapter.afterPropertiesSet();

        return adapter;
    }

    @Bean
    public SimpleMessageListenerContainer container(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames("partition.requests");
        // container.setConcurrency("10");
        // container.setConcurrentConsumers(1);
        container.setErrorHandler(t -> {
            log.error("message listener error," + t.getMessage());
        });
        container.setAutoStartup(false);

        return container;
    }
}
