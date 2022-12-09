package com.bliss.startec2demo.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
public class ApiCallResult {

    @Id
    @SequenceGenerator(
            name = "api_call_sequence",
            sequenceName = "api_call_sequence",
            allocationSize =  1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "api_call_sequence"
    )
    private String id;

    private String accountName;

    private String chAccountId;

    private String resourceId;

    private String region;

    private Status status;

    private String successMessage;

    private String failureMessage;

    public ApiCallResult(ApiCall apiCall) {
        BeanUtils.copyProperties(apiCall, this);
    }

}
