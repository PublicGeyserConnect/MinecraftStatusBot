package com.github.jensco.records;

public record ServerDataRecord(String guildId, String serverName, String serverAddress, int serverPort, boolean active,
                               String messageID, String channelID, String favicon) {
}