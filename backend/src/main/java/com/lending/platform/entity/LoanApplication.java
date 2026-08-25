package com.lending.platform.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_applications")
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(
            name = "application_number",
            nullable = false,
            unique = true,
            length = 50
    )
    private String applicationNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_product", nullable = false, length = 50)
    private LoanProduct loanProduct;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_purpose", nullable = false, length = 100)
    private LoanPurpose loanPurpose;

    @Column(
            name = "requested_amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal requestedAmount;

    @Column(name = "requested_term_months")
    private Integer requestedTermMonths;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ApplicationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_loan_officer_id")
    private User assignedLoanOfficer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_underwriter_id")
    private User assignedUnderwriter;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "decision_at")
    private LocalDateTime decisionAt;

    @Column(name = "funded_at")
    private LocalDateTime fundedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (status == null) {
            status = ApplicationStatus.DRAFT;
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public String getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public LoanProduct getLoanProduct() {
        return loanProduct;
    }

    public void setLoanProduct(LoanProduct loanProduct) {
        this.loanProduct = loanProduct;
    }

    public LoanPurpose getLoanPurpose() {
        return loanPurpose;
    }

    public void setLoanPurpose(LoanPurpose loanPurpose) {
        this.loanPurpose = loanPurpose;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    public Integer getRequestedTermMonths() {
        return requestedTermMonths;
    }

    public void setRequestedTermMonths(Integer requestedTermMonths) {
        this.requestedTermMonths = requestedTermMonths;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public User getAssignedLoanOfficer() {
        return assignedLoanOfficer;
    }

    public void setAssignedLoanOfficer(User assignedLoanOfficer) {
        this.assignedLoanOfficer = assignedLoanOfficer;
    }

    public User getAssignedUnderwriter() {
        return assignedUnderwriter;
    }

    public void setAssignedUnderwriter(User assignedUnderwriter) {
        this.assignedUnderwriter = assignedUnderwriter;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getDecisionAt() {
        return decisionAt;
    }

    public void setDecisionAt(LocalDateTime decisionAt) {
        this.decisionAt = decisionAt;
    }

    public LocalDateTime getFundedAt() {
        return fundedAt;
    }

    public void setFundedAt(LocalDateTime fundedAt) {
        this.fundedAt = fundedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}