package edu.washington.cs.knowitall.index;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

import static edu.washington.cs.knowitall.index.ExtractionSerializer.FIELD_TOKENS;
import static edu.washington.cs.knowitall.index.ExtractionSerializer.FIELD_NORM_TOKENS;

/**
 * <p>
 * This class is used to convert between {@link NormalizedSpanExtraction} 
 * objects and Lucene {@link Document} objects. This class uses the field
 * names described in the documentation of {@ExtractionSe
 * </p> 
 * <p>
 * By default, all fields of the resulting document are set to be stored in the
 * index. The only indexed/analyzed fields are {@code fieldTokens} and 
 * {@code fieldNormTokens}. 
 * </p>
 * <p>
 * This class also includes two non-stored, indexed fields: {@code allTokens} 
 * and {@code allNormTokens}. These fields contain all of the fields' tokens
 * and normalized tokens, respectively. They are included as a shortcut for
 * searching all of the extractions' fields at once.  
 * </p>
 * @author afader
 *
 */
public class LuceneExtractionSerializer {
	
	public static final String ALL_TOKENS = "allTokens";
	public static final String ALL_NORM_TOKENS = "allNormTokens";
	
	/**
	 * @param extr
	 * @return a Lucene Document representation of the given extraction
	 */
	public static Document toDocument(NormalizedSpanExtraction extr) {
		Map<String,String> map = ExtractionSerializer.toMap(extr);
		Document doc = new Document();
		
		// Add each field
		StringBuffer allTokens = new StringBuffer();
		StringBuffer allNormTokens = new StringBuffer();
		for (int i = 0; i < extr.getNumFields(); i++) {
			String tokens = map.remove(FIELD_TOKENS + i);
			String tokensNorm = map.remove(FIELD_NORM_TOKENS + i);
			doc.add(indexedField(FIELD_TOKENS+i, tokens));
			doc.add(indexedField(FIELD_NORM_TOKENS+i, tokensNorm));
			allTokens = allTokens.append(" ").append(tokens);
			allNormTokens = allNormTokens.append(" ").append(tokensNorm);
		}
		
		// Add the allTokens and allNormTokens fields
		doc.add(indexedNonStoredField(ALL_TOKENS, 
				allTokens.toString()));
		doc.add(indexedNonStoredField(ALL_NORM_TOKENS, 
				allNormTokens.toString()));
		
		// Add any remaining properties
		for (String key : map.keySet()) {
			doc.add(nonIndexedField(key, map.get(key)));
		}
		
		return doc;
	}
	
	/**
	 * @param doc
	 * @return the NormalizedSpanExtraction representation of the given Lucene
	 * document
	 * @throws ExtractionFormatException if unable to convert the Document
	 */
	public static NormalizedSpanExtraction fromDocument(Document doc) 
		throws ExtractionFormatException {
		List<Fieldable> fields = doc.getFields();
		Map<String,String> map = new HashMap<String,String>(fields.size());
		for (Fieldable field : fields) {
			String name = field.name();
			String value = field.stringValue();
			map.put(name,value);
		}
		return ExtractionSerializer.fromMap(map);
	}
	
	/**
	 * @param name
	 * @param value
	 * @return a new indexed field with the given name and value.
	 */
	private static Field indexedField(String name, String value) {
		return new Field(name, value, Field.Store.YES, Field.Index.ANALYZED);
	}
	
	/**
	 * @param name
	 * @param value
	 * @return a new non-indexed field with the given name and value
	 */
	private static Field nonIndexedField(String name, String value) {
		return new Field(name, value, Field.Store.YES, Field.Index.NO);
	}
	
	/**
	 * @param name
	 * @param value
	 * @return a new indexed, non-stored field
	 */
	private static Field indexedNonStoredField(String name, String value) {
		return new Field(name, value, Field.Store.NO, Field.Index.ANALYZED);
	}

}
