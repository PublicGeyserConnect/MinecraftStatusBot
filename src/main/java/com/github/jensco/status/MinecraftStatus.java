package com.github.jensco.status;

import com.github.jensco.pojo.java.MinecraftJavaServerQuery;
import com.github.jensco.pojo.bedrock.MinecraftBedrockServerQuery;
import com.github.jensco.pojo.java.Player;
import com.github.jensco.records.MinecraftServerInfo;
import com.github.jensco.util.PropertiesManager;
import com.google.gson.Gson;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MinecraftStatus {
    private static final String JAVA_API_ENDPOINT = PropertiesManager.getEndpointUrl() + "/status/java/";
    private static final String BEDROCK_API_ENDPOINT = PropertiesManager.getEndpointUrl() + "/status/bedrock/";

    private final String serverAddress;
    private final int serverPort;

    public MinecraftStatus(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public MinecraftServerInfo getServerInfo(String platform) {
        CompletableFuture<?> dataFuture = null;

        if (platform == null || platform.equalsIgnoreCase("Java")) {
            dataFuture = CompletableFuture.supplyAsync(() -> getServerStatus(JAVA_API_ENDPOINT + serverAddress + ":" + serverPort, MinecraftJavaServerQuery.class));
        } else if (platform.equalsIgnoreCase("Bedrock")) {
            dataFuture = CompletableFuture.supplyAsync(() -> getServerStatus(BEDROCK_API_ENDPOINT + serverAddress + ":" + serverPort, MinecraftBedrockServerQuery.class));
        }

        boolean isOnline = false;
        String motd = null;
        String version = null;
        int maxPlayers = 0;
        int currentOnline = 0;
        long latency = 0;
        int openSlots = 0;
        List<String> playerNames = new ArrayList<>();

        try {
            Object data = (dataFuture != null) ? dataFuture.get() : null;

            if (data instanceof MinecraftJavaServerQuery javaData && ((MinecraftJavaServerQuery) data).online) {
                isOnline = true;
                motd = javaData.motd.clean;
                version = javaData.version.name_clean;
                maxPlayers = javaData.players.max;
                currentOnline = javaData.players.online;
                latency = javaData.latency;
                openSlots = maxPlayers - currentOnline;
                List<Player> javaPlayers = javaData.players.list;
                for (Player player : javaPlayers) {
                    playerNames.add(player.name_clean);
                }
            } else if (data instanceof MinecraftBedrockServerQuery bedrockData && ((MinecraftBedrockServerQuery) data).online) {
                isOnline = true;
                motd = bedrockData.motd.clean;
                version = bedrockData.version.name;
                maxPlayers = bedrockData.players.max;
                currentOnline = bedrockData.players.online;
                openSlots = maxPlayers - currentOnline;
                latency = bedrockData.latency;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return new MinecraftServerInfo(isOnline, motd, version, maxPlayers, currentOnline, latency, openSlots, playerNames, platform);
    }

    @Nullable
    private <T> T getServerStatus(String apiUrl, Class<T> responseType) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Gson gson = new Gson();
                try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                    return gson.fromJson(reader, responseType);
                }
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}