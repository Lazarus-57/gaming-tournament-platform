package com.tournament.service.strategy;

import com.tournament.model.Bracket;
import com.tournament.model.Match;
import com.tournament.model.Team;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Strategy Pattern: Round Robin bracket generation.
 * Every team plays against every other team exactly once.
 */
@Component
public class RoundRobinBracketStrategy implements BracketGenerationStrategy {

    @Override
    public List<Match> generateMatches(Bracket bracket, List<Team> teams) {
        List<Match> matches = new ArrayList<>();
        int round = 1;
        LocalDateTime matchTime = LocalDateTime.now().plusDays(1);

        // Generate all pairwise matches
        for (int i = 0; i < teams.size(); i++) {
            for (int j = i + 1; j < teams.size(); j++) {
                Match match = new Match(round, matchTime, bracket);
                match.schedule(matchTime);
                match.getTeams().add(teams.get(i));
                match.getTeams().add(teams.get(j));
                matches.add(match);
                matchTime = matchTime.plusHours(1);

                // Advance round after every N/2 matches
                if (matches.size() % Math.max(1, teams.size() / 2) == 0) {
                    round++;
                    matchTime = matchTime.plusDays(1);
                }
            }
        }

        return matches;
    }
}
