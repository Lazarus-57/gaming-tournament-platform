package com.tournament.model;

import jakarta.persistence.*;

@Entity
@Table(name = "results")
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer resultId;

    private int scoreTeamA;

    private int scoreTeamB;

    private boolean verified;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_team_id")
    private Team winner;

    public Result() {}

    public Result(int scoreTeamA, int scoreTeamB, Match match, Team winner) {
        this.scoreTeamA = scoreTeamA;
        this.scoreTeamB = scoreTeamB;
        this.match = match;
        this.winner = winner;
        this.verified = false;
    }

    // Getters and Setters
    public Integer getResultId() { return resultId; }
    public void setResultId(Integer resultId) { this.resultId = resultId; }

    public int getScoreTeamA() { return scoreTeamA; }
    public void setScoreTeamA(int scoreTeamA) { this.scoreTeamA = scoreTeamA; }

    public int getScoreTeamB() { return scoreTeamB; }
    public void setScoreTeamB(int scoreTeamB) { this.scoreTeamB = scoreTeamB; }

    public Match getMatch() { return match; }
    public void setMatch(Match match) { this.match = match; }

    public Team getWinner() { return winner; }
    public void setWinner(Team winner) { this.winner = winner; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    // Information Expert: Result validates its own scores.
    public void validateScores() {
        if (scoreTeamA < 0 || scoreTeamB < 0) {
            throw new IllegalArgumentException("Scores cannot be negative");
        }
    }
}
