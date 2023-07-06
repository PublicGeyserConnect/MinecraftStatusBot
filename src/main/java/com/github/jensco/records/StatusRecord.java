package com.github.jensco.records;

public record StatusRecord(boolean serverStatus, String motd, String version, int maxPlayers,
                           int currentOnline, long latency, int openSlots) {
}
