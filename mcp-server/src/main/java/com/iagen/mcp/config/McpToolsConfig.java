package com.iagen.mcp.config;

import com.iagen.mcp.tools.EmployeeDirectoryTool;
import com.iagen.mcp.tools.LoanCalculatorTool;
import com.iagen.mcp.tools.WeatherTool;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolsConfig {

    @Bean
    public MethodToolCallbackProvider mcpTools(
            WeatherTool weatherTool,
            LoanCalculatorTool loanCalculatorTool,
            EmployeeDirectoryTool employeeDirectoryTool) {

        return MethodToolCallbackProvider.builder()
                .toolObjects(weatherTool, loanCalculatorTool, employeeDirectoryTool)
                .build();
    }
}
