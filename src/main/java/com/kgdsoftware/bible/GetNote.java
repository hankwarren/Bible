package com.kgdsoftware.bible;

import com.kgdsoftware.bible.model.Note;
import com.kgdsoftware.bible.model.Setting;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author henriwarren
 */
public class GetNote implements Runnable {
    private int mVerseId;
    private Finished mCallback;


    public interface Finished {
        public void getNoteFinished(int verseId);
    }
    
    public GetNote(int verseId, Finished callback) {
        mVerseId = verseId;
        mCallback = callback;
    }
    
    @Override
    public void run() {
        int result;
        String url = Setting.getString("URL") + "/get_note.php?verseId=" + mVerseId; // &debug=1;

        try {
            System.out.println("GetNote " + url);
            HttpGet getTable = new HttpGet(url);

            HttpClient client = HttpClientBuilder.create().build();
            HttpResponse response = client.execute(getTable);

            StatusLine statusLine = response.getStatusLine();
            result = statusLine.getStatusCode();

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                StringBuilder sb = new StringBuilder();
                try (InputStream is = entity.getContent()) {
                    int nbytes;
                    byte[] buf = new byte[0x10000];
                    while ((nbytes = is.read(buf)) != -1) {
                        sb.append(new String(buf, 0, nbytes, "UTF-8"));
                    }
                }

                parseTheResult(sb.toString());
                mCallback.getNoteFinished(mVerseId);
            }

        } catch (IOException ex) {
            Logger.getLogger(GetVerses.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void parseTheResult(String str) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();

            reader.setContentHandler(new ParseResponse());
            System.out.println("parseTheResult: " + str.length() + ":" + str);
            System.out.println("parseTheResult: *****************************");
            // Skip over any debug junk at the beginning
            reader.parse(new InputSource(new StringReader(str.substring(str.indexOf("<OK>")))));
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(GetChapterTable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(GetChapterTable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GetChapterTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private class ParseResponse extends DefaultHandler {
        private String mText;
        private StringBuilder mBuffer;
        private int mId;
        // I really throw away the bookId and chapter. These are in
        // the online database. The book and chapter data are
        // managed in NoteFrame. I guess a wart on the design.
        private int mBookId;
        private int mChapter;
        private int mVerse;
        private int mVerseId;
        private long mRemoteTimestamp;

        public ParseResponse() {
            mBuffer = new StringBuilder();
            System.out.println("ParseResponse - new parser");
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            mBuffer.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals("note")) {
                Note note = null;
                try {
                    note = Note.query(mVerseId);
                    note.setBookId(mBookId);
                    note.setChapter(mChapter);
                    note.setRemoteTimestamp(mRemoteTimestamp);
                    note.setLocalTimestamp(System.currentTimeMillis());
                    note.setPosted(true);
                    note.setConfirmed(true);
                    note.setText(mBuffer.toString());

                    System.out.println("*** ParseResponse note.update " + note);
                    note.update();
                } catch (SQLException ex) {
                    // The note did not exist, so create and insert
                    note = new Note.Builder(-1)
                        .bookId(mBookId)
                        .chapter(mChapter)
                        .verse(mVerse)
                        .verseId(mVerseId)
                        .localTimestamp(System.currentTimeMillis())
                        .remoteTimestamp(mRemoteTimestamp)
                        .posted(true)
                        .confirmed(true)
                        .text(mBuffer.toString())
                        .build();
                    note.insert();
                    System.out.println("ParseResponse note.insert " + note);
                }
            } else if (qName.equals("OK")) {
            } else {
                mBuffer.append("</" + qName + ">");
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equals("note")) {
                mBuffer.setLength(0);

                for (int i = 0; i < attributes.getLength(); i++) {
                    //System.out.println("attribute: " + attributes.getLocalName(i) + " = " + attributes.getValue(i));
                    switch (attributes.getLocalName(i)) {
                        case "id":
                            mId = Integer.parseInt(attributes.getValue(i));
                            break;
                        case "bookId":
                            mBookId = Integer.parseInt(attributes.getValue(i));
                            break;
                        case "chapter":
                            mChapter = Integer.parseInt(attributes.getValue(i));
                            break;
                        case "verseId":
                            mVerseId = Integer.parseInt(attributes.getValue(i));
                            break;
                        case "verse":
                            mVerse = Integer.parseInt(attributes.getValue(i));
                            break;
                        case "timestamp":
                            mRemoteTimestamp = Long.parseLong(attributes.getValue(i));
                        default:
                            break;
                    }
                }
            } else if (qName.equals("OK")) {
            } else {
                mBuffer.append("<" + qName + ">");
            }
        }
    }
}
