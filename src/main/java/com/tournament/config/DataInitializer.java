package com.tournament.config;

import com.tournament.model.*;
import com.tournament.model.enums.*;
import com.tournament.repository.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

/**
 * Seeds the database with sample data on application startup.
 * Creates default admin, sample organizer, and sample players for demo purposes.
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UserRepository userRepository,
                               PlayerRepository playerRepository,
                               OrganizerRepository organizerRepository,
                               TournamentRepository tournamentRepository,
                               JdbcTemplate jdbcTemplate,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            // Migration: enforce knockout-only format in persistent databases.
            jdbcTemplate.execute("UPDATE tournaments SET format = 'KNOCKOUT' WHERE format = 'ROUND_ROBIN'");

            // Only seed if database is empty
            if (userRepository.count() > 0) return;

            // Create Admin
            Admin admin = new Admin("System Admin", "admin@tournament.com",
                passwordEncoder.encode("admin123"), 1);
            admin.setAccountState(AccountState.ACTIVE);
            userRepository.save(admin);

            // Create Organizer
            Organizer organizer = new Organizer("John Organizer", "organizer@tournament.com",
                passwordEncoder.encode("org123"), "ESports League");
            organizer.setVerified(true);
            organizer.setAccountState(AccountState.ACTIVE);
            organizerRepository.save(organizer);

            // Create Players
            Player player1 = new Player("Alice Gamer", "alice@tournament.com",
                passwordEncoder.encode("player123"), "AliceX");
            player1.setRankingPoints(1200);
            player1.setAccountState(AccountState.ACTIVE);
            playerRepository.save(player1);

            Player player2 = new Player("Bob Warrior", "bob@tournament.com",
                passwordEncoder.encode("player123"), "BobTheDestroyer");
            player2.setRankingPoints(980);
            player2.setAccountState(AccountState.ACTIVE);
            playerRepository.save(player2);

            Player player3 = new Player("Charlie Pro", "charlie@tournament.com",
                passwordEncoder.encode("player123"), "CharlieSnipes");
            player3.setRankingPoints(1500);
            player3.setAccountState(AccountState.ACTIVE);
            playerRepository.save(player3);

            Player player4 = new Player("Diana Swift", "diana@tournament.com",
                passwordEncoder.encode("player123"), "DianaRush");
            player4.setRankingPoints(1100);
            player4.setAccountState(AccountState.ACTIVE);
            playerRepository.save(player4);

            // Create Sample Tournament
            Tournament t1 = new Tournament();
            t1.setName("Valorant Spring Championship 2026");
            t1.setGameTitle("Valorant");
            t1.setFormat(TournamentFormat.KNOCKOUT);
            t1.setTeamSize(1);
            t1.setRegistrationStart(LocalDate.now());
            t1.setRegistrationEnd(LocalDate.now().plusDays(14));
            t1.setPrizePool(5000.00);
            t1.setRules("Standard competitive rules. Best of 3. No cheating tolerated.");
            t1.setOrganizer(organizer);
            t1.setStatus(TournamentStatus.REGISTRATION_OPEN);
            tournamentRepository.save(t1);

            Tournament t2 = new Tournament();
            t2.setName("CS2 Knockout League");
            t2.setGameTitle("Counter-Strike 2");
            t2.setFormat(TournamentFormat.KNOCKOUT);
            t2.setTeamSize(1);
            t2.setRegistrationStart(LocalDate.now().minusDays(7));
            t2.setRegistrationEnd(LocalDate.now().plusDays(7));
            t2.setPrizePool(3000.00);
            t2.setRules("Knockout format. Single elimination rounds.");
            t2.setOrganizer(organizer);
            t2.setStatus(TournamentStatus.REGISTRATION_OPEN);
            tournamentRepository.save(t2);

            System.out.println("===========================================");
            System.out.println("  Sample data initialized successfully!");
            System.out.println("  Login credentials:");
            System.out.println("  Admin:     admin@tournament.com / admin123");
            System.out.println("  Organizer: organizer@tournament.com / org123");
            System.out.println("  Player:    alice@tournament.com / player123");
            System.out.println("  Player:    bob@tournament.com / player123");
            System.out.println("  Player:    charlie@tournament.com / player123");
            System.out.println("  Player:    diana@tournament.com / player123");
            System.out.println("===========================================");
        };
    }
}
