package com.tournament.model;

import com.tournament.model.enums.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tournaments")
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tournamentId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String gameTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TournamentFormat format;

    private int teamSize;

    private LocalDate registrationStart;

    private LocalDate registrationEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TournamentStatus status = TournamentStatus.DRAFT;

    private double prizePool;

    private String rules;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    private Organizer organizer;

    @OneToOne(mappedBy = "tournament", cascade = CascadeType.ALL)
    private Bracket bracket;

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Registration> registrations = new ArrayList<>();

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL)
    private List<Notification> notifications = new ArrayList<>();

    public Tournament() {}

    // Getters and Setters
    public Integer getTournamentId() { return tournamentId; }
    public void setTournamentId(Integer tournamentId) { this.tournamentId = tournamentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGameTitle() { return gameTitle; }
    public void setGameTitle(String gameTitle) { this.gameTitle = gameTitle; }

    public TournamentFormat getFormat() { return format; }
    public void setFormat(TournamentFormat format) { this.format = format; }

    public int getTeamSize() { return teamSize; }
    public void setTeamSize(int teamSize) { this.teamSize = teamSize; }

    public LocalDate getRegistrationStart() { return registrationStart; }
    public void setRegistrationStart(LocalDate registrationStart) { this.registrationStart = registrationStart; }

    public LocalDate getRegistrationEnd() { return registrationEnd; }
    public void setRegistrationEnd(LocalDate registrationEnd) { this.registrationEnd = registrationEnd; }

    public TournamentStatus getStatus() { return status; }
    public void setStatus(TournamentStatus status) { this.status = status; }

    // GRASP High Cohesion: Tournament owns its lifecycle transitions.
    public void markConfigured() {
        if (status != TournamentStatus.DRAFT) {
            throw new IllegalStateException("Tournament must be in DRAFT to configure");
        }
        status = TournamentStatus.CONFIGURED;
    }

    public void openRegistration() {
        if (status != TournamentStatus.CONFIGURED) {
            throw new IllegalStateException("Tournament must be CONFIGURED to open registration");
        }
        status = TournamentStatus.REGISTRATION_OPEN;
    }

    public void closeRegistration() {
        if (status != TournamentStatus.REGISTRATION_OPEN) {
            throw new IllegalStateException("Tournament must be REGISTRATION_OPEN to close registration");
        }
        status = TournamentStatus.REGISTRATION_CLOSED;
    }

    public double getPrizePool() { return prizePool; }
    public void setPrizePool(double prizePool) { this.prizePool = prizePool; }

    public String getRules() { return rules; }
    public void setRules(String rules) { this.rules = rules; }

    public Organizer getOrganizer() { return organizer; }
    public void setOrganizer(Organizer organizer) { this.organizer = organizer; }

    public Bracket getBracket() { return bracket; }
    public void setBracket(Bracket bracket) { this.bracket = bracket; }

    public List<Registration> getRegistrations() { return registrations; }
    public void setRegistrations(List<Registration> registrations) { this.registrations = registrations; }

    public List<Notification> getNotifications() { return notifications; }
    public void setNotifications(List<Notification> notifications) { this.notifications = notifications; }
}
