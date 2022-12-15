package com.bliss.startec2demo.models;


import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

@Data
@Entity
@NoArgsConstructor
public class ActionRequest {

    @Id
    @SequenceGenerator(
            name = "action_request_sequence",
            sequenceName = "action_request_sequence",
            allocationSize =  1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "action_request_sequence"
    )
    private Long id;

    private Long delegatedActionId;

    private Long customerId;

    private Long organizationId;

    private Long userId;

    private String cloudProvider;

    private String resourceType;

    private String actionType;

    private Long totalViolatorCount;

    private Long successCount;

    private Long failureCount;

    private Status status;

    private Instant createdAt;
}
