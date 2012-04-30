package edu.washington.cs.knowitall.extractor.conf.featureset;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedArgumentExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;

/**
 * Parent class for any feature that considers only tokens whose corresponding
 * pos starts with "V" or equals "MD".
 *
 * @author Rob
 *
 */
public abstract class VerbTokenFeature extends ExtractionFeature {

    private Set<String> tokens;

    public VerbTokenFeature(String... givenTokens) {
        this(Arrays.asList(givenTokens));
    }

    public VerbTokenFeature(Collection<String> givenTokens) {
        this.tokens = new HashSet<String>();
        this.tokens.addAll(givenTokens);
    }

    @Override
    protected abstract Range rangeToExamine(ChunkedBinaryExtraction cbe);

    @Override
    protected boolean testAtIndex(Integer index, ChunkedSentence sentence) {

        String pos = sentence.getPosTag(index);
        if (!pos.startsWith("V") && !pos.startsWith("MD"))
            return false;
        String token = sentence.getToken(index);
        token = stemmer.stemSingleToken(token, sentence.getPosTag(index));
        token = token.toLowerCase();
        return tokens.contains(token);
    }

    public static VerbTokenFeature withinArg2(String... tokens) {
        return new VerbTokenFeature(tokens) {
            @Override
            protected Range rangeToExamine(ChunkedBinaryExtraction cbe) {
                return cbe.getArgument2().getRange();
            }
        };
    }

    public static VerbTokenFeature withinRel(String... tokens) {
        return new VerbTokenFeature(tokens) {
            @Override
            protected Range rangeToExamine(ChunkedBinaryExtraction cbe) {
                return cbe.getRelation().getRange();
            }
        };
    }

    public static VerbTokenFeature rightBeforeArg1(Collection<String> tokens) {
        return new VerbTokenFeature(tokens) {
            @Override
            protected Range rangeToExamine(ChunkedBinaryExtraction cbe) {
                ChunkedArgumentExtraction arg1 = cbe.getArgument1();
                int index = arg1.getStart() - 1;
                if (index < 0 || index > arg1.getSentence().getLength()) {
                    return Range.EMPTY;
                } else
                    return Range.fromInterval(index, index + 1);
            }
        };
    }

    public static VerbTokenFeature rightBeforeArg1(String... tokens) {
        return rightBeforeArg1(Arrays.asList(tokens));
    }

    public static VerbTokenFeature relationHeadVerb(Collection<String> tokens) {
        return new VerbTokenFeature(tokens) {
            @Override
            protected Range rangeToExamine(ChunkedBinaryExtraction cbe) {
                Integer index = indexOfHeadVerb(cbe.getRelation(), false);
                if (index == null)
                    return Range.EMPTY;
                else
                    return Range.fromInterval(index, index + 1);
            }
        };
    }

    public static VerbTokenFeature rightAfterArg2(String... tokens) {
        return new VerbTokenFeature(tokens) {
            @Override
            protected Range rangeToExamine(ChunkedBinaryExtraction cbe) {
                ChunkedArgumentExtraction arg2 = cbe.getArgument2();
                int index = arg2.getStart() + arg2.getLength();
                if (index < 0 || index >= arg2.getSentence().getLength()) {
                    return Range.EMPTY;
                } else
                    return Range.fromInterval(index, index + 1);
            }
        };
    }

    public static VerbTokenFeature anywhereBeforeArg1(Collection<String> tokens) {
        return new VerbTokenFeature(tokens) {
            @Override
            protected Range rangeToExamine(ChunkedBinaryExtraction cbe) {
                ChunkedArgumentExtraction arg1 = cbe.getArgument1();
                int index = arg1.getStart() - 1;
                if (index < 0 || index >= arg1.getSentence().getLength()) {
                    return Range.EMPTY;
                } else
                    return Range.fromInterval(0, index + 1);
            }
        };
    }

    public static VerbTokenFeature anywhereAfterArg2(Collection<String> tokens) {
        return new VerbTokenFeature(tokens) {
            @Override
            protected Range rangeToExamine(ChunkedBinaryExtraction cbe) {
                ChunkedArgumentExtraction arg2 = cbe.getArgument2();
                int index = arg2.getStart() + arg2.getLength();
                if (index < 0 || index >= arg2.getSentence().getLength()) {
                    return Range.EMPTY;
                } else
                    return Range.fromInterval(index, cbe.getSentence()
                            .getLength());
            }
        };
    }

    public static VerbTokenFeature relSingleToken(Collection<String> tokens) {
        return new VerbTokenFeature(tokens) {
            @Override
            protected Range rangeToExamine(ChunkedBinaryExtraction cbe) {

                Range range = cbe.getRelation().getRange();
                if (range.getLength() == 1)
                    return range;
                else
                    return Range.EMPTY;
            }
        };
    }

}
