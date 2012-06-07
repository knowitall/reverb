package edu.washington.cs.knowitall.nlp.extraction;

import java.util.ArrayList;
import java.util.Collection;

import com.google.common.collect.Iterables;

import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;

/***
 * Represents a binary extraction from a {@link ChunkedSentence}. A binary
 * extraction has three parts: two arguments (arg1 and arg2) and a relation
 * (rel). Each of these parts corresponds to a disjoint span of words in a
 * sentence. The relation is represented as a {@link ChunkedExtraction} object,
 * and the two arguments are represented as {@link ChunkedArgumentExtraction}
 * objects.
 *
 * @author afader
 *
 */
public class ChunkedBinaryExtraction extends SpanExtraction {

    private ChunkedExtraction rel;
    private ChunkedArgumentExtraction arg1;
    private ChunkedArgumentExtraction arg2;

    /**
     * The field name for the first argument (see {@link SpanExtraction}).
     */
    public static final String ARG1 = "ARG1";

    /**
     * The field name for the relation (see {@link SpanExtraction}).
     */
    public static final String REL = "REL";

    /**
     * The field name for the second argument (see {@link SpanExtraction})
     */
    public static final String ARG2 = "ARG2";

    private static final String[] fieldNames = { ARG1, REL, ARG2 };

    /**
     * Constructs a new instance using the given relation and arguments. The
     * relation and arguments must all be from the same sentence.
     *
     * @param rel
     * @param arg1
     * @param arg2
     */
    public ChunkedBinaryExtraction(ChunkedExtraction rel,
            ChunkedArgumentExtraction arg1, ChunkedArgumentExtraction arg2) {
        super(new ChunkedExtraction[] { arg1, rel, arg2 }, fieldNames);
        this.rel = rel;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    /**
     * @return the first argument
     */
    public ChunkedArgumentExtraction getArgument1() {
        return arg1;
    }

    /**
     * @return the relation
     */
    public ChunkedExtraction getRelation() {
        return rel;
    }

    /**
     * @return the second argument
     */
    public ChunkedArgumentExtraction getArgument2() {
        return arg2;
    }

    public Iterable<Integer> getIndices() {
        return Iterables.concat(this.getArgument1().getRange(), this
                .getRelation().getRange(), this.getArgument2().getRange());
    }

    public Iterable<String> getTokens() {
        return Iterables.concat(this.getArgument1().getPosTags(), this
                .getRelation().getPosTags(), this.getArgument2().getPosTags());
    }

    public Iterable<String> getPosTags() {
        return Iterables.concat(this.getArgument1().getPosTags(), this
                .getRelation().getPosTags(), this.getArgument2().getPosTags());
    }

    public Iterable<String> getChunkTags() {
        return Iterables.concat(this.getArgument1().getChunkTags(), this
                .getRelation().getChunkTags(), this.getArgument2()
                .getChunkTags());
    }

    @Override
    public String toString() {
        String arg1Str = getArgument1().toString();
        String relStr = getRelation().toString();
        String arg2Str = getArgument2().toString();
        return String.format("(%s, %s, %s)", arg1Str, relStr, arg2Str);
    }

    /**
     * Given a collection of arg1s, a collection of arg2s, and a relation,
     * returns all (arg1, rel, arg2) extractions, where arg1 and arg2 range over
     * the given collections.
     *
     * @param rel
     * @param arg1s
     * @param arg2s
     * @return
     */
    public static Collection<ChunkedBinaryExtraction> productOfArgs(
            ChunkedExtraction rel,
            Iterable<? extends ChunkedArgumentExtraction> arg1s,
            Iterable<? extends ChunkedArgumentExtraction> arg2s) {
        return ChunkedBinaryExtraction.productOfArgs(rel, arg1s, arg2s, false);
    }

    /**
     * Given a collection of arg1s, a collection of arg2s, and a relation,
     * returns all (arg1, rel, arg2) extractions, where arg1 and arg2 range over
     * the given collections.
     *
     * @param rel
     * @param arg1s
     * @param arg2s
     * @return
     */
    public static Collection<ChunkedBinaryExtraction> productOfArgs(
            ChunkedExtraction rel,
            Iterable<? extends ChunkedArgumentExtraction> arg1s,
            Iterable<? extends ChunkedArgumentExtraction> arg2s,
            boolean allowUnaryRelations) {
        Collection<ChunkedBinaryExtraction> results = new ArrayList<ChunkedBinaryExtraction>();

        for (ChunkedArgumentExtraction arg1 : arg1s) {
            for (ChunkedArgumentExtraction arg2 : arg2s) {
                ChunkedBinaryExtraction extr = new ChunkedBinaryExtraction(rel,
                        arg1, arg2);
                results.add(extr);
            }

        }
        // hack to add relations that only have one argument.
        if (allowUnaryRelations && results.isEmpty()) {
            for (ChunkedArgumentExtraction arg1 : arg1s) {
                Range dummyRange = new Range(rel.getRange().getStart()
                        + rel.getRange().getLength(), 0);
                ChunkedArgumentExtraction arg2 = new ChunkedArgumentExtraction(
                        rel.getSentence(), dummyRange, rel);
                ChunkedBinaryExtraction extr = new ChunkedBinaryExtraction(rel,
                        arg1, arg2);
                results.add(extr);
            }
            for (ChunkedArgumentExtraction arg2 : arg2s) {
                Range dummyRange = new Range(rel.getRange().getStart(), 0);
                ChunkedArgumentExtraction arg1 = new ChunkedArgumentExtraction(
                        rel.getSentence(), dummyRange, rel);
                ChunkedBinaryExtraction extr = new ChunkedBinaryExtraction(rel,
                        arg1, arg2);
                results.add(extr);
            }
        }
        return results;
    }

}
