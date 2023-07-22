package com.github.jensco.records;

import java.util.List;

public record MinecraftServerInfo(boolean serverStatus, String motd, String version, int maxPlayers,
                                  int currentOnline, long latency, int openSlots, List<String> playerNames, String platform) {
}
