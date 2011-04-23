package edu.washington.cs.knowitall.streaming;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.washington.cs.knowitall.index.LuceneExtractionSerializer;
import edu.washington.cs.knowitall.index.NormalizedSpanExtraction;

/**
 * A class used to take a stream of {@link NormalizedSpanExtraction} objects
 * and write them to a Lucene index. The extractions are converted to 
 * Lucene {@link Document} objects using the {@link LuceneExtractionSerializer}
 * class.
 * @author afader
 *
 */
public class ExtractionIndexer {
	
	/**
	 * Reads {@link NormalizedSpanExtraction} objects from standard input
	 * and writes them to the index at the path given by args[0].
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		
		if (args.length != 1) {
			throw new IllegalArgumentException("Expected directory arg");
		}
		
		// Open the index for writing
		File dirPath = new File(args[0]);
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
		Directory dir = FSDirectory.open(dirPath);
		IndexWriter iwriter = new IndexWriter(dir, analyzer, true, 
				IndexWriter.MaxFieldLength.LIMITED);
		
		writeToIndex(System.in, iwriter);
		iwriter.optimize();
		iwriter.close();
		
	}
	
	/**
	 * Reads {@link NormalizedSpanExtraction} objects from in and writes them
	 * to the index using iwriter. 
	 * @param in
	 * @param iwriter
	 * @throws IOException
	 */
	public static void writeToIndex(InputStream in, IndexWriter iwriter) 
		throws IOException {
		StreamingExtractionIterator iter = new StreamingExtractionIterator(in);
		while (iter.hasNext()) {
			NormalizedSpanExtraction extr = iter.next();
			iwriter.addDocument(LuceneExtractionSerializer.toDocument(extr));
		}
	}

}
