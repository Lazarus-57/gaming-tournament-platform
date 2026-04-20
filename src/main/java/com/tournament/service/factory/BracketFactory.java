package com.tournament.service.factory;

import com.tournament.model.enums.TournamentFormat;
import com.tournament.service.strategy.BracketGenerationStrategy;
import com.tournament.service.strategy.KnockoutBracketStrategy;
import org.springframework.stereotype.Component;

@Component
public class BracketFactory {

    private final KnockoutBracketStrategy knockoutStrategy;

    public BracketFactory(KnockoutBracketStrategy knockoutStrategy) {
        this.knockoutStrategy = knockoutStrategy;
    }

    // Factory Method: chooses strategy based on tournament format.
    public BracketGenerationStrategy getStrategy(TournamentFormat format) {
        return knockoutStrategy;
    }
}
