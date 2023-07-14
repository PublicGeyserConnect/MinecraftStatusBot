package com.github.jensco.storage;

import com.github.jensco.records.NotificationRecord;
import com.github.jensco.records.PlayerListDataRecord;
import com.github.jensco.records.RconRecord;
import com.github.jensco.records.ServerDataRecord;
import com.github.jensco.util.PropertiesManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteStorageManager extends AbstractStorageManager {

    protected Connection connection;

    @Override
    public void setupStorage() throws Exception {
        Class.forName("org.sqlite.JDBC");

        connection = DriverManager.getConnection("jdbc:sqlite:" + PropertiesManager.getDatabase());
        String createTableQuery = "CREATE TABLE IF NOT EXISTS servers (" +
                "guildid VARCHAR(255) NOT NULL, " +
                "servername VARCHAR(255) NOT NULL, " +
                "serveraddress VARCHAR(255) NOT NULL, " +
                "serverport INT NOT NULL, " +
                "active BOOLEAN NOT NULL, " +
                "channelid VARCHAR(255), " +
                "messageid VARCHAR(255), " +
                "favicon VARCHAR(255), " +
                "PRIMARY KEY (guildid, servername)" +
                ")";

        String createNotificationTableQuery = "CREATE TABLE IF NOT EXISTS notification (" +
                "guildid VARCHAR(255) NOT NULL, " +
                "role VARCHAR(255) NOT NULL, " +
                "active BOOLEAN NOT NULL, " +
                "PRIMARY KEY (guildid)" +
                ")";

        String createPlayerListTableQuery = "CREATE TABLE IF NOT EXISTS playerlist (" +
                "guildid VARCHAR(255) NOT NULL, " +
                "servername VARCHAR(255) NOT NULL, " +
                "messageid VARCHAR(255) NOT NULL, " +
                "channelid VARCHAR(255) NOT NULL, " +
                "active BOOLEAN NOT NULL, " +
                "PRIMARY KEY (guildid, servername)" +
                ")";

        String createRconTableQuery = "CREATE TABLE IF NOT EXISTS rcon (" +
                "guildid VARCHAR(255) NOT NULL, " +
                "servername VARCHAR(255) NOT NULL, " +
                "serveraddress VARCHAR(255) NOT NULL, " +
                "rconport INT NOT NULL, " +
                "channelid VARCHAR(255) NOT NULL, " +
                "PRIMARY KEY (guildid, servername)" +
                ")";

        Statement createTables = connection.createStatement();
        createTables.executeUpdate(createTableQuery);
        createTables.executeUpdate(createNotificationTableQuery);
        createTables.executeUpdate(createPlayerListTableQuery);
        createTables.executeUpdate(createRconTableQuery);
        createTables.close();
    }

    @Override
    public void closeStorage() {
    }

    @Override
    public void addServer(String guildId, String serverName, String serverAddress, int serverPort, String favicon) {
        try {
            // Insert the server information
            PreparedStatement insertStatement = connection.prepareStatement(
                    "INSERT INTO servers (guildid, servername, serveraddress, serverport, active, favicon) VALUES (?, ?, ?, ?, ?, ?)"
            );
            insertStatement.setString(1, guildId);
            insertStatement.setString(2, serverName);
            insertStatement.setString(3, serverAddress);
            insertStatement.setInt(4, serverPort);
            insertStatement.setBoolean(5, false);
            insertStatement.setString(6, favicon);

            insertStatement.executeUpdate();
            insertStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean removeServer(String guildId, String serverName) {
        try {
            PreparedStatement deleteStatement = connection.prepareStatement(
                    "DELETE FROM servers WHERE guildid = ? AND servername = ?"
            );
            deleteStatement.setString(1, guildId);
            deleteStatement.setString(2, serverName);

            int rowsAffected = deleteStatement.executeUpdate();
            deleteStatement.close();

            if (rowsAffected > 0) {
                return true;  // Server was successfully removed
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;  // Server was not present or an error occurred
    }

    @Override
    public List<String> getAllServerNamesByGuildId(String guildId) {
        List<String> serverNames = new ArrayList<>();
        try {
            PreparedStatement selectStatement = connection.prepareStatement(
                    "SELECT servername FROM servers WHERE guildid = ?"
            );
            selectStatement.setString(1, guildId);
            ResultSet resultSet = selectStatement.executeQuery();

            while (resultSet.next()) {
                String serverName = resultSet.getString("servername");
                serverNames.add(serverName);
            }

            resultSet.close();
            selectStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return serverNames;
    }

    @Override
    public ServerDataRecord getServerInfo(String serverName, String guildId) {
        ServerDataRecord serverDataRecord = null;
        try {
            PreparedStatement selectStatement = connection.prepareStatement(
                    "SELECT guildid, servername, serveraddress, serverport, active, messageid, channelid, favicon FROM servers WHERE servername = ? AND guildid = ?"
            );
            selectStatement.setString(1, serverName);
            selectStatement.setString(2, guildId);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                String name = resultSet.getString("servername");
                String address = resultSet.getString("serveraddress");
                int port = resultSet.getInt("serverport");
                boolean active = resultSet.getBoolean("active");
                String messageID = resultSet.getString("messageid");
                String channelId = resultSet.getString("channelid");
                String favicon = resultSet.getString("favicon");

                serverDataRecord = new ServerDataRecord(guildId, name, address, port, active, messageID, channelId, favicon);
            }

            resultSet.close();
            selectStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return serverDataRecord;
    }

    @Override
    public void setServerActiveStatus(String guildId, String serverName, boolean active) {
        try {
            PreparedStatement updateStatement = connection.prepareStatement(
                    "UPDATE servers SET active = ? WHERE guildid = ? AND servername = ?"
            );
            updateStatement.setBoolean(1, active);
            updateStatement.setString(2, guildId);
            updateStatement.setString(3, serverName);

            updateStatement.executeUpdate();
            updateStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setServerMessageAndChannelId(String guildId, String serverName, String messageId, String channelId, boolean active) {
        try {
            PreparedStatement updateStatement = connection.prepareStatement(
                    "UPDATE servers SET messageid = ?, channelid = ?, active = ? WHERE guildid = ? AND servername = ?"
            );
            updateStatement.setString(1, messageId);
            updateStatement.setString(2, channelId);
            updateStatement.setBoolean(3, active);
            updateStatement.setString(4, guildId);
            updateStatement.setString(5, serverName);

            updateStatement.executeUpdate();
            updateStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<ServerDataRecord> getAllActiveServers() {
        List<ServerDataRecord> activeServers = new ArrayList<>();

        try {
            PreparedStatement selectStatement = connection.prepareStatement(
                    "SELECT guildid, servername, serveraddress, serverport, active, messageid, channelid, favicon FROM servers WHERE active = ?"
            );
            selectStatement.setBoolean(1, true);
            ResultSet resultSet = selectStatement.executeQuery();

            while (resultSet.next()) {
                String guildId = resultSet.getString("guildid");
                String serverName = resultSet.getString("servername");
                String serverAddress = resultSet.getString("serveraddress");
                int serverPort = resultSet.getInt("serverport");
                boolean active = resultSet.getBoolean("active");
                String messageID = resultSet.getString("messageid");
                String channelId = resultSet.getString("channelid");
                String favicon = resultSet.getString("favicon");

                ServerDataRecord serverDataRecord = new ServerDataRecord(guildId, serverName, serverAddress, serverPort, active, messageID, channelId, favicon);
                activeServers.add(serverDataRecord);
            }

            resultSet.close();
            selectStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return activeServers;
    }

    @Override
    public void updateServerFavicon(String guildId, String serverName, String favicon) {
        try {
            PreparedStatement updateStatement = connection.prepareStatement(
                    "UPDATE servers SET favicon = ? WHERE guildid = ? AND servername = ?"
            );
            updateStatement.setString(1, favicon);
            updateStatement.setString(2, guildId);
            updateStatement.setString(3, serverName);

            updateStatement.executeUpdate();
            updateStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setNotifyRole(String guildId, String roleId) {
        try {
            PreparedStatement insertStatement = connection.prepareStatement(
                    "INSERT INTO notification (guildid, role, active) VALUES (?, ?, ?)"
            );
            insertStatement.setString(1, guildId);
            insertStatement.setString(2, roleId);
            insertStatement.setBoolean(3, false);

            insertStatement.executeUpdate();
            insertStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void activeNotify(String guildId, boolean active) {
        try {
            PreparedStatement updateStatement = connection.prepareStatement(
                    "UPDATE notification SET active = ? WHERE guildid = ?"
            );
            updateStatement.setBoolean(1, active);
            updateStatement.setString(2, guildId);

            updateStatement.executeUpdate();
            updateStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeNotifyRole(String guildId) {
        try {
            PreparedStatement deleteStatement = connection.prepareStatement(
                    "DELETE FROM notification WHERE guildid = ?"
            );
            deleteStatement.setString(1, guildId);

            deleteStatement.executeUpdate();
            deleteStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public NotificationRecord getNotifiedData(String guildId) {
        NotificationRecord notifiedData = null;

        try {
            PreparedStatement selectStatement = connection.prepareStatement(
                    "SELECT guildid, role, active FROM notification WHERE guildid = ?"
            );
            selectStatement.setString(1, guildId);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                String notifiedGuildId = resultSet.getString("guildid");
                String role = resultSet.getString("role");
                boolean active = resultSet.getBoolean("active");

                notifiedData = new NotificationRecord(notifiedGuildId, role, active);
            }

            resultSet.close();
            selectStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return notifiedData;
    }

    @Override
    public int getActiveServerCount() {
        int count = 0;

        try {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) AS total FROM servers WHERE active = ?"
            );
            statement.setBoolean(1, true);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                count = resultSet.getInt("total");
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }

    @Override
    public int getUniqueGuildCount() {
        int uniqueGuildCount = 0;
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(DISTINCT guildid) AS count FROM servers");
            if (resultSet.next()) {
                uniqueGuildCount = resultSet.getInt("count");
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return uniqueGuildCount;
    }

    @Override
    public void addPlayerList(String guildId, String serverName, String messageId, String channelId, boolean active) {
        try {
            // Insert the player list information
            PreparedStatement insertStatement = connection.prepareStatement(
                    "INSERT INTO playerlist (guildid, servername, messageid, channelid, active) VALUES (?, ?, ?, ?, ?)"
            );
            insertStatement.setString(1, guildId);
            insertStatement.setString(2, serverName);
            insertStatement.setString(3, messageId);
            insertStatement.setString(4, channelId);
            insertStatement.setBoolean(5, active);

            insertStatement.executeUpdate();
            insertStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean removePlayerList(String guildId, String serverName) {
        try {
            PreparedStatement deleteStatement = connection.prepareStatement(
                    "DELETE FROM playerlist WHERE guildid = ? AND servername = ?"
            );
            deleteStatement.setString(1, guildId);
            deleteStatement.setString(2, serverName);

            deleteStatement.executeUpdate();
            deleteStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public PlayerListDataRecord getPlayerListData(String guildId, String serverName) {
        PlayerListDataRecord playerListDataRecord = null;
        try {
            PreparedStatement selectStatement = connection.prepareStatement(
                    "SELECT guildid, servername, messageid, channelid, active FROM playerlist WHERE servername = ? AND guildid = ?"
            );
            selectStatement.setString(1, serverName);
            selectStatement.setString(2, guildId);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                String retrievedGuildId = resultSet.getString("guildid");
                String retrievedServerName = resultSet.getString("servername");
                String messageId = resultSet.getString("messageid");
                String channelId = resultSet.getString("channelid");
                boolean active = resultSet.getBoolean("active");

                playerListDataRecord = new PlayerListDataRecord(retrievedGuildId, retrievedServerName, messageId, channelId, active);
            }

            resultSet.close();
            selectStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playerListDataRecord;
    }

    @Override
    public List<PlayerListDataRecord> getAllActivePlayers() {
        List<PlayerListDataRecord> activePlayers = new ArrayList<>();

        try {
            PreparedStatement selectStatement = connection.prepareStatement(
                    "SELECT guildid, servername, messageid, channelid, active FROM playerlist WHERE active = ?"
            );
            selectStatement.setBoolean(1, true);
            ResultSet resultSet = selectStatement.executeQuery();

            while (resultSet.next()) {
                String guildId = resultSet.getString("guildid");
                String serverName = resultSet.getString("servername");
                String messageId = resultSet.getString("messageid");
                String channelId = resultSet.getString("channelid");
                boolean active = resultSet.getBoolean("active");

                PlayerListDataRecord playerDataRecord = new PlayerListDataRecord(guildId, serverName, messageId, channelId, active);
                activePlayers.add(playerDataRecord);
            }

            resultSet.close();
            selectStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return activePlayers;
    }

    @Override
    public boolean removePlayerListByMessageId(String guildId, String messageId) {
        try {
            PreparedStatement deleteStatement = connection.prepareStatement(
                    "DELETE FROM playerlist WHERE guildid = ? AND messageid = ?"
            );
            deleteStatement.setString(1, guildId);
            deleteStatement.setString(2, messageId);

            int rowsAffected = deleteStatement.executeUpdate();
            deleteStatement.close();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deactivateServerByMessageId(String guildId, String messageId) {
        try {
            PreparedStatement updateStatement = connection.prepareStatement(
                    "UPDATE servers SET active = ? WHERE guildid = ? AND messageid = ?"
            );
            updateStatement.setBoolean(1, false);
            updateStatement.setString(2, guildId);
            updateStatement.setString(3, messageId);

            int rowsAffected = updateStatement.executeUpdate();
            updateStatement.close();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void setRcon(String guildId, String serverName, String serverAddress, int rconPort, String channelId) {
        try {
            // Insert the RCON information
            PreparedStatement insertStatement = connection.prepareStatement(
                    "INSERT INTO rcon (guildid, servername, serveraddress, rconport, channelid) VALUES (?, ?, ?, ?, ?)"
            );
            insertStatement.setString(1, guildId);
            insertStatement.setString(2, serverName);
            insertStatement.setString(3, serverAddress);
            insertStatement.setInt(4, rconPort);
            insertStatement.setString(5, channelId);

            insertStatement.executeUpdate();
            insertStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public RconRecord getRconData(String guildId, String serverName) {
        RconRecord rconData = null;
        try {
            PreparedStatement selectStatement = connection.prepareStatement(
                    "SELECT guildid, servername, serveraddress, rconport, channelid FROM rcon WHERE guildid = ? AND servername = ?"
            );
            selectStatement.setString(1, guildId);
            selectStatement.setString(2, serverName);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                String retrievedGuildId = resultSet.getString("guildid");
                String retrievedServerName = resultSet.getString("servername");
                String serverAddress = resultSet.getString("serveraddress");
                int rconPort = resultSet.getInt("rconport");
                String channelId = resultSet.getString("channelid");

                rconData = new RconRecord(retrievedGuildId, retrievedServerName, rconPort, serverAddress, channelId);
            }

            resultSet.close();
            selectStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rconData;
    }

    @Override
    public RconRecord getRconDataByGuildAndChannelId(String guildId, String channelId) {
        RconRecord rconData = null;
        try {
            PreparedStatement selectStatement = connection.prepareStatement(
                    "SELECT guildid, servername, serveraddress, rconport, channelid FROM rcon WHERE guildid = ? AND channelid = ?"
            );
            selectStatement.setString(1, guildId);
            selectStatement.setString(2, channelId);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                String retrievedGuildId = resultSet.getString("guildid");
                String serverName = resultSet.getString("servername");
                String serverAddress = resultSet.getString("serveraddress");
                int rconPort = resultSet.getInt("rconport");
                String retrievedChannelId = resultSet.getString("channelid");

                rconData = new RconRecord(retrievedGuildId, serverName, rconPort, serverAddress, retrievedChannelId);
            }

            resultSet.close();
            selectStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rconData;
    }

    @Override
    public boolean isChannelIdPresent(String channelId) {
        try {
            PreparedStatement selectStatement = connection.prepareStatement(
                    "SELECT COUNT(*) AS count FROM rcon WHERE channelid = ?"
            );
            selectStatement.setString(1, channelId);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt("count");
                return count > 0;
            }

            resultSet.close();
            selectStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void removeRconSettings(String guildId, String serverName) {
        try {
            PreparedStatement deleteStatement = connection.prepareStatement(
                    "DELETE FROM rcon WHERE guildid = ? AND servername = ?"
            );
            deleteStatement.setString(1, guildId);
            deleteStatement.setString(2, serverName);

            deleteStatement.executeUpdate();
            deleteStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}