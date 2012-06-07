package edu.washington.cs.knowitall.nlp.extraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;

/***
 * <p>
 * This class represents an extraction from a single sentence that is made up of
 * one or more fields, each of which corresponds to a span in the sentence.
 * </p>
 * <p>
 * For example, a binary subject-verb-object relationship can be represented as
 * a {@code SentenceSpanExtraction} with three fields: one for the subject, one
 * for the verb phrase, and one for the object. This class keeps a reference to
 * the sentence the extraction originated from, and the ranges of each field.
 * Each field is represented as a {@link ChunkedExtraction}.
 * </p>
 * <p>
 * A SpanExtraction object is also equipped with a set of properties, mapping a
 * String key to a String value.
 * </p>
 *
 * @author afader
 *
 */
public class SpanExtraction {

    private ChunkedSentence sent;
    private int numFields;
    private ImmutableList<Range> fieldRanges;
    private ImmutableList<ChunkedExtraction> fields;
    private ImmutableList<String> fieldNames;
    private HashMap<String, ChunkedExtraction> namedFields;
    private Map<String, String> props;

    /**
     * Constructs a new extraction from the given sentence, with fields defined
     * by the given ranges and names.
     *
     * @param sent
     * @param ranges
     * @param fieldNames
     */
    public SpanExtraction(ChunkedSentence sent, List<Range> fieldRanges,
            List<String> fieldNames) {
        initFromRanges(sent, fieldRanges, fieldNames);
    }

    /**
     * Constructs a new extraction from the given sentence, with fields defined
     * by the given ranges. Uses the default field names of field0, field1,
     * field2, etc.
     *
     * @param sent
     * @param ranges
     */
    public SpanExtraction(ChunkedSentence sent, List<Range> fieldRanges) {
        int n = fieldRanges.size();
        initFromRanges(sent, fieldRanges, getDefaultFieldNames(n));
    }

    /**
     * Constructs a new extraction from the given {@link ChunkedExtraction}s.
     * These must all come from the same sentence. Uses the default field names
     * of field0, field1, field2, etc.
     *
     * @param fields
     */
    public SpanExtraction(List<ChunkedExtraction> fields) {
        int n = fields.size();
        initFromFields(fields, getDefaultFieldNames(n));
    }

    /**
     * Constructs a new extraction from the given {@link ChunkedExtraction}s.
     * These must all come from the same sentence. Uses the default field names
     * of field0, field1, field2, etc.
     *
     * @param fields
     */
    public SpanExtraction(ChunkedExtraction[] fields) {
        List<ChunkedExtraction> fieldsList = new ArrayList<ChunkedExtraction>(
                fields.length);
        for (ChunkedExtraction field : fields)
            fieldsList.add(field);
        initFromFields(fieldsList, getDefaultFieldNames(fieldsList.size()));
    }

    /**
     * Constructs a new extraction from the given {@link ChunkedExtraction}s.
     * These must all come from the same sentence.
     *
     * @param fields
     * @param fieldNames
     */
    public SpanExtraction(List<ChunkedExtraction> fields,
            List<String> fieldNames) {
        initFromFields(fields, fieldNames);
    }

    /**
     * Constructs a new extraction from the given {@link ChunkedExtraction}s.
     * These must all come from the same sentence.
     *
     * @param fields
     * @param fieldNames
     */
    public SpanExtraction(ChunkedExtraction[] fields, String[] fieldNames) {
        List<ChunkedExtraction> fieldsList = new ArrayList<ChunkedExtraction>(
                fields.length);
        for (ChunkedExtraction field : fields)
            fieldsList.add(field);
        List<String> fieldNamesList = new ArrayList<String>(fieldNames.length);
        for (String name : fieldNames)
            fieldNamesList.add(name);
        initFromFields(fieldsList, fieldNamesList);
    }

    /**
     * @return the number of fields in this extraction
     */
    public int getNumFields() {
        return fieldRanges.size();
    }

    /**
     * @return the field names of this extraction
     */
    public List<String> getFieldNames() {
        return fieldNames;
    }

    /**
     * @param name
     * @return true if this extraction has the given named field
     */
    public boolean hasField(String name) {
        return namedFields.containsKey(name);
    }

    /**
     * @return the sentence
     */
    public ChunkedSentence getSentence() {
        return sent;
    }

    /**
     * @return the ranges of each field
     */
    public List<Range> getFieldRanges() {
        return fieldRanges;
    }

    /**
     * @return the fields
     */
    public List<ChunkedExtraction> getFields() {
        return fields;
    }

    /**
     * @param i
     * @return the range of the ith field
     */
    public Range getFieldRange(int i) {
        return getFieldRanges().get(i);
    }

    /**
     * @param name
     * @return the range of the field with the given name
     */
    public Range getFieldRange(String name) {
        return namedFields.get(name).getRange();
    }

    /**
     * @param name
     * @return the field with the given name
     */
    public ChunkedExtraction getField(String name) {
        return namedFields.get(name);
    }

    /**
     * @param i
     * @return the ith field
     */
    public ChunkedExtraction getField(int i) {
        return getFields().get(i);
    }

    /**
     * @param i
     * @return the name of the ith field
     */
    public String getFieldName(int i) {
        return getFieldNames().get(i);
    }

    /**
     * @return the map of property names to property values
     */
    public Map<String, String> getProperties() {
        return props;
    }

    /**
     * Sets the given property
     *
     * @param name
     *            the name of the property
     * @param value
     *            the value
     */
    public void setProperty(String name, String value) {
        props.put(name, value);
    }

    /**
     * Sets the properties to the given map.
     *
     * @param props
     */
    public void setProperties(Map<String, String> props) {
        this.props = props;
    }

    /**
     * Checks whether this extraction has the given property.
     *
     * @param name
     * @return true if this extraction has the given property
     */
    public boolean hasProperty(String name) {
        return props.containsKey(name);
    }

    /**
     * @param name
     * @return the value of the given property
     */
    public String getProperty(String name) {
        if (hasProperty(name)) {
            return props.get(name);
        } else {
            throw new IllegalArgumentException("Invalid property name: " + name);
        }
    }

    /**
     * @return the names of any properties that have been set
     */
    public Set<String> getPropertyNames() {
        return props.keySet();
    }

    /**
     * Returns a B/I/O encoding of this extraction as a list of strings. The
     * list has the same length as the underlying sentence. The tags used in the
     * B/I/O encoding are in the form B-FieldName0, I-FieldName0, ... for each
     * field name.
     *
     * @return the B/I/O encoding as a list of strings
     */
    public List<String> toBIOLayer() {
        int n = getSentence().getLength();
        ArrayList<String> result = new ArrayList<String>(n);
        for (int i = 0; i < n; i++)
            result.add("O");
        for (String fieldName : getFieldNames()) {
            Range r = getFieldRange(fieldName);
            int start = r.getStart();
            int end = r.getEnd();
            result.set(start, "B-" + fieldName);
            for (int j = start + 1; j < end; j++) {
                result.set(j, "I-" + fieldName);
            }
        }
        return result;
    }

    private void initFromRanges(ChunkedSentence sent, List<Range> fieldRanges,
            List<String> fieldNames) {

        if (fieldRanges.size() < 1) {
            throw new IllegalArgumentException("must have at least 1 field");
        }
        if (fieldNames.size() != fieldRanges.size()) {
            throw new IllegalArgumentException(
                    "length of ranges must equal length of fieldNames");
        }

        this.sent = sent;
        this.numFields = fieldRanges.size();
        this.fieldRanges = ImmutableList.copyOf(fieldRanges);

        List<ChunkedExtraction> tFields = new ArrayList<ChunkedExtraction>(
                numFields);
        this.namedFields = new HashMap<String, ChunkedExtraction>(numFields);

        for (int i = 0; i < numFields; i++) {
            Range range = fieldRanges.get(i);
            ChunkedExtraction field = new ChunkedExtraction(sent, range);
            String fieldName = fieldNames.get(i);
            tFields.add(field);
            namedFields.put(fieldName, field);
        }

        this.fields = ImmutableList.copyOf(tFields);
        this.fieldNames = ImmutableList.copyOf(fieldNames);
        this.props = new HashMap<String, String>();
    }

    private void initFromFields(List<ChunkedExtraction> fields,
            List<String> fieldNames) {

        List<Range> tFieldRanges = new ArrayList<Range>(fields.size());
        for (ChunkedExtraction field : fields) {
            tFieldRanges.add(field.getRange());
        }

        if (fields.size() < 1) {
            throw new IllegalArgumentException("must have at least 1 field");
        }

        for (int i = 0; i < fields.size(); i++) {
            ChunkedSentence sent1 = fields.get(i).getSentence();
            ChunkedSentence sent2 = fields.get((i + 1) % fields.size())
                    .getSentence();
            if (!sent1.equals(sent2)) {
                throw new IllegalArgumentException(
                        "fields must come from the same sentence");
            }
        }

        this.sent = fields.get(0).getSentence();
        this.numFields = fields.size();
        this.fieldRanges = ImmutableList.copyOf(tFieldRanges);
        this.fields = ImmutableList.copyOf(fields);
        this.fieldNames = ImmutableList.copyOf(fieldNames);

        this.namedFields = new HashMap<String, ChunkedExtraction>(
                this.numFields);
        for (int i = 0; i < this.numFields; i++) {
            this.namedFields.put(fieldNames.get(i), fields.get(i));
        }

        this.props = new HashMap<String, String>();

    }

    private static List<String> getDefaultFieldNames(int n) {
        List<String> result = new ArrayList<String>(n);
        for (int i = 0; i < n; i++)
            result.add("field" + i);
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((fieldNames == null) ? 0 : fieldNames.hashCode());
        result = prime * result
                + ((fieldRanges == null) ? 0 : fieldRanges.hashCode());
        result = prime * result + ((fields == null) ? 0 : fields.hashCode());
        result = prime * result
                + ((namedFields == null) ? 0 : namedFields.hashCode());
        result = prime * result + numFields;
        result = prime * result + ((props == null) ? 0 : props.hashCode());
        result = prime * result + ((sent == null) ? 0 : sent.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SpanExtraction other = (SpanExtraction) obj;
        if (fieldNames == null) {
            if (other.fieldNames != null)
                return false;
        } else if (!fieldNames.equals(other.fieldNames))
            return false;
        if (fieldRanges == null) {
            if (other.fieldRanges != null)
                return false;
        } else if (!fieldRanges.equals(other.fieldRanges))
            return false;
        if (fields == null) {
            if (other.fields != null)
                return false;
        } else if (!fields.equals(other.fields))
            return false;
        if (namedFields == null) {
            if (other.namedFields != null)
                return false;
        } else if (!namedFields.equals(other.namedFields))
            return false;
        if (numFields != other.numFields)
            return false;
        if (props == null) {
            if (other.props != null)
                return false;
        } else if (!props.equals(other.props))
            return false;
        if (sent == null) {
            if (other.sent != null)
                return false;
        } else if (!sent.equals(other.sent))
            return false;
        return true;
    }

}
