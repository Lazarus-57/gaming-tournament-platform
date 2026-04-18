package com.tournament.controller;

import com.tournament.model.Dispute;
import com.tournament.model.Match;
import com.tournament.model.Player;
import com.tournament.model.Result;
import com.tournament.repository.MatchRepository;
import com.tournament.repository.PlayerRepository;
import com.tournament.service.DisputeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/disputes")
public class DisputeController {

    private final DisputeService disputeService;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;

    public DisputeController(DisputeService disputeService,
                             MatchRepository matchRepository,
                             PlayerRepository playerRepository) {
        this.disputeService = disputeService;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
    }

    @PostMapping("/raise")
    public Dispute raiseDispute(@RequestParam Integer matchId,
                                @RequestParam Integer playerId,
                                @RequestParam String reason,
                                @RequestParam(required = false) String evidenceUrl) {
        Match match = matchRepository.findById(matchId)
            .orElseThrow(() -> new IllegalArgumentException("Match not found"));
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        Result result = match.getResult();
        Dispute dispute = new Dispute(reason, evidenceUrl, player, match, result);
        return disputeService.raiseDispute(dispute);
    }

    @PostMapping("/{disputeId}/review")
    public Dispute reviewDispute(@PathVariable Integer disputeId) {
        return disputeService.markUnderReview(disputeId);
    }

    @PostMapping("/{disputeId}/accept")
    public Dispute acceptDispute(@PathVariable Integer disputeId,
                                 @RequestParam int scoreA,
                                 @RequestParam int scoreB) {
        return disputeService.acceptDispute(disputeId, scoreA, scoreB);
    }

    @PostMapping("/{disputeId}/reject")
    public Dispute rejectDispute(@PathVariable Integer disputeId) {
        return disputeService.rejectDispute(disputeId);
    }
}
