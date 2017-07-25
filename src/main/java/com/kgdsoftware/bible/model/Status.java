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
 * @author henriwarren
 */
public class Status {           
    private int mId;
    private String mName;
    private String mStatus;
    private int mResult;
    
    public static final String GETTING = "GETTING";
    public static final String GOTTEN = "GOTTEN";

    public static void create() {
        
        try {
            Statement s = DatabaseUtils.connection.createStatement();
            s.execute("CREATE TABLE status("
                    + "id INT GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,"
                    + "name VARCHAR(20),"
                    + "status VARCHAR(8),"
                    + "result INT)");
        System.out.println("Status.create");

        } catch (SQLException ex) {
            if ("X0Y32".equals(ex.getSQLState())) {    // table already exists
                //System.out.println(ex.getMessage() + " ErrorCode: " + ex.getErrorCode() + " state: " + ex.getSQLState());
            } else {
                Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
 
//    public static List<Status> query() {
//        System.out.println("query all");
//        List<Status> list = new ArrayList<>();
//        try {
//            Statement stmt = DatabaseUtils.connection.createStatement();
//            ResultSet rs = stmt.executeQuery("SELECT * FROM status");
//            while(rs.next()) {
//                list.add(new Status(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getInt(4)));
//            }
//            rs.close();
//            stmt.close();
//         } catch (SQLException ex) {
//            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        System.out.println("queried all " + list.size());
//        return list;
//    }
//    
//    public static Status query(int id) {
//        Status status = null;
//        try {
//            Statement stmt = DatabaseUtils.connection.createStatement();
//            ResultSet rs = stmt.executeQuery("SELECT * FROM status WHERE id = " + id);
//            rs.next();
//            status = new Status(id, rs.getString(2), rs.getInt(3), rs.getInt(4));
//            rs.close();
//            stmt.close();
//        } catch (SQLException ex) {
//            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return status;
//    }

    public static Status query(String name) {
        System.out.println("Status.query " + name);
        Status status = null;
        try {
            Statement stmt = DatabaseUtils.connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM status WHERE name = '" + name + "'");
            rs.next();
            status = new Status(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4));
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            // The row does not exist, so create it.
            //Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Status,query() Exception: " + ex.getMessage() + " status for new table " + name);
            
            status = new Status(-1, name, "", 0);
            System.out.println("new Status query " + status.toString());
            status.insert();
        }
        return status;
    }
    
    public Status() {
        this(-1, "empty", "", 0);
    }
    public Status(String name) {
        this(-1, name, "", 0);
    }
    public Status(int id, String name, String status, int result) {
        mId = id;
        mName = name;
        mStatus = status;
        mResult = result;
    }
    
    public int getId() {
        return mId;
    }
    public Status setName(String name) {
        mName = name;
        return this;
    }
    public String getName() {
        return mName;
    }
    public Status setStatus(String status) {
        mStatus = status;
        return this;
    }
    public String getStatus() {
        return mStatus;
    }
    public Status setResult(int result) {
        mResult = result;
        return this;
    }
    public int getResult() {
        return mResult;
    }

    public int insert() {
        System.out.println("Status.insert " + mName + ": " + mStatus);
        try {
            Statement stmt = DatabaseUtils.connection.createStatement();
            String sql = "INSERT INTO status (name, status, result)"
                                + " VALUES('" + mName + "', "
                                + "'" + mStatus + "', "
                                + mResult + ")";
            System.out.println("Status.insert: " + sql);
            
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
    
    public int update() {
        System.out.println("Status.update " + mName + ": " + mStatus);
        int nrows = 0;
        try {
            Statement stmt;
            stmt = DatabaseUtils.connection.createStatement();
            nrows = stmt.executeUpdate("UPDATE status SET name='"+ mName
                            + "', status="+ mStatus
                            + ", result=" + mResult
                            + " WHERE id =" + mId);
        } catch (SQLException ex) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nrows;
    }
    
    public int updateStatus() {
        int nrows = 0;
        try {
            Statement stmt;
            stmt = DatabaseUtils.connection.createStatement();
            nrows = stmt.executeUpdate("UPDATE status SET status='" + mStatus + "' WHERE id =" + mId);
        } catch (SQLException ex) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nrows;
    }
    
    public int updateResult() {
        System.out.println("Status updateResult");
        int nrows = 0;
        try {
            Statement stmt;
            stmt = DatabaseUtils.connection.createStatement();
            nrows = stmt.executeUpdate("UPDATE status SET result=" + mResult + " WHERE id =" + mId);
        } catch (SQLException ex) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nrows;
    }
    
    public void delete() {
        Statement stmt;
        int nrows = 0;
        
        try {
            stmt = DatabaseUtils.connection.createStatement();
            nrows = stmt.executeUpdate("DELETE FROM status WHERE id=" + mId);

        } catch (SQLException ex) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String toString() {
        return "[" + mId + ", name: " + mName + ", status: " + mStatus + ", result: " + mResult + "]";
    }

}
