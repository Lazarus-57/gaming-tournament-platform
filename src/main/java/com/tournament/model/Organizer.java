package com.tournament.model;

import com.tournament.model.enums.RoleType;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "organizers")
public class Organizer extends User {

    private String organizationName;

    private boolean verified;

    @OneToMany(mappedBy = "organizer", cascade = CascadeType.ALL)
    private List<Tournament> tournaments = new ArrayList<>();

    public Organizer() {}

    public Organizer(String name, String email, String passwordHash, String organizationName) {
        super(name, email, passwordHash, RoleType.ORGANIZER);
        this.organizationName = organizationName;
        this.verified = false;
    }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public List<Tournament> getTournaments() { return tournaments; }
    public void setTournaments(List<Tournament> tournaments) { this.tournaments = tournaments; }
}
