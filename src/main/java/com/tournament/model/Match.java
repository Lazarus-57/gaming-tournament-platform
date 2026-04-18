package com.tournament.model;

import com.tournament.model.enums.MatchStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer matchId;

    private int roundNumber;

    private LocalDateTime scheduledTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status = MatchStatus.CREATED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bracket_id")
    private Bracket bracket;

    @ManyToMany
    @JoinTable(
        name = "match_teams",
        joinColumns = @JoinColumn(name = "match_id"),
        inverseJoinColumns = @JoinColumn(name = "team_id")
    )
    private List<Team> teams = new ArrayList<>();

    @OneToOne(mappedBy = "match", cascade = CascadeType.ALL)
    private Result result;

    public Match() {}

    public Match(int roundNumber, LocalDateTime scheduledTime, Bracket bracket) {
        this.roundNumber = roundNumber;
        this.scheduledTime = scheduledTime;
        this.bracket = bracket;
    }

    // Getters and Setters
    public Integer getMatchId() { return matchId; }
    public void setMatchId(Integer matchId) { this.matchId = matchId; }

    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }

    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }

    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }

    public Bracket getBracket() { return bracket; }
    public void setBracket(Bracket bracket) { this.bracket = bracket; }

    public List<Team> getTeams() { return teams; }
    public void setTeams(List<Team> teams) { this.teams = teams; }

    public Result getResult() { return result; }
    public void setResult(Result result) { this.result = result; }

    // Information Expert: Match owns its lifecycle transitions.
    public void schedule(LocalDateTime time) {
        if (status != MatchStatus.CREATED) {
            throw new IllegalStateException("Match must be CREATED to schedule");
        }
        this.scheduledTime = time;
        this.status = MatchStatus.SCHEDULED;
    }

    public void start() {
        if (status != MatchStatus.SCHEDULED) {
            throw new IllegalStateException("Match must be SCHEDULED to start");
        }
        this.status = MatchStatus.ONGOING;
    }

    public void complete() {
        if (status != MatchStatus.ONGOING) {
            throw new IllegalStateException("Match must be ONGOING to complete");
        }
        this.status = MatchStatus.COMPLETED;
    }

    public void verify() {
        if (status != MatchStatus.COMPLETED) {
            throw new IllegalStateException("Match must be COMPLETED to verify");
        }
        this.status = MatchStatus.VERIFIED;
    }
}
