package com.tournament.model;

import com.tournament.model.enums.TournamentFormat;
import com.tournament.model.enums.TournamentStatus;

public class TournamentBuilder {

    private final Tournament tournament = new Tournament();

    public TournamentBuilder() {
        tournament.setFormat(TournamentFormat.KNOCKOUT);
    }

    // Builder Pattern: step-by-step tournament construction.
    public TournamentBuilder name(String name) {
        tournament.setName(name);
        return this;
    }

    public TournamentBuilder gameTitle(String gameTitle) {
        tournament.setGameTitle(gameTitle);
        return this;
    }

    public TournamentBuilder format(TournamentFormat format) {
        tournament.setFormat(format);
        return this;
    }

    public TournamentBuilder teamSize(int teamSize) {
        tournament.setTeamSize(teamSize);
        return this;
    }

    public TournamentBuilder registrationStart(java.time.LocalDate start) {
        tournament.setRegistrationStart(start);
        return this;
    }

    public TournamentBuilder registrationEnd(java.time.LocalDate end) {
        tournament.setRegistrationEnd(end);
        return this;
    }

    public TournamentBuilder prizePool(double prizePool) {
        tournament.setPrizePool(prizePool);
        return this;
    }

    public TournamentBuilder rules(String rules) {
        tournament.setRules(rules);
        return this;
    }

    public Tournament build() {
        validate();
        tournament.setStatus(TournamentStatus.CONFIGURED);
        return tournament;
    }

    private void validate() {
        if (tournament.getName() == null || tournament.getName().isBlank()) {
            throw new IllegalArgumentException("Tournament name is required");
        }
        if (tournament.getGameTitle() == null || tournament.getGameTitle().isBlank()) {
            throw new IllegalArgumentException("Game title is required");
        }
        if (tournament.getFormat() != TournamentFormat.KNOCKOUT) {
            throw new IllegalArgumentException("Only KNOCKOUT format is supported");
        }
        if (tournament.getTeamSize() <= 0) {
            throw new IllegalArgumentException("Team size must be positive");
        }
        if (tournament.getRegistrationStart() == null || tournament.getRegistrationEnd() == null) {
            throw new IllegalArgumentException("Registration dates are required");
        }
        if (tournament.getRegistrationEnd().isBefore(tournament.getRegistrationStart())) {
            throw new IllegalArgumentException("Registration end must be after start");
        }
    }
}
