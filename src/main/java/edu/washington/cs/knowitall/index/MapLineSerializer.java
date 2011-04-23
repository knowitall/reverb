package edu.washington.cs.knowitall.index;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * A class used to serialize String key/value pairs to a single, human 
 * readable String. Given a map of key/value pairs, this class can convert
 * it to the String "key1<TAB>val1<TAB>key2<TAB>val2<TAB>...". Also provides
 * a method for reading from that format into a HashMap. Assumes that the 
 * keys and values themselves do not contain tabs or newlines. 
 * @author afader
 *
 */
public class MapLineSerializer {
	
	/**
	 * @param map
	 * @return the String version of the given map.
	 */
	public static String serialize(Map<String, String> map) {
		String[] pairs = new String[map.size()];
		int i = 0;
		for (String key : map.keySet()) {
			pairs[i] = String.format("%s\t%s", key, map.get(key));
			i++;
		}
		return StringUtils.join(pairs, '\t');
	}
	
	/**
	 * @param line
	 * @return the Map<String,String> representation of the given line.
	 */
	public static Map<String, String> deserialize(String line) {
		String[] fields = line.split("\t");
		if (fields.length % 2 == 0) {
			HashMap<String,String> result = new HashMap<String,String>(fields.length/2);
			for (int i = 0; i < fields.length; i += 2) {
				result.put(fields[i], fields[i+1]);
			}
			return result;
		} else {
			throw new IllegalArgumentException("Invalid number of fields: " + line);
		}
	}

}
