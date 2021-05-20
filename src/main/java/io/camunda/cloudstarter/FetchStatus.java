package io.camunda.cloudstarter;

import lombok.Data;

import java.util.UUID;

@Data
public class FetchStatus {
    private UUID fetchStatusID;
    private String customerName;
    private String businessLabel;
    private String catalogFileName;
    private Boolean fetchComplete;

    // No need for instance methods. Lombok @Data annotation automatically handles all getters/setters/constructors.
    //    i.e., getFetchStatusID(), setFetchStatusID(UUID fetchStatusID)
}