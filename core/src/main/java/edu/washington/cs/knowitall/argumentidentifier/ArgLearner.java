package edu.washington.cs.knowitall.argumentidentifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.extractor.Extractor;
import edu.washington.cs.knowitall.extractor.ExtractorException;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedArgumentExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;

/**
 * ArgLearner main class. Extracts either left or right argument.
 *
 * @author janara
 *
 */

public class ArgLearner extends
        Extractor<ChunkedExtraction, ChunkedArgumentExtraction> {
    public enum Mode {
        LEFT, RIGHT
    };

    private Mode mode;

    private PatternExtractor patternextractor;

    public ArgLocationClassifier arg1rightboundclassifier;
    public ArgLocationClassifier arg2leftboundclassifier;

    public ArgSubstructureClassifier arg1leftboundclassifier;
    public ArgSubstructureClassifier arg2rightboundclassifier;

    public ArgLearner(Mode mode) {
        this.mode = mode;
        patternextractor = new PatternExtractor();

        try {
            if (mode == Mode.LEFT) {
                ArgSubstructureFeatureGenerator featuregeneratorsub = new ArgSubstructureFeatureGenerator(
                        mode);
                arg1rightboundclassifier = new ArgLocationClassifier(mode);
                arg1leftboundclassifier = new ArgSubstructureClassifier(mode,
                        featuregeneratorsub);
            } else {
                ArgSubstructureFeatureGenerator featuregeneratorsub = new ArgSubstructureFeatureGenerator(
                        mode);
                arg2leftboundclassifier = new ArgLocationClassifier(mode);
                arg2rightboundclassifier = new ArgSubstructureClassifier(mode,
                        featuregeneratorsub);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected Iterable<ChunkedArgumentExtraction> extractCandidates(
            ChunkedExtraction predicate) throws ExtractorException {
        Collection<ChunkedArgumentExtraction> args = new ArrayList<ChunkedArgumentExtraction>();
        ChunkedArgumentExtraction arg = null;
        if (mode == Mode.LEFT) {
            arg = getArg1(predicate);
        } else {
            arg = getArg2(predicate);
        }
        if (arg != null) {
            ArrayList<ChunkedArgumentExtraction> splitargs = splitArg(arg);
            args.addAll(splitargs);
        }

        return args;
    }

    /**
	 *
	 */
    private ArrayList<ChunkedArgumentExtraction> splitArg(
            ChunkedArgumentExtraction arg) {
        ArrayList<ChunkedArgumentExtraction> args = new ArrayList<ChunkedArgumentExtraction>();
        if (patternextractor.matchesListStrict(arg)) {
            int start = arg.getStart();
            int length = 0;
            for (int i = arg.getStart(); i < arg.getStart() + arg.getLength(); i++) {
                if ((arg.getSentence().getToken(i).equals(",") && (i >= arg
                        .getSentence().getLength() || (!arg.getSentence()
                        .getToken(i + 1).equals("and") && !arg.getSentence()
                        .getToken(i + 1).equals("or"))))
                        || arg.getSentence().getToken(i).equals("and")
                        || arg.getSentence().getToken(i).equals("or")) {
                    args.add(new ChunkedArgumentExtraction(arg.getSentence(),
                            new Range(start, length), arg.getRelation()));
                    start = i + 1;
                    length = 0;
                } else if (!arg.getSentence().getToken(i).equals(",")) {
                    length++;
                }
            }
            args.add(new ChunkedArgumentExtraction(arg.getSentence(),
                    new Range(start, length), arg.getRelation()));

        } else if (patternextractor.matchesAppositiveStrict(arg)) {
            int start = arg.getStart();
            int length = 0;
            for (int i = arg.getStart(); i < arg.getStart() + arg.getLength(); i++) {
                if (arg.getSentence().getToken(i).equals(",")) {
                    args.add(new ChunkedArgumentExtraction(arg.getSentence(),
                            new Range(start, length), arg.getRelation()));
                    start = i + 1;
                    length = 0;
                } else {
                    length++;
                }
            }
            args.add(new ChunkedArgumentExtraction(arg.getSentence(),
                    new Range(start, length), arg.getRelation()));

        } else {
            args.add(arg);
        }
        return args;
    }

    /**
     * gets arg1 for the given predicate
     *
     * @param predicate
     * @return arg1
     */
    private ChunkedArgumentExtraction getArg1(ChunkedExtraction predicate) {
        if (predicate.getStart() < 1) {
            return null;
        }
        double[] classifierresults = arg1rightboundclassifier
                .getArgBound(predicate);

        int rightbound = (int) classifierresults[0];
        if (rightbound > 0) {
            double[] leftbound_conf = getArg1LeftBound(predicate, rightbound);
            int leftbound = (int) leftbound_conf[0];
            if (leftbound >= 0) {
                double conf = leftbound_conf[1];
                ChunkedArgumentExtraction arg1 = new ChunkedArgumentExtraction(
                        predicate.getSentence(), new Range(leftbound, rightbound
                                - leftbound), predicate, conf);
                return arg1;
            }
        }
        return null;
    }

    /**
     * finds the left bound for arg1
     *
     * @param predicate
     * @param rightbound
     * @return left bound
     */
    private double[] getArg1LeftBound(ChunkedExtraction predicate,
            int rightbound) {
        double[] resultsclassifier = arg1leftboundclassifier.getArgBound(
                predicate, rightbound);
        if (resultsclassifier[0] == -1) {
            resultsclassifier[0] = getNPStart(predicate, rightbound - 1);
        }
        return resultsclassifier;
    }

    /**
     * gets arg2 for the given predicate
     *
     * @param predicate
     * @return arg2
     */
    private ChunkedArgumentExtraction getArg2(ChunkedExtraction predicate) {

        // get the leftbound
        int leftbound = getArg2LeftBound(predicate);

        // get the rightbound
        int rightbound = leftbound;
        double conf = 0;
        if (leftbound > -1) {
            double[] confloc = getArg2RightBound(predicate, leftbound);
            rightbound = (int) confloc[0];
            conf = confloc[1];
        }

        // check that the bounds are valid
        if (rightbound <= leftbound || leftbound < 0
                || leftbound > predicate.getSentence().getLength() - 1) {
            leftbound = 0;
            rightbound = 0;
        }

        // create arg2
        Range newrange = new Range(leftbound, rightbound - leftbound);
        if (rightbound == 0) {
            return null;
        }
        ChunkedArgumentExtraction argument2 = new ChunkedArgumentExtraction(
                predicate.getSentence(), newrange, predicate, conf);

        // check final condition
        if (!argument2.getChunkTagsAsString().contains("NP")) {
            return null;
        }
        return argument2;
    }

    /**
     * finds the left bound for arg2, default is word after the predicate
     *
     * @param predicate
     * @return left bound
     */
    private int getArg2LeftBound(ChunkedExtraction predicate) {
        return (int) arg2leftboundclassifier.getArgBound(predicate)[0];
    }

    /**
     * finds the right bound and the crf confidence
     *
     * @param predicate
     * @param left
     *            bound
     * @return [right bound, crf confidence]
     */
    private double[] getArg2RightBound(ChunkedExtraction predicate,
            int leftbound) {
        double[] resultsclassifier = arg2rightboundclassifier.getArgBound(
                predicate, leftbound);
        return resultsclassifier;
    }

    /**
     * gets the start of the np given the end
     *
     * @param extr
     * @param end
     * @return start of np
     */
    private int getNPStart(ChunkedExtraction extr, int end) {
        for (int i = end; i > -1; i--) {
            if (i == extr.getRange().getStart()) {
                return i;
            }
            if (extr.getSentence().getChunkTags().get(i).equals("B-NP")) {
                return i;
            }
        }
        return -1;
    }

}
