package com.lending.platform.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "eligibility_criteria",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_eligibility_criteria_review_type",
                columnNames = {
                        "eligibility_review_id",
                        "criterion_type"
                }
        )
)
public class EligibilityCriterion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "eligibility_review_id",
            nullable = false
    )
    private EligibilityReview eligibilityReview;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "criterion_type",
            nullable = false,
            length = 100
    )
    private EligibilityCriterionType criterionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EligibilityCriterionStatus status;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (status == null) {
            status = EligibilityCriterionStatus.PENDING;
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void touch() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public EligibilityReview getEligibilityReview() {
        return eligibilityReview;
    }

    public void setEligibilityReview(
            EligibilityReview eligibilityReview
    ) {
        this.eligibilityReview = eligibilityReview;
    }

    public EligibilityCriterionType getCriterionType() {
        return criterionType;
    }

    public void setCriterionType(
            EligibilityCriterionType criterionType
    ) {
        this.criterionType = criterionType;
    }

    public EligibilityCriterionStatus getStatus() {
        return status;
    }

    public void setStatus(
            EligibilityCriterionStatus status
    ) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
