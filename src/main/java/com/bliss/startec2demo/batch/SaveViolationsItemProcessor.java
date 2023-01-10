package com.bliss.startec2demo.batch;

import com.bliss.startec2demo.models.Violation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class SaveViolationsItemProcessor implements ItemProcessor<Violation, Violation> {

    private final long requestId;
    private final long delegatedActionId;

    public SaveViolationsItemProcessor(long requestId, long delegatedActionId) {
        this.requestId = requestId;
        this.delegatedActionId = delegatedActionId;
    }

    @Override
    public Violation process(Violation violation) throws Exception {
        violation.setRequestId(this.requestId);
        violation.setDelegatedActionId(this.delegatedActionId);
        return violation;
    }

}
