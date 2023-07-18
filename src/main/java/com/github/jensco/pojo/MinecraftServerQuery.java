package com.github.jensco.pojo;

import java.util.ArrayList;

public class MinecraftServerQuery {
    public boolean online;
    public String host;
    public int port;
    public boolean eula_blocked;
    public long retrieved_at;
    public long expires_at;
    public int latency;
    public Version version;
    public Players players;
    public Motd motd;
    public String icon;
    public ArrayList<Object> mods;
    public String software;
    public ArrayList<Plugin> plugins;
}

