package com.github.jensco.util;

import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.Properties;

public class PropertiesManager {
    private static Properties properties;

    public static void loadProperties(Properties config) {
        properties = config;
    }

    /**
     * @return Bot token from discord
     */
    public static String getToken() {
        return properties.getProperty("token");
    }

    /**
     * @return Database connection type
     */
    public static String getDatabaseType() {
        return properties.getProperty("db-type");
    }

    /**
     * @return Database server hostname
     */
    public static String getHost() {
        return properties.getProperty("db-host");
    }

    /**
     * @return Database server database
     */
    public static String getDatabase() {
        return properties.getProperty("db-database");
    }

    /**
     * @return Database server database
     */
    public static String getUser() {
        return properties.getProperty("db-user");
    }

    /**
     * @return Database server database
     */
    public static String getPass() {
        return properties.getProperty("db-pass");
    }

    /**
     * @return Sentry DSN
     */
    public static String getSentryDsn() {
        return properties.getProperty("sentry-dsn");
    }

    /**
     * @return Sentry environment
     */
    public static String getSentryEnv() {
        return properties.getProperty("sentry-env");
    }
}