package com.github.jensco.storage;

import com.github.jensco.records.NotificationRecord;
import com.github.jensco.records.PlayerListDataRecord;
import com.github.jensco.records.RconRecord;
import com.github.jensco.records.ServerDataRecord;

import java.util.List;

public abstract class AbstractStorageManager {
    /**
     * Do any initial setup for the storage when the bot starts
     */
    public abstract void setupStorage() throws Exception;

    /**
     * Close any connections or do clean-up
     */
    public abstract void closeStorage();

    public abstract void addServer(String guildId, String serverName, String serverAddress, int serverPort, String favicon);


    public abstract boolean removeServer(String guildId, String serverName);

    public abstract List<String> getAllServerNamesByGuildId(String guildId);

    public abstract ServerDataRecord getServerInfo(String serverName, String guildId);

    public abstract void setServerActiveStatus(String guildId, String serverName, boolean active);

    public abstract void setServerMessageAndChannelId(String guildId, String serverName, String messageId, String channelId, boolean active);

    public abstract List<ServerDataRecord> getAllActiveServers();

    public abstract void updateServerFavicon(String guildId, String serverName, String favicon);

    public abstract void setNotifyRole(String guildId, String roleId);

    public abstract void activeNotify(String guildId, boolean active);

    public abstract void removeNotifyRole(String guildId);

    public abstract NotificationRecord getNotifiedData(String guildId);

    public abstract int getActiveServerCount();

    public abstract int getUniqueGuildCount();

    public abstract int getUniqueRconGuildCount();

    public abstract void addPlayerList(String guildId, String serverName, String messageId, String channelId, boolean active);

    public abstract boolean removePlayerList(String guildId, String serverName);

    public abstract PlayerListDataRecord getPlayerListData(String guildId, String serverName);

    public abstract List<PlayerListDataRecord> getAllActivePlayers();

    public abstract boolean removePlayerListByMessageId(String guildId, String messageId);

    public abstract boolean deactivateServerByMessageId(String guildId, String messageId);

    public abstract void setRcon(String guildId, String serverName, String serverAddress, int rconPort, String channelId);

    public abstract RconRecord getRconData(String guildId, String serverName);

    public abstract RconRecord getRconDataByGuildAndChannelId(String guildId, String channelId);

    public abstract boolean isChannelIdPresent(String channelId);

    public abstract void removeRconSettings(String guildId, String serverName);

    public abstract void removeAllRecordsByGuildId(String guildId);
}