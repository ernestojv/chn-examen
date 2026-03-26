package com.ernesto.chn_examen_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplicationResolutionResultDTO {

    private LoanApplicationDTO loanApplication;

    /**
     * Present only when the application was APPROVED and a Loan was created.
     */
    private LoanDTO createdLoan;
}
