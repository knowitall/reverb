package edu.washington.cs.knowitall.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

public class WarcReader implements Iterable<WarcPage> {
    private final BufferedReader in;
    protected long serialNumber;

    public WarcReader(BufferedReader in) {
        this.in = in;
    }

    public WarcReader(InputStream is) throws IOException {
        this(new BufferedReader(new InputStreamReader(is)));
    }

    @Override
    public Iterator<WarcPage> iterator() {
        return new Iterator<WarcPage>() {
            WarcPage w = null;

            @Override
            public boolean hasNext() {
                if (w != null) {
                    return true;
                }
                w = new WarcPage(serialNumber++);

                // Process both WARC headers
                int count = 2;
                while (count > 0) {
                    String line;
                    try {
                        line = in.readLine();
                    } catch (IOException e) {
                        return false;
                    }
                    if (line == null) {
                        return false;
                    }
                    if (line.startsWith("WARC-Target-URI")) {
                        w.setWARC_Target_URI(line.substring(17));
                    }
                    if (line.startsWith("WARC-TREC-ID")) {
                        w.setWARC_TREC_ID(line.substring(14));
                    }
                    if (line.startsWith("Content-Length:")) {
                        // once we've found this for second time, HTML follows
                        count--;
                    }
                }

                // get the page after the headers end
                while (true) {
                    String line;
                    try {
                        line = in.readLine();
                    } catch (IOException e) {
                        break;
                    }
                    if (line == null) {
                        break;
                    }
                    if (line.startsWith("WARC/0.18")) {
                        // found start of next entry, stop reading
                        break;
                    } else {
                        w.addLine(line);
                    }
                }
                return true;
            }

            @Override
            public WarcPage next() {
                WarcPage result = w;
                w = null;
                return result;
            }

            @Override
            public void remove() {
                next();
            }
        };
    }

}
