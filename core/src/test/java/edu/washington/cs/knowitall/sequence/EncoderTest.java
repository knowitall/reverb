package edu.washington.cs.knowitall.sequence;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import edu.washington.cs.knowitall.sequence.Encoder;

public class EncoderTest {

	
	private HashSet<String> words, pos, np;
	private List<Set<String>> sets;

	@Before
	public void setUp() throws Exception {
		
		sets = new ArrayList<Set<String>>();
		
		words = new HashSet<String>();
		words.add("There");
		words.add("are");
		sets.add(words);
		
		pos = new HashSet<String>();
		pos.add("IN");
		pos.add("CD");
		pos.add("DT");
		sets.add(pos);
		
		np = new HashSet<String>();
		np.add("BNP");
		np.add("INP");
		sets.add(np);
		
	}

	@Test
	public void testEncoder() throws SequenceException {
		Encoder encoder = new Encoder(sets);
		
		assertEquals(3, encoder.size());
		
		assertEquals(36, encoder.tableSize());
		
	}
}
