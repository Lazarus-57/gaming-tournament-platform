package com.tournament.repository;

import com.tournament.model.Dispute;
import com.tournament.model.enums.DisputeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisputeRepository extends JpaRepository<Dispute, Integer> {
    List<Dispute> findByMatch_MatchId(Integer matchId);
    List<Dispute> findByStatus(DisputeStatus status);
    List<Dispute> findByRaisedBy_UserIdOrderByRaisedAtDesc(Integer playerId);
    List<Dispute> findByMatch_Bracket_Tournament_Organizer_UserIdOrderByRaisedAtDesc(Integer organizerId);
    long countByStatus(DisputeStatus status);
}
