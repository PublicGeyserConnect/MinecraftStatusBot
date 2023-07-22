package com.github.jensco.status;

import com.github.jensco.Bot;
import com.github.jensco.records.MinecraftServerInfo;
import com.github.jensco.records.NotificationRecord;
import com.github.jensco.records.ServerInfoFromDatabase;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class MinecraftStatusUpdater {
    private final ScheduledExecutorService executorService;
    private final Map<String, Integer> offlineCountMap = new ConcurrentHashMap<>();

    public MinecraftStatusUpdater() {
        executorService = Executors.newScheduledThreadPool(6); // Use a thread pool with 6 threads
    }

    public void startUpdateLoop() {
        executorService.scheduleWithFixedDelay(this::retrieveMessages, 1, 5, TimeUnit.MINUTES);
    }

    public void retrieveMessages() {
        List<ServerInfoFromDatabase> serverInfoFromDatabaseList = Bot.storageManager.getAllActiveServers();

        CompletableFuture.allOf(serverInfoFromDatabaseList.stream()
                        .map(serverDataRecord -> CompletableFuture.supplyAsync(() -> retrieveAndUpdateMessage(serverDataRecord))).toArray(CompletableFuture[]::new))
                .exceptionally(ex -> {
                    Bot.LOGGER.error("Exception occurred while retrieving messages: " + ex.getMessage());
                    return null;
                })
                .join();
    }

    @Nullable
    private Void retrieveAndUpdateMessage(@NotNull ServerInfoFromDatabase serverInfoFromDatabase) {
        String channelId = serverInfoFromDatabase.channelID();
        String messageId = serverInfoFromDatabase.messageID();

        TextChannel channel = Bot.getShardManager().getTextChannelById(channelId);
        if (channel != null) {
            channel.retrieveMessageById(messageId).queue(
                    message -> updateMessageEmbed(serverInfoFromDatabase, message, channel),
                    exception -> {
                        if (Bot.storageManager.deactivateServerByMessageId(serverInfoFromDatabase.guildId(), messageId)) {
                            Bot.LOGGER.info("Embed with ID " + messageId + " has been removed from the database");
                        }
                    });
        }
        return null;
    }

    private void updateMessageEmbed(ServerInfoFromDatabase serverData, Message message, TextChannel channel) {
        if (message != null) {
            try {

                MinecraftServerInfo info = new MinecraftStatus(serverData.serverAddress(), serverData.serverPort(), serverData.platform()).getServerInfo();
                MessageEmbed updatedEmbed = MinecraftStatusEmbedBuilder.sendStatusEmbed(serverData, info);
                message.editMessageEmbeds(updatedEmbed).queue(
                        success -> {
                            if (!info.serverStatus()) {
                                NotificationRecord notifyRole = Bot.storageManager.getNotifiedData(serverData.guildId());
                                if (notifyRole != null && notifyRole.active()) {
                                    serverOfflineWarningMessage(channel, serverData, notifyRole.role());
                                }
                            } else {
                                offlineCountMap.remove(serverData.serverName());
                            }
                        },
                        exception -> Bot.LOGGER.info("Failed to update message with ID " + message.getId())
                );
            } catch (Exception e) {
                Bot.LOGGER.info("Failed to update message with ID " + message.getId());
            }
        }
    }

    private void serverOfflineWarningMessage(TextChannel channel, @NotNull ServerInfoFromDatabase data, String roleId) {
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
