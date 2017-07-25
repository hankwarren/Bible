/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kgdsoftware.bible;

import com.kgdsoftware.bible.model.Note;
import com.kgdsoftware.bible.model.Setting;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

/**
 *
 * @author henriwarren
 */
public class PostNote implements Runnable {

    private Finished mCallback;
    private Note mNote;

    public interface Finished {

        void postNoteFinished(int verseId);
    }

    public PostNote(Note note, Finished callback) {
        mNote = note;
        mCallback = callback;
    }

    @Override
    public void run() {
        int result = 0;
        String url = Setting.getString("URL") + "/save_note.php";    // ?debug=1
        System.out.println("PostNote.run: " + url);
        //System.out.println("  note: " + mNote);

        HttpPost postNote = new HttpPost(url);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("bookId", Integer.toString(mNote.getBookId())));
        params.add(new BasicNameValuePair("chapter", Integer.toString(mNote.getChapter())));
        params.add(new BasicNameValuePair("verse", Integer.toString(mNote.getVerse())));
        params.add(new BasicNameValuePair("verseId", Integer.toString(mNote.getVerseId())));
        params.add(new BasicNameValuePair("timestamp", Long.toString(mNote.getRemoteTimestamp())));

        String str = mNote.getText().trim();

        params.add(new BasicNameValuePair("text", str));

        try {
            postNote.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            HttpClient client = HttpClientBuilder.create().build();
            HttpResponse response = client.execute(postNote);

            StatusLine statusLine = response.getStatusLine();
            result = statusLine.getStatusCode();

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                XMLInputFactory factory = (XMLInputFactory) XMLInputFactory.newInstance();
                factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
                XMLStreamReader staxXmlReader = (XMLStreamReader) factory.createXMLStreamReader(entity.getContent());

                StringBuilder buffer = new StringBuilder();
                int id, bookId = 0, chapter = 0, verse = 0, verseId = 0;
                long remoteTimestamp = 0;
                String qName;

                for (int event = staxXmlReader.next();
                        event != XMLStreamConstants.END_DOCUMENT;
                        event = staxXmlReader.next()) {
                    switch (event) {
                        case XMLStreamConstants.START_DOCUMENT:
                            break;

                        case XMLStreamConstants.END_ELEMENT:
                            qName = staxXmlReader.getName().toString();
                            if (qName.equals("note")) {
                                Note note = null;
                                try {
                                    note = Note.query(verseId);
                                    note.setBookId(bookId);
                                    note.setChapter(chapter);
                                    note.setRemoteTimestamp(remoteTimestamp);
                                    note.setLocalTimestamp(System.currentTimeMillis());

                                    note.setText(buffer.toString().trim());

                                    //System.out.println("PostNote END_ELEMENT note.update " + note);
                                    note.update();
                                } catch (SQLException ex) {
                                    System.out.println("PostNote exception: " + ex.getMessage());
                                    System.out.println("ParseNoteResponse note note did not exist (wrong) " + note);
                                }
                            } else if (qName.equals("OK")) {
                            } else {
                                buffer.append("</" + qName + ">");
                            }
                            break;
                        case XMLStreamConstants.START_ELEMENT:
                            qName = staxXmlReader.getName().toString();
                            if (qName.equals("note")) {
                                buffer = new StringBuilder();

                                for (int i = 0; i < staxXmlReader.getAttributeCount(); i++) {
                                    //System.out.println("attribute: " + attributes.getLocalName(i) + " = " + attributes.getValue(i));
                                    String attributeValue = staxXmlReader.getAttributeValue(i);
                                    switch (staxXmlReader.getAttributeName(i).toString()) {
                                        case "id":
                                            id = Integer.parseInt(attributeValue);
                                            break;
                                        case "bookId":
                                            bookId = Integer.parseInt(attributeValue);
                                            break;
                                        case "chapter":
                                            chapter = Integer.parseInt(attributeValue);
                                            break;
                                        case "verseId":
                                            verseId = Integer.parseInt(attributeValue);
                                            break;
                                        case "verse":
                                            verse = Integer.parseInt(attributeValue);
                                            break;
                                        case "timestamp":
                                            remoteTimestamp = Long.parseLong(attributeValue);
                                        default:
                                            break;
                                    }
                                }
                            } else if (qName.equals("OK")) {

                            } else {
                                System.out.println("????????? qName: " + qName + " ?????????????????????????????");
                                buffer.append("<" + qName + ">");
                            }
                            break;
                        case XMLStreamConstants.CHARACTERS:
                            buffer.append(staxXmlReader.getText());
                            break;
                        default:
                    }
                }

                mCallback.postNoteFinished(mNote.getVerseId());
            }

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(PostNote.class.getName()).log(Level.SEVERE, null, ex);
        } catch (HttpHostConnectException e) {
            System.out.println("PostNote: Connection failed: move on ************************");
        } catch (IOException ex) {
            Logger.getLogger(GetVerses.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XMLStreamException ex) {
            Logger.getLogger(PostNote.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}