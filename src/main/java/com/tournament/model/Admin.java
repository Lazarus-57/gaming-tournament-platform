package com.tournament.model;

import com.tournament.model.enums.RoleType;
import jakarta.persistence.*;

@Entity
@Table(name = "admins")
public class Admin extends User {

    private int adminLevel;

    public Admin() {}

    public Admin(String name, String email, String passwordHash, int adminLevel) {
        super(name, email, passwordHash, RoleType.ADMIN);
        this.adminLevel = adminLevel;
    }

    public int getAdminLevel() { return adminLevel; }
    public void setAdminLevel(int adminLevel) { this.adminLevel = adminLevel; }
}
