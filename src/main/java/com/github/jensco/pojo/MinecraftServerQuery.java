package com.github.jensco.pojo;

public class MinecraftServerQuery {
    private boolean online;
    private String host;
    private int port;
    private boolean eulaBlocked;
    private long retrievedAt;
    private long expiresAt;
    private int latency;
    private ServerVersion version;
    private ServerPlayers players;
    private ServerMotd motd;
    private String icon;

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isEulaBlocked() {
        return eulaBlocked;
    }

    public void setEulaBlocked(boolean eulaBlocked) {
        this.eulaBlocked = eulaBlocked;
    }

    public long getRetrievedAt() {
        return retrievedAt;
    }

    public void setRetrievedAt(long retrievedAt) {
        this.retrievedAt = retrievedAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public int getLatency() {
        return latency;
    }

    public void setLatency(int latency) {
        this.latency = latency;
    }

    public ServerVersion getVersion() {
        return version;
    }

    public void setVersion(ServerVersion version) {
        this.version = version;
    }

    public ServerPlayers getPlayers() {
        return players;
    }

    public void setPlayers(ServerPlayers players) {
        this.players = players;
    }

    public ServerMotd getMotd() {
        return motd;
    }

    public void setMotd(ServerMotd motd) {
        this.motd = motd;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}

