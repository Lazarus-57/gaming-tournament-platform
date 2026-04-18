package com.tournament.repository;

import com.tournament.model.Registration;
import com.tournament.model.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Integer> {
    List<Registration> findByTournament_TournamentId(Integer tournamentId);
    List<Registration> findByTournament_TournamentIdAndStatus(Integer tournamentId, RegistrationStatus status);
    List<Registration> findByPlayer_UserId(Integer playerId);
    Optional<Registration> findByTournament_TournamentIdAndPlayer_UserId(Integer tournamentId, Integer playerId);
    boolean existsByTournament_TournamentIdAndPlayer_UserId(Integer tournamentId, Integer playerId);
    boolean existsByTournament_TournamentIdAndTeam_TeamId(Integer tournamentId, Integer teamId);
}
