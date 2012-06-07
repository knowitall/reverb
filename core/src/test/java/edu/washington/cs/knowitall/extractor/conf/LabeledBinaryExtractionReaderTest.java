package edu.washington.cs.knowitall.extractor.conf;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Iterables;

import edu.washington.cs.knowitall.extractor.conf.LabeledBinaryExtraction;
import edu.washington.cs.knowitall.extractor.conf.LabeledBinaryExtractionReader;

public class LabeledBinaryExtractionReaderTest {
	
	public static final String inputString =
			"Bush was US President .\n" +
			"NNP VBD NNP NNP .\n" +
			"B-NP O B-NP I-NP O\n" +
			"Bush\n" + 
			"0 1\n" +
			"was\n" +
			"1 1\n" +
			"US President\n" +
			"2 2\n" +
			"1\n" +
			"Mike McGinn is the mayor of Seattle .\n" +
			"NNP NNP VBZ DT NN IN NNP .\n" +
			"B-NP I-NP O B-NP I-NP I-NP I-NP O\n" +
			"Mike\n" +
			"0 1\n" +
			"is\n" +
			"2 1\n" +
			"the mayor\n" +
			"3 2\n" +
			"0\n";

	@Test
	public void testReadExtractions() throws Exception {
		
		InputStream in = new ByteArrayInputStream(inputString.getBytes("UTF-8"));
		LabeledBinaryExtractionReader reader = new LabeledBinaryExtractionReader(in);
		List<LabeledBinaryExtraction> results = new ArrayList<LabeledBinaryExtraction>();
		Iterables.addAll(results, reader.readExtractions());
		
		assertEquals(2, results.size());
		
		LabeledBinaryExtraction e1 = results.get(0);
		LabeledBinaryExtraction e2 = results.get(1);
		
		assertEquals("Bush was US President .", e1.getSentence().getTokensAsString());
		assertEquals("Bush", e1.getArgument1().getTokensAsString());
		assertEquals("was", e1.getRelation().getTokensAsString());
		assertEquals("US President", e1.getArgument2().getTokensAsString());
		assertTrue(e1.isPositive());
		assertFalse(e1.isNegative());
		
		assertEquals("Mike McGinn is the mayor of Seattle .", e2.getSentence().getTokensAsString());
		assertEquals("Mike", e2.getArgument1().getTokensAsString());
		assertEquals("is", e2.getRelation().getTokensAsString());
		assertEquals("the mayor", e2.getArgument2().getTokensAsString());
		assertTrue(e2.isNegative());
		assertFalse(e2.isPositive());
		
	}
}
