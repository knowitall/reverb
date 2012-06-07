package edu.washington.cs.knowitall.normalization;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;
import edu.washington.cs.knowitall.normalization.NormalizedField;
import edu.washington.cs.knowitall.normalization.VerbalRelationNormalizer;

public class VerbalRelationNormalizerTest {
	
	private static VerbalRelationNormalizer normalizer;

	@Before
	public void setUp() throws Exception {
		normalizer = new VerbalRelationNormalizer();
	}
	
	private static void assertNorm(String expectedStr, String tokensStr, String posTagsStr) throws Exception {
		List<String> tokens = Arrays.asList(tokensStr.split(" "));
		List<String> posTags = Arrays.asList(posTagsStr.split(" "));
		List<String> npChunkTags = new ArrayList<String>(posTags.size());
		for (int i = 0; i < posTags.size(); i++) npChunkTags.add("O");
		
		ChunkedSentence sent = new ChunkedSentence(tokens, posTags, npChunkTags);
		ChunkedExtraction extr = new ChunkedExtraction(sent, new Range(0, posTags.size()));
		
		NormalizedField normField = normalizer.normalizeField(extr);
		String resultStr = normField.toString();
		assertEquals(expectedStr, resultStr);
	}

	@Test
	public void testNormalize() throws Exception {
		assertNorm("attach to", "was attached to", "VBD VBN TO");
		assertNorm("drive", "drove", "VBD");
		assertNorm("be in", "was in", "VBD IN");
		assertNorm("incorporate into", "are incorporated into", "VBP VBN IN");
		assertNorm("take advantage of", "took advantage of", "VBD NN IN");
		assertNorm("be time for", "is the perfect time for", "VBZ DT JJ NN IN");
		assertNorm("be result of", "are the result of", "VBD DT NN IN");
		assertNorm("be liaison between", "will be the liaison between", "MD VB DT NN IN");
		assertNorm("purchase at", "can be purchased at", "MD VB VBN IN");
		assertNorm("delete from", "cannot be deleted from", "MD VB VBN IN");
		assertNorm("be", "can n't be", "MD RB VB");
		assertNorm("require level of", "also requires a high level of", "RB VBZ DT JJ NN IN");
		assertNorm("take kid to", "took my kids to", "VBD PRP$ NNS TO");
		assertNorm("ravage by", "has been ravaged by", "VBZ VBN VBN IN");
		assertNorm("have nothing to do with", "has nothing to do with", "VBZ NN TO VB IN");
		assertNorm("be high in", "are high in", "VBZ JJ IN");
		assertNorm("be high in", "are very high in", "VBZ RB JJ IN");
		assertNorm("look high in", "looks very high in", "VBZ RB JJ IN");
		assertNorm("be taller than", "is taller than", "VBZ JJR IN");
		assertNorm("be source of", "is an excellent source of", "VBZ DT JJ NN IN");
	}

}