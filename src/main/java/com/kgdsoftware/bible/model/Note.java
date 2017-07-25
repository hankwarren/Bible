package com.kgdsoftware.bible.model;

import com.kgdsoftware.database.DatabaseUtils;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;

/**
 *
 * @author henriwarren
 */
public class Note {

    public static final int ID_INDEX = 1;
    public static final int BOOKID_INDEX = 2;
    public static final int CHAPTER_INDEX = 3;
    public static final int VERSE_INDEX = 4;
    public static final int VERSEID_INDEX = 5;
    public static final int LOCALTIMESTAMP_INDEX = 6;
    public static final int REMOTETIMESTAMP_INDEX = 7;
    public static final int POSTED_INDEX = 8;
    public static final int CONFIRMED_INDEX = 9;
    public static final int TEXT_INDEX = 10;

    private int mId;
    private int mBookId;
    private int mChapter;
    private int mVerse;
    private int mVerseId;
    private String mText;
    private long mRemoteTimestamp;
    private long mLocalTimestamp;

    private boolean mPosted;
    private boolean mConfirmed;
    private boolean mChanged;

    public static void create() {
        try {
            Statement s = DatabaseUtils.connection.createStatement();
            s.execute("CREATE TABLE Note("
                    + "id INT GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,"
                    + "bookId INT,"
                    + "chapter INT,"
                    + "verse INT,"
                    + "verseId INT,"
                    + "localTimestamp BIGINT,"
                    + "remoteTimestamp BIGINT,"
                    + "posted INT,"
                    + "confirmed INT,"
                    + "text CLOB(128 K))");
            System.out.println("Note create");
        } catch (SQLException ex) {
            if ("X0Y32".equals(ex.getSQLState())) {    // table already exists
                //System.out.println(ex.getMessage() + " ErrorCode: " + ex.getErrorCode() + " state: " + ex.getSQLState());
            } else {
                Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // One way or another return a note
    public static Note query(Book book, Chapter chapter, int verse) {
        System.out.println("Note.query " + book + " " + chapter + " verse: " + verse);
        int verseId = chapter.getVerseId() + verse - 1;
        Note note;
        try {
            note = query(verseId);
        } catch (SQLException ex) {
            System.out.println("Note Create a new local note");
            note = new Note.Builder(-1)
                    .bookId(book.getId())
                    .chapter(chapter.getChapterNumber())
                    .verse(verse)
                    .verseId(chapter.getVerseId() + verse - 1)
                    .localTimestamp(System.currentTimeMillis())
                    .remoteTimestamp(0)
                    .text("")
                    .build();
            note.insert();
        }
        return note;
    }

    // Really not a good method. If limit can be getter than 1, then return a List<Note>
    public static Note queryLocalOnly() {
        try {
            Statement stmt = DatabaseUtils.connection.createStatement();
            stmt.setMaxRows(1);
            String sql = "SELECT * from Note WHERE remoteTimestamp = 0";
            
            ResultSet resultSet = stmt.executeQuery(sql);
            List<Note> noteList = new ArrayList<>();
            while (resultSet.next()) {
                Note note = new Note.Builder(resultSet.getInt(ID_INDEX))
                        .bookId(resultSet.getInt(BOOKID_INDEX))
                        .chapter(resultSet.getInt(CHAPTER_INDEX))
                        .verse(resultSet.getInt(VERSE_INDEX))
                        .verseId(resultSet.getInt(VERSEID_INDEX))
                        .localTimestamp(resultSet.getLong(LOCALTIMESTAMP_INDEX))
                        .remoteTimestamp(resultSet.getLong(REMOTETIMESTAMP_INDEX))
                        .posted(resultSet.getInt(POSTED_INDEX) == 1)
                        .confirmed(resultSet.getInt(CONFIRMED_INDEX) == 1)
                        .text(resultSet.getString(TEXT_INDEX))
                        .build();

                System.out.println("got a note that needs to be posted: " + note.getVerseId());

                resultSet.close();
                stmt.close();
                return note;
            }
        } catch (SQLException e) {
            return null;
        }
        return null;
    }

    public static List<Integer> queryVerseId(int first, int last) {
        List<Integer> intList = new ArrayList<>();
        try {
            Statement stmt = DatabaseUtils.connection.createStatement();
            String sql = "SELECT verseId FROM Note"
                    + " WHERE verseId >= " + first
                    + " AND verseId < " + last
                    + " ORDER BY verseId";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                intList.add(rs.getInt(1));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, e);
        }
        return intList;
    }

    public static Note query(int verseId) throws SQLException {
        System.out.println("Note.query " + verseId);

        Statement stmt = DatabaseUtils.connection.createStatement();
        String sql = "SELECT * FROM Note WHERE verseId=" + verseId;
        ResultSet rs = stmt.executeQuery(sql);
        rs.next();
        // There was a local note
        // There was is a 'replaceAll' for the text. The single quote was escaped
        //  to insert into the database, so it must be undone on the way back out.
        Note note = new Note.Builder(rs.getInt(ID_INDEX))
                .bookId(rs.getInt(BOOKID_INDEX))
                .chapter(rs.getInt(CHAPTER_INDEX))
                .verse(rs.getInt(VERSE_INDEX))
                .verseId(rs.getInt(VERSEID_INDEX))
                .localTimestamp(rs.getLong(LOCALTIMESTAMP_INDEX))
                .remoteTimestamp(rs.getLong(REMOTETIMESTAMP_INDEX))
                .posted(rs.getInt(POSTED_INDEX) == 1)
                .confirmed(rs.getInt(CONFIRMED_INDEX) == 1)
                .text(rs.getString(TEXT_INDEX).replaceAll("''", "'").trim())
                .build();

        //System.out.println("note.query(" + verseId + ") text: " + rs.getString(TEXT_INDEX));
        rs.close();
        stmt.close();
        return note;
    }

    public static ListModel<Note> query() {
        System.out.println("Note.query returns ListModel<Note>");
        DefaultListModel<Note> nlm = new DefaultListModel<>();
        try (Statement stmt = DatabaseUtils.connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM Note")) {
            while (rs.next()) {
                Note note = new Note.Builder(rs.getInt(ID_INDEX))
                        .bookId(rs.getInt(BOOKID_INDEX))
                        .chapter(rs.getInt(CHAPTER_INDEX))
                        .verse(rs.getInt(VERSE_INDEX))
                        .verseId(rs.getInt(VERSEID_INDEX))
                        .localTimestamp(rs.getLong(LOCALTIMESTAMP_INDEX))
                        .remoteTimestamp(rs.getLong(REMOTETIMESTAMP_INDEX))
                        .posted(rs.getInt(POSTED_INDEX) == 1)
                        .confirmed(rs.getInt(CONFIRMED_INDEX) == 1)
                        .text(rs.getString(TEXT_INDEX))
                        .build();
                //System.out.println("ListModel<Note> query: " + note.toString());
                nlm.addElement(note);
            }
            //System.out.println("note text: " + rs.getString(7));
        } catch (SQLException e) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, e);
        }
        return nlm;
    }

    // Just for debug? Only used in NoteList when clicking on a note in the list
    public String queryText() {
        System.out.println("Note.queryText " + mVerseId);
        String text = null;
        try (Statement stmt = DatabaseUtils.connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT text FROM Note WHERE id=" + mId)) {
            rs.next();

            text = rs.getString(1);
            //System.out.println("note text: " + rs.getString(7));

        } catch (SQLException e) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, e);
        }
        return text;
    }

    public static boolean exists(int verseId) {
        boolean result;
        try (Statement stmt = DatabaseUtils.connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM Note WHERE verseId=" + verseId);
            rs.close();
            result = true;

        } catch (SQLException ex) {
            result = false;
        }
        return result;
    }

    public static class Builder {

        private int id;
        private int bookId;
        private int chapter;
        private int verse;
        private int verseId;
        private String text;
        private long remoteTimestamp;
        private long localTimestamp;
        private boolean posted;
        private boolean confirmed;
        private boolean changed;

        public Builder(int val) {
            id = val;
            posted = false;
            confirmed = false;
            changed = false;
        }

        public Builder bookId(int val) {
            bookId = val;
            return this;
        }

        public Builder chapter(int val) {
            chapter = val;
            return this;
        }

        public Builder verse(int val) {
            verse = val;
            return this;
        }

        public Builder verseId(int val) {
            verseId = val;
            return this;
        }

        public Builder text(String val) {
            text = val;
            return this;
        }

        public Builder localTimestamp(long val) {
            localTimestamp = val;
            return this;
        }

        public Builder remoteTimestamp(long val) {
            remoteTimestamp = val;
            return this;
        }

        public Builder posted(boolean val) {
            posted = val;
            return this;
        }

        public Builder confirmed(boolean val) {
            confirmed = val;
            return this;
        }

        public Note build() {
            return new Note(this);
        }
    }

    public Note(Builder builder) {
        mId = builder.id;
        mBookId = builder.bookId;
        mChapter = builder.chapter;
        mVerse = builder.verse;
        mVerseId = builder.verseId;
        mText = builder.text;
        mRemoteTimestamp = builder.remoteTimestamp;
        mLocalTimestamp = builder.localTimestamp;
        mPosted = builder.posted;
        mConfirmed = builder.confirmed;
    }

    public int getId() {
        return mId;
    }

    public Note setId(int id) {
        mId = id;
        return this;
    }

    public int getVerseId() {
        return mVerseId;
    }

    public Note setVerseId(int verseId) {
        mVerseId = verseId;
        return this;
    }

    public int getBookId() {
        return mBookId;
    }

    public Note setBookId(int bookId) {
        mBookId = bookId;
        return this;
    }

    public int getChapter() {
        return mChapter;
    }

    public Note setChapter(int chapter) {
        mChapter = chapter;
        return this;
    }

    public int getVerse() {
        return mVerse;
    }

    public Note setVerse(int verse) {
        mVerse = verse;
        return this;
    }

    public String getText() {
        return mText;
    }

    public Note setText(String text) {
        mChanged = true;    // should check if the new test is different
        mText = text;
        return this;
    }

    public long getLocalTimestamp() {
        return mLocalTimestamp;
    }

    public Note setLocalTimestamp(long timestamp) {
        mLocalTimestamp = timestamp;
        return this;
    }

    public long getRemoteTimestamp() {
        return mRemoteTimestamp;
    }

    public Note setRemoteTimestamp(long timestamp) {
        mRemoteTimestamp = timestamp;
        return this;
    }

    public boolean isPosted() {
        return mPosted;
    }

    public Note setPosted(boolean posted) {
        mPosted = posted;
        return this;
    }

    public boolean isConfirmed() {
        return mConfirmed;
    }

    public Note setConfirmed(boolean confirmed) {
        mConfirmed = confirmed;
        return this;
    }

    public boolean isEmpty() {
        return mText.length() == 0;
    }

    public int insert() {
        //System.out.println("Note.insert " + mId + " (" + mBookId + ") " + mChapter + ":" + mVerse + "<" + toString() + ">");

        try {
            String sql = "INSERT INTO Note(bookId, chapter, verse, verseId, localTimestamp, remoteTimestamp, posted, confirmed, text) "
                    + "VALUES("
                    + mBookId + ","
                    + mChapter + ","
                    + mVerse + ","
                    + mVerseId + ","
                    + mLocalTimestamp + ","
                    + mRemoteTimestamp + ","
                    + "0," // not posted
                    + "0," // not confirmed
                    + "?)";
            PreparedStatement stmt = DatabaseUtils.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            String text = mText.replace("'", "''").trim();

            stmt.setString(1, text);
            //System.out.println("insert text: " + text);

            //System.out.println("Note.insert sql: " + sql);
            //System.out.println("Note: " + toString());

            // Need to use the ? statements. It is possible for the simple
            //  string constant to be too long for SQL.
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            rs.next();
            mId = rs.getInt(1);
            //System.out.println("Note.insert " + toString());
            // post the note to the server
        } catch (SQLException ex) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mId;
    }

    public int update() {
        mChanged = true;
        //System.out.println("Note.update " + mId + " (" + mBookId + ") " + mChapter + ":" + mVerse + "< " + toString() + " >");
        int nrows = 0;
        try {
            String sql = "UPDATE Note SET "
                    + "bookId=" + mBookId + ","
                    + "chapter=" + mChapter + ","
                    + "verse=" + mVerse + ","
                    + "verseId=" + mVerseId + ","
                    + "localTimestamp=" + mLocalTimestamp + ","
                    + "remoteTimestamp=" + mRemoteTimestamp + ","
                    + "posted=0," // not posted
                    + "confirmed=0," // not confirmed
                    + "text=? "
                    + "WHERE id =" + mId;
            PreparedStatement stmt = DatabaseUtils.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            String text = mText.replace("'", "''").trim();
            stmt.setString(1, text);
            //System.out.println("update text: " + text);
            //System.out.println("Note.update sql: " + sql);
            nrows = stmt.executeUpdate();
            System.out.println("Note update " + nrows + " updated");
            if (nrows < 1) {
                int id = insert();
                System.out.println("insert record " + id);
                mId = id;
            }
            // post the note to the server
        } catch (SQLException ex) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, "update error", ex);
        }
        return nrows;
    }

    public int delete() {
        System.out.println("Note.delete " + mId);
        int nrows = 0;
        try {
            Statement stmt;
            stmt = DatabaseUtils.connection.createStatement();
            nrows = stmt.executeUpdate("DELETE FROM Note WHERE id=" + mId);
            // must do the remote delete at some time?
        } catch (SQLException ex) {
            Logger.getLogger(Note.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nrows;
    }

    @Override
    public String toString() {
        return "[Note " + mId
                + " (" + mBookId + ") " + mChapter + ":" + mVerse
                + " verseId: " + mVerseId
                + " localTimestamp: " + mLocalTimestamp
                + " remoteTimestamp: " + mRemoteTimestamp
                + " posted: " + mPosted
                + " confirmed: " + mConfirmed
                + " <" + mText + "> ]";
    }
}
