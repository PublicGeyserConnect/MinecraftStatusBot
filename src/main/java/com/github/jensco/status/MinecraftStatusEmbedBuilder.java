package com.github.jensco.status;

import com.github.jensco.records.MinecraftServerInfo;
import com.github.jensco.records.ServerInfoFromDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class MinecraftStatusEmbedBuilder {

    @NotNull
    public static MessageEmbed sendStatusEmbed(@NotNull ServerInfoFromDatabase serverData, @NotNull MinecraftServerInfo info) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        boolean isOnline = info.serverStatus();
        String motd = info.motd();
        long latency = info.latency();
        String version = info.version();
        int maxPlayers = info.maxPlayers();
        int currentPlayers = info.currentOnline();
        int openSlots = info.openSlots();

        embedBuilder.setTitle("Status for " + serverData.serverName() + "\n(" + serverData.serverAddress() + ")")
                .addField("Server is", isOnline ? ":green_circle: Online" : ":red_circle: Offline", false)
                .addField("MOTD", motd != null ? motd : "Unable to retrieve server information", true);

        // Check if the version contains only numbers (protocol)
        if (version.matches("\\d+")) {
            embedBuilder.addField("Protocol", version, true);
        } else {
            embedBuilder.addField("Version", version, true);
        }

        embedBuilder
                .addField("Maximum Players", String.valueOf(maxPlayers), true)
                .addField("Currently Online", String.valueOf(currentPlayers), true)
                .addField("Latency", latency > 0 ? latency + " ms" : "Unavailable", true)
                .addField("Open Slots", String.valueOf(openSlots), true)
                .setFooter("last updated ")
                .setTimestamp(Instant.now())
                .setThumbnail(serverData.favicon());

        return embedBuilder.build();
    }
}
