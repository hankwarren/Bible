/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kgdsoftware.bible;

import com.kgdsoftware.bible.model.Setting;
import com.kgdsoftware.bible.model.Version;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
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
public class GetVersionTable implements Runnable {
    private VersionTableFinished mCallback;

    public interface VersionTableFinished {
        public void getVersionTableFinished(String msg);
    }
    
    public GetVersionTable(VersionTableFinished callback) {
        mCallback = callback;
    }

    @Override
    public void run() {
        int result = 0;
        String url = Setting.getString("URL") + "/get_versions_table.php";
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
                    byte[] buf = new byte[8192];
                    while ((nbytes = is.read(buf)) != -1) {
                        sb.append(new String(buf, 0, nbytes, "UTF-8"));
                    }
                }

                parseTheResult(sb.toString());
                mCallback.getVersionTableFinished("VersionTableFinished");
            }

        } catch (IOException ex) {
            Logger.getLogger(GetChapterTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void parseTheResult(String msg) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();

            reader.setContentHandler(new ParseResponse());
            reader.parse(new InputSource(new StringReader(msg)));
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(GetChapterTable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(GetChapterTable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GetChapterTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private class ParseResponse extends DefaultHandler {
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            //System.out.println("ParseVersionTableResponse.startElement " + qName);
            if (qName.equals("Version")) {
                int id = -1;
                String name = null;
                String abbr = null;
                
                for (int i = 0; i < attributes.getLength(); i++) {
                    //System.out.println("attribute: " + attributes.getLocalName(i) + " = " + attributes.getValue(i));
                    switch (attributes.getLocalName(i)) {
                        case "id":
                            id = Integer.parseInt(attributes.getValue(i));
                            break;
                        case "name":
                            name = attributes.getValue(i);
                            break;
                        case "abbr":
                            abbr = attributes.getValue(i);
                            break;
                        default:
                            break;
                    }
                }
                Version version = Version.newInstance(id, false, name, abbr);
                version.insert();
                //System.out.println(version.toString());
                // Create the table for this version
                
            }
        }
    }
}
