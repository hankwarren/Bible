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
public class LastChapter {
    private int mId;
    private int mVersionId;
    private Book mBook;
    private int mChapter;
    private int mVerse;
    
    
    public static void create() {
        try {
            Statement s = DatabaseUtils.connection.createStatement();
            s.execute("CREATE TABLE LastChapter(id INT GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,"
                    + "versionId INT,"
                    + "bookId INT,"
                    + "chapter INT,"
                    + "verse INT)");
            System.out.println("LastChapter create");
        } catch (SQLException ex) {
            if ("X0Y32".equals(ex.getSQLState())) {    // table already exists
                //System.out.println(ex.getMessage() + " ErrorCode: " + ex.getErrorCode() + " state: " + ex.getSQLState());
            } else {
                Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static LastChapter query(int bookId) {
        LastChapter lastChapter = null;
        try {
            Statement stmt = DatabaseUtils.connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM LastChapter WHERE bookId=" + bookId);
            rs.next();
            lastChapter = new LastChapter.Builder()
                                    .id(-1)
                                    .versionId(rs.getInt(2))
                                    .book(Book.query(rs.getInt(3)))
                                    .chapter(rs.getInt(4))
                                    .verse(rs.getInt(5))
                                    .build();
            rs.close();
            stmt.close();
         } catch (SQLException ex) {
             System.out.println("LastChapter.query - select threw exception");
             lastChapter = new LastChapter.Builder()
                                    .id(-1)
                                    .versionId(1)
                                    .book(Book.query(bookId))
                                    .chapter(1)
                                    .verse(1)
                                    .build();
             lastChapter.insert();
        }
        return lastChapter;
    }
    
    public int getId() { return mId; }
    public LastChapter setVersionId(int version) {
        mVersionId = version;
        return this;
    }
    public int getVersionId() { return mVersionId; }
    public Book getBook() { return mBook; }
    public int getChapter() { return mChapter; }
    public LastChapter setChapter(int chapter) {
        mChapter = chapter;
        return this;
    }
    public int getVerse() { return mVerse; }
    public LastChapter setVerse(int verse) {
        mVerse = verse;
        return this;
    }
    
    // insert or update
    public int insert() {
        System.out.println("LastChapter.insert " + toString());
        try {
            Statement stmt = DatabaseUtils.connection.createStatement();
            stmt.execute("INSERT INTO LastChapter(versionId, bookId, chapter, verse)"
                    + "VALUES("
                    + mVersionId + ", "
                    + mBook.getId() + ", "
                    + mChapter + ", "
                    + mVerse + ")",
                    Statement.RETURN_GENERATED_KEYS);

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
        System.out.println("LastChapter.update: " + toString());
        int nrows = 0;
        try {
            Statement stmt;
            stmt = DatabaseUtils.connection.createStatement();
            nrows = stmt.executeUpdate("UPDATE LastChapter SET "
                    + "versionId=" + mVersionId + ","
                    + "bookId=" + mBook.getId() + ","
                    + "chapter=" + mChapter + ","
                    + "verse=" + mVerse
                    + " WHERE id =" + mId);
            if (nrows < 1) {
                int id = insert();
                System.out.println("insert record " + id);
                mId = id;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nrows;
    }
    
    @Override
    public String toString() {
        return "[LastChapter id: " + mId
                + " versionId: " + mVersionId
                + " book: " + mBook
                + " chapter: " + mChapter
                + " verse: " + mVerse + "]";
    }
    
    public static class Builder {
        private int id;
        private int versionId;
        private Book book;
        private int chapter;
        private int verse;
        
        public Builder id(int id) {
            this.id = id;
            return this;
        }
        public Builder versionId(int versionId) {
            this.versionId = versionId;
            return this;
        }
        public Builder book(Book book) {
            this.book = book;
            return this;
        }
        public Builder chapter(int chapter) {
            this.chapter = chapter;
            return this;
        }
        public Builder verse(int verse) {
            this.verse = verse;
            return this;
        }
        public LastChapter build() {
            return new LastChapter(this);
        }
    }
    
    public LastChapter(Builder builder) {
        mId = builder.id;
        mVersionId = builder.versionId;
        mBook = builder.book;
        mChapter = builder.chapter;
        mVerse = builder.verse;
    }
    
    public LastChapter(LastChapter lastChapter) {
        mId = lastChapter.getId();
        mVersionId = lastChapter.getVersionId();
        mBook = lastChapter.getBook();
        mChapter = lastChapter.getChapter();
        mVerse = lastChapter.getVerse();
    }
}
