package edu.washington.cs.knowitall.extractor.conf;

import java.util.HashMap;
import java.util.List;

import com.google.common.base.Predicate;

import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.regex.Match;
import edu.washington.cs.knowitall.regex.RegularExpression;
import edu.washington.cs.knowitall.extractor.ReVerbExtractor;
import edu.washington.cs.knowitall.extractor.conf.featureset.BooleanFeatureSet;
import edu.washington.cs.knowitall.extractor.conf.featureset.ChunkFeature;
import edu.washington.cs.knowitall.extractor.conf.featureset.PosFeature;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.ChunkedSentencePattern;
import edu.washington.cs.knowitall.nlp.ChunkedSentenceToken;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedArgumentExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;
import edu.washington.cs.knowitall.sequence.LayeredTokenMatcher;
import edu.washington.cs.knowitall.sequence.LayeredTokenPattern;
import edu.washington.cs.knowitall.sequence.SequenceException;

/***
 * This class defines the features used by the ReVerb confidence function. A
 * reference to the feature set can be obtained by calling the
 * <code>getFeatureSet()</code> method after constructing an instance of this
 * class.
 *
 * @author afader
 *
 */
public class ReVerbFeatures {

    private HashMap<String, Predicate<ChunkedBinaryExtraction>> featureMap;
    private BooleanFeatureSet<ChunkedBinaryExtraction> featureSet;

    public ReVerbFeatures() throws ConfidenceFunctionException {

        try {
            initFeatureSet();
        } catch (SequenceException e) {
            throw new ConfidenceFunctionException(
                    "Unable to initialize features", e);
        }
    }

    /**
     * @return the feature set used for the ReVerb confidence function
     */
    public BooleanFeatureSet<ChunkedBinaryExtraction> getFeatureSet() {
        return featureSet;
    }

    private void initFeatureSet() throws SequenceException {
        initFeatureMap();
        featureSet = new BooleanFeatureSet<ChunkedBinaryExtraction>(featureMap);
    }

    private void initFeatureMap() throws SequenceException {
        featureMap = new HashMap<String, Predicate<ChunkedBinaryExtraction>>();
        featureMap.put("sent starts w/arg1", startArg1());
        featureMap.put("sent ends w/arg2", endArg2());
        featureMap.put("which|who|that before rel", relPronounBeforeRel());
        featureMap.put("arg2 is proper", arg2IsProper());
        featureMap.put("arg1 is proper", arg1IsProper());
        featureMap.put("rel is VW+P", relIsVWP());
        featureMap.put("rel ends with to", relEndsWithToken("to"));
        featureMap.put("rel ends with in", relEndsWithToken("in"));
        featureMap.put("rel ends with for", relEndsWithToken("for"));
        featureMap.put("rel ends with of", relEndsWithToken("of"));
        featureMap.put("rel ends with on", relEndsWithToken("on"));
        featureMap.put("extr covers phrase", extrCoversPhrase());
        featureMap.put("arg2 part of a list", arg2InList());
        featureMap.put("np before arg1",
                ChunkFeature.rightBeforeArg1("B-NP", "I-NP"));
        featureMap.put("rel contains vbz", PosFeature.withinRel("VBZ"));
        featureMap.put("rel contains vbg", PosFeature.withinRel("VBG"));
        featureMap.put("rel contains vbd", PosFeature.withinRel("VBD"));
        featureMap.put("rel contains vbn", PosFeature.withinRel("VBN"));
        featureMap.put("rel contains vbp", PosFeature.withinRel("VBP"));
        featureMap.put("rel contains vb", PosFeature.withinRel("VB"));
        featureMap.put("conj before rel", PosFeature.rightBeforeRel("CC"));
        featureMap.put("prep before arg1",
                PosFeature.rightBeforeArg1("IN", "TO"));
        featureMap.put("verb after arg2",
                PosFeature.rightAfterArg2(PosFeature.allVerbPosTags));
        featureMap.put("np after arg2",
                ChunkFeature.rightAfterArg2("B-NP", "I-NP"));
        featureMap
                .put("prep after arg2", PosFeature.rightAfterArg2("IN", "TO"));
        featureMap.put("arg2 contains pronoun", PosFeature.withinArg2("PRP"));
        featureMap.put("arg1 contains pronoun", PosFeature.withinArg1("PRP"));
        featureMap.put("arg2 contains pos pronoun",
                PosFeature.withinArg2("PRP$"));
        featureMap.put("arg2 contains pos pronoun",
                PosFeature.withinArg2("PRP$"));
        featureMap.put("rel is a single verb", PosFeature.relSingleVerb());

        // token-level features
        featureMap.put("if before arg1", tokenBeforeArg1("if"));
        featureMap.put("in before arg1", tokenBeforeArg1("in"));
        featureMap.put("that before rel", tokenBeforeRel("that"));
        featureMap.put("to after arg2", tokenAfterArg2("to"));
        featureMap.put("that after arg2", tokenAfterArg2("that"));

        // "3F" - conjunction of "Extr begins sentence", "ends sentence", and
        // "covers phrase".
        featureMap.put("3F", new Predicate<ChunkedBinaryExtraction>() {
            Predicate<ChunkedBinaryExtraction> startArg1 = startArg1();
            Predicate<ChunkedBinaryExtraction> endArg2 = endArg2();
            Predicate<ChunkedBinaryExtraction> extrCoversPhrase = extrCoversPhrase();

            @Override
            public boolean apply(ChunkedBinaryExtraction arg0) {
                return startArg1.apply(arg0) && endArg2.apply(arg0)
                        && extrCoversPhrase.apply(arg0);
            }
        });

        //
        // Add all Nested and Hypothetical features.
        //
        featureMap.putAll(new NestedFeatures().getFeatureMap());
        featureMap.putAll(new HypotheticalFeatures().getFeatureMap());
    }

    /**
     * Each of the private methods below defines a feature.
     */

    // Used for features related to the relation string
    private String VERB = ReVerbExtractor.VERB;
    private String WORD = ReVerbExtractor.WORD;
    private String PREP = ReVerbExtractor.PREP;

    // Used for the list feature
    private String list1 = "<chunk='B-NP'> <chunk='I-NP'>* (<string=','> (<chunk='B-PP'>)? <chunk='B-NP'> <chunk='I-NP'>*)+ (<string=','> | (<string=','> <pos='CC'>)) <chunk='B-NP'> <chunk='I-NP'>*";
    private String list2 = "<chunk='B-NP'> <chunk='I-NP'>* (<string=','> <chunk='B-NP'> <chunk='I-NP'>*)+ <string=','>* <pos='CC'> <chunk='B-NP'> <chunk='I-NP'>*";
    public RegularExpression<ChunkedSentenceToken> listPattern1 = ChunkedSentencePattern
            .compile(list1);
    public RegularExpression<ChunkedSentenceToken> listPattern2 = ChunkedSentencePattern
            .compile(list2);

    private Predicate<ChunkedBinaryExtraction> arg2InList() {
        return new Predicate<ChunkedBinaryExtraction>() {
            @Override
            public boolean apply(ChunkedBinaryExtraction extr) {
                // find all matches to the list pattern. If arg2 overlaps with
                // the match, return true.
                ChunkedArgumentExtraction arg2 = extr.getArgument2();
                ChunkedSentence sentence = arg2.getSentence();
                List<Match<ChunkedSentenceToken>> matchList = listPattern2
                        .findAll(ChunkedSentenceToken.tokenize(sentence));
                for (Match<ChunkedSentenceToken> match : matchList) {
                    Range matchRange = new Range(match.startIndex(),
                            match.endIndex() - match.startIndex());
                    if (matchRange.overlapsWith(arg2.getRange())) {
                        return true;
                    }
                }
                matchList = listPattern1.findAll(ChunkedSentenceToken
                        .tokenize(sentence));
                for (Match<ChunkedSentenceToken> match : matchList) {
                    Range matchRange = new Range(match.startIndex(),
                            match.endIndex() - match.startIndex());
                    if (matchRange.overlapsWith(arg2.getRange())) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    private Predicate<ChunkedBinaryExtraction> startArg1() {
        return new Predicate<ChunkedBinaryExtraction>() {
            public boolean apply(ChunkedBinaryExtraction e) {
                return e.getArgument1().getRange().getStart() == 0;
            }
        };
    }

    private Predicate<ChunkedBinaryExtraction> endArg2() {
        return new Predicate<ChunkedBinaryExtraction>() {
            public boolean apply(ChunkedBinaryExtraction e) {
                int arg2End = e.getArgument2().getRange().getLastIndex();
                int sentEnd = e.getSentence().getLength() - 2;
                return arg2End == sentEnd;
            }
        };
    }

    private Predicate<ChunkedBinaryExtraction> relPronounBeforeRel() {
        return new Predicate<ChunkedBinaryExtraction>() {
            public boolean apply(ChunkedBinaryExtraction e) {
                ChunkedExtraction pred = e.getRelation();
                int predStart = pred.getStart();
                if (predStart > 0) {
                    String precToken = e.getSentence().getTokens()
                            .get(predStart - 1).toLowerCase();
                    if (precToken.equals("which") || precToken.equals("who")
                            || precToken.equals("that")) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    private boolean isProperNp(ChunkedExtraction e) {
        for (String tag : e.getPosTags()) {
            if (tag.equalsIgnoreCase("NNP") || tag.equalsIgnoreCase("NNPS")) {
                return true;
            }
            /*
             * if (!tag.startsWith("NNP") && !tag.equals("DT") &&
             * !tag.equals("IN")) { return false; }
             */
        }
        return false;
    }

    private Predicate<ChunkedBinaryExtraction> arg1IsProper() {
        return new Predicate<ChunkedBinaryExtraction>() {
            public boolean apply(ChunkedBinaryExtraction e) {
                return isProperNp(e.getArgument1());
            }
        };
    }

    private Predicate<ChunkedBinaryExtraction> arg2IsProper() {
        return new Predicate<ChunkedBinaryExtraction>() {
            public boolean apply(ChunkedBinaryExtraction e) {
                return isProperNp(e.getArgument2());
            }
        };
    }

    public Predicate<ChunkedBinaryExtraction> relIsOneVerb() {
        return new Predicate<ChunkedBinaryExtraction>() {
            public boolean apply(ChunkedBinaryExtraction e) {
                ChunkedExtraction rel = e.getRelation();
                List<String> posTags = rel.getPosTags();
                return posTags.size() == 1 && posTags.get(0).startsWith("V");
            }
        };
    }

    /**
     * @return
     * @throws SequenceException
     */
    private Predicate<ChunkedBinaryExtraction> relIsVWP()
            throws SequenceException {
        final String patternStr = String.format("(%s (%s+ (%s)+))+", VERB,
                WORD, PREP);
        final LayeredTokenPattern pattern = new LayeredTokenPattern(patternStr);
        return new Predicate<ChunkedBinaryExtraction>() {
            public boolean apply(ChunkedBinaryExtraction e) {
                try {
                    LayeredTokenMatcher m = pattern.matcher(e.getRelation());
                    int n = 0;
                    while (m.find())
                        n++;
                    return n == 1;
                } catch (SequenceException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        };
    }

    private Predicate<ChunkedBinaryExtraction> relEndsWithToken(String t) {
        final String token = t;
        return new Predicate<ChunkedBinaryExtraction>() {
            public boolean apply(ChunkedBinaryExtraction e) {
                List<String> tokens = e.getRelation().getTokens();
                return tokens.get(tokens.size() - 1).equals(token);
            }
        };
    }

    /**
     * A feature that returns true when the following are all true: - there are
     * no tokens between arg1 and rel, and rel and arg2. - the token to the left
     * of arg1 is a comma or the sentence start - the token to the rigth of arg2
     * is a period, comma, or sentence end
     *
     * @return the feature
     */
    private Predicate<ChunkedBinaryExtraction> extrCoversPhrase() {
        return new Predicate<ChunkedBinaryExtraction>() {
            public boolean apply(ChunkedBinaryExtraction e) {
                ChunkedSentence sent = e.getSentence();
                List<String> tokens = sent.getTokens();

                Range x = e.getArgument1().getRange();
                Range y = e.getArgument2().getRange();
                Range r = e.getRelation().getRange();
                boolean adj = x.isAdjacentTo(r) && r.isAdjacentTo(y);

                int xs = x.getStart();
                boolean leftOk = xs == 0 || tokens.get(xs - 1).equals(",")
                        || tokens.get(xs - 1).equals(".");

                int l = sent.getLength() - 1;
                int yr = y.getLastIndex();
                boolean rightOk = yr == l || tokens.get(yr + 1).equals(",")
                        || tokens.get(yr + 1).equals(".");

                return adj && leftOk && rightOk;
            }
        };
    }

    //
    // Token-level features below this point.
    //

    private Predicate<ChunkedBinaryExtraction> tokenBeforeArg1(
            final String token) {
        return new Predicate<ChunkedBinaryExtraction>() {
            public boolean apply(ChunkedBinaryExtraction e) {
                ChunkedArgumentExtraction arg1 = e.getArgument1();
                int arg1Start = arg1.getStart();
                if (arg1Start > 0) {
                    String precTok = e.getSentence().getTokens()
                            .get(arg1Start - 1);
                    if (precTok.equalsIgnoreCase(token)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    private Predicate<ChunkedBinaryExtraction> tokenBeforeRel(final String token) {
        return new Predicate<ChunkedBinaryExtraction>() {
            public boolean apply(ChunkedBinaryExtraction e) {
                ChunkedExtraction rel = e.getRelation();
                int relStart = rel.getStart();
                if (relStart > 0) {
                    String precTok = e.getSentence().getTokens()
                            .get(relStart - 1);
                    if (precTok.equalsIgnoreCase(token)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    private Predicate<ChunkedBinaryExtraction> tokenAfterArg2(final String token) {
        return new Predicate<ChunkedBinaryExtraction>() {
            public boolean apply(ChunkedBinaryExtraction e) {
                ChunkedArgumentExtraction arg2 = e.getArgument2();
                int arg1End = arg2.getStart() + arg2.getLength() - 1;
                if (arg1End + 1 < e.getSentence().getLength() - 1) {
                    String nextTok = e.getSentence().getTokens()
                            .get(arg1End + 1);
                    if (nextTok.equalsIgnoreCase(token)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

}
