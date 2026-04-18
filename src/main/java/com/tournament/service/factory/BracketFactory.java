package com.tournament.service.factory;

import com.tournament.model.enums.TournamentFormat;
import com.tournament.service.strategy.BracketGenerationStrategy;
import com.tournament.service.strategy.KnockoutBracketStrategy;
import com.tournament.service.strategy.RoundRobinBracketStrategy;
import org.springframework.stereotype.Component;

@Component
public class BracketFactory {

    private final KnockoutBracketStrategy knockoutStrategy;
    private final RoundRobinBracketStrategy roundRobinStrategy;

    public BracketFactory(KnockoutBracketStrategy knockoutStrategy,
                          RoundRobinBracketStrategy roundRobinStrategy) {
        this.knockoutStrategy = knockoutStrategy;
        this.roundRobinStrategy = roundRobinStrategy;
    }

    // Factory Method: chooses strategy based on tournament format.
    public BracketGenerationStrategy getStrategy(TournamentFormat format) {
        return knockoutStrategy;
    }
}
