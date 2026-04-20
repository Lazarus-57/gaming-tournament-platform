package com.tournament.controller;

import com.tournament.model.Match;
import com.tournament.model.Organizer;
import com.tournament.model.Player;
import com.tournament.repository.MatchRepository;
import com.tournament.repository.OrganizerRepository;
import com.tournament.repository.PlayerRepository;
import com.tournament.service.DisputeService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DisputePageController {

    private final DisputeService disputeService;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final OrganizerRepository organizerRepository;

    public DisputePageController(DisputeService disputeService,
                                 MatchRepository matchRepository,
                                 PlayerRepository playerRepository,
                                 OrganizerRepository organizerRepository) {
        this.disputeService = disputeService;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        this.organizerRepository = organizerRepository;
    }

    private Player currentPlayer(Authentication auth) {
        return playerRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new IllegalStateException("Player not found"));
    }

    private Organizer currentOrganizer(Authentication auth) {
        return organizerRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new IllegalStateException("Organizer not found"));
    }

    @GetMapping("/player/disputes")
    public String playerDisputes(Authentication auth, Model model) {
        Player player = currentPlayer(auth);
        model.addAttribute("player", player);
        model.addAttribute("disputes", disputeService.getPlayerDisputes(player.getUserId()));
        return "player/disputes";
    }

    @GetMapping("/player/disputes/new")
    public String raiseDisputeForm(@RequestParam Integer matchId, Authentication auth, Model model) {
        Player player = currentPlayer(auth);
        Match match = matchRepository.findById(matchId)
            .orElseThrow(() -> new IllegalArgumentException("Match not found"));
        model.addAttribute("player", player);
        model.addAttribute("match", match);
        return "player/raise-dispute";
    }

    // GRASP Controller: coordinates UI workflow; business rules remain in DisputeService.
    @PostMapping("/player/disputes")
    public String raiseDispute(@RequestParam Integer matchId,
                               @RequestParam String reason,
                               @RequestParam(required = false, defaultValue = "") String evidenceUrl,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {
        try {
            Player player = currentPlayer(auth);
            disputeService.raiseDispute(matchId, player, reason, evidenceUrl);
            redirectAttributes.addFlashAttribute("success", "Dispute raised successfully.");
            return "redirect:/player/disputes";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/player/disputes/new?matchId=" + matchId;
        }
    }

    @GetMapping("/organizer/disputes")
    public String organizerDisputes(Authentication auth, Model model) {
        Organizer organizer = currentOrganizer(auth);
        model.addAttribute("organizer", organizer);
        model.addAttribute("disputes", disputeService.getOrganizerDisputes(organizer.getUserId()));
        return "organizer/disputes";
    }

    @PostMapping("/organizer/disputes/{id}/review")
    public String markUnderReview(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            disputeService.markUnderReview(id);
            redirectAttributes.addFlashAttribute("success", "Dispute marked under review.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/organizer/disputes";
    }

    @PostMapping("/organizer/disputes/{id}/accept")
    public String accept(@PathVariable Integer id,
                         @RequestParam int scoreA,
                         @RequestParam int scoreB,
                         Authentication auth,
                         RedirectAttributes redirectAttributes) {
        try {
            Organizer organizer = currentOrganizer(auth);
            disputeService.acceptDispute(id, organizer, scoreA, scoreB);
            redirectAttributes.addFlashAttribute("success", "Dispute accepted and result updated.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/organizer/disputes";
    }

    @PostMapping("/organizer/disputes/{id}/reject")
    public String reject(@PathVariable Integer id,
                         Authentication auth,
                         RedirectAttributes redirectAttributes) {
        try {
            Organizer organizer = currentOrganizer(auth);
            disputeService.rejectDispute(id, organizer);
            redirectAttributes.addFlashAttribute("success", "Dispute rejected.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/organizer/disputes";
    }
}
