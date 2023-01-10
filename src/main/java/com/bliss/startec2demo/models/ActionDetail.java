package com.bliss.startec2demo.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ActionDetail {
    private String resourceType;
    private String delegatedActionId;
    private String customerId;
    private String organizationId;
    private String userId;
    private List<Violation> violations;
}
