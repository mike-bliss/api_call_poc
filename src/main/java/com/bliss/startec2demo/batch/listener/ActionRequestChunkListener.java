package com.bliss.startec2demo.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;

@Slf4j
public class ActionRequestChunkListener implements ChunkListener {

    @Override
    public void beforeChunk(ChunkContext context) {
        log.info("For action request job started chunk processing. Chunk context: {}", context.toString());
    }

    @Override
    public void afterChunk(ChunkContext context) {
        log.info("For action request job successfully committed the chunk. Chunk context: {}", context.toString());
    }

    @Override
    public void afterChunkError(ChunkContext context) {
        log.error("--srb For action request job failed to process the chunk. Chunk context: {}", context.toString());
    }
}