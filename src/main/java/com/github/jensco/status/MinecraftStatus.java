package com.github.jensco.status;

import br.com.azalim.mcserverping.MCPing;
import br.com.azalim.mcserverping.MCPingOptions;
import br.com.azalim.mcserverping.MCPingResponse;
import br.com.azalim.mcserverping.MCPingUtil;
import com.github.jensco.records.StatusRecord;
import com.nukkitx.protocol.bedrock.BedrockClient;
import com.nukkitx.protocol.bedrock.BedrockPong;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MinecraftStatus {
    private final String ip;
    private final int port;

    private static final int TIMEOUT_MS = 1500;

    public MinecraftStatus(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public StatusRecord getServerInfo() {
        CompletableFuture<MCPingResponse> javaDataFuture = CompletableFuture.supplyAsync(this::getJavaServerInfo);
        CompletableFuture<BedrockPong> bedrockDataFuture = CompletableFuture.supplyAsync(this::getBedrockServerInfo);

        boolean javaOnline = false;
        boolean bedrockOnline = false;
        String motd = null;
        String version = null;
        int maxPlayers = 0;
        int currentOnline = 0;
        long latency = 0;
        int openSlots = 0;
        List<String> playerNames = new ArrayList<>();

        try {
            MCPingResponse javaData = javaDataFuture.get();
            if (javaData != null) {
                motd = javaData.getDescription().getStrippedText();
                version = javaData.getVersion().getName();
                maxPlayers = javaData.getPlayers().getMax();
                currentOnline = javaData.getPlayers().getOnline();
                latency = javaData.getPing();
                javaOnline = true;

                if (!(javaData.getPlayers().getSample() == null)) {
                    for (MCPingResponse.Player player : javaData.getPlayers().getSample()) {
                        playerNames.add(player.getName());
                    }
                } else {
                    playerNames = null;
                }
            }

            BedrockPong bedrockData = bedrockDataFuture.get();
            if (bedrockData != null) {
                motd = MCPingUtil.stripColors(bedrockData.getMotd());
                version = bedrockData.getVersion();
                maxPlayers = bedrockData.getMaximumPlayerCount();
                currentOnline = bedrockData.getPlayerCount();
                // Bedrock servers do not provide latency information
                openSlots = maxPlayers - currentOnline;
                bedrockOnline = true;
            }
        } catch (InterruptedException | ExecutionException ignored) {
           return null;
        }

        return new StatusRecord(javaOnline || bedrockOnline, motd, version, maxPlayers, currentOnline, latency, openSlots, playerNames);
    }

    @Nullable
    private MCPingResponse getJavaServerInfo() {
        try {
            MCPingOptions options = MCPingOptions.builder()
                    .hostname(ip)
                    .port(port)
                    .timeout(TIMEOUT_MS)
                    .build();

            return MCPing.getPing(options);
        } catch (IOException ignored) {
            return null;
        }
    }

    @Nullable
    private BedrockPong getBedrockServerInfo() {
        try {
            BedrockClient client = new BedrockClient(new InetSocketAddress("0.0.0.0", 0));
            client.bind().join();
            InetSocketAddress addressToPing = new InetSocketAddress(ip, port);
            return client.ping(addressToPing, TIMEOUT_MS, TimeUnit.MILLISECONDS).get();
        } catch (InterruptedException | ExecutionException ignored) {
            return null;
        }
    }
}