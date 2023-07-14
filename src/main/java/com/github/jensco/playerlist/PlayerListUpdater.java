package com.github.jensco.playerlist;

import com.github.jensco.Bot;
import com.github.jensco.records.PlayerListDataRecord;
import com.github.jensco.records.ServerDataRecord;
import com.github.jensco.status.MinecraftStatus;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.List;
import java.util.concurrent.*;

import static com.github.jensco.Bot.LOGGER;

public class PlayerListUpdater {
    private final ScheduledExecutorService executorService;

    public PlayerListUpdater() {
        executorService = Executors.newScheduledThreadPool(6); // Use a thread pool with 6 threads
    }

    public void startUpdateLoop() {
        executorService.scheduleWithFixedDelay(this::retrieveMessages, 2, 5, TimeUnit.MINUTES);
    }

    public void retrieveMessages() {
        List<PlayerListDataRecord> playerListDataRecordList = Bot.storageManager.getAllActivePlayers();

        List<CompletableFuture<Void>> futures = playerListDataRecordList.stream()
                .map(playerListDataRecord -> CompletableFuture.supplyAsync(() -> retrieveAndUpdateMessage(playerListDataRecord)))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .exceptionally(ex -> {
                    LOGGER.error("Exception occurred while retrieving messages: " + ex.getMessage());
                    return null;
                })
                .join();
    }

    private Void retrieveAndUpdateMessage(PlayerListDataRecord playerListDataRecord) {
        String channelId = playerListDataRecord.channelID();
        String messageId = playerListDataRecord.messageID();

        TextChannel channel = Bot.getShardManager().getTextChannelById(channelId);
        if (channel != null) {
            channel.retrieveMessageById(messageId).queue(
                    message -> updateMessageEmbed(message, playerListDataRecord.serverName(), playerListDataRecord.guildID()),
                    exception -> {
                        if (Bot.storageManager.removePlayerListByMessageId(playerListDataRecord.guildID(), messageId)) {
                            LOGGER.info("Embed with ID " + messageId + " has been removed from the database");
                        }
                    });
        }
        return null;
    }

    private void updateMessageEmbed(Message message, String serverName, String guildID) {
        if (message != null) {
            try {
                ServerDataRecord record = Bot.storageManager.getServerInfo(serverName, guildID);
                MinecraftStatus data = new MinecraftStatus(record.serverAddress(), record.serverPort());
                MessageEmbed updatedEmbed = PlayerListEmbedBuilder.playerListEmbed(record.serverAddress(), data);
                message.editMessageEmbeds(updatedEmbed).queue(
                        success -> {
                        },
                        exception -> LOGGER.info("Failed to update message with ID " + message.getId())
                );
            } catch (Exception e) {
                LOGGER.info("Failed to update message with ID " + message.getId());
            }
        }
    }
}
