package com.tournament.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class Leaderboard {

    private final Map<Integer, LeaderboardEntry> entries = new LinkedHashMap<>();

    // Information Expert: leaderboard updates itself from results.
    public void update(Result result) {
        Team winner = result.getWinner();
        if (winner == null) {
            return;
        }
        Team loser = result.getMatch().getTeams().stream()
            .filter(team -> !team.getTeamId().equals(winner.getTeamId()))
            .findFirst()
            .orElse(null);

        LeaderboardEntry winnerEntry = entries.computeIfAbsent(
            winner.getTeamId(), id -> new LeaderboardEntry(winner));
        winnerEntry.recordWin();

        if (loser != null) {
            LeaderboardEntry loserEntry = entries.computeIfAbsent(
                loser.getTeamId(), id -> new LeaderboardEntry(loser));
            loserEntry.recordLoss();
        }
    }

    public Collection<LeaderboardEntry> getEntries() {
        return entries.values();
    }
}
