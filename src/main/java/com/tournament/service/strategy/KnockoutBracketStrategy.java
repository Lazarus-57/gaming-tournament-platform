package com.tournament.service.strategy;

import com.tournament.model.Bracket;
import com.tournament.model.Match;
import com.tournament.model.Team;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Strategy Pattern: Knockout (Single Elimination) bracket generation.
 * Teams are paired up; losers are eliminated each round.
 */
@Component
public class KnockoutBracketStrategy implements BracketGenerationStrategy {

    @Override
    public List<Match> generateMatches(Bracket bracket, List<Team> teams) {
        List<Match> matches = new ArrayList<>();
        List<Team> shuffled = new ArrayList<>(teams);
        Collections.shuffle(shuffled);

        int round = 1;
        LocalDateTime matchTime = LocalDateTime.now().plusDays(1);

        // Generate first round matches by pairing teams
        for (int i = 0; i + 1 < shuffled.size(); i += 2) {
            Match match = new Match(round, matchTime, bracket);
            match.schedule(matchTime);
            match.getTeams().add(shuffled.get(i));
            match.getTeams().add(shuffled.get(i + 1));
            matches.add(match);
            matchTime = matchTime.plusHours(1);
        }

        // If odd number of teams, last team gets a bye (auto-advance)
        // Generate placeholder matches for subsequent rounds
        int matchesInRound = matches.size();
        while (matchesInRound > 1) {
            round++;
            matchTime = matchTime.plusDays(1);
            int nextRoundMatches = matchesInRound / 2;
            for (int i = 0; i < nextRoundMatches; i++) {
                Match match = new Match(round, matchTime, bracket);
                match.schedule(matchTime);
                // Teams TBD - filled in as results come in
                matches.add(match);
                matchTime = matchTime.plusHours(1);
            }
            matchesInRound = nextRoundMatches;
        }

        return matches;
    }
}
