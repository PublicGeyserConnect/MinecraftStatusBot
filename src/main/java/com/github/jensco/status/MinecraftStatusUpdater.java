package com.github.jensco.status;

import com.github.jensco.Bot;
import com.github.jensco.records.NotificationRecord;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import com.github.jensco.records.ServerDataRecord;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MinecraftStatusUpdater {
    private static final int EMBED_UPDATE_DELAY_SECONDS = 1;
    private final Map<String, Integer> offlineCountMap = new HashMap<>();

    public void startUpdateLoop() {
        try {
            ScheduledExecutorService executor = Bot.getGeneralThreadPool();
            executor.scheduleAtFixedRate(this::retrieveMessages, 0, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void retrieveMessages() {
        List<ServerDataRecord> serverDataRecordList = Bot.storageManager.getAllActiveServers();
        try (ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor()) {

            for (int i = 0; i < serverDataRecordList.size(); i++) {
                int finalI = i;
                scheduler.schedule(() -> {
                    ServerDataRecord serverDataRecord = serverDataRecordList.get(finalI);
                    String channelId = serverDataRecord.channelID();
                    String messageId = serverDataRecord.messageID();

                    TextChannel channel = Bot.getShardManager().getTextChannelById(channelId);
                    if (channel != null) {
                        channel.retrieveMessageById(messageId).queue(
                                message -> updateMessageEmbed(serverDataRecord, message, channel),
                                exception -> {
                                    Bot.getLogger().error("Could not locate status embed " + messageId);
                                    Bot.storageManager.setServerActiveStatus(serverDataRecord.guildId(), serverDataRecord.serverName(), false);
                                });
                    }
                }, i * EMBED_UPDATE_DELAY_SECONDS, TimeUnit.SECONDS);
            }

            scheduler.shutdown();
        }
    }

    private void updateMessageEmbed(ServerDataRecord serverData, Message message, TextChannel channel) {
        if (message != null) {
            try {
                MinecraftStatus statusData = new MinecraftStatus(serverData.serverAddress(), serverData.serverPort());
                MessageEmbed updatedEmbed = MinecraftStatusEmbedBuilder.statusEmbed(serverData, statusData);
                message.editMessageEmbeds(updatedEmbed).queue(
                        success -> {
                            if (!statusData.getServerInfo().serverStatus()) {
                                NotificationRecord notifyRole = Bot.storageManager.getNotifiedDataByGuildId(serverData.guildId());
                                if (!(notifyRole == null) && notifyRole.active()) {
                                    serverOfflineWarningMessage(channel, serverData, notifyRole.role());
                                }
                            } else offlineCountMap.remove(serverData.serverName());
                        },
                        exception -> Bot.getLogger().error("Failed to update message with ID " + message.getId())
                );
            } catch (Exception e) {
                Bot.getLogger().error("Failed to update message with ID " + message.getId());
            }
        }
    }

    private void serverOfflineWarningMessage(TextChannel channel, @NotNull ServerDataRecord data, String roleId) {
        int offlineCount = offlineCountMap.getOrDefault(data.serverName(), 0); // Get the offline count for the server
        offlineCount++; // Increment the offline count

        if (offlineCount == 2) { // Check if the server has been offline for 2 consecutive loops
            Role role = channel.getGuild().getRoleById(roleId);
            if (role == null) {
                channel.sendMessage("The provided role does not exist").queue();
                return;
            }
            channel.sendMessage(role.getAsMention() + ", Server **" + data.serverName() + "** is offline for 2 consecutive loops!").queue();
        }
        offlineCountMap.put(data.serverName(), offlineCount); // Update the offline count for the server
    }
}