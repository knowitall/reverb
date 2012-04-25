package edu.washington.cs.knowitall.extractor.conf;

import java.io.File;
import java.io.IOException;
import java.util.SortedMap;

import opennlp.maxent.GISModel;
import opennlp.maxent.io.SuffixSensitiveGISModelReader;
import edu.washington.cs.knowitall.extractor.conf.featureset.BooleanFeatureSet;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;
import edu.washington.cs.knowitall.util.DefaultObjects;

/***
 * An extraction confidence function that is backed by a logistic regression
 * classifier. This function will assign an extraction a real valued number
 * between 0 and 1 according to the logistic regression model.
 * 
 * It represents an extraction using the boolean features defined by the
 * <code>ReVerbFeatures</code> class. See that documentation for details.
 * 
 * It uses the model returned by
 * <code>DefaultObjects.getDefaultConfClassifier</code>, which searches the
 * classpath for a file called "conf.weka".
 * 
 * @author afader
 * 
 */
public class ReVerbOpenNlpConfFunction implements ConfidenceFunction {

    private ReVerbFeatures reverbFeatures;
    private BooleanFeatureSet<ChunkedBinaryExtraction> featureSet;
    private GISModel model;

    /**
     * Constructs a new instance of the confidence function.
     * 
     * @throws ConfidenceFunctionException
     *             if unable to initialize
     */
    public ReVerbOpenNlpConfFunction() throws ConfidenceFunctionException {
        this(DefaultObjects.confFunctionModelFile);
    }

    public ReVerbOpenNlpConfFunction(String model) {
        try {
            try {
                this.model = (GISModel) new SuffixSensitiveGISModelReader(
                        new File("/home/michael/model.tar.gz")).getModel();
            } catch (Exception e) {
                throw new IOException(e);
            }

            initializeConfFunction();

        } catch (IOException e) {
            throw new ConfidenceFunctionException("Unable to load classifier: "
                    + model, e);
        }
    }

    /* Assumes that this.classifier is valid */
    private void initializeConfFunction() {
        this.reverbFeatures = new ReVerbFeatures();
        this.featureSet = reverbFeatures.getFeatureSet();
    }

    private String[] featurize(ChunkedBinaryExtraction extr) {
        SortedMap<String, Boolean> featurized = this.featureSet
                .featurize(extr);
        String[] stringFeatures = new String[featurized.size()];

        int i = 0;
        for (String feature : featurized.keySet()) {
            stringFeatures[i++] = feature + "="
                    + Boolean.toString(featurized.get(feature));
        }

        return stringFeatures;
    }

    /**
     * @param extr
     * @return the probability that the given extraction belongs to the positive
     *         class
     * @throws ConfidenceFunctionException
     *             if unable to compute the confidence score
     */
    public double getConf(ChunkedBinaryExtraction extr)
            throws ConfidenceFunctionException {
        /*
        for (double d : this.model.eval(this.featurize(extr))) {
            System.out.println(d);
        }
        System.out.println(this.model.getBestOutcome(this.model.eval(this.featurize(extr))));
        */
        return this.model.eval(this.featurize(extr))[1];
    }
}
