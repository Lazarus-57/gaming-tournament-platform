package com.tournament.service.analytics;

import com.tournament.model.Tournament;

public class TournamentReport implements Report {

    private String tournamentName;
    private int totalRegistrations;
    private int completedMatches;

    public String getTournamentName() { return tournamentName; }
    public int getTotalRegistrations() { return totalRegistrations; }
    public int getCompletedMatches() { return completedMatches; }

    public void setTournamentName(String tournamentName) { this.tournamentName = tournamentName; }
    public void setTotalRegistrations(int totalRegistrations) { this.totalRegistrations = totalRegistrations; }
    public void setCompletedMatches(int completedMatches) { this.completedMatches = completedMatches; }

    // Prototype Pattern: clone from template for fast report generation.
    @Override
    public Report copy() {
        TournamentReport copy = new TournamentReport();
        copy.tournamentName = this.tournamentName;
        copy.totalRegistrations = this.totalRegistrations;
        copy.completedMatches = this.completedMatches;
        return copy;
    }

    public static TournamentReport fromTournament(Tournament tournament, int registrations, int completedMatches) {
        TournamentReport report = new TournamentReport();
        report.tournamentName = tournament.getName();
        report.totalRegistrations = registrations;
        report.completedMatches = completedMatches;
        return report;
    }
}
