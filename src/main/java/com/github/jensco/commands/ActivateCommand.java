package com.github.jensco.commands;

import com.github.jensco.status.MinecraftStatus;
import com.github.jensco.util.MessageHelper;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import com.github.jensco.Bot;
import com.github.jensco.status.MinecraftStatusEmbedBuilder;
import com.github.jensco.records.ServerRecord;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Objects;

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
            // get server info
            ServerRecord info = Bot.storageManager.getServerInfoByServerName(serverName, event.getGuild().getId());
            // check if there is a server match
            if (info == null) {
                event.replyEmbeds(MessageHelper.handleCommand(true, "The server you tried to enablee was not found.")).queue();
                return;
            }

            if (info.active()) {
                event.replyEmbeds(MessageHelper.handleCommand(true, "Server embed is already active.")).queue();
                return;
            }

            event.replyEmbeds(MessageHelper.handleCommand(false, "Server embed is being enabled, embed will be visible in +- 4 seconds.")).queue();
            MinecraftStatus data = new MinecraftStatus(info.serverAddress(), info.serverPort());
            // create server status embed
            MessageEmbed embed = MinecraftStatusEmbedBuilder.statusEmbed(
                    info.serverAddress(),
                    data,
                    info.favicon());

            Message sentMessage = null;
            try {
                //send embed to channel
                sentMessage = Objects.requireNonNull(Bot.getShardManager().getTextChannelById(event.getChannel().getId())).sendMessageEmbeds(embed).complete();
            } catch (
                    ErrorResponseException e) {
                event.replyEmbeds(MessageHelper.handleCommand(true, "Failed to send message to channel: " + e.getErrorResponse().getMeaning())).queue();

            }
            // set channel messageID and channelID
            assert sentMessage != null;
            Bot.storageManager.setServerMessageAndChannelId(
                    event.getGuild().getId(), serverName,
                    sentMessage.getId(),
                    event.getChannel().getId(),
                    true);

        }
    }

    public static class DisableEmbedSubCommand extends SlashCommand {
        public DisableEmbedSubCommand() {
            this.name = "disable";
            this.help = "Disables the embed.";
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
            ServerRecord info = Bot.storageManager.getServerInfoByServerName(serverName, event.getGuild().getId());

            if (info == null) {
                event.replyEmbeds(MessageHelper.handleCommand(true, "The server you are looking for was not found in our database")).queue();
                return;
            }

            if (!info.active()) {
                event.replyEmbeds(MessageHelper.handleCommand(false, "Server embed was already disabled")).queue();
                return;
            }

            // Retrieve the channelId and messageId from the ServerInfo object
            String channelId = info.channelID();
            String messageId = info.messageID();
            // Remove the embed message using the channelId and messageId
            Objects.requireNonNull(event.getGuild()
                            .getTextChannelById(channelId))
                    .retrieveMessageById(messageId)
                    .queue(message -> message.delete().queue());

            event.replyEmbeds(MessageHelper.handleCommand(false, "Server embed **" + serverName + "** is disabled.")).queue();
            Bot.storageManager.setServerActiveStatus(event.getGuild().getId(), serverName, false);
        }
    }
}
