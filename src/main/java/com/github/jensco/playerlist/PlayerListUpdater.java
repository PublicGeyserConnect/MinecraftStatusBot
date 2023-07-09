package com.github.jensco.playerlist;

import com.github.jensco.Bot;
import com.github.jensco.records.PlayerListDataRecord;
import com.github.jensco.records.ServerDataRecord;
import com.github.jensco.status.MinecraftStatus;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlayerListUpdater {

    private static final int EMBED_UPDATE_DELAY_SECONDS = 1;

    public void startUpdateLoop() {
        try {
            ScheduledExecutorService executor = Bot.getGeneralThreadPool();
            executor.scheduleAtFixedRate(this::retrieveMessages, 0, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void retrieveMessages() {
        List<PlayerListDataRecord> playerListDataRecordList = Bot.storageManager.getAllActivePlayers();
        try (ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor()) {

            for (int i = 0; i < playerListDataRecordList.size(); i++) {
                int finalI = i;
                scheduler.schedule(() -> {
                    PlayerListDataRecord playerListDataRecord = playerListDataRecordList.get(finalI);
                    String channelId = playerListDataRecord.channelID();
                    String messageId = playerListDataRecord.messageID();

                    TextChannel channel = Bot.getShardManager().getTextChannelById(channelId);
                    if (channel != null) {
                        channel.retrieveMessageById(messageId).queue(
                                message -> updateMessageEmbed(message, playerListDataRecord.serverName(), playerListDataRecord.guildID()),
                                exception -> {
                                    Bot.getLogger().error("Could not locate PlayerList embed " + messageId);
                                    Bot.storageManager.removePlayerList(playerListDataRecord.guildID(), playerListDataRecord.serverName());
                                });
                    }
                }, i * EMBED_UPDATE_DELAY_SECONDS, TimeUnit.SECONDS);
            }

            scheduler.shutdown();
        }
    }

    private void updateMessageEmbed(Message message, String serverName, String guildID) {
        if (message != null) {
            try {
                ServerDataRecord record = Bot.storageManager.getServerInfoByServerName(serverName, guildID);
                MinecraftStatus data = new MinecraftStatus(record.serverAddress(), record.serverPort());
                MessageEmbed updatedEmbed = PlayerListEmbedBuilder.playerListEmbed(record.serverAddress(), data);
                message.editMessageEmbeds(updatedEmbed).queue(
                        success -> {
                        },
                        exception -> Bot.getLogger().error("Failed to update message with ID " + message.getId())
                );
            } catch (Exception e) {
                Bot.getLogger().error("Failed to update message with ID " + message.getId());
            }
        }
    }
}
