package com.tournament.repository;

import com.tournament.model.Bracket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BracketRepository extends JpaRepository<Bracket, Integer> {
    Optional<Bracket> findByTournament_TournamentId(Integer tournamentId);
}
