package com.bliss.startec2demo.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
public class Violation {

    @Id
    @SequenceGenerator(
            name = "violation_sequence",
            sequenceName = "violation_sequence",
            allocationSize =  1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "violation_sequence"
    )
    private Long id;

    private Long requestId;

    private Long delegatedActionId;

    private String resourceId;

    private String accountName;

    private Long chAccountId;

    private String region;

    private Status status;

    private String statusMessage;

}
