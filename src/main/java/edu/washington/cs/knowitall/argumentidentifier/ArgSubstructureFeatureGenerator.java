package edu.washington.cs.knowitall.argumentidentifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import edu.washington.cs.knowitall.argumentidentifier.ArgLearner.Mode;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;

/**
 * ArgSubstructureFeatureGenerator creates the features for for Arg1 and right
 * bound for Arg2
 *
 * @author janara
 *
 */

public class ArgSubstructureFeatureGenerator {
    private Mode mode;

    static int CHUNK_FEATURE = 0;
    static int TAG_FEATURE = 1;
    static int LEX_FEATURE = 2;
    static int PARSE_FEATURE = 3;

    static String B_ARG = "B-ARG"; // beginning of argument
    static String I_ARG = "I-ARG"; // inside/continuation of argument - can only
                                   // follow B-ARG or I-ARG
    static String O = "O"; // not in an argument

    private static String CAPS = "[A-Zçêòéñì]";
    private static String ALPHA = "[A-Zçêòéñìa-z]";
    private static String ALPHANUM = "[A-Zçêòéñìa-z0-9]";
    private static String PUNT = "[,\\.;:?!()]";

    private Vector<Pattern> patterns = null;
    private Vector<String> featureNames = null;

    // include words with certain POS tags (determiners, preps, etc)
    private static HashSet<String> lexicalizeIfTag = null;
    private static HashSet<String> lexicalizeIfWord = null;
    private static HashSet<String> stopWord = null;

    private PatternExtractor pattern_extractor;

    public ArgSubstructureFeatureGenerator(Mode mode) {
        this.mode = mode;
        this.pattern_extractor = new PatternExtractor();
        init();
    }

    private void init() {
        // we use lexical features for these closed POS tag classes
        lexicalizeIfTag = new HashSet<String>();
        lexicalizeIfTag.add("IN");
        lexicalizeIfTag.add("MD");
        lexicalizeIfTag.add("DT");
        lexicalizeIfTag.add("WDT");
        lexicalizeIfTag.add("WP");
        lexicalizeIfTag.add("RP");
        lexicalizeIfTag.add("PRT");
        lexicalizeIfTag.add("WRB");
        lexicalizeIfTag.add("WRB");
        lexicalizeIfTag.add("CC");

        // we use lexical features for these common words
        lexicalizeIfWord = new HashSet<String>();
        lexicalizeIfWord.add("be");
        lexicalizeIfWord.add("been");
        lexicalizeIfWord.add("is");
        lexicalizeIfWord.add("was");
        lexicalizeIfWord.add("are");
        lexicalizeIfWord.add("were");
        lexicalizeIfWord.add("has");
        lexicalizeIfWord.add("had");
        lexicalizeIfWord.add("have");
        lexicalizeIfWord.add("n't");
        lexicalizeIfWord.add("not");
        lexicalizeIfWord.add("that");
        lexicalizeIfWord.add("which");
        lexicalizeIfWord.add("as");
        lexicalizeIfWord.add("but");
        lexicalizeIfWord.add("and");
        lexicalizeIfWord.add("or");
        lexicalizeIfWord.add("said");
        lexicalizeIfWord.add("say");
        lexicalizeIfWord.add("says");

        stopWord = new HashSet<String>();
        stopWord.add("said");
        stopWord.add("say");
        stopWord.add("says");

        // regexp features -- same as Peng & McCallum
        patterns = new Vector<Pattern>();
        featureNames = new Vector<String>();

        featureNames.add("INITCAP");
        patterns.add(Pattern.compile(CAPS + ".*"));
        featureNames.add("ALLDIGITS");
        patterns.add(Pattern.compile("[0-9]*"));
        featureNames.add("ALLCAPS");
        patterns.add(Pattern.compile(CAPS + "+"));
        featureNames.add("CONTAINSDIGITS");
        patterns.add(Pattern.compile(".*[0-9].*"));
        featureNames.add("ALLDIGITS");
        patterns.add(Pattern.compile("[0-9]+"));
        featureNames.add("CONTAINSDOTS");
        patterns.add(Pattern.compile("[^\\.]*\\..*"));
        featureNames.add("CONTAINSDASH");
        patterns.add(Pattern.compile(ALPHANUM + "+-" + ALPHANUM + "*"));
        featureNames.add("ACRO");
        patterns.add(Pattern.compile("[A-Z][A-Z\\.]*\\.[A-Z\\.]*"));
        featureNames.add("LONELYINITIAL");
        patterns.add(Pattern.compile(CAPS + "\\."));
        featureNames.add("SINGLECHAR");
        patterns.add(Pattern.compile(ALPHA));
        featureNames.add("CAPLETTER");
        patterns.add(Pattern.compile(CAPS));
        featureNames.add("PUNC");
        patterns.add(Pattern.compile(PUNT));
    }

    private String convertInstancesToFeatures(Vector<Vector<String>> instances,
            Boolean train) {
        if (instances == null) {
            return "";
        }

        // build a string out of the features, skipping the label for testing
        // instances
        StringBuilder features = new StringBuilder();
        for (Vector<String> instance : instances) {
            String label = instance.lastElement();

            for (String feature : instance) {
                if (train || !feature.equals(label)) {
                    features.append(feature + " ");
                }
            }
            features.append("\n");
        }

        return features.toString();
    }

    private ArrayList<String> getSetFeatures(ChunkedExtraction extr,
            int argend, boolean train) {
        ArrayList<String> setfeatures = new ArrayList<String>();

        boolean rel = false;
        boolean nest1 = false;
        boolean adj = false;
        boolean comp = false;
        boolean nest2 = false;
        boolean npinf = false;
        boolean inf = false;
        boolean doublenp = false;
        boolean list = false;
        boolean ifclause = false;
        boolean compoundverb = false;

        boolean nextIsThat = false;
        boolean comma = false;
        boolean pp = false;
        boolean npvp = false;
        boolean quotes = false;
        boolean statement = false;
        if (mode != ArgLearner.Mode.LEFT) {
            adj = pattern_extractor.adjRelation(extr);
            comp = pattern_extractor.complementClause(extr);
            nest2 = pattern_extractor.nestedRelation2(extr);
            npinf = pattern_extractor.npInfinitiveClause(extr);
            inf = pattern_extractor.infinitiveClause(extr);
            doublenp = pattern_extractor.doubleNP(extr);
            list = pattern_extractor.startsList(extr);
            ifclause = pattern_extractor.ifClause(extr);
            compoundverb = pattern_extractor.compoundVerb(extr);
            rel = pattern_extractor.npRelativeClause(extr);
            nest1 = pattern_extractor.nestedRelation1(extr);
        } else {
            nextIsThat = pattern_extractor.nextIsThat(extr, argend - 1);
            comma = pattern_extractor.matchesCommaBeforeVerb(extr);
            pp = pattern_extractor.matchesPPBeforeVerb(extr);
            npvp = pattern_extractor.matchesNPVerb(extr);
            quotes = pattern_extractor.matchesQuotes(extr);
            rel = pattern_extractor.matchesRelativeClause(extr, argend);
        }
        if (rel) {
            setfeatures.add("REL");
        } else if (nest2) {
            setfeatures.add("NEST2");
        }
        if (nest1) {
            setfeatures.add("NEST1");
        }
        if (adj) {
            setfeatures.add("ADJ");
        }
        if (comp) {
            setfeatures.add("COMP");
        }
        if (npinf) {
            setfeatures.add("NPINF");
        }
        if (doublenp) {
            setfeatures.add("DOUBLENP");
        }
        if (inf) {
            setfeatures.add("INF");
        }
        if (nextIsThat) {
            setfeatures.add("NEXT_THAT");
        }
        if (list) {
            setfeatures.add("LIST");
        }
        if (compoundverb) {
            setfeatures.add("COMPOUND");
        }
        if (ifclause) {
            setfeatures.add("IFCLAUSE");
        }
        if (comma) {
            setfeatures.add("COMMA");
        }
        if (pp) {
            setfeatures.add("PP");
        }
        if (npvp) {
            setfeatures.add("NPVP");
        }
        if (quotes) {
            setfeatures.add("QUOTES");
        }
        if (statement) {
            setfeatures.add("STATEMENT");
        }
        return setfeatures;
    }

    private PositionInstance addRegexPatternFeatures(ChunkedExtraction extr,
            PositionInstance instance, int argstart, int argend, int current,
            boolean train) {
        boolean relnext = false;
        boolean list = false;
        boolean vbg = false;
        boolean app = false;
        boolean app2 = false;
        boolean prev_stop = false;

        if (mode != ArgLearner.Mode.LEFT) {
            vbg = pattern_extractor.vbgIsNext(extr, current + 1);
            app = pattern_extractor.appClause(extr, current + 1);
            relnext = pattern_extractor.relClause(extr, current + 1);
        } else {
            app = pattern_extractor.matchesAppositiveClause(extr, current);
            list = pattern_extractor.matchesList(extr, argend, current);
            prev_stop = pattern_extractor.prevStop(extr, current);
        }
        if (app) {
            instance.addFeature("APP");
        }
        if (app2) {
            instance.addFeature("APP2");
        }
        if (prev_stop) {
            instance.addFeature("END");
        }
        if (list) {
            instance.addFeature("LIST");
        }
        if (relnext) {
            instance.addFeature("RELNEXT");
        }
        if (vbg) {
            instance.addFeature("NEXTVBG");
        }
        return instance;
    }

    private int getCountToEnd(List<String> chunkLabels, int start) {
        int counttoend = 0;
        if (mode == ArgLearner.Mode.LEFT) {
            for (int j = start - 1; j > -1; j--) {
                if (!chunkLabels.get(j).equals("I-NP")) {
                    counttoend++;
                }
            }
        } else {
            for (int j = start + 1; j < chunkLabels.size(); j++) {
                if (!chunkLabels.get(j).equals("I-NP")) {
                    counttoend++;
                }
            }
        }
        return counttoend;
    }

    private PositionInstance addBasicFeatures(PositionInstance instance,
            String word, String chunkLabel, String tag) {
        // CHUNK feature
        instance.addFeature("CHUNK_" + chunkLabel);

        // TAG feature
        instance.addFeature("TAG_" + tag);

        // LEXICAL features, when applicable
        if (ExtractionParameters.USE_LEX_FEATURES
                && (lexicalizeIfTag.contains(tag) || lexicalizeIfWord
                        .contains(word.toLowerCase()))) {
            instance.addFeature("WD_" + word.toLowerCase());
        }

        // REGEX features, when applicable
        if (ExtractionParameters.USE_PATTERN_FEATURES) {
            Iterator<String> nameIt = featureNames.iterator();
            for (Iterator<Pattern> regexIt = patterns.iterator(); regexIt
                    .hasNext();) {
                Pattern regex = regexIt.next();
                String feature = nameIt.next();
                if (regex.matcher(word).matches()) {
                    instance.addFeature(feature);
                }
            }
        }
        return instance;
    }

    private PositionInstance addPatternFeatures(PositionInstance instance,
            ChunkedExtraction extr, List<String> setfeatures,
            List<String> chunkLabels, int i, int argstart, int argend,
            boolean train) {
        // COUNT TO END FEATURE
        instance.addFeature("C_" + getCountToEnd(chunkLabels, i));

        // PATTERN FEATURES
        if (instance.isMidInstance()) {
            for (int j = 0; j < setfeatures.size(); j++) {
                instance.addFeature(setfeatures.get(j));
            }
            instance = addRegexPatternFeatures(extr, instance, argstart,
                    argend, i, train);
        }
        return instance;
    }

    private Vector<PositionInstance> createBaseInstances(
            ChunkedExtraction extr, int argstart, int argend, Boolean train) {
        Vector<PositionInstance> instances = new Vector<PositionInstance>();

        int predstart = extr.getStart();
        int predend = extr.getLength() + predstart;

        // get the chunk labels, if the first np after predicate the predicate
        // starts with I-NP, replace with B-NP
        List<String> chunkLabelsOld = extr.getSentence().getChunkTags();
        List<String> chunkLabels = new ArrayList<String>();
        for (int i = 0; i < chunkLabelsOld.size(); i++) {
            chunkLabels.add(chunkLabelsOld.get(i));
        }
        if (predend < chunkLabels.size() - 1
                && chunkLabels.get(predend).equals("I-NP")) {
            chunkLabels.set(predend, "B-NP");
        }

        List<String> words = extr.getSentence().getTokens();
        List<String> tags = extr.getSentence().getPosTags();

        ArrayList<String> setfeatures = getSetFeatures(extr, argend, train);

        String class_i = "O";
        boolean foundpred = false;
        int lastnparg1 = -1;
        for (int i = 0; i < extr.getSentence().getLength(); i++) {
            // Inside a NP. Skip, because we treat all NPs as a single unit
            if (chunkLabels.get(i).equals("I-NP")) {
                continue;
            }
            PositionInstance instance = new PositionInstance(i);

            if ((((mode == ArgLearner.Mode.LEFT) && i < predstart && i < argend)
                    || (!train && !(mode == ArgLearner.Mode.LEFT)
                            && i >= predend && i >= argend) || (train
                    && !(mode == ArgLearner.Mode.LEFT) && i >= argstart))) {
                instance.setIsMidInstance(true);
            } else {
                instance.setIsMidInstance(false);
            }

            /****************************************************************
             * Assign LABEL (B_REL, I_REL, O_REL, O_NP, B_ARG, I_ARG) given to
             * CRF
             ****************************************************************/
            if ((i >= predstart && i < predend)) {
                instance.setIsRelInstance(true);
                foundpred = true;
            } else if (i == argstart && (mode != ArgLearner.Mode.LEFT)) {
                class_i = B_ARG;
            } else if (i >= argstart && i < argend) {
                class_i = I_ARG;
                lastnparg1 = instances.size();
            } else {
                class_i = O;
            }

            /****************************************************************
             * Basic Feature Extraction at each position i between E1 and E2:
             * WORD, CHUNK, TAG, LEXICAL, REGEX, COUNT
             ****************************************************************/
            instance = addBasicFeatures(instance, words.get(i),
                    chunkLabels.get(i), tags.get(i));
            instance = addPatternFeatures(instance, extr, setfeatures,
                    chunkLabels, i, argstart, argend, train);

            instance.addFeature(class_i);
            instances.add(instance);
        }
        if (train && (mode == ArgLearner.Mode.LEFT) && lastnparg1 > -1) {
            instances.get(lastnparg1).setFeature(
                    instances.get(lastnparg1).size() - 1, "B-ARG");
        }
        if (!foundpred
                || ((mode == ArgLearner.Mode.LEFT) && train && lastnparg1 < 0)) {
            return null;
        }
        return instances;
    }

    private PositionInstance addConjunctionFeatures(PositionInstance inst_i) {
        if (ExtractionParameters.USE_CONJUNCTIVE_FEATURES) {
            String f_str = "";
            // conjunctions of features at current timestep
            for (int k = 0; k < 3 && k < inst_i.size(); k++) {
                if (!inst_i.get(k).contains("^")) {
                    for (int m = k + 1; m < 3 && m < inst_i.size(); m++) {
                        if (!inst_i.get(m).contains("^")) {
                            f_str = inst_i.get(k) + "^" + inst_i.get(m);
                            inst_i.addFeature(inst_i.size() - 1, f_str);
                        }
                    }
                }
            }
        }
        return inst_i;
    }

    private PositionInstance addLeftWindowFeatures(
            Vector<PositionInstance> instances, int i) {
        PositionInstance inst_i = instances.get(i);
        String f_str = "";
        // Go WINDOW positions to the left
        for (int j = Math.max(0, i - ExtractionParameters.WINDOW); j < i; j++) {
            int d = i - j;
            PositionInstance inst_j = instances.get(j);
            if (inst_j.isMidInstance()) {
                if (ExtractionParameters.USE_CONTEXTUAL_FEATURES
                        || inst_j.isMidInstance()) {

                    String label_j = inst_j.label();

                    // previous singleton features: TAG, CHUNK features, and
                    // closed-class lexical features when present
                    for (int k = 0; k < 3 && k < inst_j.size(); k++) {
                        String f_jk = inst_j.get(k);
                        if (k == CHUNK_FEATURE) {
                            f_str = "L" + Integer.toString(d) + "-" + f_jk;
                            inst_i.addFeature(inst_i.size() - 1, f_str);
                            // conjunction of current and previous
                            if (ExtractionParameters.USE_CONJUNCTIVE_FEATURES
                                    && j == i - 1) {
                                f_str = f_str + "^L0-" + inst_i.get(k);
                                inst_i.addFeature(inst_i.size() - 1, f_str);
                            }
                        } else if (k == TAG_FEATURE && !label_j.equals("O-NP")) {
                            f_str = "L" + Integer.toString(d) + "-" + f_jk;
                            inst_i.addFeature(inst_i.size() - 1, f_str);
                            if (ExtractionParameters.USE_CONJUNCTIVE_FEATURES
                                    && j == i - 1) {
                                f_str = f_str + "^L0-" + inst_i.get(k);
                                inst_i.addFeature(inst_i.size() - 1, f_str);
                            }
                        } else if (k == LEX_FEATURE && f_jk.charAt(0) == 'W') {
                            f_str = "L" + Integer.toString(d) + "-" + f_jk;
                            inst_i.addFeature(inst_i.size() - 1, f_str);
                            if (ExtractionParameters.USE_CONJUNCTIVE_FEATURES
                                    && j == i - 1
                                    && inst_i.get(k).charAt(0) == 'W') {
                                f_str = f_str + "^L0-" + inst_i.get(k);
                                inst_i.addFeature(inst_i.size() - 1, f_str);
                            }
                        }

                    }
                }
            }
        } // end LEFT WINDOW
        return inst_i;
    }

    private PositionInstance addRightWindowFeatures(
            Vector<PositionInstance> instances, int i) {
        PositionInstance inst_i = instances.get(i);
        String f_str = "";
        PositionInstance prev_inst = null;
        String prev_label = null;

        for (int j = i + 1; j < instances.size()
                && j <= i + ExtractionParameters.WINDOW; j++) {

            int d = j - i;
            PositionInstance inst_j = instances.get(j);
            String label_j = inst_j.label();
            if (inst_j.isMidInstance()) {
                if (ExtractionParameters.USE_CONTEXTUAL_FEATURES
                        || inst_j.isMidInstance()) {
                    for (int k = 0; k < 3 && k < inst_j.size(); k++) {
                        String f_jk = inst_j.get(k);

                        // future singleton features: TAG, CHUNK features, and
                        // closed-class lexical features when present
                        if (k == CHUNK_FEATURE) {
                            f_str = "R" + Integer.toString(d) + "-" + f_jk;
                            inst_i.addFeature(inst_i.size() - 1, f_str);

                            // conjunction of current and next two
                            if (ExtractionParameters.USE_CONJUNCTIVE_FEATURES
                                    && j == i + 2) {
                                f_str += "^R1-" + prev_inst.get(k);
                                f_str += "^R0-" + inst_i.get(k);
                                inst_i.addFeature(inst_i.size() - 1, f_str);
                            }
                        } else if (k == TAG_FEATURE && !label_j.equals("O-NP")) {
                            f_str = "R" + Integer.toString(d) + "-" + f_jk;
                            inst_i.addFeature(inst_i.size() - 1, f_str);
                            if (ExtractionParameters.USE_CONJUNCTIVE_FEATURES
                                    && j == i + 2 && !prev_label.equals("O-NP")) { // (0,1,2)
                                f_str += "^R1-" + prev_inst.get(k);
                                f_str += "^R0-" + inst_i.get(k);
                                inst_i.addFeature(inst_i.size() - 1, f_str);
                            }
                        } else if (k == LEX_FEATURE && f_jk.charAt(0) == 'W') {
                            f_str = "R" + Integer.toString(d) + "-" + f_jk;
                            inst_i.addFeature(inst_i.size() - 1, f_str);
                            if (ExtractionParameters.USE_CONJUNCTIVE_FEATURES) {
                                if (j == i + 2) {
                                    if (!prev_label.equals("O-NP")) {
                                        if (inst_i.get(k).charAt(0) == 'W') {
                                            if (prev_inst.get(k).charAt(0) == 'W') {
                                                f_str += "^R1-"
                                                        + prev_inst.get(k);
                                                f_str += "^R0-" + inst_i.get(k);
                                                inst_i.addFeature(
                                                        inst_i.size() - 1,
                                                        f_str);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                prev_label = label_j;
                prev_inst = inst_j;
            } // end RIGHT WINDOW
        }
        return inst_i;
    }

    /**
     * Feature Extraction at each position +/0 WINDOW arg1/after E1 and E2
     * Conjunctive features are again same as Peng and McCallum
     */
    private Vector<Vector<String>> addWindowFeatures(
            Vector<PositionInstance> instances) {
        Vector<Vector<String>> features = new Vector<Vector<String>>();
        for (int i = 0; i < instances.size(); i++) {
            PositionInstance inst_i = instances.get(i);
            if (inst_i.isMidInstance()) {
                inst_i = addConjunctionFeatures(inst_i);
                inst_i = addLeftWindowFeatures(instances, i);
                inst_i = addRightWindowFeatures(instances, i);

                // we reverse the order for arg1
                if (mode == ArgLearner.Mode.LEFT) {
                    features.add(0, inst_i.features());
                } else {
                    features.add(inst_i.features());
                }
            }
        }
        return features;
    }

    private Vector<Vector<String>> addPredicateFeatures(
            Vector<PositionInstance> instances, Vector<Vector<String>> features) {

        Vector<String> rel_inst = new Vector<String>();
        String relchunks = "";
        String reltags = "";
        String relwds = "";

        for (int i = 0; i < instances.size(); i++) {
            PositionInstance inst_i = instances.get(i);
            if (inst_i.isRelInstance()) {
                Vector<String> inst_i_features = inst_i.features();
                for (int j = 0; j < inst_i_features.size(); j++) {
                    if (inst_i_features.get(j).split("_").length > 1) {
                        String curtype = inst_i_features.get(j).split("_")[0]
                                .trim();
                        String curfeature = inst_i_features.get(j).split("_")[1]
                                .trim();
                        if (curtype.equals("TAG")) {
                            reltags += "TAG_" + curfeature + "^";
                        } else if (curtype.equals("CHUNK")) {
                            relchunks += "CHUNK_" + curfeature + "^";
                        } else if (curtype.equals("WD")) {
                            relwds += "WD_" + curfeature + "^";
                        }
                    }
                }
            }
        }

        if (relwds.length() > 0) {
            relwds = relwds.substring(0, relwds.length() - 1);
            rel_inst.add(0, relwds);
        }
        if (reltags.length() > 0) {
            reltags = reltags.substring(0, reltags.length() - 1);
            rel_inst.add(0, reltags);
        }
        if (relchunks.length() > 0) {
            relchunks = relchunks.substring(0, relchunks.length() - 1);
            rel_inst.add(0, relchunks);
        }
        rel_inst.add("ENTITY1");
        rel_inst.add("O");

        features.add(0, rel_inst);

        return features;
    }

    // Extract a list of features for this extraction
    public String extractCRFFeatures(ChunkedExtraction extr, int argstart,
            int argend, Boolean train) {
        Vector<PositionInstance> instances = createBaseInstances(extr,
                argstart, argend, train);
        if (instances == null) {
            return null;
        }

        Vector<Vector<String>> features = addWindowFeatures(instances);
        if (features.size() == 0) {
            return null;
        }
        features = addPredicateFeatures(instances, features);

        String featureString = convertInstancesToFeatures(features, train);
        return featureString;
    }
}
