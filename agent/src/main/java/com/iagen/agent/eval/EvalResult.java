package com.iagen.agent.eval;

public record EvalResult(
        int id,
        String type,
        String question,
        String expectedRoute,
        String actualRoute,
        boolean routeOk,
        boolean contentOk,
        boolean pass,
        String answer)
{

}
