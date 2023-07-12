package com.github.jensco.listeners;

import com.github.jensco.Bot;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

public class ShutdownHandler extends ListenerAdapter {
    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        LoggerFactory.getLogger(ShutdownHandler.class).info("JDA shutdown!");
        Bot.shutdown();
    }
}