package com.bliss.startec2demo.batch;

import com.bliss.startec2demo.models.ApiCallResult;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ApiCallResultRowMapper implements RowMapper<ApiCallResult> {

    @Override
    public ApiCallResult mapRow(ResultSet rs, int rowNum) throws SQLException {
        ApiCallResult apiCallResult = new ApiCallResult();
        apiCallResult.setId(rs.getLong("id"));
        apiCallResult.setAccountName(rs.getString("account_name"));
        apiCallResult.setChAccountId(rs.getString("ch_account_id"));
        apiCallResult.setResourceId(rs.getString("resource_id"));
        apiCallResult.setRegion(rs.getString("region"));
        return apiCallResult;
    }

}
