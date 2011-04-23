package edu.washington.cs.knowitall.extractor.conf;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

public class BooleanFeatureSetTest {

	@Test
	public void testFeaturize() {
		String jane = "jane";
		Map<String, Boolean> janeFeats = StringFeatures.featureSet.featurize(jane);
		assertEquals(3, janeFeats.size());
		assertTrue(janeFeats.get("contains e"));
		assertTrue(janeFeats.get("starts with j"));
		assertTrue(janeFeats.get("longer than 3"));
		
		String joe = "joe";
		Map<String, Boolean> joeFeats = StringFeatures.featureSet.featurize(joe);
		assertEquals(3, joeFeats.size());
		assertTrue(joeFeats.get("contains e"));
		assertTrue(joeFeats.get("starts with j"));
		assertFalse(joeFeats.get("longer than 3"));
	}

	@Test
	public void testFeaturizeToDouble() {
		double[] expectedJane = { 1.0, 1.0, 1.0 };
		double[] gotJane = StringFeatures.featureSet.featurizeToDouble("jane");
		assertArrayEquals(expectedJane, gotJane, 0.001);
		
		double[] expectedJoe = { 1.0, 0.0, 1.0 };
		double[] gotJoe = StringFeatures.featureSet.featurizeToDouble("joe");
		assertArrayEquals(expectedJoe, gotJoe, 0.001);
	}

	@Test
	public void testFeaturizeToBool() {
		boolean[] expectedJane = { true, true, true };
		boolean[] gotJane = StringFeatures.featureSet.featurizeToBool("jane");
		assertEquals(3, gotJane.length);
		for (int i = 0; i < 3; i++) assertEquals(expectedJane[i], gotJane[i]);
		
		boolean[] expectedJoe = { true, false, true };
		boolean[] gotJoe = StringFeatures.featureSet.featurizeToBool("joe");
		assertEquals(3, gotJoe.length);
		for (int i = 0; i < 3; i++) assertEquals(expectedJoe[i], gotJoe[i]);
	}

}
