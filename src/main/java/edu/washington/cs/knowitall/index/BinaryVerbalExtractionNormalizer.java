package edu.washington.cs.knowitall.index;

import java.util.ArrayList;
import java.util.List;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;
import edu.washington.cs.knowitall.nlp.extraction.SpanExtraction;

/**
 * Normalizes a {@link SpanExtraction} object that has three fields, assuming
 * that the first field is a noun phrase, the second field is a verbal realtion,
 * and the third field is a noun phrase. Uses {@link HeadNounNormalizer} to
 * normalize the noun phrase arguments and a {@link VerbalRelationNormalizer}
 * to normalize the verbal relation. 
 * @author afader
 *
 */
public class BinaryVerbalExtractionNormalizer implements
		SpanExtractionNormalizer {
	
	private VerbalRelationNormalizer relNormalizer;
	private HeadNounNormalizer argNormalizer;
	private IdentityFieldNormalizer idNormalizer;
	
	/**
	 * Constructs a new normalizer.
	 */
	public BinaryVerbalExtractionNormalizer() {
		this.relNormalizer = new VerbalRelationNormalizer();
		this.argNormalizer = new HeadNounNormalizer();
		this.idNormalizer = new IdentityFieldNormalizer();
	}

	/**
	 * Normalizes the given extraction. The extraction does not have three
	 * fields, then applies a {@link IdentityNormalizer} to each field. 
	 */
	public NormalizedSpanExtraction normalizeExtraction(SpanExtraction extr) {
		if (extr.getNumFields() == 3) {
			return normalizeBinary(extr);
		} else {
			return identityNormalize(extr);
		}
	}
	
	private NormalizedSpanExtraction normalizeBinary(SpanExtraction extr) {
		List<NormalizedField> fields = new ArrayList<NormalizedField>(3);
		fields.add(argNormalizer.normalizeField(extr.getField(0)));
		fields.add(relNormalizer.normalizeField(extr.getField(1)));
		fields.add(argNormalizer.normalizeField(extr.getField(2)));
		return new NormalizedSpanExtraction(extr, fields);
	}
	
	private NormalizedSpanExtraction identityNormalize(SpanExtraction extr) {
		List<NormalizedField> fields = new ArrayList<NormalizedField>(extr.getNumFields());
		for (ChunkedExtraction field : extr.getFields()) {
			fields.add(idNormalizer.normalizeField(field));
		}
		return new NormalizedSpanExtraction(extr, fields);
	}

}
