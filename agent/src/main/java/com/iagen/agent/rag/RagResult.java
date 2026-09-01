package com.iagen.agent.rag;

import java.util.List;

public record RagResult(
        String contextBlock,
        List<String> sources,
        boolean notInCorpus) {

}
