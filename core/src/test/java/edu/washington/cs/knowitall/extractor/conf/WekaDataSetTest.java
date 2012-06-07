package edu.washington.cs.knowitall.extractor.conf;

import static org.junit.Assert.*;

import java.util.List;

import opennlp.model.Event;

import org.junit.Test;

public class WekaDataSetTest {

	@Test
	public void testGetWekaInstances() {
		List<Event> events = StringFeatures.dataSet.getInstances();
		assertEquals(4, events.size());
	}

}
