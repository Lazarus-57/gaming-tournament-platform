package com.tournament.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer teamId;

    @Column(nullable = false)
    private String teamName;

    @ManyToMany
    @JoinTable(
        name = "team_members",
        joinColumns = @JoinColumn(name = "team_id"),
        inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    private List<Player> members = new ArrayList<>();

    @ManyToMany(mappedBy = "teams")
    private List<Match> matches = new ArrayList<>();

    public Team() {}

    public Team(String teamName) {
        this.teamName = teamName;
    }

    // Getters and Setters
    public Integer getTeamId() { return teamId; }
    public void setTeamId(Integer teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public List<Player> getMembers() { return members; }
    public void setMembers(List<Player> members) { this.members = members; }

    public List<Match> getMatches() { return matches; }
    public void setMatches(List<Match> matches) { this.matches = matches; }

    public boolean isSizeValid(int requiredSize) {
        return members != null && members.size() == requiredSize;
    }
}
