package com.github.jensco.records;

import java.util.List;

public record StatusRecord(boolean serverStatus, String motd, String version, int maxPlayers,
                           int currentOnline, long latency, int openSlots) {
}
