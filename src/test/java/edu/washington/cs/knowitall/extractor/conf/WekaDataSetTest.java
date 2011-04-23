package edu.washington.cs.knowitall.extractor.conf;

import static org.junit.Assert.*;

import org.junit.Test;

import weka.core.Instances;

public class WekaDataSetTest {

	@Test
	public void testGetWekaInstances() {
		Instances wekaInstances = StringFeatures.dataSet.getWekaInstances();
		assertEquals(4, wekaInstances.numAttributes());
		assertEquals(4, wekaInstances.numInstances());
		assertEquals(2, wekaInstances.numClasses());
		assertEquals(3, wekaInstances.classIndex());
	}

}
