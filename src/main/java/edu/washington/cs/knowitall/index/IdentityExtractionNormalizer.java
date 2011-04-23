package edu.washington.cs.knowitall.index;

import java.util.ArrayList;
import java.util.List;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;
import edu.washington.cs.knowitall.nlp.extraction.SpanExtraction;

/**
 * A {@link SpanExtractionNormalizer} that returns the un-normalized input.
 * @author afader
 *
 */
public class IdentityExtractionNormalizer implements SpanExtractionNormalizer {
	
	private IdentityFieldNormalizer id;
	
	public IdentityExtractionNormalizer() {
		id = new IdentityFieldNormalizer();
	}

	@Override
	public NormalizedSpanExtraction normalizeExtraction(SpanExtraction extr) {
		List<NormalizedField> normFields = new ArrayList<NormalizedField>(extr.getNumFields());
		for (ChunkedExtraction field : extr.getFields()) {
			normFields.add(id.normalizeField(field));
		}
		return new NormalizedSpanExtraction(extr, normFields);
	}

}
