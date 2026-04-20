package com.tournament.controller;

import com.tournament.model.*;
import com.tournament.model.enums.TournamentStatus;
import com.tournament.repository.PlayerRepository;
import com.tournament.service.INotificationService;
import com.tournament.service.TournamentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/player")
public class PlayerController {

    private final TournamentService tournamentService;
    private final INotificationService notificationService;
    private final PlayerRepository playerRepository;

    public PlayerController(TournamentService tournamentService,
                            INotificationService notificationService,
                            PlayerRepository playerRepository) {
        this.tournamentService = tournamentService;
        this.notificationService = notificationService;
        this.playerRepository = playerRepository;
    }

    private Player getCurrentPlayer(Authentication auth) {
        return playerRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new IllegalStateException("Player not found"));
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        Player player = getCurrentPlayer(auth);
        List<Registration> registrations = tournamentService.getPlayerRegistrations(player.getUserId());
        List<Tournament> upcomingTournaments = tournamentService.findByStatus(TournamentStatus.REGISTRATION_OPEN);
        long unreadCount = notificationService.getUnreadCount(player.getUserId());

        model.addAttribute("player", player);
        model.addAttribute("registrations", registrations);
        model.addAttribute("upcomingTournaments", upcomingTournaments);
        model.addAttribute("unreadCount", unreadCount);
        return "player/dashboard";
    }

    @GetMapping("/tournaments")
    public String browseTournaments(Authentication auth, Model model) {
        Player player = getCurrentPlayer(auth);
        List<Tournament> tournaments = tournamentService.findAll();
        model.addAttribute("tournaments", tournaments);
        model.addAttribute("player", player);
        return "player/tournaments";
    }

    @GetMapping("/tournaments/{id}")
    public String viewTournament(@PathVariable Integer id, Authentication auth, Model model) {
        Player player = getCurrentPlayer(auth);
        Tournament tournament = tournamentService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));
        List<Match> matches = tournamentService.getMatches(id);
        List<Registration> registrations = tournamentService.getApprovedRegistrations(id);

        model.addAttribute("tournament", tournament);
        model.addAttribute("matches", matches);
        model.addAttribute("registrations", registrations);
        model.addAttribute("player", player);
        return "player/tournament-detail";
    }

    @PostMapping("/tournaments/{id}/join")
    public String joinTournament(@PathVariable Integer id, Authentication auth,
                                 RedirectAttributes redirectAttributes) {
        try {
            Player player = getCurrentPlayer(auth);
            tournamentService.registerSoloPlayer(id, player);
            redirectAttributes.addFlashAttribute("success", "Successfully joined the tournament!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/player/tournaments/" + id;
    }

    @GetMapping("/notifications")
    public String notifications(Authentication auth, Model model) {
        Player player = getCurrentPlayer(auth);
        List<Notification> notifications = notificationService.getUserNotifications(player.getUserId());
        model.addAttribute("notifications", notifications);
        model.addAttribute("player", player);
        return "player/notifications";
    }

    @PostMapping("/notifications/read-all")
    public String markAllRead(Authentication auth) {
        Player player = getCurrentPlayer(auth);
        notificationService.markAllAsRead(player.getUserId());
        return "redirect:/player/notifications";
    }

    @GetMapping("/standings/{tournamentId}")
    public String viewStandings(@PathVariable Integer tournamentId, Authentication auth, Model model) {
        Player player = getCurrentPlayer(auth);
        Tournament tournament = tournamentService.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));
        List<Match> matches = tournamentService.getMatches(tournamentId);
        List<Registration> registrations = tournamentService.getApprovedRegistrations(tournamentId);

        model.addAttribute("tournament", tournament);
        model.addAttribute("matches", matches);
        model.addAttribute("registrations", registrations);
        model.addAttribute("player", player);
        return "player/standings";
    }
}
