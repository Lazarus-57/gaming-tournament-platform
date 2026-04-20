package com.tournament.controller;

import com.tournament.model.*;
import com.tournament.model.enums.*;
import com.tournament.repository.OrganizerRepository;
import com.tournament.service.TournamentService;
import com.tournament.service.analytics.AnalyticsService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/organizer")
public class OrganizerController {

    private final TournamentService tournamentService;
    private final OrganizerRepository organizerRepository;
    private final AnalyticsService analyticsService;

    public OrganizerController(TournamentService tournamentService,
                               OrganizerRepository organizerRepository,
                               AnalyticsService analyticsService) {
        this.tournamentService = tournamentService;
        this.organizerRepository = organizerRepository;
        this.analyticsService = analyticsService;
    }

    private Organizer getCurrentOrganizer(Authentication auth) {
        return organizerRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new IllegalStateException("Organizer not found"));
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        Organizer organizer = getCurrentOrganizer(auth);
        List<Tournament> tournaments = tournamentService.findByOrganizer(organizer.getUserId());
        
        // Calculate statistics in controller instead of template
        long ongoingCount = tournaments.stream()
            .filter(t -> t.getStatus() == TournamentStatus.ONGOING).count();
        long completedCount = tournaments.stream()
            .filter(t -> t.getStatus() == TournamentStatus.COMPLETED).count();
        long upcomingCount = tournaments.stream()
            .filter(t -> t.getStatus() == TournamentStatus.REGISTRATION_OPEN
                || t.getStatus() == TournamentStatus.CONFIGURED).count();
        
        model.addAttribute("organizer", organizer);
        model.addAttribute("tournaments", tournaments);
        model.addAttribute("ongoingCount", ongoingCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("upcomingCount", upcomingCount);
        return "organizer/dashboard";
    }

    @GetMapping("/tournaments/create")
    public String createForm(Model model) {
        return "organizer/create-tournament";
    }

    @PostMapping("/tournaments/create")
    public String createTournament(@RequestParam String name,
                                   @RequestParam String gameTitle,
                                   @RequestParam int teamSize,
                                   @RequestParam String registrationStart,
                                   @RequestParam String registrationEnd,
                                   @RequestParam double prizePool,
                                   @RequestParam(required = false, defaultValue = "") String rules,
                                   Authentication auth,
                                   RedirectAttributes redirectAttributes) {
        try {
            Organizer organizer = getCurrentOrganizer(auth);
            Tournament tournament = new TournamentBuilder()
                .name(name)
                .gameTitle(gameTitle)
                .format(TournamentFormat.KNOCKOUT)
                .teamSize(teamSize)
                .registrationStart(LocalDate.parse(registrationStart))
                .registrationEnd(LocalDate.parse(registrationEnd))
                .prizePool(prizePool)
                .rules(rules)
                .build();

            tournamentService.createTournament(tournament, organizer);
            redirectAttributes.addFlashAttribute("success", "Tournament created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/organizer/tournaments/create";
        }
        return "redirect:/organizer/dashboard";
    }

    @GetMapping("/tournaments/{id}")
    public String viewTournament(@PathVariable Integer id, Authentication auth, Model model) {
        Organizer organizer = getCurrentOrganizer(auth);
        Tournament tournament = tournamentService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));
        List<Registration> registrations = tournamentService.getRegistrations(id);
        List<Match> matches = tournamentService.getMatches(id);

        model.addAttribute("organizer", organizer);
        model.addAttribute("tournament", tournament);
        model.addAttribute("registrations", registrations);
        model.addAttribute("matches", matches);
        return "organizer/tournament-detail";
    }

    @PostMapping("/tournaments/{tournamentId}/registrations/{registrationId}/approve")
    public String approveRegistration(@PathVariable Integer tournamentId,
                                      @PathVariable Integer registrationId,
                                      RedirectAttributes redirectAttributes) {
        try {
            tournamentService.approveRegistration(registrationId);
            redirectAttributes.addFlashAttribute("success", "Registration approved.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/organizer/tournaments/" + tournamentId;
    }

    @PostMapping("/tournaments/{tournamentId}/registrations/{registrationId}/reject")
    public String rejectRegistration(@PathVariable Integer tournamentId,
                                     @PathVariable Integer registrationId,
                                     RedirectAttributes redirectAttributes) {
        try {
            tournamentService.rejectRegistration(registrationId);
            redirectAttributes.addFlashAttribute("success", "Registration rejected.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/organizer/tournaments/" + tournamentId;
    }

    @PostMapping("/tournaments/{id}/generate-bracket")
    public String generateBracket(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            tournamentService.generateBracket(id);
            redirectAttributes.addFlashAttribute("success", "Brackets generated successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/organizer/tournaments/" + id;
    }

    @PostMapping("/tournaments/{id}/open-registration")
    public String openRegistration(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            tournamentService.openRegistration(id);
            redirectAttributes.addFlashAttribute("success", "Registration opened.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/organizer/tournaments/" + id;
    }

    @PostMapping("/tournaments/{id}/close-registration")
    public String closeRegistration(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            tournamentService.closeRegistration(id);
            redirectAttributes.addFlashAttribute("success", "Registration closed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/organizer/tournaments/" + id;
    }

    @GetMapping("/tournaments/{tournamentId}/matches/{matchId}/result")
    public String resultForm(@PathVariable Integer tournamentId, @PathVariable Integer matchId, Model model) {
        Match match = tournamentService.getMatch(matchId);
        model.addAttribute("match", match);
        model.addAttribute("tournamentId", tournamentId);
        return "organizer/submit-result";
    }

    @PostMapping("/tournaments/{tournamentId}/matches/{matchId}/result")
    public String submitResult(@PathVariable Integer tournamentId,
                               @PathVariable Integer matchId,
                               @RequestParam int scoreA,
                               @RequestParam int scoreB,
                               @RequestParam(required = false) Integer tieBreakerWinnerTeamId,
                               RedirectAttributes redirectAttributes) {
        try {
            tournamentService.submitResult(matchId, scoreA, scoreB, tieBreakerWinnerTeamId);
            redirectAttributes.addFlashAttribute("success", "Result submitted successfully!");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/organizer/tournaments/" + tournamentId;
    }

    @PostMapping("/tournaments/{id}/complete")
    public String completeTournament(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            tournamentService.completeTournament(id);
            redirectAttributes.addFlashAttribute("success", "Tournament completed!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/organizer/tournaments/" + id;
    }

    @GetMapping("/tournaments/{id}/report")
    public String tournamentReport(@PathVariable Integer id, Authentication auth, Model model) {
        Organizer organizer = getCurrentOrganizer(auth);
        Tournament tournament = tournamentService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));
        List<Match> matches = tournamentService.getMatches(id);
        List<Registration> registrations = tournamentService.getRegistrations(id);

        long completedMatches = matches.stream()
            .filter(m -> m.getStatus() == MatchStatus.COMPLETED).count();

        model.addAttribute("organizer", organizer);
        model.addAttribute("tournament", tournament);
        model.addAttribute("matches", matches);
        model.addAttribute("registrations", registrations);
        model.addAttribute("completedMatches", completedMatches);
        model.addAttribute("totalMatches", matches.size());
        model.addAttribute("report", analyticsService.generateTournamentReport(tournament));
        return "organizer/report";
    }
}
