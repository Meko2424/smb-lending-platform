package com.lending.platform.underwriting.entity;

import com.lending.platform.entity.LoanApplication;
import com.lending.platform.entity.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "underwriting_reviews")
public class UnderwritingReview {

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
    private UnderwritingReviewStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "underwriter_user_id")
    private User underwriterUser;

    @Column(name = "requested_amount", precision = 15, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "annual_revenue", precision = 15, scale = 2)
    private BigDecimal annualRevenue;

    @Column(name = "existing_debt", precision = 15, scale = 2)
    private BigDecimal existingDebt;

    @Column(name = "monthly_debt_service", precision = 15, scale = 2)
    private BigDecimal monthlyDebtService;

    @Column(name = "monthly_cash_flow", precision = 15, scale = 2)
    private BigDecimal monthlyCashFlow;

    @Column(
            name = "debt_service_coverage_ratio",
            precision = 10,
            scale = 4
    )
    private BigDecimal debtServiceCoverageRatio;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_rating", length = 50)
    private RiskRating riskRating;

    @Column(length = 3000)
    private String strengths;

    @Column(length = 3000)
    private String weaknesses;

    @Column(name = "risk_factors", length = 3000)
    private String riskFactors;

    @Column(length = 3000)
    private String mitigants;

    @Column(name = "financial_analysis", length = 4000)
    private String financialAnalysis;

    @Column(name = "recommendation_notes", length = 4000)
    private String recommendationNotes;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (status == null) {
            status = UnderwritingReviewStatus.PENDING;
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

    public UnderwritingReviewStatus getStatus() {
        return status;
    }

    public void setStatus(UnderwritingReviewStatus status) {
        this.status = status;
    }

    public User getUnderwriterUser() {
        return underwriterUser;
    }

    public void setUnderwriterUser(User underwriterUser) {
        this.underwriterUser = underwriterUser;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    public BigDecimal getAnnualRevenue() {
        return annualRevenue;
    }

    public void setAnnualRevenue(BigDecimal annualRevenue) {
        this.annualRevenue = annualRevenue;
    }

    public BigDecimal getExistingDebt() {
        return existingDebt;
    }

    public void setExistingDebt(BigDecimal existingDebt) {
        this.existingDebt = existingDebt;
    }

    public BigDecimal getMonthlyDebtService() {
        return monthlyDebtService;
    }

    public void setMonthlyDebtService(BigDecimal monthlyDebtService) {
        this.monthlyDebtService = monthlyDebtService;
    }

    public BigDecimal getMonthlyCashFlow() {
        return monthlyCashFlow;
    }

    public void setMonthlyCashFlow(BigDecimal monthlyCashFlow) {
        this.monthlyCashFlow = monthlyCashFlow;
    }

    public BigDecimal getDebtServiceCoverageRatio() {
        return debtServiceCoverageRatio;
    }

    public void setDebtServiceCoverageRatio(
            BigDecimal debtServiceCoverageRatio
    ) {
        this.debtServiceCoverageRatio = debtServiceCoverageRatio;
    }

    public RiskRating getRiskRating() {
        return riskRating;
    }

    public void setRiskRating(RiskRating riskRating) {
        this.riskRating = riskRating;
    }

    public String getStrengths() {
        return strengths;
    }

    public void setStrengths(String strengths) {
        this.strengths = strengths;
    }

    public String getWeaknesses() {
        return weaknesses;
    }

    public void setWeaknesses(String weaknesses) {
        this.weaknesses = weaknesses;
    }

    public String getRiskFactors() {
        return riskFactors;
    }

    public void setRiskFactors(String riskFactors) {
        this.riskFactors = riskFactors;
    }

    public String getMitigants() {
        return mitigants;
    }

    public void setMitigants(String mitigants) {
        this.mitigants = mitigants;
    }

    public String getFinancialAnalysis() {
        return financialAnalysis;
    }

    public void setFinancialAnalysis(String financialAnalysis) {
        this.financialAnalysis = financialAnalysis;
    }

    public String getRecommendationNotes() {
        return recommendationNotes;
    }

    public void setRecommendationNotes(String recommendationNotes) {
        this.recommendationNotes = recommendationNotes;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
