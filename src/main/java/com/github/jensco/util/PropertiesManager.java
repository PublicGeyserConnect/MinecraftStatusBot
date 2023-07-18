package com.github.jensco.util;

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
     * @return ShardID
     */
    public static String getShardsId() {
        return properties.getProperty("shards-id");
    }

    /**
     * @return totalShards
     */
    public static String getTotalShards() {
        return properties.getProperty("total-shards");
    }

    /**
     * @return serverName
     */
    public static String getServerName() {
        return properties.getProperty("server-name");
    }

    public static String getEndpointUrl() {
        return properties.getProperty("endpoint-url");
    }

}