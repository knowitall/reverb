package edu.washington.cs.knowitall.index;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class MapLineSerializerTest {

	@Test
	public void testSerialize() {
		Map<String,String> map = new HashMap<String,String>();
		map.put("hello", "world");
		map.put("name", "Joe");
		assertEquals(map, 
			MapLineSerializer.deserialize(MapLineSerializer.serialize(map)));
		
		String line = "hello\tworld\tname\tJoe";
		assertEquals(line,
			MapLineSerializer.serialize(MapLineSerializer.deserialize(line)));
	}

}
