package com.tournament.repository;

import com.tournament.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Integer> {
    Optional<Player> findByGamerTag(String gamerTag);
    Optional<Player> findByEmail(String email);
    boolean existsByGamerTag(String gamerTag);
}
