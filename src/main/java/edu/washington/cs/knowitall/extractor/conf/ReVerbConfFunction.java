package edu.washington.cs.knowitall.extractor.conf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import weka.classifiers.Classifier;
import weka.core.SerializationHelper;
import edu.washington.cs.knowitall.extractor.conf.featureset.BooleanFeatureSet;
import edu.washington.cs.knowitall.extractor.conf.weka.WekaClassifierConfFunction;
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
public class ReVerbConfFunction implements ConfidenceFunction {
    public final Classifier classifier;
    private ReVerbFeatures reverbFeatures;
    private BooleanFeatureSet<ChunkedBinaryExtraction> featureSet;
    private WekaClassifierConfFunction<ChunkedBinaryExtraction> func;

    /**
     * Constructs a new instance of the confidence function using the default
     * model.
     *
     * @throws ConfidenceFunctionException
     *             if unable to initialize
     * @throws IOException
     */
    public ReVerbConfFunction() throws ConfidenceFunctionException, IOException {
        this(DefaultObjects.confFunctionModelFile);
    }

    /**
     * Constructs a new instance of the confidence function from the specified
     * URL.
     *
     * @throws ConfidenceFunctionException
     *             if unable to initialize
     * @throws IOException
     */
    public ReVerbConfFunction(URL url) throws IOException {
        InputStream is = url.openStream();
        try {
            try {
                classifier = (Classifier) SerializationHelper.read(is);
            } catch (Exception e) {
                throw new IOException(e);
            }

            initializeConfFunction();

        } catch (IOException e) {
            throw new ConfidenceFunctionException("Unable to load classifier: "
                    + url, e);
        } finally {
            is.close();
        }
    }

    /**
     * Constructs a new instance of the confidence function from the specified
     * resource item.
     *
     * @throws ConfidenceFunctionException
     *             if unable to initialize
     * @throws IOException
     */
    public ReVerbConfFunction(String model) throws IOException {
        this(ReVerbConfFunction.class.getClassLoader().getResource(model));
    }

    /**
     * Constructs a new instance of the confidence function from the specified
     * file.
     *
     * @throws ConfidenceFunctionException
     *             if unable to initialize
     * @throws IOException
     */
    public ReVerbConfFunction(File file) throws IOException {
        this(file.toURI().toURL());
    }

    /* Assumes that this.classifier is valid */
    private void initializeConfFunction() {

        reverbFeatures = new ReVerbFeatures();
        featureSet = reverbFeatures.getFeatureSet();
        func = new WekaClassifierConfFunction<ChunkedBinaryExtraction>(
                featureSet, classifier);
    }

    public ReVerbConfFunction(Classifier classifier) {

        this.classifier = classifier;

        initializeConfFunction();
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
        try {
            return func.getConf(extr);
        } catch (Exception e) {
            throw new ConfidenceFunctionException(e);
        }
    }
}
