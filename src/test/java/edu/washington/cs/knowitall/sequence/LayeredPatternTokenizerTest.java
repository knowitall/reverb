package edu.washington.cs.knowitall.sequence;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import edu.washington.cs.knowitall.sequence.LayeredPatternTokenizer;

public class LayeredPatternTokenizerTest {
private LayeredPatternTokenizer tokenizer;
	
	@Before
	public void setUp() {
		tokenizer = new LayeredPatternTokenizer();
	}

	@Test
	public void testTokenize() throws SequenceException {
		
		String pattern = "Hello_token";
		String[] tokens = tokenizer.tokenize(pattern);
		assertArrayEquals(new String[]{"Hello_token"}, tokens); 
		
		pattern = "   Hello_token ";
		tokens = tokenizer.tokenize(pattern);
		assertArrayEquals(new String[]{"Hello_token"}, tokens);
		
		pattern = "Foo_0*";
		tokens = tokenizer.tokenize(pattern);
		assertArrayEquals(new String[]{"Foo_0", "*"}, tokens);
		
		pattern = "( Foo_0 * Bar_1)+";
		tokens = tokenizer.tokenize(pattern);
		assertArrayEquals(new String[]{"(", "Foo_0", "*", "Bar_1", ")", "+"}, tokens);
		
		pattern = "(Foo_bar baz)";
		try{
			tokens = tokenizer.tokenize(pattern);
			fail();
		} catch (SequenceException e) {
			assertTrue(true);
		}
		
		pattern ="Hello_word (? world_word)";
		tokens = tokenizer.tokenize(pattern);
		assertArrayEquals(new String[]{"Hello_word", "(", "?", "world_word", ")"}, tokens);
		
		pattern = "( B-NP_np I-NP_np* (? B-NP_np I-NP_np*))";
		tokens = tokenizer.tokenize(pattern);
		assertArrayEquals(new String[] {
				"(", "B-NP_np", "I-NP_np", "*", "(", "?", "B-NP_np",
				"I-NP_np", "*", ")", ")"
		}, tokens);
		
		
		
	}
}
