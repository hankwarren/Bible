/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kgdsoftware.bible;

import com.kgdsoftware.bible.model.Book;
import com.kgdsoftware.bible.model.Setting;
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
public class GetBookTable implements Runnable {
    private BookTableFinished mCallback;

    public interface BookTableFinished {
        public void getBookTableFinished(String msg);
    }
    
    public GetBookTable(BookTableFinished callback) {
        System.out.println("new GetBookTable");
        
        mCallback = callback;
    }

    @Override
    public void run() {
        int result = 0;
        String url = Setting.getString("URL") + "/get_books_table.php";
        
        try {
            HttpGet getTable = new HttpGet(url);

            HttpClient client = HttpClientBuilder.create().build();
            
            System.out.println("wait for BookTable response");
            
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
                //System.out.println(sb.toString());
                parseTheResult(sb.toString());

                mCallback.getBookTableFinished("BookTableFinished");
            }

        } catch (IOException ex) {
            Logger.getLogger(GetBookTable.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(GetBookTable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(GetBookTable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GetBookTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private class ParseResponse extends DefaultHandler {

        // All the data is in the attributes, so just do the database access here.

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            //System.out.println("startElement " + uri + " localName: " + localName + " qName: " + qName);
            if (qName.equals("Book")) {
                String name = null;
                int chapters = 0;
                int id = -1;
                String abbr = null;
                int testamentId = 0;
                for (int i = 0; i < attributes.getLength(); i++) {
                    //System.out.println("attribute: " + attributes.getLocalName(i) + " = " + attributes.getValue(i));
                    if (attributes.getLocalName(i).equals("name")) {
                        name = attributes.getValue(i);
                    } else if (attributes.getLocalName(i).equals("id")) {
                        id = Integer.parseInt(attributes.getValue(i));
                    } else if (attributes.getLocalName(i).equals("chapters")) {
                        chapters = Integer.parseInt(attributes.getValue(i));
                    } else if (attributes.getLocalName(i).equals("abbr")) {
                        abbr = attributes.getValue(i);
                    } else if (attributes.getLocalName(i).equals("testamentId")) {
                        testamentId = Integer.parseInt(attributes.getValue(i));
                    } else {
                    }
                }
                Book books = new Book(id, name, chapters, abbr, testamentId);
                books.insert();
            }
        }
    }

}
