package edu.washington.cs.knowitall.extractor.conf;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Predicate;

import edu.washington.cs.knowitall.extractor.conf.featureset.VerbTokenFeature;
import edu.washington.cs.knowitall.extractor.conf.featureset.TokenFeature;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedArgumentExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;
import edu.washington.cs.knowitall.normalization.BasicFieldNormalizer;
import edu.washington.cs.knowitall.sequence.SequenceException;

/**
 * Features designed to detect sentences expressing a hypothesis or belief and
 * not a fact.
 *
 * @author Rob
 *
 */
public class HypotheticalFeatures {

    private BasicFieldNormalizer stemmer;

    private HashMap<String, Predicate<ChunkedBinaryExtraction>> featureMap;

    private static String[] ifWords = new String[] { "if", "whether", "though",
            "although" };
    private static String[] thatWords = new String[] { "that", "which", "who" };
    private static String[] mayWords = new String[] { "may", "might", "would",
            "could", "should", "suppose" };

    // keyword lists. See end of file for hardcoded keywords.
    private Set<String> ifSet;
    private Set<String> maySet;
    private Set<String> comSet;
    private Set<String> cogSet;
    private Set<String> thatSet;

    public HypotheticalFeatures() {

        this.stemmer = new BasicFieldNormalizer();

        initKeywordSets();

        initFeatureSet();
    }

    private void initKeywordSets() {

        ifSet = new HashSet<String>();
        ifSet.addAll(Arrays.asList(ifWords));

        maySet = new HashSet<String>();
        maySet.addAll(Arrays.asList(mayWords));

        comSet = new HashSet<String>();
        comSet.addAll(Arrays.asList(NestedFeatures.comWords));

        cogSet = new HashSet<String>();
        cogSet.addAll(Arrays.asList(NestedFeatures.cogWords));

        thatSet = new HashSet<String>();
        thatSet.addAll(Arrays.asList(thatWords));
    }

    public Map<String, Predicate<ChunkedBinaryExtraction>> getFeatureMap() {

        return featureMap;
    }

    private void initFeatureSet() throws SequenceException {
        initFeatureMap();
    }

    private void initFeatureMap() {

        featureMap = new HashMap<String, Predicate<ChunkedBinaryExtraction>>();

        featureMap.put("hyp: that,which,who imm before arg1",
                tokenImmediatelyBeforeArg1(thatSet));
        featureMap.put("hyp: that,which,who btw arg1/pred",
                tokenBtwArg1AndPred(thatSet));
        featureMap.put("hyp: if,whether,though,although anwh before arg1",
                TokenFeature.anywhereBeforeArg1(ifSet));
        featureMap.put(
                "hyp: may,might,would,could,should,suppose anwh before arg1",
                TokenFeature.anywhereBeforeArg1(maySet));
        featureMap.put("hyp: communic verb anwh before arg1",
                VerbTokenFeature.anywhereBeforeArg1(comSet));
        featureMap.put("hyp: cognitn verb anwh before arg1",
                VerbTokenFeature.anywhereBeforeArg1(cogSet));
        featureMap.put("hyp: communic verb anwh after arg2",
                VerbTokenFeature.anywhereAfterArg2(comSet));
        featureMap.put("rel is single communication verb",
                VerbTokenFeature.relSingleToken(comSet));
    }

    private Predicate<ChunkedBinaryExtraction> tokenBtwArg1AndPred(
            final Set<String> keyWords) {
        return new Predicate<ChunkedBinaryExtraction>() {
            @Override
            public boolean apply(ChunkedBinaryExtraction extr) {

                ChunkedSentence sentence = extr.getSentence();
                ChunkedArgumentExtraction arg1 = extr.getArgument1();
                ChunkedExtraction rel = extr.getRelation();
                for (int i = arg1.getStart() + arg1.getLength(); i < rel
                        .getStart(); ++i) {
                    String token = sentence.getToken(i);
                    String pos = sentence.getPosTag(i);

                    String lemma = stemmer.stemSingleToken(token, pos);
                    if (keyWords.contains(lemma.toLowerCase())) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    private Predicate<ChunkedBinaryExtraction> tokenImmediatelyBeforeArg1(
            final Set<String> keyWords) {
        return new Predicate<ChunkedBinaryExtraction>() {
            @Override
            public boolean apply(ChunkedBinaryExtraction extr) {

                ChunkedSentence sentence = extr.getSentence();
                ChunkedArgumentExtraction arg1 = extr.getArgument1();
                int i = arg1.getStart() - 1;
                if (i < 0)
                    return false;
                String token = sentence.getToken(i);
                String pos = sentence.getPosTag(i);
                String lemma = stemmer.stemSingleToken(token, pos);
                if (keyWords.contains(lemma.toLowerCase())) {
                    return true;
                }
                return false;
            }
        };
    }
}
