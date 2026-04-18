package com.tournament.model;

import com.tournament.model.enums.BracketType;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "brackets")
public class Bracket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bracketId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BracketType type;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    @OneToMany(mappedBy = "bracket", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("roundNumber ASC, matchId ASC")
    private List<Match> matches = new ArrayList<>();

    public Bracket() {}

    public Bracket(BracketType type, Tournament tournament) {
        this.type = type;
        this.tournament = tournament;
    }

    // Getters and Setters
    public Integer getBracketId() { return bracketId; }
    public void setBracketId(Integer bracketId) { this.bracketId = bracketId; }

    public BracketType getType() { return type; }
    public void setType(BracketType type) { this.type = type; }

    public Tournament getTournament() { return tournament; }
    public void setTournament(Tournament tournament) { this.tournament = tournament; }

    public List<Match> getMatches() { return matches; }
    public void setMatches(List<Match> matches) { this.matches = matches; }
}
