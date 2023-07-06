package com.github.jensco.storage;

public enum StorageType {
    SQLITE("sqlite", SqliteStorageManager.class),
    MYSQL("mysql", MySQLStorageManager.class),
    UNKNOWN("unknown", AbstractStorageManager.class);

    private final String name;

    private final Class<? extends AbstractStorageManager> storageManager;

    StorageType(String name, Class<? extends AbstractStorageManager> storageManager) {
        this.name = name;
        this.storageManager = storageManager;
    }

    public static final StorageType[] VALUES = values();

    /**
     * Convert the StorageType string (from properties) to the enum, UNKNOWN on fail
     *
     * @param name StorageType string
     *
     * @return The converted StorageType
     */
    public static StorageType getByName(String name) {
        String upperCase = name.toUpperCase();
        for (StorageType type : VALUES) {
            if (type.name().equals(upperCase)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public String getName() {
        return name;
    }

    public Class<? extends AbstractStorageManager> getStorageManager() {
        return storageManager;
    }
}