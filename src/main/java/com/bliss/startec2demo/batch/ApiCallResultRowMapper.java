package com.bliss.startec2demo.batch;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ApiCallResultRowMapper implements org.springframework.jdbc.core.RowMapper {

    @Override
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        return null;
    }

}
