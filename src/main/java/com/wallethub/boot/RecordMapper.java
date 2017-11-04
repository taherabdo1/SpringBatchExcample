package com.wallethub.boot;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.wallethub.boot.entities.LogRecord;

public class RecordMapper implements FieldSetMapper<LogRecord>{

	@Override
	public LogRecord mapFieldSet(FieldSet fieldSet) throws BindException {
		LogRecord logRecord = new LogRecord();
		logRecord.setDate(fieldSet.readDate(0));
		logRecord.setIp(fieldSet.readString(1));
		logRecord.setRequest(fieldSet.readString(2));
		logRecord.setStatus(fieldSet.readInt(3));
		logRecord.setAgent(fieldSet.readString(4));
		return logRecord;
	}

}
