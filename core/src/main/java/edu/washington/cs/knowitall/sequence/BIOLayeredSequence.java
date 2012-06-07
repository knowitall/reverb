package edu.washington.cs.knowitall.sequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.TreeMultimap;

import edu.washington.cs.knowitall.commonlib.Range;

/***
 * Represents a layered sequence where some of the layers can be spans of tags,
 * encoded using B/I/O prefixes. For example, the sequence of tags
 * {@code O B-X I-X O B-Y O B-Z B-Z I-Z} represents four spans at positions 1-2,
 * 4, 6, and 7-8.
 * 
 * @author afader
 * 
 */
public class BIOLayeredSequence extends SimpleLayeredSequence {

    // Maps layerName => immutable list of span ranges
    private HashMap<String, List<Range>> spans;

    // Maps layerName => span type => immutable list of ranges
    private HashMap<String, ImmutableMultimap<String, Range>> spanTypes;

    /**
     * Constructs a new {@linkplain BIOLayeredSequence} class.
     * 
     * @param length
     */
    public BIOLayeredSequence(int length) {
        super(length);
        spans = new HashMap<String, List<Range>>();
        spanTypes = new HashMap<String, ImmutableMultimap<String, Range>>();
    }

    /**
     * Creates a copy of this object.
     */
    public BIOLayeredSequence clone() {
        BIOLayeredSequence clone = new BIOLayeredSequence(getLength());
        for (String layerName : getLayerNames()) {
            if (isSpanLayer(layerName)) {
                clone.addSpanLayer(layerName, getLayer(layerName));
            } else {
                clone.addLayer(layerName, getLayer(layerName));
            }
        }
        return clone;
    }

    /**
     * Returns the ranges of all of the spans on the given layer.
     * 
     * @param layerName
     * @return a list of ranges in order
     */
    public List<Range> getSpans(String layerName) {
        if (hasLayer(layerName)) {
            if (spans.containsKey(layerName)) {
                return spans.get(layerName);
            } else {
                return new ArrayList<Range>();
            }
        } else {
            throw new IllegalArgumentException("Invalid layer name: "
                    + layerName);
        }
    }

    /**
     * Returns the ranges of all of the spans on the given layer that are of the
     * given type (e.g. will return all B-X/I-X given X).
     * 
     * @param layerName
     * @param type
     * @return
     */
    public ImmutableCollection<Range> getSpans(String layerName, String type) {
        if (hasLayer(layerName)) {
            if (spans.containsKey(layerName)
                    && spanTypes.get(layerName).containsKey(type)) {
                return spanTypes.get(layerName).get(type);
            } else {
                return ImmutableSet.of();
            }
        } else {
            throw new IllegalArgumentException("Invalid layer name: "
                    + layerName);
        }
    }

    /**
     * Adds a new layer to this sequence, but interprets it using B/I/O
     * notation. This means that each tag must start with <code>B-</code>,
     * <code>I-</code>, or equal <code>O</code>. Any tag that equals
     * <code>I-X</code> for some string <code>X</code> must come immediately
     * after either <code>I-X</code> or <code>B-X</code>.
     * 
     * @param layerName
     * @param input
     * @throws SequenceException
     *             if unable to add a layer with the given name, or if input
     *             does not follow the B/I/O encoding.
     */
    public void addSpanLayer(String layerName, List<String> input)
            throws SequenceException {

        ImmutableMultimap<String, Range> typeToSpans = ImmutableMultimap
                .copyOf(getRanges(input));
        List<Range> allRanges = new ArrayList<Range>();
        Collections.sort(allRanges);
        super.addLayer(layerName, input);

        spans.put(layerName, ImmutableList.copyOf(typeToSpans.values()));
        spanTypes.put(layerName, typeToSpans);
    }

    /**
     * Adds a new span layer to this sequence. The span layer encodes the given
     * tag and the B/I/O encoding. For example, if <code>tag = "NP"</code> then
     * this will add <code>B-NP</code>, <code>I-NP</code>, and <code>O</code>
     * tags at the indexes covered by <code>ranges</code>. The ranges must not
     * overlap.
     * 
     * @param layerName
     * @param tag
     * @param ranges
     * @throws SequenceException
     *             if any of the layers overlap, or if any of the ranges are
     *             outside of the range of this sequence
     */
    public void addSpanLayerRanges(String layerName, String tag,
            List<Range> ranges) throws SequenceException {

        List<Range> rangesCopy = new ArrayList<Range>(ranges.size());
        rangesCopy.addAll(ranges);
        Collections.sort(ranges);

        if (!Range.isDisjoint(ranges)) {
            throw new SequenceException("ranges cannot overlap");
        }

        List<String> sequence = new ArrayList<String>(getLength());
        for (int i = 0; i < getLength(); i++)
            sequence.add("O");
        for (Range range : ranges) {
            int first = range.getStart();
            int end = range.getEnd();

            if (end > getLength()) {
                throw new IndexOutOfBoundsException("Range out of bounds: "
                        + range);
            }

            sequence.set(first, "B-" + tag);
            for (int i = first + 1; i < end; i++) {
                sequence.set(i, "I-" + tag);
            }
        }

        addSpanLayer(layerName, sequence);

    }

    /**
     * Returns a subsequence of the given layer. If the given layer is a span
     * layer with B/I/O tags, and the subsequence partially intersects a span
     * (i.e. it starts with I-X), then this initial tag will be replaced with a
     * B-X tag.
     */
    public ImmutableList<String> getSubSequence(String layerName, int start,
            int length) {
        ImmutableList<String> subLayer = super.getSubSequence(layerName, start,
                length);
        if (spans.containsKey(layerName)) {
            List<String> spanSub = new ArrayList<String>(length);
            spanSub.addAll(subLayer);
            if (spanSub.size() > 0 && spanSub.get(0).startsWith("I-")) {

                try {
                    String type = getType(spanSub.get(0));
                    spanSub.set(0, "B-" + type);
                } catch (SequenceException e) {
                    // TODO: what is going on here?
                }
            }
            return ImmutableList.copyOf(spanSub);
        } else {
            return subLayer;
        }
    }

    /**
     * Returns a subsequence of the given layer. If the given layer is a span
     * layer with B/I/O tags, and the subsequence partially intersects a span
     * (i.e. it starts with I-X), then this initial tag will be replaced with a
     * B-X tag.
     */
    public ImmutableList<String> getSubSequence(String layerName, Range r) {
        if (r == null) {
            throw new IllegalArgumentException("range cannot be null.");
        }

        return getSubSequence(layerName, r.getStart(), r.getLength());
    }

    /**
     * Constructs a new subsequence from this instance. If the subsequence
     * partially intersects a span (e.g. the subsequence starts at a I-X tag),
     * then it will be replaced with a B-X tag.
     */
    public BIOLayeredSequence getSubSequence(int start, int length) {
        BIOLayeredSequence sub = new BIOLayeredSequence(length);
        for (String layerName : getLayerNames()) {
            List<String> subLayer = getSubSequence(layerName, start, length);

            try {
                if (isSpanLayer(layerName)) {
                    sub.addSpanLayer(layerName, subLayer);
                } else {
                    sub.addLayer(layerName, subLayer);
                }
            } catch (SequenceException e) {
                String msg = String.format(
                        "Could not create subsequence of length %s starting at %s "
                                + "for layer %s", start, length, layerName);
                throw new IllegalStateException(msg, e);
            }
        }
        return sub;
    }

    /**
     * Constructs a new subsequence from this instance. If the subsequence
     * partially intersects a span (e.g. the subsequence starts at a I-X tag),
     * then it will be replaced with a B-X tag.
     */
    public BIOLayeredSequence getSubSequence(Range r) {
        return getSubSequence(r.getStart(), r.getLength());
    }

    protected boolean isSpanLayer(String layerName) {
        return spans.containsKey(layerName);
    }

    private TreeMultimap<String, Range> getRanges(List<String> tags)
            throws SequenceException {

        String inType = null;
        int startIndex = -1;
        int length = 0;

        TreeMultimap<String, Range> results = TreeMultimap.create();

        for (int i = 0; i < tags.size(); i++) {

            String tag = tags.get(i);
            if (inType != null && (!tag.equals("I-" + inType))) {

                Range r = new Range(startIndex, length);
                results.put(inType, r);

                inType = null;
                startIndex = -1;
                length = 0;

            }

            if (tag.startsWith("B-")) {

                inType = getType(tag);
                startIndex = i;
                length = 1;

            } else if (tag.startsWith("I-") && inType != null
                    && tag.equals("I-" + inType)) {

                length++;

            }
        }

        if (inType != null) {
            Range r = new Range(startIndex, length);
            results.put(inType, r);
        }

        return results;
    }

    private String getType(String tag) throws SequenceException {
        String[] splitTag = tag.split("-");
        if (splitTag.length == 2) {
            return splitTag[1];
        } else {
            throw new SequenceException("Invalid tag: " + tag);
        }
    }

}
