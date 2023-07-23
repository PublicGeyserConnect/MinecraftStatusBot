package com.github.jensco.status;

import com.github.jensco.records.MinecraftServerInfo;
import com.github.jensco.util.BotColors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;


public class PlayerListEmbedBuilder {

    @NotNull
    public static MessageEmbed playerListEmbed(String serverName, @NotNull MinecraftServerInfo info) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        List<String> playerNames = info.playerNames();
        if (playerNames == null) {
            embedBuilder.setTitle("Player List Unavailable")
                    .setDescription("There are no players online.")
                    .setColor(BotColors.WARNING.getColor());
        } else if (playerNames.isEmpty()) {
            embedBuilder.setTitle("Server Not Reachable")
                    .setDescription("The server is currently not reachable")
                    .setColor(BotColors.FAILURE.getColor());
        } else {
            StringBuilder playerListBuilder = new StringBuilder();
            for (String playerName : playerNames) {
                playerListBuilder.append(playerName).append("\n");
            }
            String playerList = playerListBuilder.toString().trim();

            embedBuilder.setTitle("Online PlayerList for " + serverName)
                    .addField("Player List", playerList, false)
                    .setFooter("last updated ");
        }

        embedBuilder.setTimestamp(Instant.now());

        return embedBuilder.build();
    }
}