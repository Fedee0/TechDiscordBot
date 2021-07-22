package me.TechsCode.TechDiscordBot.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySQL {

    private final List<String> errorMessages;
    private final MySQLSettings mySQLSettings;

    private MySQL(MySQLSettings mySQLSettings) {
        this.errorMessages = new ArrayList<>();
        this.mySQLSettings = mySQLSettings;
    }

    public static MySQL of(MySQLSettings mySQLSettings) {
        return new MySQL(mySQLSettings);
    }

    public String update(String query) {
        try {
            Connection connection = getConnection();
            PreparedStatement p = connection.prepareStatement(query);
            p.execute();
            connection.close();
            p.close();

            return "Success";
        } catch(SQLException ex) {
            ex.printStackTrace();
            errorMessages.add(ex.getMessage());
            return ex.getMessage();
        }
    }

    public String update(String query, Object... objs) {
        try {
            Connection connection = getConnection();
            PreparedStatement p = connection.prepareStatement(query);

            int i = 1;
            for(Object obj : objs) {
                if(obj instanceof String) {
                    p.setString(i, (String) obj);
                } else if(obj instanceof Boolean) {
                    p.setBoolean(i, (Boolean)obj);
                } else {
                    p.setObject(i, obj);
                }

                i++;
            }

            p.execute();
            connection.close();
            p.close();

            return "Success";
        } catch(SQLException ex) {
            ex.printStackTrace();
            errorMessages.add(ex.getMessage());
            return ex.getMessage();
        }
    }

    public Connection getConnection() throws SQLException {
        String connectString = "jdbc:mysql://" + mySQLSettings.getHost() + ":" + mySQLSettings.getPort() + "/" + mySQLSettings.getDatabase() + "?useSSL=false&characterEncoding=UTF-8&useUnicode=true&serverTimezone=UTC";
        return DriverManager.getConnection(connectString, mySQLSettings.getUsername(), mySQLSettings.getPassword());
    }

    public String getLatestErrorMessage() {
        if(errorMessages.size() == 0) return "";
        return errorMessages.get(errorMessages.size() - 1);
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
}