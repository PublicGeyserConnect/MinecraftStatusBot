package com.github.jensco.status;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class MinecraftStatusEmbedBuilder {

    @NotNull
    public static MessageEmbed statusEmbed(String ip, MinecraftStatus data, String favicon) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        boolean isOnline = data.getServerInfo().serverStatus();
        String motd = data.getServerInfo().motd();
        long latency = data.getServerInfo().latency();
        String version = data.getServerInfo().version();
        int maxPlayers = data.getServerInfo().maxPlayers();
        int currentPlayers = data.getServerInfo().currentOnline();
        int openSlots = data.getServerInfo().openSlots();

        long unixTimestamp = Instant.now().getEpochSecond();
        String discordTime = "<t:"+ unixTimestamp +":R>";

        embedBuilder.setTitle("Status for " + ip)
                .setColor(0x00FF00)
                .addField("Server is", isOnline ? ":green_circle: Online" : ":red_circle: Offline", false)
                .addField("MOTD", motd != null ? motd : "Unable to retrieve server information", true)
                .addField("Version", version != null ? version : "Unavailable", true)
                .addField("Maximum Players", String.valueOf(maxPlayers), true)
                .addField("Currently Online", String.valueOf(currentPlayers), true)
                .addField("Latency", latency > 0 ? latency + " ms" : "Unavailable", true)
                .addField("Open Slots", String.valueOf(openSlots), true)
                .addField("Last Status Update", discordTime, true)
                .setThumbnail(favicon);

        return embedBuilder.build();
    }
}
