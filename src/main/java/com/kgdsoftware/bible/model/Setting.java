/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kgdsoftware.bible.model;

import com.kgdsoftware.database.DatabaseUtils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hank
 */
public class Setting {
    // create setting table
    public static void create() {
        try {
            Statement s = DatabaseUtils.connection.createStatement();
            s.execute("CREATE TABLE setting("
                    + "id INT GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,"
                    + "name VARCHAR(16),"
                    + "value VARCHAR(64))");
            System.out.println("Setting.create");

        } catch (SQLException ex) {
            if ("X0Y32".equals(ex.getSQLState())) {    // table already exists
                //System.out.println(ex.getMessage() + " ErrorCode: " + ex.getErrorCode() + " state: " + ex.getSQLState());
            } else {
                Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static String getString(String name, String defaultValue) {
        String value = null;
        try {
            Statement stmt = DatabaseUtils.connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT value FROM setting WHERE name = '"
                    + name + "'");
            rs.next();
            value = rs.getString(1);
            rs.close();
            stmt.close();
            
            System.out.println("Setting.get " + name + " value: " + value);
        } catch (SQLException ex) {
            // The row does not exist, so create it.
            //Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Setting,get() Exception: " + ex.getMessage() + " status for new table " + name);
            insert(name, defaultValue);
            value = defaultValue;
            
            System.out.println("Setting.get " + name + " default value: " + value);

        }
        return value;
    }

    public static String getString(String name) {
        return getString(name, "");
    }
    
    public static int getInt(String name, int defaultValue) {
        String result = getString(name,Integer.toString(defaultValue));
        return Integer.parseInt(result);
    }
    
    public static void insert(String name, String value) {
        System.out.println("Setting.insert " + name + ": " + value);
        try {
            Statement stmt = DatabaseUtils.connection.createStatement();
            String sql = "INSERT INTO setting (name, value)"
                    + " VALUES('" + name + "', "
                    + "'" + value + "')";
            System.out.println("Setting.insert: " + sql);

            stmt.execute(sql, Statement.RETURN_GENERATED_KEYS);

            ResultSet rs = stmt.getGeneratedKeys();
            rs.next();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void update(String name, String value) {
        System.out.println("Setting.update " + name + ": " + value);
        int nrows = 0;
        try {
            Statement stmt;
            stmt = DatabaseUtils.connection.createStatement();
            String sql = "UPDATE setting SET value='" + value
                            + "' where name = '" + name + "'";
            nrows = stmt.executeUpdate(sql);        
        } catch (SQLException ex) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
