package edu.washington.cs.knowitall.extractor.conf;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import edu.washington.cs.knowitall.extractor.conf.classifier.LogisticRegression;
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
 * @author schmmd
 *
 */
public class ReVerbIndependentConfFunction implements ConfidenceFunction {

    private ReVerbFeatures reverbFeatures;
    private BooleanFeatureSet<ChunkedBinaryExtraction> featureSet;
    private final LogisticRegression<ChunkedBinaryExtraction> logreg;

    /**
     * Loads the model from the specified url.
     * 
     * @param  model  an URL to the model
     */
    public ReVerbIndependentConfFunction(URL modelUrl) {
        try {
            reverbFeatures = new ReVerbFeatures();
            featureSet = reverbFeatures.getFeatureSet();
            logreg = new LogisticRegression<ChunkedBinaryExtraction>(
                    featureSet, modelUrl.openStream());
        } catch (IOException e) {
            throw new ConfidenceFunctionException("Unable to load classifier: "
                    + modelUrl, e);
        }
    }

    /**
     * Loads the model from specified file.
     * 
     * @param  model  the model file to load
     * @throws MalformedURLException 
     */
    public ReVerbIndependentConfFunction(File modelFile) throws MalformedURLException {
    	this(modelFile.toURI().toURL());
    }

    /**
     * Loads the model as a resource from the root.
     * 
     * @param  model  the model file to load
     */
    public ReVerbIndependentConfFunction(String modelResourceName) {
    	this(ReVerbIndependentConfFunction.class.getClassLoader().getResource(modelResourceName));
    }

    /**
     * @param extr
     * @return the probability that the given extraction belongs to the positive
     *         class
     * @throws ConfidenceFunctionException
     *             if unable to compute the confidence score
     */
    @Override
    public double getConf(ChunkedBinaryExtraction extr)
            throws ConfidenceFunctionException {
        try {
            return logreg.confidence(extr);
        } catch (Exception e) {
            throw new ConfidenceFunctionException(e);
        }
    }
}
