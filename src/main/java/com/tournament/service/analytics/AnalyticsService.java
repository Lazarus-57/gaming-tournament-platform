package com.tournament.service.analytics;

import com.tournament.model.Match;
import com.tournament.model.Registration;
import com.tournament.model.Tournament;
import com.tournament.model.enums.DisputeStatus;
import com.tournament.model.enums.MatchStatus;
import com.tournament.repository.DisputeRepository;
import com.tournament.repository.MatchRepository;
import com.tournament.repository.RegistrationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalyticsService {

    private final RegistrationRepository registrationRepository;
    private final MatchRepository matchRepository;
    private final DisputeRepository disputeRepository;
    private final TournamentReport template = new TournamentReport();

    public AnalyticsService(RegistrationRepository registrationRepository,
                            MatchRepository matchRepository,
                            DisputeRepository disputeRepository) {
        this.registrationRepository = registrationRepository;
        this.matchRepository = matchRepository;
        this.disputeRepository = disputeRepository;
    }

    public TournamentReport generateTournamentReport(Tournament tournament) {
        List<Registration> registrations = registrationRepository
            .findByTournament_TournamentId(tournament.getTournamentId());
        List<Match> completedMatches = matchRepository
            .findByBracket_Tournament_TournamentIdAndStatus(tournament.getTournamentId(), MatchStatus.COMPLETED);

        TournamentReport report = (TournamentReport) template.copy();
        report.setTournamentName(tournament.getName());
        report.setTotalRegistrations(registrations.size());
        report.setCompletedMatches(completedMatches.size());
        return report;
    }

    public long getRaisedDisputes() {
        return disputeRepository.countByStatus(DisputeStatus.RAISED);
    }

    public long getUnderReviewDisputes() {
        return disputeRepository.countByStatus(DisputeStatus.UNDER_REVIEW);
    }

    public long getClosedDisputes() {
        return disputeRepository.countByStatus(DisputeStatus.CLOSED);
    }
}
