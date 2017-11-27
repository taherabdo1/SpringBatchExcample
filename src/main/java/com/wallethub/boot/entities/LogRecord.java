package com.wallethub.boot.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Entity
@Table(name = "log_record",     uniqueConstraints = @UniqueConstraint(name = "uc_date", columnNames = {"date"}))
@Data
public class LogRecord {

	@Id
    @GeneratedValue
    Long id;
	
    @Column(nullable = false)
    @DateTimeFormat(pattern="yyyy-MM-dd hh:mm:ss.sss")
	Date date;
    @Column(nullable = false)
	String ip;
    @Column(nullable = false)
	String request;
    @Column(nullable = false)
	int status;
    @Column(nullable = false)
	String agent;
}
