package com.iagen.mcp.tools;

import com.iagen.mcp.security.OutputSanitizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class LoanCalculatorToolTest {

    @Mock
    private OutputSanitizer sanitizer;

    private LoanCalculatorTool tool;

    @BeforeEach
    void setUp() {
        tool = new LoanCalculatorTool(sanitizer);
        org.mockito.Mockito.lenient().when(sanitizer.sanitize(anyString(), eq("LoanCalculatorTool")))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void calculateLoan_standardLoan_correctValues() {
        String result = tool.calculateLoan(200000, 3.5, 20);

        assertThat(result).contains("Capital emprunté  : 200000.00");
        assertThat(result).contains("Taux annuel       : 3.50");
        assertThat(result).contains("Durée             : 20 ans (240 mensualités)");
        assertThat(result).contains("Mensualité        :");
        assertThat(result).contains("Coût total        :");
        assertThat(result).contains("Coût des intérêts :");
    }

    @Test
    void calculateLoan_monthlyPayment_mathematicallyCorrect() {
        String result = tool.calculateLoan(100000, 12.0, 1);

        assertThat(result).contains("Mensualité");
    }

    @Test
    void calculateLoan_zeroRate_simpleDivision() {
        String result = tool.calculateLoan(120000, 0.0, 10);

        assertThat(result).contains("Mensualité");
        assertThat(result).contains("Coût des intérêts");
    }

    @Test
    void calculateLoan_negativeParam_error() {
        String result = tool.calculateLoan(-100000, 3.5, 20);

        assertThat(result).contains("Erreur");
        assertThat(result).contains("strictement positifs");
    }

    @Test
    void calculateLoan_negativeRate_error() {
        String result = tool.calculateLoan(100000, -3.5, 20);

        assertThat(result).contains("Erreur");
    }

    @Test
    void calculateLoan_negativeYears_error() {
        String result = tool.calculateLoan(100000, 3.5, -20);

        assertThat(result).contains("Erreur");
    }

    @Test
    void calculateLoan_zeroParam_error() {
        String result = tool.calculateLoan(0, 3.5, 20);

        assertThat(result).contains("Erreur");
    }

    @Test
    void calculateLoan_realisticScenario() {
        String result = tool.calculateLoan(150000, 4.0, 15);

        assertThat(result).contains("Capital emprunté  : 150000.00");
        assertThat(result).contains("Durée             : 15 ans (180 mensualités)");
        assertThat(result).contains("Mensualité");
        assertThat(result).contains("Coût total");
        assertThat(result).contains("Coût des intérêts");
    }

    @Test
    void calculateLoan_callsSanitizer() {
        tool.calculateLoan(100000, 3.5, 20);

        org.mockito.Mockito.verify(sanitizer).sanitize(anyString(), eq("LoanCalculatorTool"));
    }
}
