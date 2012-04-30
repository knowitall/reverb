package edu.washington.cs.knowitall.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import com.google.common.collect.AbstractIterator;

/**
 * A wrapper class that converts a <code>BufferedReader</code> object into a
 * <code>Iterator<String></code> object, which iterates over the lines returned
 * by the <code>BufferedReader</code>.
 * @author afader
 *
 */
public class BufferedReaderIterator extends AbstractIterator<String> {

    private BufferedReader reader;
    /**
     * Constructs a new iterator over the lines in <code>reader</code>.
     * @param reader
     */
    public BufferedReaderIterator(Reader reader) {
        this.reader = new BufferedReader(reader);
    }

    @Override
    protected String computeNext() {
        try {
            String line = reader.readLine();
            if (line != null) {
                return line;
            } else {
                return endOfData();
            }
        } catch (IOException e) {
            return endOfData();
        }
    }
}
