package com.tournament.service.observer;

import com.tournament.model.Tournament;
import com.tournament.model.User;

/**
 * Observer Pattern: Interface for tournament event observers.
 * Implementations react to tournament-related events (e.g., sending notifications).
 */
public interface TournamentObserver {
    void onRegistration(User user, Tournament tournament);
    void onMatchScheduled(Tournament tournament);
    void onResultUpdated(Tournament tournament);
    void onTournamentCompleted(Tournament tournament);
}
