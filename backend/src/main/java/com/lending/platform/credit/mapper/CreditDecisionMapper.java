package com.lending.platform.credit.mapper;

import com.lending.platform.credit.dto.response.CreditDecisionResponse;
import com.lending.platform.credit.entity.CreditDecision;
import com.lending.platform.entity.User;
import org.springframework.stereotype.Component;

@Component
public class CreditDecisionMapper {

    public CreditDecisionResponse toResponse(
            CreditDecision decision
    ) {

        User creditManager =
                decision.getCreditManagerUser();

        Long creditManagerUserId = null;
        String creditManagerUserName = null;

        if (creditManager != null) {
            creditManagerUserId =
                    creditManager.getId();

            creditManagerUserName =
                    creditManager.getFirstName()
                            + " "
                            + creditManager.getLastName();
        }

        return new CreditDecisionResponse(
                decision.getId(),
                decision.getApplication().getId(),
                decision.getApplication().getApplicationNumber(),

                decision.getStatus(),
                decision.getDecisionType(),

                creditManagerUserId,
                creditManagerUserName,

                decision.getRequestedAmount(),
                decision.getApprovedAmount(),

                decision.getRequestedTermMonths(),
                decision.getApprovedTermMonths(),

                decision.getInterestRate(),

                decision.getConditions(),
                decision.getDeclineReason(),
                decision.getDecisionNotes(),

                decision.getStartedAt(),
                decision.getDecidedAt(),
                decision.getCreatedAt(),
                decision.getUpdatedAt()
        );
    }
}
