package com.iagen.agent.orchestration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TraceCollector {

    private static final Logger log = LoggerFactory.getLogger(TraceCollector.class);
    private final List<TraceEntry> entries = new ArrayList<>();

    public void add(String prefix, String message) {
        String entry = "[" + prefix + "] " + message;
        entries.add(new TraceEntry(Instant.now().toString(), entry));
        log.debug("{}", entry);
    }

    public List<String> getEntries() {
        return entries.stream()
                .map(e -> e.timestamp() + " | " + e.message())
                .toList();
    }

    private record TraceEntry(String timestamp, String message) {
    }
}
