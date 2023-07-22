package com.github.jensco.commands;

import com.github.jensco.Bot;
import com.github.jensco.records.RconLoginRecord;
import com.github.jensco.records.RconRecord;
import com.github.jensco.records.ServerInfoFromDatabase;
import com.github.jensco.util.MessageHelper;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class RconCommand extends SlashCommand {

    public RconCommand() {
        this.name = "rcon";
        this.help = "Rcon options";
        this.guildOnly = false;
        this.children =
                new SlashCommand[]{
                        new SetRconSubCommand(),
                        new LoginRconSubCommand(),
                        new DeleteRconSubCommand()
                };
    }

    @Override
    protected void execute(SlashCommandEvent event) {

    }

    public static class SetRconSubCommand extends SlashCommand {

        public SetRconSubCommand() {
            this.name = "set";
            this.help = "Set rcon for specific server";
            this.cooldown = 20;
            this.userPermissions = new Permission[]{
                    Permission.MANAGE_SERVER
            };
            this.options = Arrays.asList(
                    new OptionData(OptionType.STRING, "displayname", "The displayname of the server", true),
                    new OptionData(OptionType.INTEGER, "port", "The rcon port number", true),
                    new OptionData(OptionType.STRING, "channelid", "The discord channelID where rcon will listen at.", true)

            );
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {
            String serverName = event.optString("displayname");
            String channelId = event.optString("channelid");
            int rconPort = Integer.parseInt(event.optString("port", "25575"));

            Checks.notNull(event.getGuild(), "server");

            event.deferReply().queue(interactionHook -> {
                RconRecord rconData = Bot.storageManager.getRconData(event.getGuild().getId(), serverName);
                ServerInfoFromDatabase serverData = Bot.storageManager.getServerInfo(serverName, event.getGuild().getId());

                // port cant be bigger then 65535
                if (rconPort > 65534) {
                    interactionHook.editOriginalEmbeds(MessageHelper.errorResponse(null, "Rcon Settings", "Port number that you provided is invalid or to high.")).queue();
                    return;
                }

                if (serverData == null) {
                    interactionHook.editOriginalEmbeds(MessageHelper.errorResponse(null, "Rcon Settings", "The server you tried to enable the Rcon for was not found."))
                            .queue();
                    return;
                }

                if (channelId == null || event.getGuild().getTextChannelById(channelId) == null) {
                    interactionHook.editOriginalEmbeds(MessageHelper.errorResponse(null, "Rcon Settings", "The channelID you tried to setup does not exist."))
                            .queue();
                    return;
                }

                if (Bot.storageManager.isChannelIdPresent(channelId)) {
                    interactionHook.editOriginalEmbeds(MessageHelper.errorResponse(null, "Rcon Settings", "Sorry the channelID that you provided was already used."))
                            .queue();
                    return;
                }

                if (rconData == null) {
                    Bot.storageManager.setRcon(event.getGuild().getId(), serverName, serverData.serverAddress(), rconPort, channelId);
                    interactionHook.editOriginalEmbeds(MessageHelper.handleCommand( "Rcon has been setup, You can now activate rcon for this server.", "Rcon Settings"))
                            .queue();
                    return;
                }

                interactionHook.editOriginalEmbeds(MessageHelper.errorResponse(null, "Rcon Settings", "Rcon was already setup for this server."))
                        .queue();
            });
        }
    }

    public static class LoginRconSubCommand extends SlashCommand {

        public LoginRconSubCommand() {
            this.name = "login";
            this.help = "Login into rcon server.";
            this.cooldown = 20;
            this.userPermissions = new Permission[]{
                    Permission.MANAGE_SERVER
            };
            this.options = Arrays.asList(
                    new OptionData(OptionType.STRING, "displayname", "The displayname of the server", true),
                    new OptionData(OptionType.STRING, "password", "The rcon password", true)

            );
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {
            String serverName = event.optString("displayname");
            String rconPassword = event.optString("password");

            Checks.notNull(event.getGuild(), "server");

            event.deferReply().queue(interactionHook -> {
                ServerInfoFromDatabase serverData = Bot.storageManager.getServerInfo(serverName, event.getGuild().getId());
                RconRecord rconData = Bot.storageManager.getRconData(event.getGuild().getId(), serverName);

                if (serverData == null) {
                    interactionHook.editOriginalEmbeds(MessageHelper.errorResponse(null, "Rcon Login", "The server you tried to enable the Rcon for was not found."))
                            .queue();
                    return;
                }

                if (rconData == null) {
                    interactionHook.editOriginalEmbeds(MessageHelper.errorResponse(null, "Rcon Login", "Rcon was not setup."))
                            .queue();
                    return;
                }

                RconLoginRecord loginInfo = new RconLoginRecord(event.getGuild().getId(), serverName, rconPassword);
                String guildId = event.getGuild().getId();

                RconLoginRecord existingInfo = Bot.rconDataCache.getIfPresent(guildId);
                if (existingInfo == null || !existingInfo.equals(loginInfo)) {
                    Bot.rconDataCache.put(guildId, loginInfo);
                }

                interactionHook.editOriginalEmbeds(MessageHelper.handleCommand( "Successfully been logged in and are now able to send commands to console. Login expires automatically after 30 minutes.", "Rcon Login"))
                        .queue();
            });
        }
    }

    public static class DeleteRconSubCommand extends SlashCommand {

        public DeleteRconSubCommand() {
            this.name = "delete";
            this.help = "Delete rcon settings.";
            this.cooldown = 20;
            this.userPermissions = new Permission[]{
                    Permission.MANAGE_SERVER
            };
            this.options = List.of(
                    new OptionData(OptionType.STRING, "displayname", "The displayname of the server", true)
            );
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {
            String serverName = event.optString("displayname");

            event.deferReply().queue(interactionHook -> {
                RconRecord rconData = Bot.storageManager.getRconData(event.getGuild().getId(), serverName);

                if (rconData == null) {
                    interactionHook.editOriginalEmbeds(MessageHelper.errorResponse(null, "Rcon Delete", "The Rcon settings for "+ serverName + " was not found."))
                            .queue();
                    return;
                }

                Bot.storageManager.removeRconSettings(event.getGuild().getId(), serverName);
                interactionHook.editOriginalEmbeds(MessageHelper.handleCommand("The Rcon settings for "+ serverName + " are deleted.", "Rcon Delete"))
                        .queue();
            });

        }
    }
}
