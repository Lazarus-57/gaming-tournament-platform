package com.tournament.repository;

import com.tournament.model.Match;
import com.tournament.model.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Integer> {
    List<Match> findByBracket_Tournament_TournamentId(Integer tournamentId);
    List<Match> findByBracket_Tournament_TournamentIdAndStatus(Integer tournamentId, MatchStatus status);
    long countByStatus(MatchStatus status);
}
