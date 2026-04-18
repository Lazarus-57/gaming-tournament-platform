package com.tournament.model;

public class LeaderboardEntry {
    private final Team team;
    private int wins;
    private int losses;
    private int points;

    public LeaderboardEntry(Team team) {
        this.team = team;
    }

    public Team getTeam() { return team; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public int getPoints() { return points; }

    public void recordWin() {
        wins++;
        points += 3;
    }

    public void recordLoss() {
        losses++;
    }
}
