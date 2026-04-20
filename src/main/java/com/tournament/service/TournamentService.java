package com.tournament.service;

import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tournament.model.Bracket;
import com.tournament.model.Leaderboard;
import com.tournament.model.Match;
import com.tournament.model.Organizer;
import com.tournament.model.Player;
import com.tournament.model.Registration;
import com.tournament.model.Result;
import com.tournament.model.Team;
import com.tournament.model.Tournament;
import com.tournament.model.enums.BracketType;
import com.tournament.model.enums.MatchStatus;
import com.tournament.model.enums.RegistrationStatus;
import com.tournament.model.enums.TournamentFormat;
import com.tournament.model.enums.TournamentStatus;
import com.tournament.repository.BracketRepository;
import com.tournament.repository.MatchRepository;
import com.tournament.repository.RegistrationRepository;
import com.tournament.repository.ResultRepository;
import com.tournament.repository.TeamRepository;
import com.tournament.repository.TournamentRepository;
import com.tournament.service.factory.BracketFactory;
import com.tournament.service.observer.TournamentObserver;
import com.tournament.service.strategy.BracketGenerationStrategy;

@Service
@Transactional
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final RegistrationRepository registrationRepository;
    private final BracketRepository bracketRepository;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final ResultRepository resultRepository;

    private final BracketFactory bracketFactory;
    private final List<TournamentObserver> observers;
    private final Map<Integer, Leaderboard> leaderboards = new ConcurrentHashMap<>();

    public TournamentService(TournamentRepository tournamentRepository,
            RegistrationRepository registrationRepository,
            BracketRepository bracketRepository,
            MatchRepository matchRepository,
            TeamRepository teamRepository,
            ResultRepository resultRepository,
            BracketFactory bracketFactory,
            List<TournamentObserver> observers) {
        this.tournamentRepository = tournamentRepository;
        this.registrationRepository = registrationRepository;
        this.bracketRepository = bracketRepository;
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.resultRepository = resultRepository;
        this.bracketFactory = bracketFactory;
        this.observers = observers;
    }

    // ---- Tournament CRUD ----
    public Tournament createTournament(Tournament tournament, Organizer organizer) {
        tournament.setOrganizer(organizer);
        if (tournament.getStatus() == TournamentStatus.DRAFT) {
            tournament.markConfigured(); // Builder/Creator flow
        }
        if (tournament.getStatus() == TournamentStatus.CONFIGURED) {
            tournament.openRegistration();
        }
        return tournamentRepository.save(tournament);
    }

    public Tournament openRegistration(Integer tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));
        tournament.openRegistration();
        return tournamentRepository.save(tournament);
    }

    public Tournament closeRegistration(Integer tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));
        tournament.closeRegistration();
        return tournamentRepository.save(tournament);
    }

    public Optional<Tournament> findById(Integer id) {
        return tournamentRepository.findById(id);
    }

    public List<Tournament> findAll() {
        return tournamentRepository.findAll();
    }

    public List<Tournament> findByStatus(TournamentStatus status) {
        return tournamentRepository.findByStatus(status);
    }

    public List<Tournament> findByOrganizer(Integer organizerId) {
        return tournamentRepository.findByOrganizer_UserId(organizerId);
    }

    public Tournament updateTournament(Tournament tournament) {
        return tournamentRepository.save(tournament);
    }

    // ---- Registration ----
    public Registration registerPlayer(Integer tournamentId, Player player, Team team) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));

        if (tournament.getStatus() != TournamentStatus.REGISTRATION_OPEN) {
            throw new IllegalStateException("Registration is not open for this tournament");
        }

        if (registrationRepository.existsByTournament_TournamentIdAndPlayer_UserId(
                tournamentId, player.getUserId())) {
            throw new IllegalArgumentException("Player already registered for this tournament");
        }

        if (team != null && !team.isSizeValid(tournament.getTeamSize())) {
            throw new IllegalArgumentException("Team size does not match tournament rules");
        }

        // Save team if new
        if (team != null && team.getTeamId() == null) {
            team = teamRepository.save(team);
        }

        if (team != null && registrationRepository
                .existsByTournament_TournamentIdAndTeam_TeamId(tournamentId, team.getTeamId())) {
            throw new IllegalArgumentException("Team already registered for this tournament");
        }

        Registration registration = new Registration(tournament, team, player);
        registration.setStatus(RegistrationStatus.PENDING);
        registration.submitForReview();
        Registration saved = registrationRepository.save(registration);

        // Notify observers
        observers.forEach(o -> o.onRegistration(player, tournament));
        return saved;
    }

    public Registration registerSoloPlayer(Integer tournamentId, Player player) {
        // Create a solo team with the player's gamer tag
        Team soloTeam = new Team(player.getGamerTag());
        soloTeam.getMembers().add(player);
        soloTeam = teamRepository.save(soloTeam);
        return registerPlayer(tournamentId, player, soloTeam);
    }

    public Registration approveRegistration(Integer registrationId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));
        registration.approve();
        return registrationRepository.save(registration);
    }

    public Registration rejectRegistration(Integer registrationId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));
        registration.reject();
        return registrationRepository.save(registration);
    }

    public List<Registration> getRegistrations(Integer tournamentId) {
        return registrationRepository.findByTournament_TournamentId(tournamentId);
    }

    public List<Registration> getApprovedRegistrations(Integer tournamentId) {
        return registrationRepository.findByTournament_TournamentIdAndStatus(
                tournamentId, RegistrationStatus.APPROVED);
    }

    public List<Registration> getPlayerRegistrations(Integer playerId) {
        return registrationRepository.findByPlayer_UserId(playerId);
    }

    // ---- Bracket Generation (Strategy Pattern) ----
    public Bracket generateBracket(Integer tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));

        if (tournament.getStatus() != TournamentStatus.REGISTRATION_CLOSED) {
            throw new IllegalStateException("Registration must be closed before generating brackets");
        }

        // Remove existing bracket if any
        if (tournament.getBracket() != null) {
            bracketRepository.delete(tournament.getBracket());
            tournament.setBracket(null);
            tournamentRepository.save(tournament);
        }

        // Get approved teams
        List<Registration> approved = registrationRepository
                .findByTournament_TournamentIdAndStatus(tournamentId, RegistrationStatus.APPROVED);
        List<Team> teams = approved.stream()
                .map(Registration::getTeam)
                .filter(t -> t != null)
                .distinct()
                .toList();

        if (teams.size() < 2) {
            throw new IllegalArgumentException("Need at least 2 teams to generate brackets");
        }

        // System rule: only knockout format is supported.
        BracketType bracketType = BracketType.SINGLE_ELIM;

        Bracket bracket = new Bracket(bracketType, tournament);
        bracket = bracketRepository.save(bracket);

        BracketGenerationStrategy strategy = bracketFactory.getStrategy(TournamentFormat.KNOCKOUT);

        List<Match> matches = strategy.generateMatches(bracket, teams);
        for (Match match : matches) {
            matchRepository.save(match);
        }
        bracket.setMatches(matches);

        tournament.setBracket(bracket);
        tournament.setStatus(TournamentStatus.ONGOING);
        tournamentRepository.save(tournament);

        // Notify observers
        observers.forEach(o -> o.onMatchScheduled(tournament));
        return bracket;
    }

    // ---- Match Results ----
    public List<Match> getMatches(Integer tournamentId) {
        return matchRepository.findByBracket_Tournament_TournamentId(tournamentId);
    }

    public Match getMatch(Integer matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));
    }

    public Match startMatch(Integer matchId) {
        Match match = getMatch(matchId);
        match.start();
        return matchRepository.save(match);
    }

    public Result submitResult(Integer matchId, int scoreA, int scoreB) {
        return submitResult(matchId, scoreA, scoreB, null);
    }

    public Result submitResult(Integer matchId, int scoreA, int scoreB, Integer tieBreakerWinnerTeamId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));

        if (match.getResult() != null || match.getStatus() == MatchStatus.COMPLETED
                || match.getStatus() == MatchStatus.VERIFIED) {
            throw new IllegalStateException("Result already submitted for this match");
        }

        if (match.getTeams().size() < 2) {
            throw new IllegalArgumentException("Match does not have two teams assigned");
        }

        Team teamA = match.getTeams().get(0);
        Team teamB = match.getTeams().get(1);
        Team winner;

        if (scoreA == scoreB) {
            if (tieBreakerWinnerTeamId == null) {
                throw new IllegalArgumentException("Scores are tied. Select a winner for tie-break.");
            }

            if (Objects.equals(teamA.getTeamId(), tieBreakerWinnerTeamId)) {
                winner = teamA;
            } else if (Objects.equals(teamB.getTeamId(), tieBreakerWinnerTeamId)) {
                winner = teamB;
            } else {
                throw new IllegalArgumentException("Invalid tie-break winner selected");
            }
        } else {
            winner = (scoreA > scoreB) ? teamA : teamB;
        }

        Result result = new Result(scoreA, scoreB, match, winner);
        result.validateScores();
        result = resultRepository.save(result);

        match.setResult(result);
        if (match.getStatus() == MatchStatus.SCHEDULED) {
            match.start();
        }
        match.complete();
        matchRepository.save(match);
        advanceWinnerToNextRound(match, winner);

        // Notify observers
        Tournament tournament = match.getBracket().getTournament();
        observers.forEach(o -> o.onResultUpdated(tournament));

        return result;
    }

    public Result verifyResult(Integer matchId) {
        Match match = getMatch(matchId);
        Result result = match.getResult();
        if (result == null) {
            throw new IllegalArgumentException("Match result not found");
        }
        result.setVerified(true);
        resultRepository.save(result);
        match.verify();
        matchRepository.save(match);

        Leaderboard leaderboard = leaderboards.computeIfAbsent(
                match.getBracket().getTournament().getTournamentId(), id -> new Leaderboard());
        leaderboard.update(result);

        Tournament tournament = match.getBracket().getTournament();
        observers.forEach(o -> o.onResultUpdated(tournament));
        return result;
    }

    public void completeTournament(Integer tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));
        tournament.setStatus(TournamentStatus.COMPLETED);
        tournamentRepository.save(tournament);
        observers.forEach(o -> o.onTournamentCompleted(tournament));
    }

    public Leaderboard getLeaderboard(Integer tournamentId) {
        return leaderboards.getOrDefault(tournamentId, new Leaderboard());
    }

    public void rebuildLeaderboard(Integer tournamentId) {
        Leaderboard rebuilt = new Leaderboard();
        List<Match> matches = getMatches(tournamentId);
        matches.stream()
                .map(Match::getResult)
                .filter(result -> result != null)
                .forEach(rebuilt::update);
        leaderboards.put(tournamentId, rebuilt);
    }

    // ---- Statistics ----
    public long getTournamentCount() {
        return tournamentRepository.count();
    }

    public long getCompletedMatchCount() {
        return matchRepository.countByStatus(MatchStatus.COMPLETED);
    }

    public long getOngoingTournamentCount() {
        return tournamentRepository.countByStatus(TournamentStatus.ONGOING);
    }

    private void advanceWinnerToNextRound(Match completedMatch, Team winner) {
        if (completedMatch.getBracket() == null || completedMatch.getBracket().getTournament() == null) {
            return;
        }

        Integer tournamentId = completedMatch.getBracket().getTournament().getTournamentId();
        Integer bracketId = completedMatch.getBracket().getBracketId();
        if (tournamentId == null || bracketId == null) {
            return;
        }

        List<Match> bracketMatches = matchRepository.findByBracket_Tournament_TournamentId(tournamentId).stream()
                .filter(m -> m.getBracket() != null && Objects.equals(m.getBracket().getBracketId(), bracketId))
                .toList();

        List<Match> currentRoundMatches = bracketMatches.stream()
                .filter(m -> m.getRoundNumber() == completedMatch.getRoundNumber())
                .sorted(Comparator.comparing(Match::getScheduledTime, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Match::getMatchId, Comparator.nullsLast(Integer::compareTo)))
                .toList();

        int currentMatchIndex = -1;
        for (int i = 0; i < currentRoundMatches.size(); i++) {
            if (Objects.equals(currentRoundMatches.get(i).getMatchId(), completedMatch.getMatchId())) {
                currentMatchIndex = i;
                break;
            }
        }
        if (currentMatchIndex < 0) {
            return;
        }

        List<Match> nextRoundMatches = bracketMatches.stream()
                .filter(m -> m.getRoundNumber() == completedMatch.getRoundNumber() + 1)
                .sorted(Comparator.comparing(Match::getScheduledTime, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Match::getMatchId, Comparator.nullsLast(Integer::compareTo)))
                .toList();

        if (nextRoundMatches.isEmpty()) {
            return;
        }

        Match targetNextRoundMatch = nextRoundMatches.get(currentMatchIndex / 2);
        boolean alreadyAdded = targetNextRoundMatch.getTeams().stream()
                .anyMatch(t -> Objects.equals(t.getTeamId(), winner.getTeamId()));
        if (alreadyAdded) {
            return;
        }
        if (targetNextRoundMatch.getTeams().size() >= 2) {
            throw new IllegalStateException("Next-round match already has two teams assigned");
        }

        targetNextRoundMatch.getTeams().add(winner);
        matchRepository.save(targetNextRoundMatch);
    }
}
