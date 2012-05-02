package edu.washington.cs.knowitall.extractor.mapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import edu.washington.cs.knowitall.commonlib.ResourceUtils;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;

/***
 * Filters relations based on how many distinct arg2 values it takes in a large
 * corpus. These frequencies must be pre-computed and stored in a flat file in
 * the tab-delimited format (#arg2s, relation). The relations should be
 * normalized using the <code>VerbalRelationNormalizer</code> class.
 *
 * By default, this class searches the classpath for a file called
 * <code>freq_rel.txt.gz</code> and reads the relations with at least 20
 * distinct arg2s from it.
 *
 * @author afader
 *
 */
public class ReVerbRelationDictionaryFilter extends
        FilterMapper<ChunkedExtraction> {

    private static final String relationDictFile = "freq_rel.txt.gz";
    public static final int defaultMinFreq = 20;
    private NormalizedRelationDictionaryFilter filter;

    /**
     * Constructs a new dictionary filter from the data in <code>in</code>. This
     * data should be in the tab-delimited format (#arg2, relation).
     *
     * @param in
     *            the dictionary of relations and their number of distinct
     *            arg2s.
     * @param minFreq
     *            the minimum number of distinct arg2s a relation must have to
     *            be included.
     * @throws IOException
     */
    public ReVerbRelationDictionaryFilter(InputStream in, int minFreq)
            throws IOException {
        init(in, minFreq);
    }

    /**
     * Constructs a new dictionary filter using the data in the file
     * <code>freq_rel.txt.gz</code>, which is found on the classpath.
     *
     * @param minFreq
     *            the minimum number of distinct arg2s a relation must have to
     *            be included.
     * @throws IOException
     */
    public ReVerbRelationDictionaryFilter(int minFreq) throws IOException {
        init(getDefaultRelationStream(), minFreq);
    }

    /**
     * Constructs a new dictionary filter using the data in the file
     * <code>freq_rel.txt.gz</code>, and loads all relations with at least 20
     * distinct arg2s.
     *
     * @throws IOException
     */
    public ReVerbRelationDictionaryFilter() throws IOException {
        init(getDefaultRelationStream(), defaultMinFreq);
    }

    private InputStream getDefaultRelationStream() throws IOException {
        InputStream in = ResourceUtils.loadResource(relationDictFile,
                ReVerbRelationDictionaryFilter.class);
        // InputStream in =
        // ReVerbRelationDictionaryFilter.class.getClassLoader().getResourceAsStream(relationDictFile);
        if (in != null) {
            return new GZIPInputStream(in);
        } else {
            throw new IOException("Could not load file " + relationDictFile
                    + " from classpath.");
        }
    }

    private void init(InputStream in, int minFreq) throws IOException {
        String line;
        HashSet<String> relations = new HashSet<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        Pattern delim = Pattern.compile("\t");
        int lineNum = 0;
        while ((line = reader.readLine()) != null) {
            lineNum++;
            String[] fields = delim.split(line);
            if (fields.length != 2) {
                System.err.println("Could not read line " + lineNum + ": '"
                        + line + "'");
                continue;
            }
            int freq = Integer.parseInt(fields[0]);
            if (freq >= minFreq) {
                relations.add(fields[1]);
            }
        }
        filter = new NormalizedRelationDictionaryFilter(relations);
    }

    @Override
    public boolean doFilter(ChunkedExtraction extr) {
        return filter.doFilter(extr);
    }

}
