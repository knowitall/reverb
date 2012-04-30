package edu.washington.cs.knowitall.io;

import java.io.BufferedReader;
import java.util.Iterator;
import java.util.regex.Pattern;

import com.google.common.collect.AbstractIterator;

/**
 * Buffers text "blocks" from a source of strings and iterates over the blocks.
 * By default, a newline is used to separate blocks, but a custom value can be
 * set.
 *
 * @author afader
 *
 */
public class SmartTextBlockIterator extends AbstractIterator<String> {

    private Iterator<String> lineIter;
    private String blockBreak = "";

    /**
     * @param reader
     *            the reader to extract blocks from.
     * @param blockBreak
     *            the value that represents a block break.
     */
    public SmartTextBlockIterator(BufferedReader reader, String blockBreak) {
        init(new BufferedReaderIterator(reader), blockBreak);
    }

    /**
     * Constructs a <code>TextBlockIterator</code> using a newline as the
     * default break.
     *
     * @param reader
     *            the reader to extract blocks from.
     */
    public SmartTextBlockIterator(BufferedReader reader) {
        init(new BufferedReaderIterator(reader), "");
    }

    /**
     * Constructs a <code>TextBlockIterator</code> over the strings returned by
     * <code>lineIter</code>.
     *
     * @param lineIter
     * @param blockBreak
     */
    public SmartTextBlockIterator(Iterator<String> lineIter, String blockBreak) {
        init(lineIter, blockBreak);
    }

    /**
     * Constructs a <code>TextBlockIterator</code> over the strings returned by
     * <code>lineIter</code>, using the default block break.
     *
     * @param lineIter
     */
    public SmartTextBlockIterator(Iterator<String> lineIter) {
        init(lineIter, "");
    }

    /**
     * Constructs a <code>TextBlockIterator</code> over the strings returned by
     * <code>iter</code>.
     *
     * @param iter
     * @param blockBreak
     */
    public SmartTextBlockIterator(Iterable<String> iter, String blockBreak) {
        init(iter.iterator(), blockBreak);
    }

    /**
     * Constructs a <code>TextBlockIterator</code> over the strings returned by
     * <code>iter</code>, using the default block break.
     *
     * @param iter
     */
    public SmartTextBlockIterator(Iterable<String> iter) {
        init(iter.iterator(), "");
    }

    private void init(Iterator<String> lineIter, String blockBreak) {
        this.lineIter = lineIter;
        this.blockBreak = blockBreak;
    }

    // remove non-breaking whitespace at this level because
    // they do not even get matched by the regex \s
    Pattern convertToSpace = Pattern.compile("\\xa0");

    private String cleanupLine(String line) {
        return convertToSpace.matcher(line).replaceAll(" ").trim();
    }

    protected String computeNext() {
        while (lineIter.hasNext()) {
            String line = cleanupLine(lineIter.next());

            StringBuffer buf = new StringBuffer(line).append(" ");
            while (lineIter.hasNext() && !line.equals(blockBreak)) {
                line = cleanupLine(lineIter.next());
                buf.append(line).append(" ");
            }
            String result = buf.toString();
            if (!result.equals("")) {
                return result.trim();
            }
        }
        return endOfData();
    }

}
