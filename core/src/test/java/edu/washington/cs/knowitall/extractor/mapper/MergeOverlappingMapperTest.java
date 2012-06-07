package edu.washington.cs.knowitall.extractor.mapper;


import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.washington.cs.knowitall.extractor.ExtractorUnion;
import edu.washington.cs.knowitall.extractor.RegexExtractor;
import edu.washington.cs.knowitall.extractor.mapper.MergeOverlappingMapper;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;

public class MergeOverlappingMapperTest {
	
	ExtractorUnion<ChunkedSentence, ChunkedExtraction> e1;
	ExtractorUnion<ChunkedSentence, ChunkedExtraction> e2;
	ExtractorUnion<ChunkedSentence, ChunkedExtraction> e3;

	@Before
	public void setUp() throws Exception {
		RegexExtractor r1 = new RegexExtractor("wants_tok to_tok");
		RegexExtractor r2 = new RegexExtractor("go_tok to_tok");
		RegexExtractor r3 = new RegexExtractor("to_tok go_tok to_tok");
		
		e1 = new ExtractorUnion<ChunkedSentence, ChunkedExtraction>();
		e1.addExtractor(r1);
		e1.addExtractor(r2);
		e1.addMapper(new MergeOverlappingMapper());
		
		e2 = new ExtractorUnion<ChunkedSentence, ChunkedExtraction>();
		e2.addExtractor(r1);
		e2.addExtractor(r3);
		e2.addMapper(new MergeOverlappingMapper());
		
		e3 = new ExtractorUnion<ChunkedSentence, ChunkedExtraction>();
		e3.addExtractor(r2);
		e3.addExtractor(r3);
		e3.addMapper(new MergeOverlappingMapper());
	}
	
	@Test
	public void testMerge() throws Exception {
		ChunkedSentence sent = new ChunkedSentence(
			new String[] { "He", "wants", "to", "go", "to", "the", "store", "." },
            new String[] { "PRP", "VBZ", "TO", "VB", "TO", "DT", "NN", "." },
            new String[] { "B-NP", "O", "O", "O", "O", "B-NP", "I-NP", "O" }
        );
		
		String result1 = e1.extract(sent).iterator().next().toString();
		assertEquals("wants to go to", result1);
		
		String result2 = e2.extract(sent).iterator().next().toString();
		assertEquals("wants to go to", result2);
		
		String result3 = e3.extract(sent).iterator().next().toString();
		assertEquals("to go to", result3);
		
	}

}
