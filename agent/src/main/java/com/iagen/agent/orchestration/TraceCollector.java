package com.iagen.agent.orchestration;

import lombok.extern.slf4j.Slf4j;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TraceCollector {

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

}
