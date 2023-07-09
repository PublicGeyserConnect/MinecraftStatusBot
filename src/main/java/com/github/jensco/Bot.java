package com.github.jensco;

import com.github.jensco.playerlist.PlayerListUpdater;
import com.github.jensco.util.PropertiesManager;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import io.sentry.Sentry;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.messages.MessageRequest;
import com.github.jensco.status.MinecraftStatusUpdater;
import com.github.jensco.storage.AbstractStorageManager;
import com.github.jensco.storage.StorageType;
import com.github.jensco.util.SentryEventManager;
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
    public static EventWaiter waiter = new EventWaiter();

    public static void main(String[] args) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        // Load properties into the PropertiesManager
        Properties prop = new Properties();
        prop.load(new FileInputStream("bot.properties"));
        PropertiesManager.loadProperties(prop);

        // Setup sentry.io
        if (PropertiesManager.getSentryDsn() != null) {
            LOGGER.info("Loading sentry.io...");
            Sentry.init(options -> {
                options.setDsn(PropertiesManager.getSentryDsn());
                options.setEnvironment(PropertiesManager.getSentryEnv());
                LOGGER.info("Sentry.io loaded");
            });
        }

        // Load the db
        StorageType storageType = StorageType.getByName(PropertiesManager.getDatabaseType());
        if (storageType == StorageType.UNKNOWN) {
            LOGGER.error("Invalid database type! '" + PropertiesManager.getDatabaseType() + "'");
            System.exit(1);
        }

        try {
            storageManager = storageType.getStorageManager().getDeclaredConstructor().newInstance();
            storageManager.setupStorage();
            LOGGER.info("db has been setup");
        } catch (Exception e) {
            LOGGER.error("Unable to create database link!" + e);
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

        // Register JDA
        try {
            shardManager = DefaultShardManagerBuilder.create(
                    PropertiesManager.getToken(),
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_PRESENCES,
                            GatewayIntent.MESSAGE_CONTENT)
                    .setShardsTotal(Integer.parseInt(PropertiesManager.getTotalShards())) // Total number of shards
                    .setShards(Integer.parseInt(PropertiesManager.getShardsId()))
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.MESSAGE_CONTENT)
                    .enableCache(CacheFlag.ACTIVITY)
                    .enableCache(CacheFlag.ROLE_TAGS)
                    .setStatus(OnlineStatus.ONLINE)
                    .setActivity(Activity.playing("Booting..."))
                    .setEventManagerProvider((shardId) -> new SentryEventManager())
                    .addEventListeners(waiter,
                            client.build()
                    )
                    .build();
        } catch (IllegalArgumentException exception) {
            LOGGER.error("Failed to initialize JDA!" + exception);
            System.exit(1);
        }

        // Register listeners
        shardManager.addEventListener();

        MinecraftStatusUpdater statusUpdater = new MinecraftStatusUpdater();
        PlayerListUpdater playerListUpdater = new PlayerListUpdater();
        statusUpdater.startUpdateLoop();
        playerListUpdater.startUpdateLoop();

        generalThreadPool.scheduleAtFixedRate(() -> {
            shardManager.getShardById(0).getPresence().setActivity(Activity.playing("Currently monitoring: " + storageManager.getActiveServerCount() + " active servers."));
        }, 5, 60 * 5, TimeUnit.SECONDS);

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
            LoggerFactory.getLogger(theClass).debug("Loaded SlashCommand Successfully!");
        }

        return commands.toArray(new SlashCommand[0]);
    }

    public static ShardManager getShardManager() {
        return shardManager;
    }

    public static ScheduledExecutorService getGeneralThreadPool() {
        return generalThreadPool;
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static void shutdown() {
        Bot.LOGGER.info("Shutting down storage...");
        storageManager.closeStorage();
        Bot.LOGGER.info("Shutting down thread pool...");
        generalThreadPool.shutdown();
        Bot.LOGGER.info("Finished shutdown, exiting!");
        System.exit(0);
    }
}
