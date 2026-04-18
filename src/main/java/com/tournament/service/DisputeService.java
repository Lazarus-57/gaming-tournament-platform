package com.tournament.service;

import com.tournament.model.Dispute;
import com.tournament.model.Match;
import com.tournament.model.Notification;
import com.tournament.model.Organizer;
import com.tournament.model.Player;
import com.tournament.model.Result;
import com.tournament.model.Team;
import com.tournament.model.enums.DisputeStatus;
import com.tournament.model.enums.MatchStatus;
import com.tournament.repository.DisputeRepository;
import com.tournament.repository.MatchRepository;
import com.tournament.repository.ResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DisputeService {

    private final DisputeRepository disputeRepository;
    private final MatchRepository matchRepository;
    private final ResultRepository resultRepository;
    private final INotificationService notificationService;

    public DisputeService(DisputeRepository disputeRepository,
                          MatchRepository matchRepository,
                          ResultRepository resultRepository,
                          INotificationService notificationService) {
        this.disputeRepository = disputeRepository;
        this.matchRepository = matchRepository;
        this.resultRepository = resultRepository;
        this.notificationService = notificationService;
    }

    public Dispute raiseDispute(Dispute dispute) {
        Dispute saved = disputeRepository.save(dispute);
        notificationService.sendNotification(new Notification(
            "Dispute raised for match " + dispute.getMatch().getMatchId(),
            dispute.getRaisedBy(),
            dispute.getMatch().getBracket().getTournament()));
        return saved;
    }

    // GRASP Controller + Information Expert: service orchestrates validation and dispute creation.
    public Dispute raiseDispute(Integer matchId, Player player, String reason, String evidenceUrl) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Dispute reason is required");
        }
        Match match = matchRepository.findById(matchId)
            .orElseThrow(() -> new IllegalArgumentException("Match not found"));
        if (match.getResult() == null) {
            throw new IllegalStateException("Cannot dispute a match without a result");
        }
        boolean playerInMatch = match.getTeams().stream()
            .flatMap(team -> team.getMembers().stream())
            .anyMatch(member -> member.getUserId().equals(player.getUserId()));
        if (!playerInMatch) {
            throw new SecurityException("Player is not part of this match");
        }

        Dispute dispute = new Dispute(reason, evidenceUrl, player, match, match.getResult());
        return raiseDispute(dispute);
    }

    public Dispute markUnderReview(Integer disputeId) {
        Dispute dispute = getDispute(disputeId);
        dispute.markUnderReview();
        return disputeRepository.save(dispute);
    }

    public Dispute acceptDispute(Integer disputeId, int scoreA, int scoreB) {
        Dispute dispute = getDispute(disputeId);
        dispute.accept();
        Match match = dispute.getMatch();
        Result result = match.getResult();
        if (result == null) {
            throw new IllegalStateException("Match has no result to update");
        }
        Team winner = (scoreA > scoreB) ? match.getTeams().get(0) : match.getTeams().get(1);
        result.setScoreTeamA(scoreA);
        result.setScoreTeamB(scoreB);
        result.setWinner(winner);
        result.setVerified(false);
        resultRepository.save(result);
        match.setStatus(MatchStatus.COMPLETED);
        matchRepository.save(match);

        notificationService.sendNotification(new Notification(
            "Dispute accepted. Result updated for match " + match.getMatchId(),
            dispute.getRaisedBy(),
            match.getBracket().getTournament()));
        notifyMatchParticipants(match, "Dispute accepted for match " + match.getMatchId() + ". Result has been updated.");
        dispute.close();
        return disputeRepository.save(dispute);
    }

    public Dispute acceptDispute(Integer disputeId, Organizer organizer, int scoreA, int scoreB) {
        Dispute dispute = getDispute(disputeId);
        if (dispute.getStatus() == DisputeStatus.RAISED) {
            dispute.markUnderReview();
        }
        dispute.setReviewedBy(organizer);
        disputeRepository.save(dispute);
        return acceptDispute(disputeId, scoreA, scoreB);
    }

    public Dispute rejectDispute(Integer disputeId) {
        Dispute dispute = getDispute(disputeId);
        dispute.reject();
        notificationService.sendNotification(new Notification(
            "Dispute rejected for match " + dispute.getMatch().getMatchId(),
            dispute.getRaisedBy(),
            dispute.getMatch().getBracket().getTournament()));
        notifyMatchParticipants(dispute.getMatch(),
            "Dispute rejected for match " + dispute.getMatch().getMatchId() + ". Original result stands.");
        dispute.close();
        return disputeRepository.save(dispute);
    }

    public Dispute rejectDispute(Integer disputeId, Organizer organizer) {
        Dispute dispute = getDispute(disputeId);
        if (dispute.getStatus() == DisputeStatus.RAISED) {
            dispute.markUnderReview();
        }
        dispute.setReviewedBy(organizer);
        disputeRepository.save(dispute);
        return rejectDispute(disputeId);
    }

    public Dispute closeDispute(Integer disputeId) {
        Dispute dispute = getDispute(disputeId);
        dispute.close();
        return disputeRepository.save(dispute);
    }

    public Dispute getDispute(Integer disputeId) {
        return disputeRepository.findById(disputeId)
            .orElseThrow(() -> new IllegalArgumentException("Dispute not found"));
    }

    public java.util.List<Dispute> getOpenDisputes() {
        return disputeRepository.findByStatus(DisputeStatus.UNDER_REVIEW);
    }

    public List<Dispute> getPlayerDisputes(Integer playerId) {
        return disputeRepository.findByRaisedBy_UserIdOrderByRaisedAtDesc(playerId);
    }

    public List<Dispute> getOrganizerDisputes(Integer organizerId) {
        return disputeRepository.findByMatch_Bracket_Tournament_Organizer_UserIdOrderByRaisedAtDesc(organizerId);
    }

    public List<Dispute> getAllDisputes() {
        return disputeRepository.findAll();
    }

    public long getRaisedDisputeCount() {
        return disputeRepository.countByStatus(DisputeStatus.RAISED);
    }

    public long getUnderReviewDisputeCount() {
        return disputeRepository.countByStatus(DisputeStatus.UNDER_REVIEW);
    }

    public long getClosedDisputeCount() {
        return disputeRepository.countByStatus(DisputeStatus.CLOSED);
    }

    private void notifyMatchParticipants(Match match, String message) {
        match.getTeams().stream()
            .flatMap(team -> team.getMembers().stream())
            .forEach(player -> notificationService.sendNotification(
                new Notification(message, player, match.getBracket().getTournament())));
    }
}
