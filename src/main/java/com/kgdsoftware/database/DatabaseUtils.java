/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kgdsoftware.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author henriwarren
 */
public class DatabaseUtils {
    public static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    public static final String PROTOCOL = "jdbc:derby:";
    
    public static Connection connection = null;
    
    public static void connect(String database) {
        try {
            Class.forName(DRIVER).newInstance();

            Properties props = new Properties();

            if(connection == null) {
                connection = DriverManager.getConnection(PROTOCOL + database + ";create=true", props);
                connection.setAutoCommit(false);
            }
        } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(DatabaseUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void disconnect() {
        try {
            connection.commit();
            connection.close();
            connection = null;
        } catch (SQLException ex) {
            System.err.println("Got a SQLException");
        };
        
        try {
            DriverManager.getConnection(PROTOCOL + ";shutdown=true");
        } catch (SQLException ex) {
            System.out.println("Error code: " + ex.getErrorCode() + " SQL state: " + ex.getSQLState());
            if (ex.getErrorCode() == 50000
                    && ("XJ015".equals(ex.getSQLState()))) {
                System.out.println("Derby shut down normally");
            } else {
                System.out.println("Derby did NOT shut down normally");
                Logger.getLogger(DatabaseUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }          
}
