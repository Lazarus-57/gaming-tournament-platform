package com.tournament.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.tournament.model.Bracket;
import com.tournament.model.Match;
import com.tournament.model.Result;
import com.tournament.model.Team;
import com.tournament.model.Tournament;
import com.tournament.model.enums.BracketType;
import com.tournament.service.strategy.KnockoutBracketStrategy;

class MatchResultTest {

    @Test
    void knockoutStrategy_generatesRoundTwoPlaceholderForFourTeams() {
        Tournament tournament = new Tournament();
        Bracket bracket = new Bracket(BracketType.SINGLE_ELIM, tournament);

        Team a = new Team("A");
        Team b = new Team("B");
        Team c = new Team("C");
        Team d = new Team("D");

        List<Match> matches = new KnockoutBracketStrategy().generateMatches(bracket, List.of(a, b, c, d));

        long roundOneCount = matches.stream().filter(m -> m.getRoundNumber() == 1).count();
        long roundTwoCount = matches.stream().filter(m -> m.getRoundNumber() == 2).count();
        assertEquals(2, roundOneCount);
        assertEquals(1, roundTwoCount);
        assertTrue(matches.stream().anyMatch(m -> m.getRoundNumber() == 2 && m.getTeams().isEmpty()));
    }

    @Test
    void resultValidation_allowsTieButRejectsNegativeScore() {
        Tournament tournament = new Tournament();
        Bracket bracket = new Bracket(BracketType.SINGLE_ELIM, tournament);
        Team winner = new Team("Winner");
        Match match = new Match(1, LocalDateTime.now(), bracket);

        Result tiedResult = new Result(2, 2, match, winner);
        tiedResult.validateScores();

        Result invalid = new Result(-1, 2, match, winner);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, invalid::validateScores);
        assertEquals("Scores cannot be negative", ex.getMessage());
    }

    @Test
    void matchLifecycle_requiresValidTransitions() {
        Match match = new Match();
        match.setStatus(com.tournament.model.enums.MatchStatus.CREATED);

        match.schedule(LocalDateTime.now().plusHours(1));
        match.start();
        match.complete();
        assertEquals(com.tournament.model.enums.MatchStatus.COMPLETED, match.getStatus());
    }
}
