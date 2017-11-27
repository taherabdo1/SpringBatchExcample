package com.wallethub.boot;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import com.wallethub.boot.entities.LogRecord;

@Configuration
public class ReadFileJobConfiguration {

    @Autowired
    private DataSource datasource;
    
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    
    @Autowired
    private JdbcBatchItemWriter<LogRecord> LogRecordItemWriter;

    @Autowired
    Step readFileStep1;
    
    @Autowired
    FlatFileItemReader<LogRecord> logReader;

    @Bean
    public Job readFileJob() throws Exception {
        return jobBuilderFactory.get("migrationJob").start(readFileStep1).build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<LogRecord> logReader(@Value("#{jobParameters['fileName']}")final String fileName) {
        final DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
        delimitedLineTokenizer.setNames(new String[] { "DATE", "IP","REQUEST", "STATUS", "AGENT"});
        delimitedLineTokenizer.setDelimiter("|");

        final DefaultLineMapper<LogRecord> defaultLineMapper = new DefaultLineMapper<>();
        defaultLineMapper.setLineTokenizer(delimitedLineTokenizer);
        defaultLineMapper.setFieldSetMapper(new RecordMapper());
        defaultLineMapper.afterPropertiesSet();

        final FlatFileItemReader<LogRecord> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(fileName));
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
    @JobScope
    public Step readFileStep1(@Value("#{jobParameters['fileName']}")final String fileName) throws Exception {
        final SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(50);

        return stepBuilderFactory//
                .get("readFileStep1").<LogRecord, LogRecord> chunk(20)//
                .reader(logReader)//
                .writer(LogRecordItemWriter)//
                .taskExecutor(taskExecutor)//
                .build();
    }
}
