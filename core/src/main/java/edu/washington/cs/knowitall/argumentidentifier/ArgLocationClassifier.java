package edu.washington.cs.knowitall.argumentidentifier;

import java.io.IOException;
import java.net.URL;

import edu.washington.cs.knowitall.argumentidentifier.ArgLearner.Mode;
import edu.washington.cs.knowitall.extractor.conf.classifier.DecisionTree;
import edu.washington.cs.knowitall.extractor.conf.classifier.DoubleFeatures;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;

/**
 * ArgLocationClassifier uses weka to classify the right bound for Arg1 and
 * heuristics (closest word) for Arg2
 *
 * @author janara
 *
 */
public class ArgLocationClassifier {
    private static URL modelUrl;

    {
        try {
            modelUrl = ArgLocationClassifier.class.getResource("/r2a2-arg1loc.tree");
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private DecisionTree classifier;
    private Arg1LocationFeatureGenerator featuregenerator;

    private Mode mode;

    public ArgLocationClassifier(Mode mode) throws IOException {
        this.mode = mode;
        if (mode == ArgLearner.Mode.LEFT) {
            this.featuregenerator = new Arg1LocationFeatureGenerator();
            setupClassifier(modelUrl);
        }
    }

    public double[] getArgBound(ChunkedExtraction predicate) {
        if (mode == ArgLearner.Mode.LEFT) {
            double[] resultsclassifier = { -1, 1 };
            int classification = -1;
            int k = predicate.getStart() - 1;
            int rightbound = -1;

            // classify each np
            while (k > -1 && classification == -1) {
                if (predicate.getSentence().getChunkTag(k).equals("B-NP")) {
                    DoubleFeatures features = featuregenerator.extractFeatures(
                            predicate, null, k, false);
                    String outcome = classifier.classify(features);
                    classification = outcome.equals("closest_np") ? 0 : -1;
                    rightbound = k;
                }
                k--;
            }

            // adjust the np to be the righbound
            rightbound++;
            while (rightbound < predicate.getStart()
                    && predicate.getSentence().getChunkTag(rightbound)
                            .equals("I-NP")) {
                rightbound++;
            }
            resultsclassifier[0] = rightbound;
            return resultsclassifier;
        } else {
            int bound = predicate.getStart() + predicate.getLength();
            if ((predicate.getStart() + predicate.getLength()) >= predicate
                    .getSentence().getLength()
                    || !(predicate
                            .getSentence()
                            .getPosTag(
                                    predicate.getStart()
                                            + predicate.getLength())
                            .startsWith("N")
                            || predicate
                                    .getSentence()
                                    .getPosTag(
                                            predicate.getStart()
                                                    + predicate.getLength())
                                    .startsWith("J")
                            || predicate
                                    .getSentence()
                                    .getPosTag(
                                            predicate.getStart()
                                                    + predicate.getLength())
                                    .startsWith("CD")
                            || predicate
                                    .getSentence()
                                    .getPosTag(
                                            predicate.getStart()
                                                    + predicate.getLength())
                                    .startsWith("PRP")
                            || predicate
                                    .getSentence()
                                    .getPosTag(
                                            predicate.getStart()
                                                    + predicate.getLength())
                                    .startsWith("WRB")
                            || predicate
                                    .getSentence()
                                    .getPosTag(
                                            predicate.getStart()
                                                    + predicate.getLength())
                                    .startsWith("IN") || predicate
                            .getSentence()
                            .getPosTag(
                                    predicate.getStart()
                                            + predicate.getLength())
                            .startsWith("DT"))) {
                bound = -1;
            }
            double[] results = { bound, 1 };
            return results;
        }
    }

    private void setupClassifier(URL modelUrl) throws IOException {
        this.classifier = DecisionTree.fromModel(modelUrl);
    }
}
