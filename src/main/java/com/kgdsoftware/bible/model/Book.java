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
import javax.swing.DefaultListModel;

/**
 *
 * @author henriwarren
 */
public class Book {

    public static final int ID_INDEX = 1;
    public static final int NAME_INDEX = 2;
    public static final int CHAPTERS_INDEX = 3;
    public static final int ABBR_INDEX = 4;
    public static final int TESTAMENTID_INDEX = 5;

    private int mId;
    private String mName;
    private int mChapters;
    private String mAbbreviation;
    private int mTestamentId;

    public static void create() {
        try {
            Statement s = DatabaseUtils.connection.createStatement();
            s.execute("CREATE TABLE Books(id INT GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,"
                    + "name VARCHAR(20),"
                    + "chapters INT,"
                    + "abbr VARCHAR(5),"
                    + "testamentId INT)");
            System.out.println("Book create");

        } catch (SQLException ex) {
            if ("X0Y32".equals(ex.getSQLState())) {    // table already exists
                //System.out.println(ex.getMessage() + " ErrorCode: " + ex.getErrorCode() + " state: " + ex.getSQLState());
            } else {
                Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static DefaultListModel<Book> query() {
        //System.out.println("query all");
        DefaultListModel<Book> blm = new DefaultListModel<>();

        try {
            Statement stmt = DatabaseUtils.connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Books");
            while (rs.next()) {
                blm.addElement(new Book(rs.getInt(ID_INDEX),
                        rs.getString(NAME_INDEX),
                        rs.getInt(CHAPTERS_INDEX),
                        rs.getString(ABBR_INDEX),
                        rs.getInt(TESTAMENTID_INDEX)));
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println("queried all " + blm.getSize());
        return blm;
    }

    public static Book query(int id) {
        Book book = null;
        try (Statement stmt = DatabaseUtils.connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM Books WHERE id=" + id)) {
            rs.next();
            book = new Book(rs.getInt(ID_INDEX),
                    rs.getString(NAME_INDEX),
                    rs.getInt(CHAPTERS_INDEX),
                    rs.getString(ABBR_INDEX),
                    rs.getInt(TESTAMENTID_INDEX));
        } catch (SQLException ex) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
        }
        return book;
    }

    public static int query(String name) {
        int id = 0;
        try {
            Statement stmt = DatabaseUtils.connection.createStatement();

            // remove the hypen
            String lookupName = name.replace("-", " ");
            ResultSet rs = stmt.executeQuery("SELECT * FROM Books WHERE name='" + lookupName + "'");
            rs.next();
            id = rs.getInt(ID_INDEX);
            System.out.println(lookupName + " has id " + id);
        } catch (SQLException ex) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;
    }

    public Book() {
        this(-1, "none", 0, "no", 0);
    }

    public Book(int id, String name, int chapters, String abbr, int testamentId) {
        mId = id;
        mName = name;
        mChapters = chapters;
        mAbbreviation = abbr;
        mTestamentId = testamentId;
    }

    public Book(Book book) {
        mId = book.getId();
        mName = book.getName();
        mChapters = book.getChapters();
        mAbbreviation = book.getAbbreviation();
        mTestamentId = book.getTestamentId();
    }
    
    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public int getChapters() {
        return mChapters;
    }

    public String getAbbreviation() {
        return mAbbreviation;
    }

    public int getTestamentId() {
        return mTestamentId;
    }

    public int insert() {
        try (Statement stmt = DatabaseUtils.connection.createStatement()) {
            String sql = "INSERT INTO Books (name, chapters, abbr, testamentId)"
                    + "VALUES('"
                    + mName + "', "
                    + mChapters + ", "
                    + "'" + mAbbreviation + "', "
                    + mTestamentId + ")";
            
            stmt.execute(sql, Statement.RETURN_GENERATED_KEYS);
            
            System.out.println("sql: " + sql);

            ResultSet rs = stmt.getGeneratedKeys();
            rs.next();
            mId = rs.getInt(1);
        } catch (SQLException ex) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mId;
    }

    @Override
    public String toString() {
        return "[" + mName + " id: "
                + mId + " chapters: "
                + mChapters + " abbr: "
                + mAbbreviation + " testamentId: "
                + mTestamentId + "]";
    }

}
