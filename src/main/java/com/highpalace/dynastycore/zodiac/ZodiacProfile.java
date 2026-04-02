package com.highpalace.dynastycore.zodiac;

import java.util.UUID;

public class ZodiacProfile {
    private final UUID playerId;
    private final String playerName;
    private Zodiac zodiac;
    private long chosenAt;

    public ZodiacProfile(UUID playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.zodiac = null;
        this.chosenAt = 0;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Zodiac getZodiac() {
        return zodiac;
    }

    public void setZodiac(Zodiac zodiac) {
        this.zodiac = zodiac;
        this.chosenAt = System.currentTimeMillis();
    }

    public long getChosenAt() {
        return chosenAt;
    }

    public void setChosenAt(long chosenAt) {
        this.chosenAt = chosenAt;
    }

    public boolean hasChosen() {
        return zodiac != null;
    }
}
