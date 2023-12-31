package com.github.jensco.commands;

import com.github.jensco.Bot;
import com.github.jensco.records.MinecraftServerInfo;
import com.github.jensco.status.MinecraftStatus;
import com.github.jensco.util.MessageHelper;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
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
        this.help = "Add or remove server (max 5) - Add a thumbnail on the status embed - Get a list of servers";
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
            this.cooldown = 20;
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
            InteractionHook interactionHook = event.deferReply().complete();

            try {
                boolean isRemoved = Bot.storageManager.removeServer(Objects.requireNonNull(event.getGuild()).getId(), serverName);
                String playerlist = "No playerList was deleted.";
                if (isRemoved) {
                    if (Bot.storageManager.removePlayerList(event.getGuild().getId(), serverName)) {
                        playerlist = "Removing your playerlist";
                    }
                    interactionHook.editOriginalEmbeds(MessageHelper.handleCommand("Server: **" + serverName + "** was removed from the database. " + playerlist, "Server Settings")).queue();

                } else {
                    interactionHook.editOriginalEmbeds(MessageHelper.errorResponse(null, "Server Settings", "Could not find a server named: **" + serverName + "**")).queue();
                }
            } catch (Exception e) {
                e.printStackTrace();
                interactionHook.editOriginalEmbeds(MessageHelper.errorResponse(null, "Server Settings", "Server was not removed. " + e.getMessage())).queue();
            }
        }
    }

    public static class AddServerSubCommand extends SlashCommand {
        public AddServerSubCommand() {
            this.name = "add";
            this.help = "Add a server.";
            this.cooldown = 20;
            this.userPermissions = new Permission[]{
                    Permission.MANAGE_SERVER
            };
            this.options = Arrays.asList(
                    new OptionData(OptionType.STRING, "displayname", "The display name of the server", true),
                    new OptionData(OptionType.STRING, "address", "The IP address of the server", true),
                    new OptionData(OptionType.INTEGER, "port", "The port number of the server", true),
                    new OptionData(OptionType.BOOLEAN, "image", "Favicon image url", false)
            );
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {

            InteractionHook interactionHook = event.deferReply().complete();
            interactionHook.editOriginalEmbeds(handle(event)).queue();

        }
    }

    public static class ServerListSubCommand extends SlashCommand {
        public ServerListSubCommand() {
            this.name = "list";
            this.help = "Get a list of all your servers.";
            this.cooldown = 20;
            this.guildOnly = true;
            this.userPermissions = new Permission[]{
                    Permission.MANAGE_SERVER
            };
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {
            String guildId = Objects.requireNonNull(event.getGuild()).getId();

            InteractionHook interactionHook = event.deferReply().complete();

            // Get all server names for the guild from the database
            List<String> serverNames = Bot.storageManager.getAllServerNamesByGuildId(guildId);

            if (serverNames == null || serverNames.isEmpty()) {
                interactionHook.editOriginalEmbeds(MessageHelper.errorResponse(null, "Server Settings", "No servers found.")).queue();
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Server list:\n");
            for (String serverName : serverNames) {
                sb.append("- ").append(serverName).append("\n");
            }
            interactionHook.editOriginalEmbeds(MessageHelper.handleCommand(sb.toString(), "Server Settings")).queue();
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
                    new OptionData(OptionType.STRING, "displayname", "The display name of the server", true),
                    new OptionData(OptionType.STRING, "url", "The url of the image", true)
                    );
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {
            String serverName = event.optString("displayname");
            String imageUrl = event.optString("url");
            InteractionHook interactionHook = event.deferReply().complete();

            Checks.notNull(event.getGuild(), "server");
            if (Bot.storageManager.getServerInfo(serverName, event.getGuild().getId()) == null) {
                interactionHook.editOriginalEmbeds(MessageHelper.errorResponse(null, "Server Settings", "The server that you tried to edit was not found.")).queue();
                return;
            }

            try {
                Bot.storageManager.updateServerFavicon(event.getGuild().getId(), serverName, imageUrl);
                interactionHook.editOriginalEmbeds(MessageHelper.handleCommand("Server image URL has been updated you will have to wait till next refresh for the image to update.", "Server Settings")).queue();
            } catch (Exception e) {
                interactionHook.editOriginalEmbeds(MessageHelper.errorResponse(null, "Server Settings",  "Failed to update server image URL. " + e.getMessage())).queue();
                e.printStackTrace();
            }
        }
    }

    @NotNull
    private static MessageEmbed handle(@NotNull SlashCommandEvent event) {
        String serverName = event.optString("displayname", "");
        String serverAddress = event.optString("address", "");
        int serverPort = Objects.requireNonNull(event.getOption("port")).getAsInt();
        String favicon = event.optString("favicon", "https://packpng.com/static/pack.png");

        // port cant be bigger then 65535
        if (serverPort > 65534) {
           return MessageHelper.errorResponse(null, "Server Settings", "Port number that you provided is invalid or to high");
        }

        // Create a ServerPing instance and ping the server
        MinecraftServerInfo serverInfo = new MinecraftStatus(serverAddress, serverPort).getServerInfo(null);

        if (serverInfo == null) {
            return MessageHelper.errorResponse(null, "Server Settings", "The ip you provided is invalid");

        }

        if (!serverInfo.serverStatus()) {
            return MessageHelper.errorResponse(null, "Server Settings", "Server was not reachable.");
        }
        // people only allowed to have 5 servers at a time
        List<String> serverNames = Bot.storageManager.getAllServerNamesByGuildId(event.getGuild().getId());
        if (serverNames.size() >= 5) {
            return MessageHelper.errorResponse(null, "Server Settings", "You can only have a maximum of 5 servers.");

        }
        // do not allow duplicate server names
        if (serverNames.contains(serverName)) {
            return MessageHelper.errorResponse(null, "Server Settings", "Database already contains this server.");
        }
        // add server to database
        try {
            Bot.storageManager.addServer(event.getGuild().getId(), serverName, serverAddress, serverPort, favicon, serverInfo.platform());
        } catch (Exception e) {
            return MessageHelper.errorResponse(null, "Server Settings", "Server was not added in the database. " + e.getMessage());
        }

        return MessageHelper.handleCommand("Server **" + serverName + "** was reachable and has been saved into our database.", "Server Settings");
    }
}
