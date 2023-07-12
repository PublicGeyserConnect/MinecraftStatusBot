package com.github.jensco;

import com.github.jensco.listeners.ShutdownHandler;
import com.github.jensco.playerlist.PlayerListUpdater;
import com.github.jensco.status.MinecraftStatusUpdater;
import com.github.jensco.storage.AbstractStorageManager;
import com.github.jensco.storage.StorageType;
import com.github.jensco.util.PropertiesManager;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.messages.MessageRequest;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Bot {

    public static AbstractStorageManager storageManager;
    private static ShardManager shardManager;
    private static ScheduledExecutorService generalThreadPool;
    public static final Logger LOGGER = LoggerFactory.getLogger(Bot.class);


    public static void main(String[] args) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        // Load properties into the PropertiesManager
        Properties prop = new Properties();
        prop.load(new FileInputStream("bot.properties"));
        PropertiesManager.loadProperties(prop);

        // Load the db
        StorageType storageType = StorageType.getByName(PropertiesManager.getDatabaseType());
        if (storageType == StorageType.UNKNOWN) {
            LOGGER.info("Invalid database type! '" + PropertiesManager.getDatabaseType() + "'");
            System.exit(1);
        }

        try {
            storageManager = storageType.getStorageManager().getDeclaredConstructor().newInstance();
            storageManager.setupStorage();
        } catch (Exception e) {
            LOGGER.info("Unable to create database link!" + e);
            System.exit(1);
        }

        // Set up the main client
        CommandClientBuilder client = new CommandClientBuilder();
        client.setActivity(null);
        client.setOwnerId("0"); // No owner
        client.useHelpBuilder(false);
        client.addSlashCommands(getSlashCommands());

        MessageRequest.setDefaultMentionRepliedUser(false);

        generalThreadPool = Executors.newScheduledThreadPool(5);

        try {
            shardManager = DefaultShardManagerBuilder.create(
                            PropertiesManager.getToken(),
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_PRESENCES,
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.GUILD_MESSAGES)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .enableIntents(GatewayIntent.GUILD_PRESENCES, GatewayIntent.MESSAGE_CONTENT)
                    .setShardsTotal(Integer.parseInt(PropertiesManager.getTotalShards())) // Total number of shards
                    .setShards(Integer.parseInt(PropertiesManager.getShardsId()))
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableCache(CacheFlag.ACTIVITY)
                    .enableCache(CacheFlag.ROLE_TAGS)
                    .setEnableShutdownHook(true)
                    .setStatus(OnlineStatus.ONLINE)
                    .setActivity(Activity.playing("Booting..."))
                    .addEventListeners(new EventWaiter(),
                            client.build(),
                            new ShutdownHandler()
                    )
                    .build();

        } catch (IllegalArgumentException exception) {
            LOGGER.info("Failed to initialize JDA!" + exception);
            System.exit(1);
        }

        generalThreadPool.scheduleAtFixedRate(() -> {
            shardManager.getShardById(0).getPresence().setActivity(Activity.watching(storageManager.getActiveServerCount() + " active servers."));
        }, 5, 60 * 5, TimeUnit.SECONDS);

        MinecraftStatusUpdater statusUpdater = new MinecraftStatusUpdater();
        PlayerListUpdater playerListUpdater = new PlayerListUpdater();
        statusUpdater.startUpdateLoop();
        playerListUpdater.startUpdateLoop();

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(Bot::shutdown));

        LOGGER.info("MinecraftStatusBot has been started");


    }

    private static SlashCommand[] getSlashCommands() throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Reflections reflections = new Reflections("com.github.jensco.commands");
        Set<Class<? extends SlashCommand>> subTypes = reflections.getSubTypesOf(SlashCommand.class);
        List<SlashCommand> commands = new ArrayList<>();

        for (Class<? extends SlashCommand> theClass : subTypes) {
            // Ignore if "SubCommand" is in the name
            if (theClass.getSimpleName().contains("SubCommand")) continue;

            commands.add(theClass.getDeclaredConstructor().newInstance());
            LOGGER.debug("Loaded SlashCommand Successfully!");
        }

        return commands.toArray(new SlashCommand[0]);
    }

    public static ShardManager getShardManager() {
        return shardManager;
    }

    public static ScheduledExecutorService getGeneralThreadPool() {
        return generalThreadPool;
    }

    public static void shutdown() {
        storageManager.closeStorage();
        generalThreadPool.shutdown();
        LOGGER.info("Finished shutdown, exiting!");
        System.exit(0);
    }
}
