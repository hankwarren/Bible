package com.kgdsoftware.bible.model;

import com.kgdsoftware.database.DatabaseUtils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author henriwarren
 */
public class Verse {
    private int mId;
    private int mVerseId;
    private int mVerse;
    private String mText;

    public static void create(String version) {
        try {
            Statement s = DatabaseUtils.connection.createStatement();
            s.execute("CREATE TABLE " + version + " ("
                    + "id INT GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,"
                    + "verseId INT,"
                    + "verse INT,"
                    + "text CLOB(32 K))");

        } catch (SQLException ex) {
            if ("X0Y32".equals(ex.getSQLState())) {    // table already exists
                System.out.println(ex.getMessage() + " ErrorCode: " + ex.getErrorCode() + " state: " + ex.getSQLState());
            } else {
                Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public String toString() {
        return "[Verse " + mId
                + " verseId: " + mVerseId + "\n "
                + " verse: " + mVerse + "\n "
                + mText + "]";
    }

    public static List<Verse> query(String versionAbbreviation, Chapter chapter) {
        System.out.println("List<Verse> query " + chapter.toString());

        List<Verse> list = new LinkedList<>();
        try {
            Statement stmt = DatabaseUtils.connection.createStatement();
            String sql = "SELECT * FROM " + versionAbbreviation
                    + " WHERE verseId>=" + chapter.getVerseId()
                    + " AND verseId <" + (chapter.getVerseId() + chapter.getVerses());

            System.out.println("query " + sql);
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(new Verse(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getString(4)));
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("queried all " + list.size());
        return list;
    }

    public Verse(int id, int verseId, int verse, String text) {
        mId = id;
        mVerseId = verseId;
        mVerse = verse;
        mText = text;
    }

    public int getId() {
        return mId;
    }

    public int getVerse() {
        return mVerse;
    }

    public Verse setText(String text) {
        mText = text;
        return this;
    }

    public String getText() {
        return mText;
    }

    public int insert(String versionAbbreviation) {
        try {
            Statement stmt = DatabaseUtils.connection.createStatement();
            String sql = "INSERT INTO " + versionAbbreviation
                    + " (verseId, verse, text) "
                    + "VALUES(" + mVerseId + "," + mVerse + ",'" + mText + "')";
            //System.out.println("Verse.insert: " + sql);
            stmt.execute(sql, Statement.RETURN_GENERATED_KEYS);

            ResultSet rs = stmt.getGeneratedKeys();
            rs.next();
            mId = rs.getInt(1);
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mId;
    }

    public int update(String versionAbbreviation) {
        int nrows = 0;
        try {
            Statement stmt;
            stmt = DatabaseUtils.connection.createStatement();
            String sql = "UPDATE " + versionAbbreviation
                    + " SET text='" + mText + "'"
                    + " WHERE verseId =" + mVerseId;
            nrows = stmt.executeUpdate(sql);

        } catch (SQLException ex) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nrows;
    }
}
