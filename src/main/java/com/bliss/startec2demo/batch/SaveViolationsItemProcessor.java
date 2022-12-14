package com.bliss.startec2demo.batch;

import com.bliss.startec2demo.models.Violation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class SaveViolationsItemProcessor implements ItemProcessor<Violation, Violation> {

    @Override
    public Violation process(Violation violation) throws Exception {
        violation.setRequestId(1L);
        violation.setDelegatedActionId(123L);
        return violation;
    }

}
