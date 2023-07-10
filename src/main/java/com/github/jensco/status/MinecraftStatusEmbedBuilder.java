package com.github.jensco.status;

import com.github.jensco.records.ServerDataRecord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class MinecraftStatusEmbedBuilder {

    @NotNull
    public static MessageEmbed statusEmbed(ServerDataRecord serverData, @NotNull MinecraftStatus statusData) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        boolean isOnline = statusData.getServerInfo().serverStatus();
        String motd = statusData.getServerInfo().motd();
        long latency = statusData.getServerInfo().latency();
        String version = statusData.getServerInfo().version();
        int maxPlayers = statusData.getServerInfo().maxPlayers();
        int currentPlayers = statusData.getServerInfo().currentOnline();
        int openSlots = statusData.getServerInfo().openSlots();

        long unixTimestamp = Instant.now().getEpochSecond();
        String discordTime = "<t:"+ unixTimestamp +":R>";

        embedBuilder.setTitle("Status for " + serverData.serverName() + "\n(" + serverData.serverAddress() + ")")
                .setColor(0x00FF00)
                .addField("Server is", isOnline ? ":green_circle: Online" : ":red_circle: Offline", false)
                .addField("MOTD", motd != null ? motd : "Unable to retrieve server information", true)
                .addField("Version", version != null ? version : "Unavailable", true)
                .addField("Maximum Players", String.valueOf(maxPlayers), true)
                .addField("Currently Online", String.valueOf(currentPlayers), true)
                .addField("Latency", latency > 0 ? latency + " ms" : "Unavailable", true)
                .addField("Open Slots", String.valueOf(openSlots), true)
                .addField("Last Status Update", discordTime, true)
                .setThumbnail(serverData.favicon());

        return embedBuilder.build();
    }
}
