package com.github.jensco.status;

import com.github.jensco.Bot;
import com.github.jensco.records.NotificationRecord;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import com.github.jensco.records.ServerRecord;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MinecraftStatusChecker {
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
        List<ServerRecord> serverRecordList = Bot.storageManager.getAllActiveServers();
        try (ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor()) {

            for (int i = 0; i < serverRecordList.size(); i++) {
                int finalI = i;
                scheduler.schedule(() -> {
                    ServerRecord serverRecord = serverRecordList.get(finalI);
                    String channelId = serverRecord.channelID();
                    String messageId = serverRecord.messageID();

                    TextChannel channel = Bot.getShardManager().getTextChannelById(channelId);
                    if (channel != null) {
                        channel.retrieveMessageById(messageId).queue(
                                message -> updateMessageEmbed(serverRecord, message, channel),
                                exception -> Bot.getLogger().error("Failed to retrieve message with ID " + messageId)
                        );
                    }
                }, i * EMBED_UPDATE_DELAY_SECONDS, TimeUnit.SECONDS);
            }

            scheduler.shutdown();
        }
    }

    private void updateMessageEmbed(ServerRecord serverRecord, Message message, TextChannel channel) {
        if (message != null) {
            try {
                MinecraftStatus data = new MinecraftStatus(serverRecord.serverAddress(), serverRecord.serverPort());
                MessageEmbed updatedEmbed = MinecraftStatusEmbedBuilder.statusEmbed(serverRecord.serverAddress(), data, serverRecord.favicon());
                message.editMessageEmbeds(updatedEmbed).queue(
                        success -> {
                            if (!data.getServerInfo().serverStatus()) {
                                NotificationRecord notifyRole = Bot.storageManager.getNotifiedDataByGuildId(serverRecord.guildId());
                                if (!(notifyRole == null) && notifyRole.active()) {
                                    serverOfflineWarningMessage(channel, serverRecord, notifyRole.role());
                                }
                            } else offlineCountMap.remove(serverRecord.serverName());
                        },
                        exception -> Bot.getLogger().error("Failed to update message with ID " + message.getId())
                );
            } catch (Exception e) {
                Bot.getLogger().error("Failed to update message with ID " + message.getId());
            }
        }
    }

    private void serverOfflineWarningMessage(TextChannel channel, @NotNull ServerRecord data, String roleId) {
        int offlineCount = offlineCountMap.getOrDefault(data.serverName(), 0); // Get the offline count for the server
        System.out.println(offlineCount);
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