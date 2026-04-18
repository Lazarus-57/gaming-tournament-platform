package com.tournament.model;

import com.tournament.model.enums.DisputeStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "disputes")
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer disputeId;

    @Column(nullable = false)
    private String reason;

    private String evidenceUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisputeStatus status = DisputeStatus.RAISED;

    private LocalDateTime raisedAt = LocalDateTime.now();

    private LocalDateTime resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raised_by_id")
    private Player raisedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private Organizer reviewedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id")
    private Result result;

    public Dispute() {}

    public Dispute(String reason, String evidenceUrl, Player raisedBy, Match match, Result result) {
        this.reason = reason;
        this.evidenceUrl = evidenceUrl;
        this.raisedBy = raisedBy;
        this.match = match;
        this.result = result;
    }

    public Integer getDisputeId() { return disputeId; }
    public String getReason() { return reason; }
    public String getEvidenceUrl() { return evidenceUrl; }
    public DisputeStatus getStatus() { return status; }
    public LocalDateTime getRaisedAt() { return raisedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public Player getRaisedBy() { return raisedBy; }
    public Organizer getReviewedBy() { return reviewedBy; }
    public Match getMatch() { return match; }
    public Result getResult() { return result; }

    public void setReviewedBy(Organizer reviewedBy) { this.reviewedBy = reviewedBy; }

    // Information Expert: Dispute controls its workflow.
    public void markUnderReview() {
        if (status != DisputeStatus.RAISED) {
            throw new IllegalStateException("Dispute must be RAISED to review");
        }
        status = DisputeStatus.UNDER_REVIEW;
    }

    public void accept() {
        if (status != DisputeStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Dispute must be UNDER_REVIEW to accept");
        }
        status = DisputeStatus.ACCEPTED;
        resolvedAt = LocalDateTime.now();
    }

    public void reject() {
        if (status != DisputeStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Dispute must be UNDER_REVIEW to reject");
        }
        status = DisputeStatus.REJECTED;
        resolvedAt = LocalDateTime.now();
    }

    public void close() {
        if (status != DisputeStatus.ACCEPTED && status != DisputeStatus.REJECTED) {
            throw new IllegalStateException("Dispute must be resolved to close");
        }
        status = DisputeStatus.CLOSED;
        resolvedAt = LocalDateTime.now();
    }
}
