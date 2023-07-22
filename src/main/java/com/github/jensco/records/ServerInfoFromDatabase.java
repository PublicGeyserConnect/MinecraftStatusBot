package com.github.jensco.records;

public record ServerInfoFromDatabase(String guildId, String serverName, String serverAddress, int serverPort, boolean active,
                                     String messageID, String channelID, String favicon, String platform) {
}