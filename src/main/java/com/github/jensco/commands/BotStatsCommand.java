package com.github.jensco.commands;

import com.github.jensco.Bot;
import com.github.jensco.util.BotColors;
import com.github.jensco.util.PropertiesManager;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.time.Instant;

public class BotStatsCommand extends SlashCommand {
    public BotStatsCommand() {
        this.name = "botstatus";
        this.help = "General information about this bot";
        this.guildOnly = false;
        this.cooldown = 60;
    }

    @Override
    protected void execute(@NotNull SlashCommandEvent event) {
        event.replyEmbeds(handle()).queue();
    }

    @NotNull
    private MessageEmbed handle() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        long uptimeMillis = runtimeMXBean.getUptime();
        long currentTimestamp = Instant.now().toEpochMilli();
        long uptimeTimestamp = currentTimestamp - uptimeMillis;
        long uptimeSeconds = uptimeTimestamp / 1000;

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        String osName = osBean.getName();
        String osArch = osBean.getArch();
        int availableProcessors = osBean.getAvailableProcessors();

        long maxMemory = Runtime.getRuntime().maxMemory();
        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        long usedMemory = totalMemory - freeMemory;

        // CPU Usage
        double cpuUsage = getCPUUsage();

        // RAM Usage
        String ramUsage = getRAMUsage(usedMemory, maxMemory);

        // Build the embed using descriptions without using fields
        EmbedBuilder helpEmbed = new EmbedBuilder()
                .setTitle("Bot Statistics")
                .setColor(BotColors.SUCCESS.getColor())
                .setThumbnail("https://cdn2.iconfinder.com/data/icons/whcompare-isometric-web-hosting-servers/50/server-2-512.png")
                .setDescription(
                        "**Servers Tracking :** " + Bot.storageManager.getActiveServerCount() + "\n" +
                                "**Guilds Handled:** " + Bot.storageManager.getUniqueGuildCount() + "\n" +
                                "**Rcon Handled:** " + Bot.storageManager.getUniqueRconGuildCount() + "\n" +
                                "**Bot Uptime:** <t:" + uptimeSeconds + ":f>\n" +
                                "\n" +
                                "**Server Performance**\n" +
                                "**Server:** " + PropertiesManager.getServerName() + "\n" +
                                "**OS and Cores:** " + osName + " " + osArch + " (" + availableProcessors + " cores)\n" +
                                "**CPU Usage:** " + String.format("%.2f%%", cpuUsage) + "\n" +
                                "**Memory Usage:** " + ramUsage + "\n" +
                                "\n" +
                                "**Shard Info**\n" +
                                "**Shards:** " + PropertiesManager.getTotalShards() + "\n" +
                                "**Shard ID:** " + PropertiesManager.getShardsId() + "\n"
                );

        return helpEmbed.build();
    }

    private double getCPUUsage() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
            return sunOsBean.getCpuLoad() * 100;
        } else {
            return 0.0;
        }
    }

    private String getRAMUsage(long usedMemory, long maxMemory) {
        double usedMegabytes = usedMemory / (1024.0 * 1024.0);
        double maxMegabytes = maxMemory / (1024.0 * 1024.0);

        return String.format("%.2f MB / %.2f MB", usedMegabytes, maxMegabytes);
    }
}