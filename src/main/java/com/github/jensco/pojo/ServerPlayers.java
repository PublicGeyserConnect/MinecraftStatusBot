package com.github.jensco.pojo;

import java.util.List;

public class ServerPlayers {
    private int online;
    private int max;
    private List<Player> list;

    // Getters and Setters

    public int getOnline() {
        return online;
    }

    public void setOnline(int online) {
        this.online = online;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public List<Player> getList() {
        return list;
    }

    public void setList(List<Player> list) {
        this.list = list;
    }
}
