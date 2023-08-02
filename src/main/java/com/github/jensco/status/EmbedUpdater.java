package com.github.jensco.status;

import com.github.jensco.Bot;
import com.github.jensco.records.MinecraftServerInfo;
import com.github.jensco.records.NotificationRecord;
import com.github.jensco.records.PlayerListDataRecord;
import com.github.jensco.records.ServerInfoFromDatabase;
import com.github.jensco.util.PropertiesManager;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class EmbedUpdater {
    private final ScheduledExecutorService executorService;
    private final Map<String, Integer> offlineCountMap = new ConcurrentHashMap<>();
    private final int DELAY = Integer.parseInt(PropertiesManager.getLoopDelay());

    public EmbedUpdater() {
        executorService = Executors.newScheduledThreadPool(6);
    }

    public void startUpdateLoop() {
        while (true) {
            List<ServerInfoFromDatabase> serverInfoFromDatabaseList = Bot.storageManager.getAllActiveServers();
            List<PlayerListDataRecord> playerListDataRecordList = Bot.storageManager.getAllActivePlayers();

            for (ServerInfoFromDatabase serverDataRecord : serverInfoFromDatabaseList) {
                CompletableFuture.supplyAsync(() -> processEmbedRecord(serverDataRecord), executorService)
                        .thenRun(this::delayForSeconds)
                        .exceptionally(ex -> {
                            Bot.LOGGER.error("Exception occurred while retrieving messages: " + ex.getMessage());
                            return null;
                        })
                        .join();
            }

            for (PlayerListDataRecord playerlist : playerListDataRecordList) {
                CompletableFuture.supplyAsync(() -> processEmbedRecord(playerlist), executorService)
                        .thenRun(this::delayForSeconds)
                        .exceptionally(ex -> {
                            Bot.LOGGER.error("Exception occurred while retrieving messages: " + ex.getMessage());
                            return null;
                        })
                        .join();
            }
        }
    }

    @Nullable
    private <T> Void processEmbedRecord(@NotNull T record) {
        String channelId;
        String messageId;

        if (record instanceof PlayerListDataRecord playerList) {
            channelId = playerList.channelID();
            messageId = playerList.messageID();
        } else if (record instanceof ServerInfoFromDatabase serverInfo) {
            channelId = serverInfo.channelID();
            messageId = serverInfo.messageID();
        } else {
            throw new IllegalArgumentException("Unsupported record type");
        }

        TextChannel channel = Bot.getShardManager().getTextChannelById(channelId);
        if (channel != null) {
            channel.retrieveMessageById(messageId).queue(
                    message -> {
                        if (record instanceof PlayerListDataRecord) {
                            updatePlayerListEmbed(Bot.storageManager.getServerInfo(
                                    ((PlayerListDataRecord) record).serverName(),
                                    ((PlayerListDataRecord) record).guildID()), message);
                        } else {
                            updateStatusEmbed((ServerInfoFromDatabase) record, message, channel);
                        }
                    },
                    exception -> {
                        // Check if the exception is of type ErrorResponseException and has error code 10008
                        if (exception instanceof ErrorResponseException errorResponseException) {
                            if (errorResponseException.getErrorCode() == 10008) {
                                if (record instanceof PlayerListDataRecord) {
                                    Bot.storageManager.removePlayerListByMessageId(
                                            ((PlayerListDataRecord) record).guildID(), messageId);
                                } else {
                                    Bot.storageManager.deactivateServerByMessageId(
                                            ((ServerInfoFromDatabase) record).guildId(), messageId);
                                }

                                Bot.LOGGER.info("Embed with ID " + messageId + " has been removed from the database for reason: " + exception.getMessage());

                            } else {
                               Bot.LOGGER.warn(exception.getMessage());
                               exception.printStackTrace();
                            }
                        }
                    });
        }

        return null;
    }

    private void updateStatusEmbed(ServerInfoFromDatabase serverData, Message message, TextChannel channel) {
        if (message != null) {
            try {
                MinecraftServerInfo info = new MinecraftStatus(serverData.serverAddress(), serverData.serverPort()).getServerInfo(serverData.platform());
                MessageEmbed updatedEmbed = StatusEmbedBuilder.sendStatusEmbed(serverData, info);
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
                        exception -> Bot.LOGGER.warn("Failed to update embed with ID " + message.getId() + " " + exception.getMessage())
                );
            } catch (Exception e) {
                e.printStackTrace();
                Bot.LOGGER.warn("Failed to edit message with ID " + message.getId() + " " + e.getMessage());
            }
        }
    }

    private void updatePlayerListEmbed(ServerInfoFromDatabase serverData, Message message) {
        if (message != null) {
            try {
                MinecraftServerInfo info = new MinecraftStatus(serverData.serverAddress(), serverData.serverPort()).getServerInfo(serverData.platform());
                MessageEmbed updatedEmbed = PlayerListEmbedBuilder.playerListEmbed(serverData.serverAddress(), info);
                message.editMessageEmbeds(updatedEmbed).queue(
                        success -> {
                        },
                        exception -> {
                            exception.printStackTrace();
                            Bot.LOGGER.warn("Failed to update embed with ID " + message.getId() + " " + exception.getMessage());
                        }
                        );
            } catch (Exception e) {
                Bot.LOGGER.warn("Failed to edit message with ID " + message.getId() + " " + e.getMessage());
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

    private void delayForSeconds() {
        ScheduledFuture<?> delayFuture = executorService.schedule(() -> {
        }, DELAY, TimeUnit.SECONDS);
        try {
            delayFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            Bot.LOGGER.error("Delay execution interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}