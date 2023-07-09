package com.github.jensco.playerlist;

import com.github.jensco.status.MinecraftStatus;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;


public class PlayerListEmbedBuilder {

    @NotNull
    public static MessageEmbed playerListEmbed(String serverName, @NotNull MinecraftStatus data) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        List<String> playerNames = data.getServerInfo().playerNames();
        if (playerNames == null) {
            embedBuilder.setTitle("Player List Unavailable")
                    .setDescription("The server does not expose the player list.")
                    .setColor(0xFF0000);
        } else if (playerNames.isEmpty()) {
            embedBuilder.setTitle("No Players Online")
                    .setDescription("There are no players currently online.")
                    .setColor(0xFFFF00);
        } else {
            StringBuilder playerListBuilder = new StringBuilder();
            for (String playerName : playerNames) {
                playerListBuilder.append(playerName).append("\n");
            }
            String playerList = playerListBuilder.toString().trim();

            long unixTimestamp = Instant.now().getEpochSecond();
            String discordTime = "<t:" + unixTimestamp + ":R>";

            embedBuilder.setTitle("Online PlayerList for " + serverName)
                    .setColor(0x00FF00)
                    .addField("Last Status Update", discordTime, true)
                    .addField("Player List", playerList, false);
        }

        return embedBuilder.build();
    }
}