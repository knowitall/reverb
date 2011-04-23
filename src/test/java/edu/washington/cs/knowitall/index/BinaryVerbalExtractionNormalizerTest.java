package edu.washington.cs.knowitall.index;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedArgumentExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;

public class BinaryVerbalExtractionNormalizerTest {
	
	private ChunkedBinaryExtraction extr;
	
	private List<String> list(String s) {
		return Arrays.asList(s.split(" "));
	}

	@Before
	public void setUp() throws Exception {
		
		ChunkedSentence sent = new ChunkedSentence(
			list("President Obama has been known to enjoy hot wings ."),
			list("NNP NNP VBZ VBN VBN TO VB JJ NNS ."),
			list("B-NP I-NP O O O O O B-NP I-NP O")
		);
		
		ChunkedExtraction rel = new ChunkedExtraction(sent, new Range(2, 5));
		ChunkedArgumentExtraction arg1 = new ChunkedArgumentExtraction(sent, new Range(0, 2), rel);
		ChunkedArgumentExtraction arg2 = new ChunkedArgumentExtraction(sent, new Range(7, 2), rel);
		
		extr = new ChunkedBinaryExtraction(rel, arg1, arg2);
		
		
	}

	@Test
	public void testNormalizeExtraction() {
		BinaryVerbalExtractionNormalizer normalizer = new BinaryVerbalExtractionNormalizer();
		NormalizedSpanExtraction norm = normalizer.normalizeExtraction(extr);
		List<NormalizedField> normFields = norm.getNormalizedFields();
		assertEquals(3, normFields.size());
		assertEquals("President Obama", normFields.get(0).toString());
		assertEquals("know to enjoy", normFields.get(1).toString());
		assertEquals("wing", normFields.get(2).toString());
	}

}
