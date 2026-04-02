package com.highpalace.dynastycore.metrics;

import com.highpalace.dynastycore.zodiac.ProfileManager;
import com.highpalace.dynastycore.zodiac.Zodiac;
import com.highpalace.dynastycore.zodiac.ZodiacProfile;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.logging.Level;

public class MetricsServer {
    private final JavaPlugin plugin;
    private final ProfileManager profileManager;
    private HttpServer server;

    public MetricsServer(JavaPlugin plugin, ProfileManager profileManager) {
        this.plugin = plugin;
        this.profileManager = profileManager;
    }

    public void start(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/metrics", exchange -> {
                String metrics = buildMetrics();
                byte[] response = metrics.getBytes();
                exchange.getResponseHeaders().set("Content-Type", "text/plain; version=0.0.4");
                exchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            });
            server.createContext("/health", exchange -> {
                byte[] response = "OK".getBytes();
                exchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            });
            server.setExecutor(null);
            server.start();
            plugin.getLogger().info("DynastyCore metrics server started on port " + port);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to start metrics server", e);
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private String buildMetrics() {
        StringBuilder sb = new StringBuilder();

        // Total players with zodiacs
        long totalChosen = profileManager.getAllCached().values().stream()
                .filter(ZodiacProfile::hasChosen).count();
        sb.append("# HELP dynastycore_zodiac_players_total Total players who have chosen a zodiac\n");
        sb.append("# TYPE dynastycore_zodiac_players_total gauge\n");
        sb.append("dynastycore_zodiac_players_total ").append(totalChosen).append("\n");

        // Distribution per zodiac
        sb.append("# HELP dynastycore_zodiac_distribution Players per zodiac sign\n");
        sb.append("# TYPE dynastycore_zodiac_distribution gauge\n");
        for (Zodiac z : Zodiac.values()) {
            long count = profileManager.getAllCached().values().stream()
                    .filter(p -> p.getZodiac() == z).count();
            sb.append("dynastycore_zodiac_distribution{sign=\"").append(z.getDisplayName()).append("\"} ")
                    .append(count).append("\n");
        }

        return sb.toString();
    }
}
