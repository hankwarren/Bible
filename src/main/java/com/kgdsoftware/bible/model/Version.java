package com.kgdsoftware.bible.model;

import com.kgdsoftware.database.DatabaseUtils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author henriwarren
 */
public class Version {
    public static final int COMPLETE = 1;
    public static final int INCOMPLETE = 2;

    private int mVersionId;
    private boolean mComplete;
    private String mName;
    private String mAbbr;

    public static Version newInstance(int versionId, boolean completed, String name, String abbr) {
        return new Version(versionId, completed, name, abbr);
    }

    public static int numberOfVersions() {
        int count = 0;
        try (Statement stmt = DatabaseUtils.connection.createStatement()) {
            String sql = "SELECT count(*) FROM Version";
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            count = rs.getInt(1);
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
        }
        return count;
    }

    public static Version get(int id) {
        Version version = null;

        try (Statement stmt = DatabaseUtils.connection.createStatement()) {
            String sql = "SELECT * FROM Version wHERE versionId=" + id;
            
            System.out.println("Version.get(" + id + "): " + sql);
            
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            version = newInstance(rs.getInt(1),
                    rs.getInt(2) == COMPLETE,
                    rs.getString(3),
                    rs.getString(4));
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            //Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
        }

        return version;
    }

    public static Version get(String abbr) {
        Version version = null;

        try (Statement stmt = DatabaseUtils.connection.createStatement()) {
            String sql = "SELECT * FROM Version wHERE abbr=" + abbr;
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            version = newInstance(rs.getInt(1),
                    rs.getInt(2) == COMPLETE,
                    rs.getString(3),
                    rs.getString(4));
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
        }

        return version;

    }

    public static int getNext(int versionId) {
        int nextVersionId = versionId + 1;
        Version version = get(nextVersionId);
        if (version == null) {
            nextVersionId = 1;
        }
        return nextVersionId;
    }

//    public static Version newInstance(int versionId, boolean complete, String name, String abbr) {
//        return new Version(versionId, complete, name, abbr);
//    }
    private Version(int versionId, boolean complete, String name, String abbr) {
        mVersionId = versionId;
        mComplete = complete;
        mName = name;
        mAbbr = abbr;
    }

    public String getName() {
        return mName;
    }

    public String getAbbreviation() {
        return mAbbr;
    }

    public int getVersionId() {
        return mVersionId;
    }

    public boolean isComplete() {
        return mComplete;
    }

    public static void create() {
        try {
            Statement s = DatabaseUtils.connection.createStatement();
            s.execute("CREATE TABLE Version("
                    + "versionId INT GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,"
                    + "complete INT,"
                    + "name VARCHAR(20),"
                    + "abbr VARCHAR(8))");
            System.out.println("Version create");
        } catch (SQLException ex) {
            if ("X0Y32".equals(ex.getSQLState())) {    // table already exists
                //System.out.println(ex.getMessage() + " ErrorCode: " + ex.getErrorCode() + " state: " + ex.getSQLState());
            } else {
                Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }


    public int insert() {
        try {
            Statement stmt = DatabaseUtils.connection.createStatement();
            stmt.execute("INSERT INTO Version(complete, name, abbr)"
                    + "VALUES(" + (mComplete ? COMPLETE : INCOMPLETE)
                    + ", '" + mName + "' "
                    + ", '" + mAbbr + "')",
                    Statement.RETURN_GENERATED_KEYS);

            ResultSet rs = stmt.getGeneratedKeys();
            rs.next();
            mVersionId = rs.getInt(1);
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mVersionId;
    }

    public int update(boolean complete) {
        mComplete = complete;

        int nrows = 0;
        try {
            Statement stmt;
            stmt = DatabaseUtils.connection.createStatement();
            nrows = stmt.executeUpdate("UPDATE Version SET"
                    + " complete = " + (mComplete ? COMPLETE : INCOMPLETE)
                    + " WHERE versionId =" + mVersionId);
        } catch (SQLException ex) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nrows;
    }

    public String toString() {
        return "[Version " + mVersionId
                + " complete: " + mComplete
                + " name: " + mName
                + " abbr: " + mAbbr + "]";
    }
}
