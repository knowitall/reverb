package edu.washington.cs.knowitall.sequence;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import edu.washington.cs.knowitall.sequence.LayeredTokenPattern;
import edu.washington.cs.knowitall.sequence.RegexTagger;
import edu.washington.cs.knowitall.sequence.SimpleLayeredSequence;

public class RegexTaggerTest {
	
	public static List<String> listize(String s) {
		String[] split = s.split(" ");
		List<String> results = new ArrayList<String>();
		for (String str: split) results.add(str);
		return results;
	}
	
	public List<String> extract(String patternStr, String test) throws SequenceException {
		LayeredTokenPattern pattern = new LayeredTokenPattern(patternStr);
		RegexTagger tagger = new RegexTagger(pattern, "R");
		List<String> testList = listize(test);
		SimpleLayeredSequence seq = new SimpleLayeredSequence(testList.size());
		seq.addLayer("w", testList);
		return tagger.tag(seq);
	}

	@Test
	public void testTag1() throws SequenceException {
		String patternStr = "[she_w saw_w sea_w shells_w]";
		List<String> result = extract(patternStr, "she saw sea shells by the sea thing");
		List<String> expected = listize("R R R R O O R O");
		assertEquals(expected, result);
	}
	
	@Test
	public void testTag2() throws SequenceException {
		String patternStr = ". saw_w .";
		List<String> result = extract(patternStr, "she saw sea shells by the sea thing");
		List<String> expected = listize("R R R O O O O O");
		assertEquals(expected, result);
	}

}
