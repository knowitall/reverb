package edu.washington.cs.knowitall.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

public class WarcPage {
    private String WARC_Target_URI;
    private String WARC_TREC_ID;
    private StringBuilder lines;
    private final long serialNumber;

    public WarcPage(long serialNumber) {
        this.serialNumber = serialNumber;
        lines = new StringBuilder();
    }

    /**
     * Creates a WarcPage object from the provided String. If the String does
     * not contain a valid WarcPage, <tt>null</tt> is returned.
     * 
     * If <tt>pageString</tt> contains multiple warc pages, only the first is
     * returned.
     * 
     * @see <tt>WarcReader</tt> to obtain multiple WarcPages at a time.
     */
    public static WarcPage fromString(String pageString) {
        // A hack to use warcreader to produce a single page.
        WarcReader tempReader = new WarcReader(new BufferedReader(
                new StringReader(pageString)));
        Iterator<WarcPage> wit = tempReader.iterator();
        if (wit.hasNext()) {
            return wit.next();
        } else {
            return null;
        }

    }

    public String getContent() {
        return lines.toString();
    }

    public void addLine(String line) {
        lines.append("\n" + line);
    }

    public void setWARC_Target_URI(String wARC_Target_URI) {
        WARC_Target_URI = wARC_Target_URI;
    }

    public String getWARC_Target_URI() {
        return WARC_Target_URI;
    }

    public long getSerialNumber() {
        return serialNumber;
    }

    public boolean hasContent() {
        return lines.length() > 0;
    }

    public StringReader getPageReader() throws IOException {
        return new StringReader(lines.toString());
    }

    public void setWARC_TREC_ID(String wARC_TREC_ID) {
        WARC_TREC_ID = wARC_TREC_ID;
    }

    public String getWARC_TREC_ID() {
        return WARC_TREC_ID;
    }
}
