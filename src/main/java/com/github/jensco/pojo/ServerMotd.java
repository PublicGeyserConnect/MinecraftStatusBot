package com.github.jensco.pojo;

public class ServerMotd {
    private String raw;
    private String clean;
    private String html;

    // Getters and Setters

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    public String getClean() {
        return clean;
    }

    public void setClean(String clean) {
        this.clean = clean;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }
}
