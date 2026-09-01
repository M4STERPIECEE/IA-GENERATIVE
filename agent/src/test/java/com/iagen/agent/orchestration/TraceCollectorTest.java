package com.iagen.agent.orchestration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class TraceCollectorTest {

    private TraceCollector collector;

    @BeforeEach
    void setUp() {
        collector = new TraceCollector();
    }

    @Test
    void addTrace_valid() {
        collector.add("RAG", "Message");
        List<String> traces = collector.getEntries();
        assertThat(traces).hasSize(1);
        assertThat(traces.get(0)).contains("[RAG] Message");
    }
}
