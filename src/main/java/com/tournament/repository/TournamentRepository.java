package com.tournament.repository;

import com.tournament.model.Tournament;
import com.tournament.model.enums.TournamentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TournamentRepository extends JpaRepository<Tournament, Integer> {
    List<Tournament> findByStatus(TournamentStatus status);
    List<Tournament> findByOrganizer_UserId(Integer organizerId);
    long countByStatus(TournamentStatus status);
}
