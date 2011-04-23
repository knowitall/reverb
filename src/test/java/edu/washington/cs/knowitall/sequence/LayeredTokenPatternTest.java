package edu.washington.cs.knowitall.sequence;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import edu.washington.cs.knowitall.nlp.OpenNlpSentenceChunker;
import edu.washington.cs.knowitall.sequence.LayeredTokenMatcher;
import edu.washington.cs.knowitall.sequence.LayeredTokenPattern;
import edu.washington.cs.knowitall.sequence.SimpleLayeredSequence;

public class LayeredTokenPatternTest {

	
	private SimpleLayeredSequence seq;
	
	public List<String> split(String s) {
		String[] toks = s.split(" ");
		ArrayList<String> result = new ArrayList<String>(toks.length);
		for (String t:toks) result.add(t);
		return result;
	}

	@Before
	public void setUp() throws Exception {
		
		String[] words = "There are 5 kinds of owls .".split(" ");
		String[] pos = "EX VBP CD NNS IN NNS .".split(" ");
		String[] np = "O O B-NP I-NP O B-NP O".split(" ");
		
		seq = new SimpleLayeredSequence(words.length);
		seq.addLayer("w", words);
		seq.addLayer("p", pos);
		seq.addLayer("n", np);
	}

	@Test
	public void testMatcher1() throws SequenceException {
		String patternStr = "There_w are_w CD_p [B-NP_n I-NP_n]+ (IN_p [B-NP_n I-NP_n]+)*";
		LayeredTokenPattern pat = new LayeredTokenPattern(patternStr);
		LayeredTokenMatcher m = pat.matcher(seq);
		assertTrue(m.find());
		assertEquals(0, m.start());
		assertEquals(6, m.end());
	}
	
	@Test
	public void testMatcher2() throws SequenceException {
		String patternStr = "B-NP_n I-NP_n*";
		LayeredTokenPattern pat = new LayeredTokenPattern(patternStr);
		LayeredTokenMatcher m = pat.matcher(seq);
		assertTrue(m.find());
		assertEquals(2, m.start());
		assertEquals(4, m.end());
		assertTrue(m.find());
		assertEquals(5, m.start());
		assertEquals(6, m.end());
		assertFalse(m.find());
	}
	
	@Test
	public void testMatcher3() throws SequenceException {
		String patternStr = "B-NP_n I-NP_n* ._p?$";
		LayeredTokenPattern pat = new LayeredTokenPattern(patternStr);
		LayeredTokenMatcher m = pat.matcher(seq);
		assertTrue(m.find());
		assertEquals(5, m.start());
		assertEquals(7, m.end());
		assertFalse(m.find());
	}
	
	@Test
	public void testMatcher4() throws SequenceException {
		String patternStr = "...";
		LayeredTokenPattern pat = new LayeredTokenPattern(patternStr);
		LayeredTokenMatcher m = pat.matcher(seq);
		assertTrue(m.find());
		assertEquals(0, m.start());
		assertEquals(3, m.end());
		assertTrue(m.find());
		assertEquals(3, m.start());
		assertEquals(6, m.end());
		assertFalse(m.find());
	}
	
	@Test(expected=SequenceException.class)
	public void testMatcher5() throws SequenceException {
		String patternStr = "^ [^A_x B_x] C_x $";
		@SuppressWarnings("unused")
		LayeredTokenPattern pat = new LayeredTokenPattern(patternStr);
	}
	
	@Test(expected=SequenceException.class)
	public void testMatcher6() throws Exception {

		String patternStr = "B-NP_np I-NP_np* from_word the_word B-NP_np I-NP_np*";
		LayeredTokenPattern pattern = new LayeredTokenPattern(patternStr);
		OpenNlpSentenceChunker chunker = new OpenNlpSentenceChunker();		
		pattern.matcher(chunker.chunkSentence("Hello, world."));
		
	}
}
