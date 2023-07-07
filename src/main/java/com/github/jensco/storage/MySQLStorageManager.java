package com.github.jensco.storage;

import com.github.jensco.records.ServerRecord;
import com.github.jensco.records.NotificationRecord;
import com.github.jensco.util.PropertiesManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLStorageManager extends AbstractStorageManager {

    protected Connection connection;

    @Override
    public void setupStorage() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");

        connection = DriverManager.getConnection("jdbc:mysql://" + PropertiesManager.getHost() + "/" + PropertiesManager.getDatabase(), PropertiesManager.getUser(), PropertiesManager.getPass());
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

        Statement createTables = connection.createStatement();
        createTables.executeUpdate(createTableQuery);
        createTables.executeUpdate(createNotificationTableQuery);
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
                    "INSERT INTO servers (guildid, servername, serveraddress, serverport, active, favicon, activeplayerlist) VALUES (?, ?, ?, ?, ?, ?, ?)"
            );
            insertStatement.setString(1, guildId);
            insertStatement.setString(2, serverName);
            insertStatement.setString(3, serverAddress);
            insertStatement.setInt(4, serverPort);
            insertStatement.setBoolean(5, false);
            insertStatement.setString(6, favicon);
            insertStatement.setBoolean(7, false);

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
    public ServerRecord getServerInfoByServerName(String serverName, String guildId) {
        ServerRecord serverRecord = null;
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

                serverRecord = new ServerRecord(guildId, name, address, port, active, messageID, channelId, favicon);
            }

            resultSet.close();
            selectStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return serverRecord;
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
    public List<ServerRecord> getAllActiveServers() {
        List<ServerRecord> activeServers = new ArrayList<>();

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

                ServerRecord serverRecord = new ServerRecord(guildId, serverName, serverAddress, serverPort, active, messageID, channelId, favicon);
                activeServers.add(serverRecord);
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
    public NotificationRecord getNotifiedDataByGuildId(String guildId) {
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
}