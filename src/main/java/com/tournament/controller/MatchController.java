package com.tournament.controller;

import com.tournament.model.Match;
import com.tournament.model.Result;
import com.tournament.service.TournamentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/matches")
public class MatchController {

    private final TournamentService tournamentService;

    public MatchController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @PostMapping("/{matchId}/start")
    public Match startMatch(@PathVariable Integer matchId) {
        return tournamentService.startMatch(matchId);
    }

    @PostMapping("/{matchId}/result")
    public Result submitResult(@PathVariable Integer matchId,
                               @RequestParam int scoreA,
                               @RequestParam int scoreB) {
        return tournamentService.submitResult(matchId, scoreA, scoreB);
    }

    @PostMapping("/{matchId}/verify")
    public Result verifyResult(@PathVariable Integer matchId) {
        return tournamentService.verifyResult(matchId);
    }
}
