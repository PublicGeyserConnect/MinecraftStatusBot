package com.github.jensco.pojo;

public class ServerVersion {
    private String nameRaw;
    private String nameClean;
    private String nameHtml;
    private int protocol;

    // Getters and Setters

    public String getNameRaw() {
        return nameRaw;
    }

    public void setNameRaw(String nameRaw) {
        this.nameRaw = nameRaw;
    }

    public String getNameClean() {
        return nameClean;
    }

    public void setNameClean(String nameClean) {
        this.nameClean = nameClean;
    }

    public String getNameHtml() {
        return nameHtml;
    }

    public void setNameHtml(String nameHtml) {
        this.nameHtml = nameHtml;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }
}
