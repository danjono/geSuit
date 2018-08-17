package net.cubespace.geSuit.database;

import au.com.addstar.dripreporter.DripReporterApi;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.geSuit.configs.SubConfig.Database;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ConfigManager;
import net.md_5.bungee.api.plugin.Plugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ConnectionPool {
    private HikariConfig dbConfig = new HikariConfig();
    private HikariDataSource dataSource;
    private Map<String, String> statementCache = new HashMap<>();
    private ArrayList<IRepository> repositories = new ArrayList<>();

    public void addRepository(IRepository repository) {
        repositories.add(repository);
    }

    public boolean initialiseConnections(Database database) {
        if (configureDataSource(database)) {
            if (!ConfigManager.main.Inited) {
                for (IRepository repository : repositories) {
                    String[] tableInformation = repository.getTable();

                    if (!doesTableExist(tableInformation[0])) {
                        try {
                            standardQuery("CREATE TABLE IF NOT EXISTS `" + tableInformation[0] + "` (" + tableInformation[1] + ");");
                        } catch (SQLException e) {
                            e.printStackTrace();
                            geSuit.instance.getLogger().severe("Could not create Table " + tableInformation[0].substring(0, 20) + " ...");
                            throw new IllegalStateException(e.getMessage());
                        }
                    }
                }

                ConfigManager.main.Inited = true;
                try {
                    ConfigManager.main.save();
                } catch (InvalidConfigurationException ignored) {

                }
            } else {
                for (IRepository repository : repositories) {
                    repository.checkUpdate();
                }
            }
        }

        return true;
    }

    protected boolean configureDataSource(Database database) {
        dbConfig.setJdbcUrl("jdbc:mysql://" + database.Host + ":" + database.Port + "/" + database.Database + "?useSSL=" + database.useSSL);
        dbConfig.setUsername(database.Username);
        dbConfig.setPassword(database.Password);
        if (database.useMetrics) enableMetrics();
        setDataSourceDefaults(database);
        dbConfig.setPoolName("geSuit-Pool");
        dataSource = new HikariDataSource(dbConfig);
        for (IRepository rep : repositories) {
            rep.registerPreparedStatements(this);
        }
        return true;
    }

    private void enableMetrics() {
        geSuit.instance.getLogger().log(Level.INFO, "geSuit is adding metrics....");
        try {
            Plugin p = geSuit.instance.getProxy().getPluginManager().getPlugin("DripCordReporter");
            dbConfig.setMetricRegistry(((DripReporterApi) p).getRegistry());
            dbConfig.setHealthCheckRegistry(((DripReporterApi) p).getHealthRegistry());
        } catch (Exception e) {
            geSuit.instance.getLogger().log(Level.WARNING, "geSuit could not add Metrics to the Database Source");
            e.printStackTrace();
        }
    }
    
    private void setDataSourceDefaults(Database database) {
        dbConfig.addDataSourceProperty("cachePrepStmts", true);
        dbConfig.addDataSourceProperty("prepStmtCacheSize", 250);
        dbConfig.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        dbConfig.addDataSourceProperty("useServerPrepStmts", true);
        dbConfig.addDataSourceProperty("useLocalSessionState", true);
        dbConfig.addDataSourceProperty("rewriteBatchedStatements", true);
        dbConfig.addDataSourceProperty("elideSetAutoCommits", true);
        dbConfig.addDataSourceProperty("maintainTimeStats", false);
        dbConfig.addDataSourceProperty("maximumPoolSize", 20);
        dbConfig.setInitializationFailTimeout(10000);
        dbConfig.setValidationTimeout(15000);
        //this allows the config to override the hardset defaults...but ensure those defaults are
        // set if non else are.
        for (Map.Entry<String, Object> property : database.properties.entrySet()) {
            dbConfig.addDataSourceProperty(property.getKey(), property.getValue());
        }
    }
    
    public String getSQL(String name) {
        return statementCache.getOrDefault(name, null);
    }
    
    public void addSQL(String name, String sql) {
        statementCache.put(name, sql);
    }
    
    public PreparedStatement prepareSQL(String name) {
        try {
            return dataSource.getConnection().prepareStatement(getSQL(name
            ));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public PreparedStatement getPreparedStatement(String name) {
        return prepareSQL(name);
    }

    public void addPreparedStatement(String name, String sql, int mode) {
        Connection connection = getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement(sql, mode);
            connection.close();
            statement.close();
            statementCache.put(name, sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addPreparedStatement(String name, String sql) {
        Connection connection = getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.close();
            statementCache.put(name, sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * Adds the given column to the given table if the column does not yet exist
     * The new column will be VARCHAR(length) NULL
     */
    public void AddStringColumnIfMissing(String table, String column, int length) {

        Boolean columnExists = doesTableHaveColumn(table, column);
        if (!columnExists) {
            addStringColumnToTable(table, column, length);
        }
    }

    private void addStringColumnToTable(String table, String column, int length) {

        try {
            geSuit.instance.getLogger().info("Adding column " + column + " to table " + table);

            String query = "ALTER TABLE `" + table + "` ADD `" + column + "` varchar(" + length + ") NULL";

            Statement statement = dataSource.getConnection().createStatement();
            statement.executeUpdate(query);
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
            geSuit.instance.getLogger().severe("Could not add column " + column + " to table " + table);
            throw new IllegalStateException();
        }
    }

    private void standardQuery(String query) throws SQLException {
        Statement statement = dataSource.getConnection().createStatement();
        statement.executeUpdate(query);
        statement.close();
    }

    private boolean doesTableExist(String table) {
        return checkTable(table);
    }

    private Boolean doesTableHaveColumn(String table, String column) {

        try {

            String query =
                    "SELECT COUNT(*) as RowCount FROM information_schema.columns " +
                            "WHERE TABLE_SCHEMA = database() AND " +
                            "TABLE_NAME = '" + table + "' AND " +
                            "COLUMN_NAME = '" + column + "';";

            Statement statement = dataSource.getConnection().createStatement();
            ResultSet res = statement.executeQuery(query);
            while (res.next()) {
                int colCount = res.getInt("RowCount");
                if (colCount > 0)
                    return true;
            }
            statement.close();
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            geSuit.instance.getLogger().severe("Could not validate column " + column + " on table " + table);
            throw new IllegalStateException();
        }
    }

    private boolean checkTable(String table) {
        DatabaseMetaData dbm;
        try {
            dbm = dataSource.getConnection().getMetaData();
        } catch (SQLException e2) {
            e2.printStackTrace();
            return false;
        }

        ResultSet tables;
        try {
            tables = dbm.getTables(null, null, table, null);
        } catch (SQLException e1) {
            e1.printStackTrace();
            return false;
        }

        boolean check;
        try {
            check = tables.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return check;
    }
}
