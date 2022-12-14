package com.bliss.startec2demo.batch;

import com.bliss.startec2demo.models.Violation;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class NoOpItemWriter implements ItemWriter<Violation> {

    @Override
    public void write(List<? extends Violation> items) throws Exception {
        // no-op
    }
}
