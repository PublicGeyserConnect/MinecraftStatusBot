package com.github.jensco.commands;

import com.github.jensco.Bot;
import com.github.jensco.playerlist.PlayerListEmbedBuilder;
import com.github.jensco.records.PlayerListDataRecord;
import com.github.jensco.records.ServerDataRecord;
import com.github.jensco.status.MinecraftStatus;
import com.github.jensco.util.MessageHelper;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PlayerListEmbedCommand extends SlashCommand {

    public PlayerListEmbedCommand() {
        this.name = "playerlist";
        this.help = "PlayerList embed commands.";
        this.guildOnly = false;
        this.children = new SlashCommand[]{
                new EnablePlayerListEmbedSubCommand(),
                new DisablePlayerListSubCommand(),
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {

    }

    private static class EnablePlayerListEmbedSubCommand extends SlashCommand {
        public EnablePlayerListEmbedSubCommand() {
            this.name = "enable";
            this.help = "Adds the player list embed in the channel.";
            this.cooldown = 20;
            this.userPermissions = new Permission[]{
                    Permission.MANAGE_SERVER
            };
            this.options = Collections.singletonList(
                    new OptionData(OptionType.STRING, "alias", "The alias or name of the server to enable the PlayerList embed.", true)
            );
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {
            String serverName = event.optString("alias");
            PlayerListDataRecord playerListInfo = Bot.storageManager.getPlayerListData(event.getGuild().getId(), serverName);
            ServerDataRecord serverInfo = Bot.storageManager.getServerInfo(serverName, event.getGuild().getId());

            event.deferReply().queue(interactionHook -> {
                if (serverInfo == null) {
                    interactionHook.editOriginalEmbeds(MessageHelper.errorResponse(null, "PlayerList Embed Settings", "The server you tried to enable the PlayerList for was not found."))
                            .queue();
                    return;
                }

                if (playerListInfo == null) {
                    interactionHook.editOriginalEmbeds(
                                    MessageHelper.handleCommand("PlayerList embed is being activated, this can take a few seconds. this message will auto delete itself.", "PlayerList Embed Settings"))
                            .queueAfter(1, TimeUnit.SECONDS, sentMessage -> Objects.requireNonNull(Bot.getShardManager().getTextChannelById(event.getChannel().getId()))
                                    .sendMessageEmbeds(handle(serverInfo))
                                    .queue(sendStatus -> {
                                        Bot.storageManager.addPlayerList(
                                                event.getGuild().getId(), serverName,
                                                sendStatus.getId(),
                                                event.getChannel().getId(),
                                                true);
                                        sentMessage.delete().queue();
                                    }));
                    return;
                }

                if (playerListInfo.active()) {
                    interactionHook.editOriginalEmbeds(MessageHelper.errorResponse(null, "PlayerList Embed Settings", "The Playerlist is already enabled."))
                            .queue();
                }
            });

        }

        @NotNull
        private static MessageEmbed handle(@NotNull ServerDataRecord info) {
            MinecraftStatus data = new MinecraftStatus(info.serverAddress(), info.serverPort());
            // create server status embed
            return PlayerListEmbedBuilder.playerListEmbed(
                    info.serverAddress(),
                    data);
        }
    }

    private static class DisablePlayerListSubCommand extends SlashCommand {

        public DisablePlayerListSubCommand() {
            this.name = "disable";
            this.help = "Disable the PlayerList of a given server.";
            this.userPermissions = new Permission[]{
                    Permission.MANAGE_SERVER
            };
            this.options = Collections.singletonList(
                    new OptionData(OptionType.STRING, "alias", "The alias or name of the server to disable the PlayerList embed.", true)
            );
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {
            String serverName = event.optString("alias");

            event.deferReply().queue(interactionHook -> {
                PlayerListDataRecord playerListInfo = Bot.storageManager.getPlayerListData(event.getGuild().getId(), serverName);

                if (playerListInfo == null) {
                    interactionHook.editOriginalEmbeds(MessageHelper.errorResponse(null, "PlayerList Embed Settings", "The PlayerList embed you are looking for was not found in our database"))
                            .queue();
                    return;
                }

                if (!playerListInfo.active()) {
                    interactionHook.editOriginalEmbeds(MessageHelper.errorResponse(null, "PlayerList Embed Settings", "PlayerList embed was already disabled"))
                            .queue();
                    return;
                }

                // Remove the embed message using the channelID and messageID
                Objects.requireNonNull(event.getGuild().getTextChannelById(playerListInfo.channelID()))
                        .retrieveMessageById(playerListInfo.messageID())
                        .queue(message -> message.delete().queue(deletedMessage -> {
                            interactionHook.editOriginalEmbeds(MessageHelper.handleCommand("PlayerList embed **" + serverName + "** is disabled.", "PlayerList Embed Settings"))
                                    .queue();
                            Bot.storageManager.removePlayerList(playerListInfo.guildID(), playerListInfo.serverName());
                        }, error -> {
                            Bot.storageManager.removePlayerList(playerListInfo.guildID(), playerListInfo.serverName());
                            interactionHook.editOriginalEmbeds(MessageHelper.handleCommand("PlayerList embed **" + serverName + "** was manually removed and has now been removed from our database.", "PlayerList Embed Settings"))
                                    .queue();
                        }), error -> {
                            Bot.storageManager.removePlayerList(playerListInfo.guildID(), playerListInfo.serverName());
                            interactionHook.editOriginalEmbeds(MessageHelper.handleCommand("PlayerList embed **" + serverName + "** was manually removed and has now been removed from our database.", "PlayerList Embed Settings"))
                                    .queue();
                        });
            });
        }
    }
}