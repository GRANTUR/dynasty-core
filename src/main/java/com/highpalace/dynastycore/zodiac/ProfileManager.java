package com.highpalace.dynastycore.zodiac;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class ProfileManager {
    private final JavaPlugin plugin;
    private final File profileDir;
    private final Map<UUID, ZodiacProfile> cache = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ProfileManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.profileDir = new File(plugin.getDataFolder(), "profiles");
        if (!profileDir.exists()) {
            profileDir.mkdirs();
        }
    }

    public ZodiacProfile getProfile(UUID playerId, String playerName) {
        return cache.computeIfAbsent(playerId, id -> loadOrCreate(id, playerName));
    }

    public ZodiacProfile getProfile(UUID playerId) {
        return cache.get(playerId);
    }

    private ZodiacProfile loadOrCreate(UUID playerId, String playerName) {
        File file = new File(profileDir, playerId + ".json");
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                ZodiacProfile profile = new ZodiacProfile(playerId, json.has("playerName") ? json.get("playerName").getAsString() : playerName);
                if (json.has("zodiac") && !json.get("zodiac").isJsonNull()) {
                    profile.setZodiac(Zodiac.valueOf(json.get("zodiac").getAsString()));
                    if (json.has("chosenAt")) {
                        profile.setChosenAt(json.get("chosenAt").getAsLong());
                    }
                }
                return profile;
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load profile for " + playerId, e);
            }
        }
        return new ZodiacProfile(playerId, playerName);
    }

    public void saveProfile(ZodiacProfile profile) {
        File file = new File(profileDir, profile.getPlayerId() + ".json");
        JsonObject json = new JsonObject();
        json.addProperty("playerId", profile.getPlayerId().toString());
        json.addProperty("playerName", profile.getPlayerName());
        json.addProperty("zodiac", profile.getZodiac() != null ? profile.getZodiac().name() : null);
        json.addProperty("chosenAt", profile.getChosenAt());

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(json, writer);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save profile for " + profile.getPlayerId(), e);
        }
    }

    public void saveAll() {
        for (ZodiacProfile profile : cache.values()) {
            saveProfile(profile);
        }
    }

    public void removeFromCache(UUID playerId) {
        ZodiacProfile profile = cache.remove(playerId);
        if (profile != null) {
            saveProfile(profile);
        }
    }

    public void resetProfile(UUID playerId) {
        cache.remove(playerId);
        File file = new File(profileDir, playerId + ".json");
        if (file.exists()) {
            file.delete();
        }
    }

    public Map<UUID, ZodiacProfile> getAllCached() {
        return cache;
    }
}
