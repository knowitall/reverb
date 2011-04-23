package edu.washington.cs.knowitall.streaming;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.washington.cs.knowitall.index.NormalizedSpanExtraction;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;

/**
 * A class used to provide a human-readable view of the extractions coming from
 * standard input.
 * @author afader
 *
 */
public class ExtractionViewer {
	
	/**
	 * Reads serialized {@link NormalizedSpanExtraction} objects from standard
	 * input and prints out human-readable summaries.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Iterator<NormalizedSpanExtraction> iter = 
			new StreamingExtractionIterator(System.in);
		while (iter.hasNext()) {
			System.out.println(extrToString(iter.next()));
			System.out.println();
		}
		
	}
	
	/**
	 * @param extr
	 * @return a multi-line string summarizing extr
	 */
	public static String extrToString(NormalizedSpanExtraction extr) {
		List<String> lines = new ArrayList<String>();
		ChunkedSentence sent = extr.getSentence();
		lines.add(sent.getTokensAsString());
		for (int i = 0; i < extr.getNumFields(); i++) {
			ChunkedExtraction field = extr.getField(i);
			String name = extr.getFieldName(i);
			String value = field.getTokensAsString();
			lines.add(String.format("\t%s = %s", name, value));
		}
		for (String propName : extr.getPropertyNames()) {
			String propVal = extr.getProperty(propName);
			lines.add(String.format("\t%s = %s", propName, propVal));
		}
		return StringUtils.join(lines.iterator(), "\n");
	}

}
