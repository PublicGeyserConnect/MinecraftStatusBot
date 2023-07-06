package com.github.jensco.util;

import java.awt.*;

public enum BotColors {
    NEUTRAL("#2B5797"),
    SUCCESS("#4CAF50"),
    FAILURE("#FF0000"),
    WARNING("#FF8C00");

    private final Color color;

    BotColors(String color) {
        this.color = Color.decode(color);
    }

    public Color getColor() {
        return color;
    }
}