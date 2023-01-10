package com.bliss.startec2demo.models;


import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ActionRequest that = (ActionRequest) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
