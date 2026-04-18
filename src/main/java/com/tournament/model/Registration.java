package com.tournament.model;

import com.tournament.model.enums.RegistrationStatus;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "registrations")
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer registrationId;

    private LocalDate registrationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status = RegistrationStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    public Registration() {}

    public Registration(Tournament tournament, Team team, Player player) {
        this.tournament = tournament;
        this.team = team;
        this.player = player;
        this.registrationDate = LocalDate.now();
    }

    // Getters and Setters
    public Integer getRegistrationId() { return registrationId; }
    public void setRegistrationId(Integer registrationId) { this.registrationId = registrationId; }

    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }

    public RegistrationStatus getStatus() { return status; }
    public void setStatus(RegistrationStatus status) { this.status = status; }

    // GRASP Information Expert: Registration controls its own workflow.
    public void submitForReview() {
        if (status != RegistrationStatus.PENDING) {
            throw new IllegalStateException("Registration must be PENDING to submit for review");
        }
        status = RegistrationStatus.UNDER_REVIEW;
    }

    public void approve() {
        if (status != RegistrationStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Registration must be UNDER_REVIEW to approve");
        }
        status = RegistrationStatus.APPROVED;
    }

    public void reject() {
        if (status != RegistrationStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Registration must be UNDER_REVIEW to reject");
        }
        status = RegistrationStatus.REJECTED;
    }

    public Tournament getTournament() { return tournament; }
    public void setTournament(Tournament tournament) { this.tournament = tournament; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
}
