package edu.washington.cs.knowitall.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;
import edu.washington.cs.knowitall.nlp.extraction.SpanExtraction;
import edu.washington.cs.knowitall.sequence.SequenceException;

/**
 * A utility class for serializing a {@link NormalizedSpanExtraction} object
 * into human-readable Strings and (key,value) maps. This class uses the 
 * following fields to represent an extraction:
 * <ul>
 * <li>{@code sentTokens} - the sentence tokens (words)</li>
 * <li>{@code sentPosTags} - the sentence part of speech tags</li>
 * <li>{@code sentChunkTags} - the sentence chunk tags (in B/I/O encoding)</li>
 * <li>{@code numFields} - the number of extraction fields (e.g. a binary
 * relation will have 3 fields - the two arguments the relation phrase)</li>
 * </ul>
 * For each field in the extraction, the following fields are added to the 
 * document:
 * <ul>
 * <li>{@code fieldNameN} - the name of the Nth field</li>
 * <li>{@code fieldTokensN} - the tokens of the Nth field</li>
 * <li>{@code fieldStartN} - the start index of the Nth field in the 
 * sentence</li>
 * <li>{@code fieldLengthN} - the number of tokens in the Nth field in the 
 * sentence</li>
 * <li>{@code fieldNormTokensN} - the normalized tokens of the Nth field</li>
 * <li>{@code fieldNormPosTagsN} - the part of speech tags of the normalized 
 * tokens of the Nth field</li> 
 * </ul>
 * <p>
 * Any properties of the {@link SpanExtraction} that have been set are also
 * included, with the field name prefixed by
 * {@link ExtractionSerializer#PROP}.
 * </p>
 * @author afader
 *
 */
public class ExtractionSerializer {
    
    // The String field names of a NormalizedSpanExtraction
    public static final String SENT_TOKENS = "sentTokens";
    public static final String SENT_POS_TAGS = "sentPosTags";
    public static final String SENT_CHUNK_TAGS = "sentChunkTags";
    public static final String NUM_FIELDS = "numFields";
    public static final String FIELD_NAME = "fieldName";
    public static final String FIELD_TOKENS = "fieldTokens";
    public static final String FIELD_START = "fieldStart";
    public static final String FIELD_LENGTH = "fieldLength";
    public static final String FIELD_NORM_TOKENS = "fieldNormTokens";
    public static final String FIELD_NORM_POS_TAGS = "fieldNormPosTags";
    public static final String PROP = "prop_";
    
    private static final IdentityExtractionNormalizer idnorm = 
        new IdentityExtractionNormalizer();
    
    /**
     * @param extr
     * @return a HashMap of String key/value pairs that represents the given
     * extraction (normalized using the identity normalizer)
     */
    public static Map<String, String> toMap(SpanExtraction extr) {
        return toMap(idnorm.normalizeExtraction(extr));
    }
    
    /**
     * @param extr
     * @return a String representation of the given extraction (normalized 
     * using the identity normalizer)
     */
    public static String toString(SpanExtraction extr) {
        return toString(idnorm.normalizeExtraction(extr));
    }
    
    /**
     * @param extr
     * @return a HashMap of String key/value pairs that represents the given
     * NormalizedSpanExtraction.
     */
    public static Map<String, String> toMap(NormalizedSpanExtraction extr) {
        
        HashMap<String, String> result = new HashMap<String, String>();
        // Add the sentence-level fields
        ChunkedSentence sent = extr.getSentence();
        result.put(SENT_TOKENS, sent.getTokensAsString());
        result.put(SENT_POS_TAGS, sent.getPosTagsAsString());
        result.put(SENT_CHUNK_TAGS, sent.getChunkTagsAsString());
        
        int numFields = extr.getNumFields();
        result.put(NUM_FIELDS, Integer.toString(numFields));
        
        List<String> fieldNames = extr.getFieldNames();
        List<ChunkedExtraction> fields = extr.getFields();
        List<NormalizedField> normFields = extr.getNormalizedFields();
        for (int i = 0; i < numFields; i++) {

            // The field name
            String fieldName = fieldNames.get(i);
            result.put(FIELD_NAME + i, fieldName);
            
            // The un-normalized fields
            ChunkedExtraction field = fields.get(i);
            result.put(FIELD_TOKENS + i, field.getTokensAsString());
            result.put(FIELD_START + i,  Integer.toString(field.getStart()));
            result.put(FIELD_LENGTH + i, Integer.toString(field.getLength()));
            
            // The normalized fields
            NormalizedField normField = normFields.get(i);
            result.put(FIELD_NORM_TOKENS + i, normField.getTokensAsString());
            result.put(FIELD_NORM_POS_TAGS + i, normField.getPosTagsAsString());
            
        }
        
        // Add any properties that have been set
        Map<String, String> extrProps = extr.getProperties();
        for (String propName : extrProps.keySet()) {
            result.put(PROP+propName, extrProps.get(propName));
        }
        return result;
    }
    
    /**
     * @param extr
     * @return a String representation of extr in the format of 
     * {@link MapLineSerializer}.
     */
    public static String toString(NormalizedSpanExtraction extr) {
        // TODO: this method should be renamed, since toString() usually 
        // means something else.
        return MapLineSerializer.serialize(toMap(extr));
    }
    
    /**
     * @param map
     * @return the extraction represented by the given map
     * @throws ExtractionFormatException if unable to deserialize the 
     * extraction
     */
    public static NormalizedSpanExtraction fromMap(Map<String,String> map) 
        throws ExtractionFormatException {
        return getNormalizedExtraction(map);
    }
    
    public static NormalizedSpanExtraction fromString(String str) 
        throws ExtractionFormatException {
        return fromMap(MapLineSerializer.deserialize(str));
    }
    
    /**
     * @param map
     * @return the normalized extraction
     * @throws ExtractionFormatException
     */
    private static NormalizedSpanExtraction getNormalizedExtraction(
            Map<String,String> map) throws ExtractionFormatException {
        SpanExtraction extr = getExtraction(map);
        List<NormalizedField> normFields = new ArrayList<NormalizedField>(
                extr.getNumFields());
        for (int i = 0; i < extr.getNumFields(); i++) {
            normFields.add(getNormalizedField(map, extr, i));
        }
        return new NormalizedSpanExtraction(extr, normFields);
    }
    
    /**
     * @param map
     * @param extr
     * @param fieldNum
     * @return the fieldNum-th normalized field
     * @throws ExtractionFormatException
     */
    private static NormalizedField getNormalizedField(
            Map<String,String> map, SpanExtraction extr, int fieldNum) 
            throws ExtractionFormatException {
        ChunkedExtraction field = extr.getField(fieldNum);
        List<String> normTokens = getTokenizedField(map, FIELD_NORM_TOKENS+fieldNum);
        List<String> normPos = getTokenizedField(map, FIELD_NORM_POS_TAGS+fieldNum);
        try {
            NormalizedField normField = new NormalizedField(field, normTokens, normPos);
            return normField;
        } catch (SequenceException e) {
            throw new ExtractionFormatException(e);
        }
        
    }
    
    /**
     * @param map
     * @return the unnormalized extraction 
     * @throws ExtractionFormatException
     */
    private static SpanExtraction getExtraction(Map<String,String> map)
        throws ExtractionFormatException {
        ChunkedSentence sent = getSentence(map);
        int numFields = getIntField(map, NUM_FIELDS);
        List<ChunkedExtraction> fields = new ArrayList<ChunkedExtraction>(numFields);
        List<String> fieldNames = new ArrayList<String>(numFields);
        for (int i = 0; i < numFields; i++) {
            fields.add(getField(sent, map, i));
            fieldNames.add(getFieldName(map, i));
        }
        SpanExtraction extr = new SpanExtraction(fields, fieldNames);
        extr.setProperties(getProperties(map));
        return extr;
    }
    
    /**
     * @param map
     * @return the properties of this extraction
     * @throws ExtractionFormatException
     */
    private static Map<String, String> getProperties(Map<String,String> map) 
        throws ExtractionFormatException {
        HashMap<String, String> props = new HashMap<String, String>();
        for (String name: map.keySet()) {
            if (name.startsWith(PROP)) {
                String value = map.get(name);
                String key = name.replaceFirst(PROP, "");
                props.put(key, value);
            }
        }
        return props;
    }
    
    /**
     * @param map
     * @return the sentence 
     * @throws ExtractionFormatException
     */
    private static ChunkedSentence getSentence(Map<String,String> map)
        throws ExtractionFormatException {
        List<String> tokens = getTokenizedField(map, SENT_TOKENS);
        List<String> posTags = getTokenizedField(map, SENT_POS_TAGS);
        List<String> chunkTags = getTokenizedField(map, SENT_CHUNK_TAGS);        
        try {
            return new ChunkedSentence(tokens, posTags, chunkTags);
        } catch (SequenceException e) {
            throw new ExtractionFormatException(e);
        }
    }
    
    /**
     * @param map
     * @param fieldNum
     * @return the name of the fieldNum-th field
     * @throws ExtractionFormatException
     */
    private static String getFieldName(Map<String,String> map, int fieldNum) 
        throws ExtractionFormatException {
        return getStringField(map, FIELD_NAME + fieldNum);
    }
    
    /**
     * @param sent
     * @param map
     * @param fieldNum
     * @return the fieldNum-th field in the given sentence
     * @throws ExtractionFormatException
     */
    private static ChunkedExtraction getField(ChunkedSentence sent, 
            Map<String, String> map, int fieldNum) throws ExtractionFormatException {
        return new ChunkedExtraction(sent, getFieldRange(map, fieldNum));
    }

    /**
     * @param map
     * @param fieldNum
     * @return the range in the sentence of the fieldNum-th field
     * @throws ExtractionFormatException
     */
    private static Range getFieldRange(Map<String,String> map, 
            int fieldNum) throws ExtractionFormatException {
        int start = getIntField(map, FIELD_START + fieldNum);
        int length = getIntField(map, FIELD_LENGTH + fieldNum);
        return new Range(start, length);
    }
    
    /**
     * @param map
     * @param key
     * @return the value of key in map, split on spaces
     * @throws ExtractionFormatException if key not in map
     */
    private static List<String> getTokenizedField(Map<String,String> map, 
            String key) throws ExtractionFormatException {
        String val = getStringField(map, key);
        return Arrays.asList(val.split(" "));
    }
    
    /**
     * @param map
     * @param key
     * @return the integer value of key in map
     * @throws ExtractionFormatException if key not in map, or value not integer
     */
    private static int getIntField(Map<String,String> map, String key) 
        throws ExtractionFormatException {
        String val = getStringField(map, key);
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            String msg = "Expected integer field for " + key + ", got: " + val;
            throw new ExtractionFormatException(msg);
        }
    }
    
    /**
     * @param map
     * @param key
      * @return the value of key in map
     * @throws ExtractionFormatException if key is not in map
     */
    private static String getStringField(Map<String,String> map, String key) 
        throws ExtractionFormatException {
        if (map.containsKey(key)) {
            return map.get(key);
        } else {
            throw new ExtractionFormatException("Field not found: " + key);
        }
    }

}
