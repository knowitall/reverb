package edu.washington.cs.knowitall.streaming;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import com.google.common.collect.AbstractIterator;

import edu.washington.cs.knowitall.index.ExtractionFormatException;
import edu.washington.cs.knowitall.index.ExtractionSerializer;
import edu.washington.cs.knowitall.index.NormalizedSpanExtraction;
import edu.washington.cs.knowitall.io.BufferedReaderIterator;

/**
 * A class used for reading serialized {@link NormalizedSpanExtraction} objects
 * from an input stream. This class assumes that the extractions have been
 * serialized using the {@link ExtractionSerializer} class.
 * @author afader
 */
public class StreamingExtractionIterator 
	extends AbstractIterator<NormalizedSpanExtraction> {
	
	private Iterator<String> lineIter;
	
	/**
	 * Constructs a new object for reading extractions from the given 
	 * stream
	 * @param in
	 * @throws IOException
	 */
	public StreamingExtractionIterator(InputStream in) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		lineIter = new BufferedReaderIterator(r);
	}

	protected NormalizedSpanExtraction computeNext() {
		while (lineIter.hasNext()) {
			String line = lineIter.next();
			try {
				return ExtractionSerializer.fromString(line);
			} catch (ExtractionFormatException e) {
				continue;
			} 
		}
		return endOfData();
	}

}
