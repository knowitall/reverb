package edu.washington.cs.knowitall.streaming;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;

import edu.washington.cs.knowitall.extractor.ExtractorException;
import edu.washington.cs.knowitall.extractor.ReVerbExtractor;
import edu.washington.cs.knowitall.extractor.conf.ConfidenceFunctionException;
import edu.washington.cs.knowitall.extractor.conf.ReVerbConfFunction;
import edu.washington.cs.knowitall.index.BinaryVerbalExtractionNormalizer;
import edu.washington.cs.knowitall.index.ExtractionSerializer;
import edu.washington.cs.knowitall.index.NormalizedSpanExtraction;
import edu.washington.cs.knowitall.io.BufferedReaderIterator;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.OpenNlpChunkedSentenceParser;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;

/**
 * <p>
 * A class for taking an input stream of (url, chunked sentence) pairs, running
 * ReVerb on them, and then returning a String representation of the resulting
 * extraction. It sets two properties in each extraction that is returned:
 * the confidence (assigned by {@link ReVerbConfFunction}) and the url.
 * </p>
 * <p>
 * Each input line should be in the form "url<TAB>sentence", where sentence
 * is in the OpenNLP chunked sentence representation (see
 * {@link OpenNlpChunkedSentenceParser} for details on this format). This
 * input format can be generated using the {@link StreamingWarcChunker} class, which
 * takes web pages in WARC format as an input stream, and returns 
 * (url, chunked sentence) pairs. 
 * </p>
 * <p>
 * The output extractions are serialized into Strings using the 
 * {@link ExtractionSerializer} class. These objects can then be deserialized
 * using the same class.
 * </p>
 * @author afader
 *
 */
public class StreamingReVerb {
	
	private static final String NAME = "ReVerb";


	/**
	 * The main class to be called from the command line.
	 * @param args
	 * @throws ConfidenceFunctionException 
	 * @throws Exception
	 */
	public static void main(String[] args) 
		throws IOException, ExtractorException, ConfidenceFunctionException {
		
		// The objects used for extracting, scoring, normalizing
		ReVerbExtractor extractor = new ReVerbExtractor();
		ReVerbConfFunction confFunction = new ReVerbConfFunction();
		BinaryVerbalExtractionNormalizer normalizer = 
			new BinaryVerbalExtractionNormalizer();
		
		// Iterates over the input lines
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		BufferedReaderIterator lineIter = new BufferedReaderIterator(r);
		
		// Converts string sentences into ChunkedSentence objects
		OpenNlpChunkedSentenceParser parser = new OpenNlpChunkedSentenceParser();
		
		while (lineIter.hasNext()) {
			
			String[] fields = lineIter.next().split("\t");
			if (fields.length != 2) {
				continue;
			}
				
			String url = fields[0];
			String sentStr = fields[1];
				
			try {
				
				// Parse the sentence, run the extractor, compute the
				// confidence, and then print the resulting extraction as 
				// a string to standard output
				ChunkedSentence sent = parser.parseSentence(sentStr);
				for (ChunkedBinaryExtraction extr : extractor.extract(sent)) {
					NormalizedSpanExtraction nextr = normalizer.normalizeExtraction(extr);
					nextr.setProperty("extractorName", NAME);
					nextr.setProperty("url", url);
					nextr.setProperty("conf", 
							Double.toString(confFunction.getConf(extr)));
					System.out.println(ExtractionSerializer.toString(nextr));
				}
				
			} catch (ParseException e) {
				continue;
			} catch (ConfidenceFunctionException e) {
				continue;
			} catch (ExtractorException e) {
				continue;
			}
			
		}
		
	}
	

}
