package com.wallethub.boot;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.validation.BindException;

import com.wallethub.boot.entities.BlockedIp;

public class BlockedIpMapper implements RowMapper<BlockedIp>{

	@Override
	public BlockedIp mapRow(ResultSet resultSet, int rowNum) throws SQLException {
		BlockedIp blockedIp = new BlockedIp();
		blockedIp.setIp(resultSet.getString(0));
		blockedIp.setCount(resultSet.getInt(1));
		return blockedIp;
	}

}
