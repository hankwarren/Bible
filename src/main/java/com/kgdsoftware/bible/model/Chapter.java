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
public class Chapter {
    private int mId;
    private int mBookId;
    private int mChapter;
    private final int mVerses;
    private final int mVerseId;

    public static void create() {
        try {
            Statement s = DatabaseUtils.connection.createStatement();
            s.execute("CREATE TABLE Chapter(id INT GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,"
                    + "bookId INT,"
                    + "chapter INT,"
                    + "verses INT,"
                    + "verseId INT)");
            System.out.println("Chapter create");

        } catch (SQLException ex) {
            if ("X0Y32".equals(ex.getSQLState())) {    // table already exists
                //System.out.println(ex.getMessage() + " ErrorCode: " + ex.getErrorCode() + " state: " + ex.getSQLState());
            } else {
                Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static Chapter query(Book book, int chapter) {
        Chapter ch = null;
        try {
            Statement stmt = DatabaseUtils.connection.createStatement();
            String sql = "SELECT * FROM Chapter WHERE bookId=" + book.getId()
                            + " AND chapter=" + chapter;
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            
            ch = new Chapter(rs.getInt(1), book.getId(), rs.getInt(3), rs.getInt(4), rs.getInt(5));
            
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ch;
    }

    public static int getVerseId(int bookId, int chapter) {
        int verseId = 0;
        try {
            Statement stmt = DatabaseUtils.connection.createStatement();
            String sql = "SELECT verseid FROM chapter WHERE bookId="
                                + bookId + " AND chapter=" + chapter;
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            verseId = rs.getInt(1);
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
        }
        return verseId;
    }

    public Chapter(int id, int bookId, int chapter, int verses, int verseId) {
        System.out.println("new Chapter: " + bookId + " " + chapter + ":" + verseId);
        mId = id;
        mBookId = bookId;
        mChapter = chapter;
        mVerses = verses;
        mVerseId = verseId;
    }

    public Chapter(Chapter chapter) {
        mId = chapter.getId();
        mBookId = chapter.getBookId();
        mChapter = chapter.getChapterNumber();
        mVerses = chapter.getVerses();
        mVerseId = chapter.getVerseId();
    }
    
    public int getId() {
        return mId;
    }

    public int getBookId() {
        return mBookId;
    }

    public int getChapterNumber() {
        return mChapter;
    }

    public Chapter previous() {
        if (mChapter > 1) {
            mChapter--;
        }
        Book book = Book.query(mBookId);
        return Chapter.query(book, mChapter);
    }

    public Chapter next() {
        Book book = Book.query(mBookId);
        if (mChapter < book.getChapters()) {
            mChapter++;
        }
        return Chapter.query(book, mChapter);
    }

    public int getVerses() {
        return mVerses;
    }

    public int getVerseId() {
        return mVerseId;
    }

    public int insert() {
        try {
            Statement stmt = DatabaseUtils.connection.createStatement();
            stmt.execute("INSERT INTO Chapter (bookId, chapter, verses, verseId )"
                    + "VALUES(" + mBookId + ", " + mChapter + ", " + mVerses + ", " + mVerseId + ")",
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

    public String toString() {
        return "[ Chapter " + mId
                + " bookId: " + mBookId
                + " chapter: " + mChapter
                + " verses: " + mVerses
                + " verseId: " + mVerseId + "]";
    }
}
