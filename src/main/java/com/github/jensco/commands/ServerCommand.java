package com.github.jensco.commands;

import com.github.jensco.Bot;
import com.github.jensco.status.MinecraftStatus;
import com.github.jensco.util.MessageHelper;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ServerCommand extends SlashCommand {

    public ServerCommand() {
        this.name = "server";
        this.help = "server options";
        this.guildOnly = false;
        this.children = new SlashCommand[]{
                new AddServerSubCommand(),
                new RemoveServerSubCommand(),
                new ServerListSubCommand(),
                new SetServerImageSubCommand()
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // unused
    }

    public static class RemoveServerSubCommand extends SlashCommand {
        public RemoveServerSubCommand() {
            this.name = "remove";
            this.help = "Remove a server.";
            this.userPermissions = new Permission[]{
                    Permission.MANAGE_SERVER
            };
            this.options = Collections.singletonList(
                    new OptionData(OptionType.STRING, "alias", "The alias or name of the server to remove", true)
            );
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {
            String serverName = event.optString("alias");

            try {
                boolean isRemoved = Bot.storageManager.removeServer(Objects.requireNonNull(event.getGuild()).getId(), serverName);
                if (isRemoved) {
                    event.replyEmbeds(MessageHelper.handleCommand(false, "Server: **" + serverName + "** was removed from the database.")).queue();
                } else {
                    event.replyEmbeds(MessageHelper.handleCommand(true, "Could not find a server named: **" + serverName)).queue();
                }
            } catch (Exception e) {
                e.printStackTrace();
                event.replyEmbeds(MessageHelper.handleCommand(true, "Server was not removed. " + e.getMessage())).queue();
            } finally {
                Bot.storageManager.closeStorage();
            }
        }
    }

    public static class AddServerSubCommand extends SlashCommand {
        public AddServerSubCommand() {
            this.name = "add";
            this.help = "Add a server.";
            this.userPermissions = new Permission[]{
                    Permission.MANAGE_SERVER
            };
            this.options = Arrays.asList(
                    new OptionData(OptionType.STRING, "alias", "The alias or name of the server", true),
                    new OptionData(OptionType.STRING, "address", "The IP address of the server", true),
                    new OptionData(OptionType.INTEGER, "port", "The port number of the server", true),
                    new OptionData(OptionType.BOOLEAN, "image", "Favicon image url", false)
            );
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {
            String serverName = event.optString("alias", "");
            String serverAddress = event.optString("address", "");
            int serverPort = Objects.requireNonNull(event.getOption("port")).getAsInt();
            String favicon = event.optString("favicon", "https://packpng.com/static/pack.png");
            // port cant be bigger then 65535
            if (serverPort > 65534) {
                event.replyEmbeds(MessageHelper.handleCommand(true, "Port number that you provided is invalid or to high")).queue();
                return;
            }

            // Create a ServerPing instance and ping the server
            MinecraftStatus data = new MinecraftStatus(serverAddress, serverPort);
            if (!data.getServerInfo().serverStatus()) {
                event.replyEmbeds(MessageHelper.handleCommand(true, "Server was not reachable.")).queue();
                return;
            }
            // people only allowed to have 5 servers at a time
            Checks.notNull(event.getGuild(), "server");
            List<String> serverNames = Bot.storageManager.getAllServerNamesByGuildId(event.getGuild().getId());
            if (serverNames.size() >= 5) {
                event.replyEmbeds(MessageHelper.handleCommand(true, "You can only have a maximum of 5 servers.")).queue();
                return;
            }
            // do not allow duplicate server names
            if (serverNames.contains(serverName)) {
                event.replyEmbeds(MessageHelper.handleCommand(true, "Database already contains this server.")).queue();
                return;
            }
            // add server to database
            try {
                Bot.storageManager.addServer(Objects.requireNonNull(event.getGuild()).getId(), serverName, serverAddress, serverPort, favicon);
            } catch (Exception e) {
                event.replyEmbeds(MessageHelper.handleCommand(true, "Server was not added in the database. " + e.getMessage())).queue();
                e.printStackTrace();
            } finally {
                Bot.storageManager.closeStorage();
            }

            event.replyEmbeds(MessageHelper.handleCommand(false, "Server **" + serverName + "** was reachable and has been saved into our database.")).queue();
        }
    }

    public static class ServerListSubCommand extends SlashCommand {
        public ServerListSubCommand() {
            this.name = "list";
            this.help = "Get a list of all your servers.";
            this.guildOnly = true;
            this.userPermissions = new Permission[]{
                    Permission.MANAGE_SERVER
            };
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {
            String guildId = Objects.requireNonNull(event.getGuild()).getId();

            // Get all server names for the guild from the database
            List<String> serverNames = Bot.storageManager.getAllServerNamesByGuildId(guildId);

            if (serverNames.isEmpty()) {
                event.replyEmbeds(MessageHelper.handleCommand(true, "No servers found.")).queue();
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("Server list:\n");
                for (String serverName : serverNames) {
                    sb.append("- ").append(serverName).append("\n");
                }
                event.replyEmbeds(MessageHelper.handleCommand(false, sb.toString())).queue();
            }
        }
    }

    public static class SetServerImageSubCommand extends SlashCommand {
        public SetServerImageSubCommand() {
            this.name = "image";
            this.help = "Set an favicon image in the status embed.";
            this.userPermissions = new Permission[]{
                    Permission.MANAGE_SERVER
            };
            this.options = Arrays.asList(
                    new OptionData(OptionType.STRING, "alias", "The alias or name of the server", true),
                    new OptionData(OptionType.STRING, "url", "The url of the image", true)
                    );
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {
            String serverName = event.optString("alias");
            String imageUrl = event.optString("url");

            Checks.notNull(event.getGuild(), "server");
            if (Bot.storageManager.getServerInfoByServerName(serverName, event.getGuild().getId()) == null) {
                event.replyEmbeds(MessageHelper.handleCommand(true, "The server that you tried to edit was not found.")).queue();
                return;
            }

            try {
                Bot.storageManager.updateServerFavicon(event.getGuild().getId(), serverName, imageUrl);
                event.replyEmbeds(MessageHelper.handleCommand(false, "Server image URL has been updated you will have to wait till next refresh for the image to update.")).queue();
            } catch (Exception e) {
                event.replyEmbeds(MessageHelper.handleCommand(true, "Failed to update server image URL. " + e.getMessage())).queue();
                e.printStackTrace();
            } finally {
                Bot.storageManager.closeStorage();
            }
        }
    }
}
