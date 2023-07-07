package com.github.jensco.commands;

import com.github.jensco.status.MinecraftStatus;
import com.github.jensco.util.MessageHelper;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import com.github.jensco.Bot;
import com.github.jensco.status.MinecraftStatusEmbedBuilder;
import com.github.jensco.records.ServerRecord;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;


import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ActivateCommand extends SlashCommand {
    public ActivateCommand() {
        this.name = "status";
        this.help = "activate embed status";
        this.guildOnly = false;
        this.children = new SlashCommand[]{
                new EnableEmbedSubCommand(),
                new DisableEmbedSubCommand(),
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
    }

    public static class EnableEmbedSubCommand extends SlashCommand {
        public EnableEmbedSubCommand() {
            this.name = "enable";
            this.help = "Adds the status embed in the channel.";
            this.cooldown = 20;
            this.userPermissions = new Permission[]{
                    Permission.MANAGE_SERVER
            };
            this.options = Collections.singletonList(
                    new OptionData(OptionType.STRING, "alias", "The alias or name of the server to activate", true)
            );
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {
            String serverName = event.optString("alias");

            InteractionHook interactionHook = event.deferReply().complete();

            ServerRecord info = Bot.storageManager.getServerInfoByServerName(serverName, event.getGuild().getId());

            if (info == null) {
                interactionHook.editOriginalEmbeds(MessageHelper.errorResponse(null, "Status Embed Settings", "The server you tried to enable was not found."))
                        .queue();
                return;
            }

            if (info.active()) {
                interactionHook.editOriginalEmbeds(MessageHelper.errorResponse(null, "Status Embed Settings", "Server status embed is already active."))
                        .queue();
                return;
            }

            interactionHook.editOriginalEmbeds(handle(info))
                    .queueAfter(1, TimeUnit.SECONDS, sentMessage -> {
                        Bot.storageManager.setServerMessageAndChannelId(
                                event.getGuild().getId(), serverName,
                                sentMessage.getId(),
                                event.getChannel().getId(),
                                true);
                    });
        }
    }

    public static class DisableEmbedSubCommand extends SlashCommand {
        public DisableEmbedSubCommand() {
            this.name = "disable";
            this.help = "Disables the embed.";
            this.cooldown = 20;
            this.userPermissions = new Permission[]{
                    Permission.MANAGE_SERVER
            };
            this.options = Collections.singletonList(
                    new OptionData(OptionType.STRING, "alias", "The alias or name of the server to disable", true)
            );
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {
            String serverName = event.optString("alias");
            Checks.notNull(event.getGuild(), "server");
            ServerRecord info = Bot.storageManager.getServerInfoByServerName(serverName, event.getGuild().getId());

            if (info == null) {
                event.replyEmbeds(Objects.requireNonNull(MessageHelper.errorResponse(event, "Status Embed Settings", "The server you are looking for was not found in our database"))).queue();
                return;
            }

            if (!info.active()) {
                event.replyEmbeds(Objects.requireNonNull(MessageHelper.errorResponse(event, "Status Embed Settings", "Server embed was already disabled"))).queue();
                return;
            }

            // Remove the embed message using the channelId and messageId
            Objects.requireNonNull(event.getGuild()
                            .getTextChannelById(info.channelID()))
                    .retrieveMessageById(info.messageID())
                    .queue(message -> {
                        message.delete().queue();
                        event.replyEmbeds(MessageHelper.handleCommand("Server embed **" + serverName + "** is disabled.", "Status Embed Settings")).queue();
                        Bot.storageManager.setServerActiveStatus(event.getGuild().getId(), serverName, false);
                    });
        }
    }

    @NotNull
    private static MessageEmbed handle(@NotNull ServerRecord info) {
        MinecraftStatus data = new MinecraftStatus(info.serverAddress(), info.serverPort());
        // create server status embed
        return MinecraftStatusEmbedBuilder.statusEmbed(
                info.serverAddress(),
                data,
                info.favicon());
    }
}
