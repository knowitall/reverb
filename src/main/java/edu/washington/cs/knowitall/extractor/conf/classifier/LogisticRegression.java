package edu.washington.cs.knowitall.extractor.conf.classifier;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import edu.washington.cs.knowitall.extractor.conf.featureset.FeatureSet;

public class LogisticRegression<E> {
    private final FeatureSet<E> featureSet;
    private final Map<String, Double> featureWeights;
    private final double intercept;

    public LogisticRegression(FeatureSet<E> featureSet,
            Map<String, Double> weights) {
        this.featureSet = featureSet;
        this.featureWeights = weights;
        this.intercept = featureWeights.get("intercept");
    }

    public LogisticRegression(FeatureSet<E> featureSet, InputStream input)
            throws FileNotFoundException {
        this(featureSet, buildFeatureWeightMap(input));
    }

    public static Map<String, Double> buildFeatureWeightMap(InputStream input)
            throws FileNotFoundException {
        Map<String, Double> featureWeights = new HashMap<String, Double>();
        Scanner scan = new Scanner(input);

        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            String[] parts = line.split("(?:    \\s*)|\\t\\s*");

            String featureName = parts[0];
            Double weight = Double.parseDouble(parts[1]);
            featureWeights.put(featureName.toLowerCase(), weight);
        }

        return featureWeights;
    }

    public double confidence(E extraction) {
        double z = intercept;
        for (String featureName : this.featureSet.getFeatureNames()) {
            if (featureWeights.containsKey(featureName)) {
                double weight = featureWeights.get(featureName);
                double feature = featureSet.featurize(featureName, extraction);

                z += weight * feature;
            }
        }

        return 1.0 / (1.0 + Math.exp(-z));
    }
}
