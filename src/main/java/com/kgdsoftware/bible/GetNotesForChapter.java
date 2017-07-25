/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kgdsoftware.bible;

import com.kgdsoftware.bible.model.Note;
import com.kgdsoftware.bible.model.Setting;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
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
import org.apache.http.conn.HttpHostConnectException;
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
public class GetNotesForChapter implements Runnable {
    private Finished mCallback;
    private int mFirstVerseId;
    private int mLastVerseId;
    
    public interface Finished {
        public void getNotesForChapterFinished(List<Integer> list, boolean done);
    }
    
    public GetNotesForChapter(int firstVerseId, int lastVerseId, Finished callback) {
        //System.out.println("GetNotesForChapter first: " + firstVerseId + " last: " + lastVerseId);
        mFirstVerseId = firstVerseId;
        mLastVerseId = lastVerseId;
        mCallback = callback;
    }
    
    @Override
    public void run() {
        // query the local database for initial setting
        // query remote database for additional info
        // at this point I would know what is on the local device
        // and not on the remote
        getNotesForChapterLocal();
        
        getNotesForChapterRemote();
    }
    
    private void getNotesForChapterLocal() {
        List<Integer> list = Note.queryVerseId(mFirstVerseId, mLastVerseId);
        mCallback.getNotesForChapterFinished(list, false);
    }
    
    private void getNotesForChapterRemote() {
        String url = Setting.getString("URL") + "/get_notes_for_chapter.php?"
                    + "firstVerseId=" + mFirstVerseId
                    + "&lastVerseId=" + mLastVerseId;
        
        int result;
        try {
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
            }
        } catch (HttpHostConnectException e) {
            System.out.println("GetNotesForChapter: HTTP connection failed, move on");
        } catch (IOException ex) {
            Logger.getLogger(GetVerses.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void parseTheResult(String str) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();

            ParseResponse parse = new ParseResponse();
            reader.setContentHandler(parse);
            System.out.println("GetNotesForChapter.parseTheResult: " + str.length() + ":" + str);
            System.out.println("*****************************");

            reader.parse(new InputSource(new StringReader(str)));
            mCallback.getNotesForChapterFinished(parse.getList(), true);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(GetChapterTable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(GetChapterTable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GetChapterTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private class ParseResponse extends DefaultHandler {
        private StringBuilder mBuffer;
        private List<Integer> mList;

        public ParseResponse() {
            mBuffer = new StringBuilder();
            mList = new ArrayList<>();
        }

        public List<Integer> getList() {
            return mList;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            mBuffer.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals("verse")) {
                mList.add(Integer.parseInt(mBuffer.toString()));
//            System.out.println("startElement: " + qName + " " + mBuffer.toString());
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equals("verse")) {
                mBuffer.setLength(0);
            }
        }
    }
}
