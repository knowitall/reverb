package edu.washington.cs.knowitall.index;

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

/**
 * A class for accessing a Lucene index containing 
 * {@link NormalizedSpanExtraction} objects. This class wraps a Lucene 
 * {@link IndexSearcher} object and provides an additional method for 
 * converting an extraction from Lucene's representation (a {@link Document}
 * object) to a {@link NormalizedSpanExtraction}. The conversion process is
 * controlled by the {@link LuceneExtractionSerializer} class.
 * @author afader
 *
 */
public class ExtractionIndexSearcher extends IndexSearcher {
	
	/**
	 * {@see IndexSearcher} 
	 * @param path
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public ExtractionIndexSearcher(Directory path) throws CorruptIndexException, 
		IOException {
		super(path);
	}
	
	/**
	 * {@see IndexSearcher}
	 * @param path
	 * @param readOnly
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public ExtractionIndexSearcher(Directory path, boolean readOnly) 
		throws CorruptIndexException, IOException {
		super(path, readOnly);
	}
	
	/**
	 * {@see IndexSearcher}
	 * @param r
	 */
	public ExtractionIndexSearcher(IndexReader r) {
		super(r);
	}
	
	/**
	 * {@see IndexSearcher}
	 * @param reader
	 * @param subReaders
	 * @param docStarts
	 */
	public ExtractionIndexSearcher(IndexReader reader, IndexReader[] subReaders, 
			int[] docStarts) {
		super(reader, subReaders, docStarts);
	}
	
	/**
	 * Returns the document at position i as a NormalizedSpanExtraction
	 * @param i
	 * @return
	 * @throws CorruptIndexException
	 * @throws ExtractionFormatException
	 * @throws IOException
	 */
	public NormalizedSpanExtraction extraction(int i) 
		throws CorruptIndexException, ExtractionFormatException, IOException {
		return LuceneExtractionSerializer.fromDocument(doc(i));
	}

}
