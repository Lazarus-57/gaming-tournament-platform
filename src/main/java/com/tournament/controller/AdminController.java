package com.tournament.controller;

import com.tournament.model.User;
import com.tournament.service.DisputeService;
import com.tournament.service.analytics.AnalyticsService;
import com.tournament.model.enums.TournamentStatus;
import com.tournament.model.enums.AccountState;
import com.tournament.service.TournamentService;
import com.tournament.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final TournamentService tournamentService;
    private final DisputeService disputeService;
    private final AnalyticsService analyticsService;

    public AdminController(UserService userService,
                           TournamentService tournamentService,
                           DisputeService disputeService,
                           AnalyticsService analyticsService) {
        this.userService = userService;
        this.tournamentService = tournamentService;
        this.disputeService = disputeService;
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long userCount = userService.getUserCount();
        long tournamentCount = tournamentService.getTournamentCount();
        long ongoingCount = tournamentService.getOngoingTournamentCount();
        long completedMatches = tournamentService.getCompletedMatchCount();
        long openDisputes = analyticsService.getRaisedDisputes() + analyticsService.getUnderReviewDisputes();

        model.addAttribute("userCount", userCount);
        model.addAttribute("tournamentCount", tournamentCount);
        model.addAttribute("ongoingCount", ongoingCount);
        model.addAttribute("completedMatches", completedMatches);
        model.addAttribute("openDisputes", openDisputes);
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            AccountState newStatus = (user.getAccountState() == AccountState.SUSPENDED)
                ? AccountState.ACTIVE
                : AccountState.SUSPENDED;
            userService.updateUserStatus(id, newStatus);
            redirectAttributes.addFlashAttribute("success", "User status updated to " + newStatus.name());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/tournaments")
    public String viewTournaments(Model model) {
        model.addAttribute("tournaments", tournamentService.findAll());
        return "admin/tournaments";
    }

    @GetMapping("/reports")
    public String viewReports(Model model) {
        long userCount = userService.getUserCount();
        long tournamentCount = tournamentService.getTournamentCount();
        long ongoingCount = tournamentService.getOngoingTournamentCount();
        long completedMatches = tournamentService.getCompletedMatchCount();

        long upcomingCount = tournamentService.findByStatus(TournamentStatus.REGISTRATION_OPEN).size();
        long completedCount = tournamentService.findByStatus(TournamentStatus.COMPLETED).size();

        model.addAttribute("userCount", userCount);
        model.addAttribute("tournamentCount", tournamentCount);
        model.addAttribute("ongoingCount", ongoingCount);
        model.addAttribute("completedMatches", completedMatches);
        model.addAttribute("upcomingCount", upcomingCount);
        model.addAttribute("completedTournaments", completedCount);
        model.addAttribute("raisedDisputes", analyticsService.getRaisedDisputes());
        model.addAttribute("underReviewDisputes", analyticsService.getUnderReviewDisputes());
        model.addAttribute("closedDisputes", analyticsService.getClosedDisputes());
        return "admin/reports";
    }

    @GetMapping("/disputes")
    public String monitorDisputes(Model model) {
        model.addAttribute("disputes", disputeService.getAllDisputes());
        model.addAttribute("raisedDisputes", analyticsService.getRaisedDisputes());
        model.addAttribute("underReviewDisputes", analyticsService.getUnderReviewDisputes());
        model.addAttribute("closedDisputes", analyticsService.getClosedDisputes());
        return "admin/disputes";
    }
}
