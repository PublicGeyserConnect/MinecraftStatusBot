package com.github.jensco.status;

import com.github.jensco.Bot;
import com.github.jensco.records.NotificationRecord;
import com.github.jensco.records.ServerDataRecord;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static com.github.jensco.Bot.LOGGER;

public class MinecraftStatusUpdater {
    private final ScheduledExecutorService executorService;
    private final Map<String, Integer> offlineCountMap = new ConcurrentHashMap<>();

    public MinecraftStatusUpdater() {
        executorService = Executors.newScheduledThreadPool(6); // Use a thread pool with 6 threads
    }

    public void startUpdateLoop() {
        executorService.scheduleWithFixedDelay(this::retrieveMessages, 0, 5, TimeUnit.MINUTES);
    }

    public void retrieveMessages() {
        List<ServerDataRecord> serverDataRecordList = Bot.storageManager.getAllActiveServers();

        CompletableFuture.allOf(serverDataRecordList.stream()
                        .map(serverDataRecord -> CompletableFuture.supplyAsync(() -> retrieveAndUpdateMessage(serverDataRecord))).toArray(CompletableFuture[]::new))
                .exceptionally(ex -> {
                    LOGGER.error("Exception occurred while retrieving messages: " + ex.getMessage());
                    return null;
                })
                .join();
    }

    private Void retrieveAndUpdateMessage(ServerDataRecord serverDataRecord) {
        String channelId = serverDataRecord.channelID();
        String messageId = serverDataRecord.messageID();

        TextChannel channel = Bot.getShardManager().getTextChannelById(channelId);
        if (channel != null) {
            channel.retrieveMessageById(messageId).queue(
                    message -> updateMessageEmbed(serverDataRecord, message, channel),
                    exception -> {
                        if (Bot.storageManager.deactivateServerByMessageId(serverDataRecord.guildId(), messageId)) {
                            LOGGER.info("Embed with ID " + messageId + " has been removed from the database");
                        }
                    });
        }
        return null;
    }

    private void updateMessageEmbed(ServerDataRecord serverData, Message message, TextChannel channel) {
        if (message != null) {
            try {
                MinecraftStatus statusData = new MinecraftStatus(serverData.serverAddress(), serverData.serverPort());
                MessageEmbed updatedEmbed = MinecraftStatusEmbedBuilder.statusEmbed(serverData, statusData);
                message.editMessageEmbeds(updatedEmbed).queue(
                        success -> {
                            if (!statusData.getServerInfo().serverStatus()) {
                                NotificationRecord notifyRole = Bot.storageManager.getNotifiedData(serverData.guildId());
                                if (notifyRole != null && notifyRole.active()) {
                                    serverOfflineWarningMessage(channel, serverData, notifyRole.role());
                                }
                            } else {
                                offlineCountMap.remove(serverData.serverName());
                            }
                        },
                        exception -> LOGGER.info("Failed to update message with ID " + message.getId())
                );
            } catch (Exception e) {
                LOGGER.info("Failed to update message with ID " + message.getId());
            }
        }
    }

    private void serverOfflineWarningMessage(TextChannel channel, @NotNull ServerDataRecord data, String roleId) {
        int offlineCount = offlineCountMap.getOrDefault(data.serverName(), 0); // Get the offline count for the server
        offlineCount++; // Increment the offline count

        if (offlineCount == 2) { // Check if the server has been offline for 2 consecutive loops
            Role role = channel.getGuild().getRoleById(roleId);
            if (role != null) {
                channel.sendMessage(role.getAsMention() + ", Server **" + data.serverName() + "** is offline for 2 consecutive loops!").queue();
            } else {
                channel.sendMessage("The provided role does not exist").queue();
            }
        }
        offlineCountMap.put(data.serverName(), offlineCount); // Update the offline count for the server
    }
}
