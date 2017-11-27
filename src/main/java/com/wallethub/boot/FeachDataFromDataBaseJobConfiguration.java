package com.wallethub.boot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.session.SessionProperties.Jdbc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import com.wallethub.boot.entities.BlockedIp;
import com.wallethub.boot.entities.LogRecord;

@Configuration
public class FeachDataFromDataBaseJobConfiguration {

    @Autowired
    private DataSource datasource;
    
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    JdbcCursorItemReader<BlockedIp> blockedIpItemReader;
    
    @Autowired
    JdbcBatchItemWriter<BlockedIp> blockedIpItemWriter;

    @Autowired
    Step processBlockedIpStep1;
    
    @Bean
    public Job processBlockedIpsJob() throws Exception {
        return jobBuilderFactory.get("processBlockedIpsJob").start(processBlockedIpStep1).build();
    }

    //read from database
    @Bean
    @StepScope
    public JdbcCursorItemReader<BlockedIp> blockedIpItemReader(@Value("#{jobParameters['startDate']}")final String startDate, @Value("#{jobParameters['duration']}")final String duration, @Value("#{jobParameters['threshold']}")final String threshold) throws Exception{
    	JdbcCursorItemReader<BlockedIp> itemReader = new JdbcCursorItemReader<>();
    
    	itemReader.setDataSource(this.datasource);
    	itemReader.setRowMapper(new BlockedIpMapper());
    	
    	String endDate = null;
    	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss");
    	Date startDateFormatted = simpleDateFormat.parse(startDate);
    	final Calendar c = Calendar.getInstance();
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT record1.ip , count(*) FROM wallethub.log_record record1 where date >= '");
    	sql.append(startDate);
    	sql.append("' and date <= '");
    	if(duration.equalsIgnoreCase("Hourly")){
    		c.setTime(startDateFormatted);
    		c.add(Calendar.HOUR, 1);
    		endDate = simpleDateFormat.format(c.getTime());
        	sql.append(endDate);    		
    	}else if(duration.equalsIgnoreCase("daily")){
    		c.setTime(startDateFormatted);
    		c.add(Calendar.DATE, 1);
    		endDate = simpleDateFormat.format(c.getTime());
        	sql.append(endDate);    		
    	}
    	System.out.println("insede item reader in fetch from database job");
    	sql.append("' and count(*) >= ");
    	sql.append(threshold);
    	sql.append(" group by ip");
    	itemReader.setSql(sql.toString());
    	itemReader.afterPropertiesSet();
    	return itemReader;
    }
    

    @Bean
    public JdbcBatchItemWriter<BlockedIp> blockedIpItemWriter(){
    	JdbcBatchItemWriter<BlockedIp> itemWriter = new JdbcBatchItemWriter<>();
    	itemWriter.setDataSource(this.datasource);
    	itemWriter.setSql("INSERT INTO bloack_ips(ip , blockType, count) VALUES (:ip, :blockType, :count)");
    	itemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
    	itemWriter.afterPropertiesSet();
    	return itemWriter;
    }
    
    
    @Bean
    @JobScope
    public Step processBlockedIpStep1() throws Exception {
        final SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(50);

        return stepBuilderFactory//
                .get("processBlockedIpStep1").<BlockedIp, BlockedIp> chunk(20)//
                .reader(blockedIpItemReader)//
                .writer(blockedIpItemWriter)//
                .taskExecutor(taskExecutor)//
                .build();
    }
}
