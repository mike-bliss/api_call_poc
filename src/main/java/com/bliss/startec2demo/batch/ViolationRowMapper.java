package com.bliss.startec2demo.batch;

import com.bliss.startec2demo.models.Violation;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ViolationRowMapper implements RowMapper<Violation> {

    @Override
    public Violation mapRow(ResultSet rs, int rowNum) throws SQLException {
        Violation violation = new Violation();
        violation.setId(rs.getLong("id"));
        violation.setRequestId(rs.getLong("request_id"));
        violation.setDelegatedActionId(rs.getLong("delegated_action_id"));
        violation.setResourceId(rs.getString("resource_id"));
        violation.setAccountName(rs.getString("account_name"));
        violation.setChAccountId(rs.getLong("ch_account_id"));
        violation.setRegion(rs.getString("region"));
//        violation.setStatus(rs.getObject("status", Status.class));
        violation.setStatusMessage(rs.getString("status_message"));
        return violation;
    }

}
