package com.github.jensco.util;

import io.sentry.Scope;
import io.sentry.Sentry;
import io.sentry.protocol.User;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.InterfacedEventManager;
import org.jetbrains.annotations.NotNull;

public class SentryEventManager extends InterfacedEventManager {
    @Override
    public void handle(@NotNull GenericEvent event) {
        if (event instanceof MessageReceivedEvent messageReceivedEvent) {
            Sentry.configureScope(scope -> buildMessageScope(scope, messageReceivedEvent.getAuthor(), messageReceivedEvent.getGuild(), messageReceivedEvent.getChannel(), messageReceivedEvent.getMessageId()));
        }

        super.handle(event);
    }

    private void buildMessageScope(Scope scope, net.dv8tion.jda.api.entities.User author, Guild server, MessageChannel channel, String messageId) {
        User user = new User();
        user.setId(author.getId());
        user.setUsername(author.getName());
        scope.setUser(user);

        scope.setExtra("guild_id", server == null ? "null" : server.getId());
        scope.setExtra("channel_id", channel.getId());
        scope.setExtra("message_id", messageId);
        scope.setExtra("message_link", String.format("https://discord.com/channels/%s/%s/%s", server == null ? "@me" : server.getId(), channel.getId(), messageId));
    }
}