package com.tournament.service.strategy;

import com.tournament.model.Bracket;
import com.tournament.model.Match;
import com.tournament.model.Team;
import java.util.List;

/**
 * Strategy Pattern: Interface for different bracket generation algorithms.
 * Concrete strategies implement this for Knockout and Round Robin formats.
 */
public interface BracketGenerationStrategy {
    List<Match> generateMatches(Bracket bracket, List<Team> teams);
}
