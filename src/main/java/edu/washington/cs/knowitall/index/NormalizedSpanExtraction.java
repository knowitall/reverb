package edu.washington.cs.knowitall.index;

import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.washington.cs.knowitall.nlp.extraction.SpanExtraction;

/**
 * Represents a {@link SpanExtraction} whose fields have been normalized. Each
 * normalized field is represented as a {@link NormalizedField} object. This
 * class is useful for representing extractions that have undergone 
 * morphological normalization (e.g. stemming).
 * @author afader
 *
 */
public class NormalizedSpanExtraction extends SpanExtraction {
	
	private List<NormalizedField> normFields;
	private HashMap<String, NormalizedField> namedNormFields;
	
	/**
	 * Constructs a new NormalizedSpanExtraction from the given SpanExtraction
	 * and the given list of normalized fields. There must be the same number
	 * of fields in the extraction and the normFields list.
	 * @param extr
	 * @param normFields
	 */
	public NormalizedSpanExtraction(SpanExtraction extr, List<NormalizedField> normFields) {
		super(extr.getFields(), extr.getFieldNames());
		if (normFields.size() == getNumFields()) {
			this.normFields = ImmutableList.copyOf(normFields);
		} else {
			String msg = String.format("Expected %s normFields, got %s", getNumFields(), normFields.size());
			throw new IllegalArgumentException(msg);
		}
		namedNormFields = new HashMap<String, NormalizedField>(normFields.size());
		List<String> names = getFieldNames();
		for (int i = 0; i < names.size(); i++) {
			namedNormFields.put(names.get(i), normFields.get(i));
		}
		for (String propName : extr.getPropertyNames()) {
			setProperty(propName, extr.getProperty(propName));
		}
	}
	
	/**
	 * @return an immutable list of the normalized fields
	 */
	public List<NormalizedField> getNormalizedFields() {
		return normFields;
	}
	
	/**
	 * @param fieldName
	 * @return the normalized version of the given field name
	 */
	public NormalizedField getNormalizedField(String fieldName) {
		if (hasField(fieldName)) {
			return namedNormFields.get(fieldName);
		} else {
			throw new IllegalArgumentException("Invalid field name: " + fieldName);
		}
	}

}
