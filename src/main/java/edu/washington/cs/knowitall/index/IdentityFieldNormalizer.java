package edu.washington.cs.knowitall.index;

import java.util.List;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;
import edu.washington.cs.knowitall.sequence.SequenceException;

/**
 * A field normalizer that returns the un-normalized input field.
 * @author afader
 *
 */
public class IdentityFieldNormalizer implements FieldNormalizer {

	@Override
	public NormalizedField normalizeField(ChunkedExtraction field) {
		List<String> tokens = field.getTokens();
		List<String> posTags = field.getPosTags();
		try {
			return new NormalizedField(field, tokens, posTags);
		} catch (SequenceException e) {
			String msg = String.format(
					"tokens and posTags are not the same length for field %s", 
					field);
			throw new IllegalStateException(msg, e);
		}
	}

}
