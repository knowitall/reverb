package edu.washington.cs.knowitall.extractor.conf;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.base.Predicate;

import edu.washington.cs.knowitall.extractor.conf.featureset.ChunkFeature;
import edu.washington.cs.knowitall.extractor.conf.featureset.TokenFeature;
import edu.washington.cs.knowitall.extractor.conf.featureset.PosFeature;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedArgumentExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;
import edu.washington.cs.knowitall.sequence.SequenceException;

/**
 * Features designed to detect nested extractions.
 *
 * @author Rob
 *
 */
public class NestedFeatures {

    private Map<String, Predicate<ChunkedBinaryExtraction>> featureMap;

    // keyword lists. See end of file for hardcoded keywords.
    private Set<String> com;
    private Set<String> cog;
    private Set<String> othr;

    public NestedFeatures() {

        initKeywordSets();

        initFeatureSet();
    }

    private void initKeywordSets() {

        com = new HashSet<String>();
        com.addAll(Arrays.asList(comWords));

        cog = new HashSet<String>();
        cog.addAll(Arrays.asList(cogWords));

        othr = new HashSet<String>();
        othr.addAll(Arrays.asList(otherWords));

    }

    public Map<String, Predicate<ChunkedBinaryExtraction>> getFeatureMap() {

        return featureMap;
    }

    private void initFeatureSet() throws SequenceException {
        initFeatureMap();
    }

    private void initFeatureMap() {
        featureMap = new HashMap<String, Predicate<ChunkedBinaryExtraction>>();
        featureMap.put("nest: that appeas anywhere after arg2", that());
        featureMap.put("nest: non-period punct immediately after arg2", p2());
        featureMap.put("nest: comma immediately before arg1",
                tokensImmediatelyBeforeArg1(","));
        featureMap.put("nest: ' or \" immediately before arg1",
                tokensImmediatelyBeforeArg1("'", "\""));

        featureMap.put("nest: verb in arg2",
                PosFeature.withinArg2(PosFeature.allVerbPosTags));
        featureMap.put("nest: NP immediately after arg2",
                ChunkFeature.rightAfterArg2("B-NP", "I-NP"));
        featureMap.put(
                "nest: normalized predicate head is a communication verb",
                TokenFeature.relationHeadVerb(com));
        featureMap.put("nest: normalized predicate head is a cognitive verb",
                TokenFeature.relationHeadVerb(cog));
        featureMap.put("nest: normalized predicate head is an \"other\" verb",
                TokenFeature.relationHeadVerb(othr));
    }

    /** that appears anywhere after arg2 */
    private Predicate<ChunkedBinaryExtraction> that() {
        return new Predicate<ChunkedBinaryExtraction>() {
            @Override
            public boolean apply(ChunkedBinaryExtraction arg0) {

                ChunkedSentence sentence = arg0.getSentence();
                ChunkedArgumentExtraction arg2 = arg0.getArgument2();

                for (int i = arg2.getStart() + arg2.getLength(); i < sentence
                        .getLength(); ++i) {

                    if (sentence.getToken(i).equalsIgnoreCase("that")) {
                        return true;
                    }
                }

                return false;
            }
        };
    }

    /** punctuation immediately after arg2 */
    private static Pattern punct = Pattern.compile("[\\p{Punct}]+");
    private static Pattern period = Pattern.compile("\\.");

    private Predicate<ChunkedBinaryExtraction> p2() {
        return new Predicate<ChunkedBinaryExtraction>() {
            @Override
            public boolean apply(ChunkedBinaryExtraction arg0) {

                ChunkedSentence sentence = arg0.getSentence();
                ChunkedArgumentExtraction arg2 = arg0.getArgument2();

                int i = arg2.getStart() + arg2.getLength();
                if (i < sentence.getLength()) {
                    String token = sentence.getToken(i);
                    if (!token.isEmpty() && !period.matcher(token).matches()
                            && punct.matcher(token).matches()) {

                        return true;
                    }
                }

                return false;
            }
        };
    }

    /** given token, case insensitive, immediately before arg1 */
    private Predicate<ChunkedBinaryExtraction> tokensImmediatelyBeforeArg1(
            final String... givenTokens) {
        return new Predicate<ChunkedBinaryExtraction>() {
            @Override
            public boolean apply(ChunkedBinaryExtraction arg0) {

                ChunkedSentence sentence = arg0.getSentence();
                ChunkedArgumentExtraction arg1 = arg0.getArgument1();

                int i = arg1.getStart() - 1;
                if (i >= 0 && sentence.getLength() > 0) {
                    String token = sentence.getToken(i);
                    for (String givenTok : givenTokens) {
                        if (token.equals(givenTok)) {
                            return true;
                        }
                    }
                }

                return false;
            }
        };
    }

    public static final String[] comWords = new String[] { "acknowledge",
            "add", "address", "admit", "advertise", "advise", "agree",
            "allege", "announce", "answer", "appear", "argue", "ask", "assert",
            "assume", "assure", "believe", "boast", "claim", "comment",
            "complain", "conclude", "confirm", "consider", "contend",
            "convince", "decide", "declare", "demand", "demonstrate", "deny",
            "describe", "determine", "disclose", "discover", "discuss",
            "doubt", "emphasize", "expect", "explain", "express", "fear",
            "feel", "figure", "forget", "hear", "hope", "imply", "indicate",
            "inform", "insist", "instruct", "know", "learn", "maintain",
            "mean", "mention", "note", "notice", "observe", "pray", "predict",
            "proclaim", "promise", "propose", "repeat", "reply", "report",
            "request", "respond", "reveal", "say", "signal", "specify",
            "speculate", "state", "suggest", "teach", "tell", "testify",
            "warn", "write" };

    public static final String[] cogWords = new String[] { "estimate",
            "pretend", "prove", "realise", "realize", "recognize", "remember",
            "remind", "saw", "seem", "surmise", "suspect", "suspect",
            "theorize", "think", "understand", "verify", "wish", "worry" };

    public static final String[] otherWords = new String[] { "arrange", "call",
            "cause", "charge", "establish", "find", "get", "give", "offer",
            "prefer", "provide", "put", "recall", "receive", "recommend",
            "reflect", "require", "rule", "send", "show", "support" };
}
