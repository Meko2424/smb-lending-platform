package com.lending.platform.credit.entity;


import com.lending.platform.entity.LoanApplication;
import com.lending.platform.entity.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_decisions")
public class CreditDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "application_id",
            nullable = false,
            unique = true
    )
    private LoanApplication application;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CreditDecisionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_type", length = 50)
    private CreditDecisionType decisionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_manager_user_id")
    private User creditManagerUser;

    @Column(
            name = "requested_amount",
            precision = 15,
            scale = 2
    )
    private BigDecimal requestedAmount;

    @Column(
            name = "approved_amount",
            precision = 15,
            scale = 2
    )
    private BigDecimal approvedAmount;

    @Column(name = "requested_term_months")
    private Integer requestedTermMonths;

    @Column(name = "approved_term_months")
    private Integer approvedTermMonths;

    @Column(
            name = "interest_rate",
            precision = 8,
            scale = 4
    )
    private BigDecimal interestRate;

    @Column(length = 4000)
    private String conditions;

    @Column(
            name = "decline_reason",
            length = 2000
    )
    private String declineReason;

    @Column(
            name = "decision_notes",
            length = 4000
    )
    private String decisionNotes;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (status == null) {
            status = CreditDecisionStatus.PENDING;
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void touch() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public LoanApplication getApplication() {
        return application;
    }

    public void setApplication(LoanApplication application) {
        this.application = application;
    }

    public CreditDecisionStatus getStatus() {
        return status;
    }

    public void setStatus(CreditDecisionStatus status) {
        this.status = status;
    }

    public CreditDecisionType getDecisionType() {
        return decisionType;
    }

    public void setDecisionType(CreditDecisionType decisionType) {
        this.decisionType = decisionType;
    }

    public User getCreditManagerUser() {
        return creditManagerUser;
    }

    public void setCreditManagerUser(User creditManagerUser) {
        this.creditManagerUser = creditManagerUser;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    public BigDecimal getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(BigDecimal approvedAmount) {
        this.approvedAmount = approvedAmount;
    }

    public Integer getRequestedTermMonths() {
        return requestedTermMonths;
    }

    public void setRequestedTermMonths(Integer requestedTermMonths) {
        this.requestedTermMonths = requestedTermMonths;
    }

    public Integer getApprovedTermMonths() {
        return approvedTermMonths;
    }

    public void setApprovedTermMonths(Integer approvedTermMonths) {
        this.approvedTermMonths = approvedTermMonths;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public String getDeclineReason() {
        return declineReason;
    }

    public void setDeclineReason(String declineReason) {
        this.declineReason = declineReason;
    }

    public String getDecisionNotes() {
        return decisionNotes;
    }

    public void setDecisionNotes(String decisionNotes) {
        this.decisionNotes = decisionNotes;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(LocalDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
