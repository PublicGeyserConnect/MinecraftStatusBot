package com.github.jensco.commands;

import com.github.jensco.records.MinecraftServerInfo;
import com.github.jensco.status.MinecraftStatus;
import com.github.jensco.util.MessageHelper;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public class PingCommand extends SlashCommand {
    public PingCommand() {
        this.name = "ping";
        this.help = "Ping a minecraft server";
        this.guildOnly = false;
        this.options = Arrays.asList(
                new OptionData(OptionType.STRING, "address", "The IP address of the server", true),
                new OptionData(OptionType.INTEGER, "port", "The port number of the server", true)
        );
    }

    @Override
    protected void execute(@NotNull SlashCommandEvent event) {
        // Defer to wait for us to load a response and allows for files to be uploaded
        InteractionHook interactionHook = event.deferReply().complete();

        String ip = event.optString("address");
        int port = Objects.requireNonNull(event.getOption("port")).getAsInt();
        interactionHook.editOriginalEmbeds(handle(ip, port)).queue();
    }
    @NotNull
    private MessageEmbed handle(String ip, int port) {
        // port cant be bigger then 65535
        if (port > 65534) {
            return MessageHelper.errorResponse(null, "Server Settings", "Port number that you provided is invalid or to high");
        }

        // Create a ServerPing instance and ping the server
        MinecraftServerInfo serverInfo = new MinecraftStatus(ip, port, null).getServerInfo();

        if (serverInfo == null) {
            return MessageHelper.errorResponse(null, "Server Settings", "The ip you provided is invalid");

        }

        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Status for: " + ip + ":" + port)
                .addField("Server is", serverInfo.serverStatus() ? ":green_circle: Online" : ":red_circle: Offline", false)
                .addField("MOTD", serverInfo.motd() != null ? serverInfo.motd() : "Unable to retrieve server information", true)
                .addField("Version", serverInfo.version() != null ? serverInfo.version() : "Version information unavailable", true)
                .addField("Maximum Players", String.valueOf(serverInfo.maxPlayers()), true)
                .addField("Currently Online", String.valueOf(serverInfo.currentOnline()), true)
                .addField("Latency", serverInfo.latency() > 0 ? serverInfo.latency() + " ms" : "Unavailable", true)
                .addField("Open Slots", String.valueOf(serverInfo.openSlots()), true)
                .setFooter("Does not update! ");

        return embedBuilder.build();
    }
}