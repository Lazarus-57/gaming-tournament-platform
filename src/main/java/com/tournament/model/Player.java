package com.tournament.model;

import com.tournament.model.enums.RoleType;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "players")
public class Player extends User {

    @Column(unique = true)
    private String gamerTag;

    private int rankingPoints;

    @ManyToMany(mappedBy = "members")
    private List<Team> teams = new ArrayList<>();

    public Player() {}

    public Player(String name, String email, String passwordHash, String gamerTag) {
        super(name, email, passwordHash, RoleType.PLAYER);
        this.gamerTag = gamerTag;
        this.rankingPoints = 0;
    }

    public String getGamerTag() { return gamerTag; }
    public void setGamerTag(String gamerTag) { this.gamerTag = gamerTag; }

    public int getRankingPoints() { return rankingPoints; }
    public void setRankingPoints(int rankingPoints) { this.rankingPoints = rankingPoints; }

    public List<Team> getTeams() { return teams; }
    public void setTeams(List<Team> teams) { this.teams = teams; }
}
