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
    private final String platform;

    public MinecraftStatus(String serverAddress, int serverPort, String platform) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.platform = platform;
    }

    public MinecraftServerInfo getServerInfo() {
        CompletableFuture<MinecraftJavaServerQuery> javaDataFuture = null;
        CompletableFuture<MinecraftBedrockServerQuery> bedrockDataFuture = null;

        if (platform == null || platform.equals("Java")) {
            javaDataFuture = CompletableFuture.supplyAsync(() -> getJavaServerStatus(JAVA_API_ENDPOINT + serverAddress + ":" + serverPort));
        }

        if (platform == null || platform.equals("Bedrock")) {
            bedrockDataFuture = CompletableFuture.supplyAsync(() -> getBedrockServerStatus(BEDROCK_API_ENDPOINT + serverAddress + ":" + serverPort));
        }

        boolean javaOnline = false;
        boolean bedrockOnline = false;
        String motd = null;
        String version = null;
        int maxPlayers = 0;
        int currentOnline = 0;
        long latency = 0;
        int openSlots = 0;
        List<String> playerNames = new ArrayList<>();
        String platform = null;

        try {
            MinecraftJavaServerQuery javaData = (javaDataFuture != null) ? javaDataFuture.get() : null;
            MinecraftBedrockServerQuery bedrockData = (bedrockDataFuture != null) ? bedrockDataFuture.get() : null;

            if (javaData != null && javaData.online) {
                javaOnline = true;
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
                platform = "Java";
            } else if (bedrockData != null && bedrockData.online) {
                bedrockOnline = true;
                motd = bedrockData.motd.clean;
                version = bedrockData.version.name;
                maxPlayers = bedrockData.players.max;
                currentOnline = bedrockData.players.online;
                openSlots = maxPlayers - currentOnline;
                latency = bedrockData.latency;
                platform = "Bedrock";
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return new MinecraftServerInfo(javaOnline || bedrockOnline, motd, version, maxPlayers, currentOnline, latency, openSlots, playerNames, platform);
    }

    @Nullable
    private MinecraftJavaServerQuery getJavaServerStatus(String apiUrl) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Gson gson = new Gson();
                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                MinecraftJavaServerQuery statusResponse = gson.fromJson(reader, MinecraftJavaServerQuery.class);
                reader.close();

                return statusResponse;
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    private MinecraftBedrockServerQuery getBedrockServerStatus(String apiUrl) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Gson gson = new Gson();
                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                MinecraftBedrockServerQuery statusResponse = gson.fromJson(reader, MinecraftBedrockServerQuery.class);
                reader.close();

                return statusResponse;
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}