package com.github.jensco.listeners;

import com.github.jensco.Bot;
import com.github.jensco.records.RconLoginRecord;
import com.github.jensco.records.RconRecord;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.Checks;
import nl.vv32.rcon.Rcon;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RconListener extends ListenerAdapter {

    private final Cache<User, Integer> messageCache;
    private final HashMap<String, RconRecord> rconUser = new HashMap<>();
    private RconRecord rconRecord = null;

    public RconListener() {
        this.messageCache = CacheBuilder.newBuilder()
                .expireAfterWrite(5, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Checks.notNull(event.getGuild(), "server");
        if (event.getAuthor().isBot()) return;

        Member member = event.getMember();
        if (member != null) {
            if (!member.hasPermission(Permission.MANAGE_SERVER)) {
                return;
            }
        }

        MessageChannel channel = event.getChannel();
        if (!(channel instanceof TextChannel textChannel)) {
            return;
        }

        if (!rconUser.containsKey(event.getGuild().getId())) {
            rconRecord = Bot.storageManager.getRconDataByGuildAndChannelId(event.getGuild().getId(), textChannel.getId());
        }

        if (rconRecord == null) {
            return;
        }

        if (!Objects.equals(rconRecord.channelId(), event.getChannel().getId())) {
            return;
        }

        Integer count = messageCache.getIfPresent(event.getAuthor());
        if (count == null) {
            messageCache.put(event.getAuthor(), 1);
        }

        if (count != null && count >= 1) {
            textChannel = event.getChannel().asTextChannel();
            textChannel.sendMessage("You can only send 1 command every 5 seconds in this channel.").queue();
            return;
        }

        RconLoginRecord loginInfo = Bot.rconDataCache.getIfPresent(event.getGuild().getId());
        if (loginInfo == null) {
            textChannel.sendMessage("You need to login into Rcon before commands gets send.").queue();
            return;
        }

        try {
            try(Rcon rcon = Rcon.open(rconRecord.serverAddress(), rconRecord.rconPort())) {
                if (rcon.authenticate(loginInfo.password())) {
                    textChannel.sendMessage(rcon.sendCommand(event.getMessage().getContentRaw())).queue();
                } else {
                    textChannel.sendMessage("Failed to authenticate").queue();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
