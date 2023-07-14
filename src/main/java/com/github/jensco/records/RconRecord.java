package com.github.jensco.records;

public record RconRecord(String guildId, String serverName, int rconPort, String serverAddress, String channelId) {
}
