package edu.washington.cs.knowitall.extractor.conf;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.Test;

import edu.washington.cs.knowitall.extractor.conf.LabeledBinaryExtraction;
import edu.washington.cs.knowitall.extractor.conf.LabeledBinaryExtractionReader;
import edu.washington.cs.knowitall.extractor.conf.LabeledBinaryExtractionWriter;

public class LabeledBinaryExtractionWriterTest {
	
	@Test
	public void testWriteExtraction() throws Exception {
		
		String inputString = LabeledBinaryExtractionReaderTest.inputString;
		InputStream in = new ByteArrayInputStream(inputString.getBytes("UTF-8"));
		LabeledBinaryExtractionReader reader = new LabeledBinaryExtractionReader(in);
		Iterable<LabeledBinaryExtraction> extrs = reader.readExtractions();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		LabeledBinaryExtractionWriter writer = new LabeledBinaryExtractionWriter(baos);
		writer.writeExtractions(extrs);
		
		String result = baos.toString();
		assertEquals(inputString, result);
		
	}

}
