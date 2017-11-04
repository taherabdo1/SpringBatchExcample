package com.wallethub.boot;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import com.wallethub.boot.entities.LogRecord;

@Configuration
public class JobConfiguration {

    @Autowired
    private DataSource datasource;
    
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    
    @Autowired
    private FlatFileItemReader<LogRecord> logReader;
    
    @Autowired
    private JdbcBatchItemWriter<LogRecord> LogRecordItemWriter;


    @Bean
    public Job migrationJob() throws Exception {
        return jobBuilderFactory.get("migrationJob").start(step1()).build();
    }

    @Bean
    public FlatFileItemReader<LogRecord> logReader() {
        final DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
        delimitedLineTokenizer.setNames(new String[] { "DATE", "IP","REQUEST", "STATUS", "AGENT"});
        delimitedLineTokenizer.setDelimiter("|");

        final DefaultLineMapper<LogRecord> defaultLineMapper = new DefaultLineMapper<>();
        defaultLineMapper.setLineTokenizer(delimitedLineTokenizer);
        defaultLineMapper.setFieldSetMapper(new RecordMapper());
        defaultLineMapper.afterPropertiesSet();

        final FlatFileItemReader<LogRecord> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("/access.log"));
        reader.setLineMapper(defaultLineMapper);
        return reader;
    }

    @Bean
    public JdbcBatchItemWriter<LogRecord> LogRecordItemWriter(){
    	JdbcBatchItemWriter<LogRecord> itemWriter = new JdbcBatchItemWriter<>();
    	itemWriter.setDataSource(this.datasource);
    	itemWriter.setSql("INSERT INTO Log_record(date, ip , request, status, agent) VALUES (:date, :ip, :request , :status , :agent)");
    	itemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
    	itemWriter.afterPropertiesSet();
    	return itemWriter;
    }
    
    @Bean
    public Step step1() throws Exception {
        final SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(50);

        return stepBuilderFactory//
                .get("loadData").<LogRecord, LogRecord> chunk(20)//
                .reader(logReader)//
                .writer(LogRecordItemWriter)//
                .taskExecutor(taskExecutor)//
                .build();
    }
}
