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
public class Text {
    private int mId;
    private int mVerseId;
    private int mVerse;
    private String mText;
    
    public static void create(String version) {
                try {
            Statement s = DatabaseUtils.connection.createStatement();
            s.execute("CREATE TABLE " + version
                    + "(id INT GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,"
                    + "verseId INT,"
                    + "verse INT,"
                    + "text CLOB(4 K))");

        } catch (SQLException ex) {
            if ("X0Y32".equals(ex.getSQLState())) {    // table already exists
                System.out.println(ex.getMessage() + " ErrorCode: " + ex.getErrorCode() + " state: " + ex.getSQLState());
            } else {
                Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public int insert(String version) {
        try {
            Statement stmt = DatabaseUtils.connection.createStatement();
            stmt.execute("INSERT INTO " + version + "(verse, text)"
                    + "VALUES(" + mVerseId + ", "
                    + mVerse + ", "
                    + "'" + mText.replace("'", "''").trim() + "')",
                    Statement.RETURN_GENERATED_KEYS);
            
            ResultSet rs = stmt.getGeneratedKeys();            
            rs.next();
            mId = rs.getInt(1);
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mVerse;
    }
    
    public int query(String version) {
        return mVerse;
    }
}
