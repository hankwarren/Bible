package com.kgdsoftware.bible;

import com.kgdsoftware.bible.model.Chapter;
import com.kgdsoftware.bible.model.Setting;
import com.kgdsoftware.bible.model.Verse;
import com.kgdsoftware.bible.model.Version;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
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
public class GetVerses implements Runnable {
    private Finished mCallback;
    private int mVersionId;
    private Chapter mChapter;
    
    public interface Finished {
        public void getVersesFinished(List<Verse> list);
    }
    
    public GetVerses(int versionId, Chapter chapter, Finished callback) {
        mCallback = callback;
        mVersionId = versionId;
        mChapter = chapter;
    }

    @Override
    public void run() {
        String versionAbbreviation = Version.get(mVersionId).getAbbreviation();

        List<Verse> verseList = Verse.query(versionAbbreviation, mChapter);
        
        System.out.println(verseList.size() + " verses in local database");
        
        if (verseList.size() == mChapter.getVerses()) {
            System.out.println("we have all the verses");
        } else if(verseList.size() == 0) {
            System.out.println("empty list - get verses");
            getVerse(versionAbbreviation, mChapter.getVerseId(), mChapter.getVerses());
            verseList = Verse.query(versionAbbreviation, mChapter);
        } else {
            // the list is partial
            int verse = 0;
            for(Verse v : verseList) {
                if(v.getVerse() != verse+1) {
                     getVerse(versionAbbreviation, mChapter.getVerseId()+verse+1, 1);
                }
                verse++;
            }
        }
        mCallback.getVersesFinished(verseList);
    }

    private void getVerse(String versionAbbreviation, int verseId, int count) {
        int result;
        String url = Setting.getString("URL") + "/get_verses.php?"
                + "version=" + versionAbbreviation
                + "&verseId=" + verseId
                + "&count=" + count;
        
        System.out.println("getVerse; " + url);

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

                parseTheResult(versionAbbreviation, sb.toString());
            }

        } catch (IOException ex) {
            Logger.getLogger(GetVerses.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void parseTheResult(String versionAbbreviation, String str) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();

            reader.setContentHandler(new ParseResponse(versionAbbreviation));
            //System.out.println("GetVerses.parseTheResult: " + str.length() + ":" + str);
            //System.out.println("*****************************");

            reader.parse(new InputSource(new StringReader(str)));
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(GetChapterTable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(GetChapterTable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GetChapterTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private class ParseResponse extends DefaultHandler {
        private String mVersionAbbreviation;
        private StringBuffer mBuffer;
        private Verse mVerse;

        ParseResponse(String versionAbbreviation) {
            mBuffer = new StringBuffer();
            mVersionAbbreviation = versionAbbreviation;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            mBuffer.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            //System.out.println("endElement: " + qName + " text: " + mBuffer.length() + ":" + mBuffer.toString());
            if (qName.equals("Verse")) {
                mVerse.setText(mBuffer.toString().replace("'", "''"));
                mVerse.insert(mVersionAbbreviation);
                //System.out.println("endElement: " + mVerse.toString());
            }
        }

        // All the data is in the attributes, so just do the database access here.
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            //System.out.println("startElement " + uri + " localName: " + localName + " qName: " + qName);
            mBuffer.setLength(0);
            if (qName.equals("Verse")) {
                int verse = 0;
                int id = 0;
                for (int i = 0; i < attributes.getLength(); i++) {
                    //System.out.println("attribute: " + attributes.getLocalName(i) + " = " + attributes.getValue(i));
                    switch (attributes.getLocalName(i)) {
                        case "verse":
                            verse = Integer.parseInt(attributes.getValue(i));
                            break;
                        case "id":
                            id = Integer.parseInt(attributes.getValue(i));
                            break;
                        default:
                            break;
                    }
                }
                mVerse = new Verse(0, id, verse, null);
            }
        }
    }
}
