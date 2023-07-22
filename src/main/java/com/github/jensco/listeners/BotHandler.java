package com.github.jensco.listeners;

import com.github.jensco.Bot;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class BotHandler extends ListenerAdapter {

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        String guildId = event.getGuild().getId();
        removeFromDatabase(guildId);
    }

    @Override
    public void onGuildBan(@NotNull GuildBanEvent event) {
        String guildId = event.getGuild().getId();
        removeFromDatabase(guildId);
    }

    private void removeFromDatabase(String guildId) {
        Bot.storageManager.removeAllRecordsByGuildId(guildId);
    }
}