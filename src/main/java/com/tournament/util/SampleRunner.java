package com.tournament.util;

import com.tournament.model.Tournament;
import com.tournament.model.TournamentBuilder;
import com.tournament.model.enums.TournamentFormat;

import java.time.LocalDate;

public class SampleRunner {

    public static void main(String[] args) {
        Tournament tournament = new TournamentBuilder()
            .name("Sample Cup")
            .gameTitle("Arena")
            .format(TournamentFormat.KNOCKOUT)
            .teamSize(1)
            .registrationStart(LocalDate.now())
            .registrationEnd(LocalDate.now().plusDays(7))
            .prizePool(1000)
            .rules("Sample rules")
            .build();

        System.out.println("Built tournament: " + tournament.getName());
    }
}
