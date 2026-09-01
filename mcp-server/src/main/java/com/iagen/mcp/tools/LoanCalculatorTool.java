package com.iagen.mcp.tools;

import com.iagen.mcp.security.OutputSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoanCalculatorTool {

    private final OutputSanitizer sanitizer;

    @Tool(description = "Calcule la mensualité, le coût total et le coût des intérêts d'un prêt immobilier à taux fixe. Paramètres : montant emprunté (€), taux annuel (%), durée (années).")
    public String calculateLoan(double principal, double annualRatePercent, int years) {
        log.info("[MCP][LoanCalculatorTool] Calcul prêt : {}€ à {}% sur {} ans", principal, annualRatePercent, years);

        if (principal <= 0 || annualRatePercent <= 0 || years <= 0) {
            return "Erreur : tous les paramètres doivent être strictement positifs.";
        }

        double monthlyRate = annualRatePercent / 100.0 / 12.0;
        int months = years * 12;

        double monthlyPayment;
        double totalCost;
        double totalInterest;

        if (monthlyRate == 0) {
            monthlyPayment = principal / months;
            totalCost = principal;
            totalInterest = 0;
        } else {
            double factor = Math.pow(1 + monthlyRate, months);
            monthlyPayment = principal * (monthlyRate * factor) / (factor - 1);
            totalCost = monthlyPayment * months;
            totalInterest = totalCost - principal;
        }

        String result = String.format(
                "Simulation prêt immobilier :\n" +
                        "  Capital emprunté  : %.2f €\n" +
                        "  Taux annuel       : %.2f %%\n" +
                        "  Durée             : %d ans (%d mensualités)\n" +
                        "  Mensualité        : %.2f €\n" +
                        "  Coût total        : %.2f €\n" +
                        "  Coût des intérêts : %.2f €",
                principal, annualRatePercent, years, months,
                monthlyPayment, totalCost, totalInterest
        );

        return sanitizer.sanitize(result, "LoanCalculatorTool");
    }
}
