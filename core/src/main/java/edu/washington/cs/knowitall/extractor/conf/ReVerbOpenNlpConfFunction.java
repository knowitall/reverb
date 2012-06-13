package edu.washington.cs.knowitall.extractor.conf;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import opennlp.maxent.GISModel;
import opennlp.maxent.io.PlainTextGISModelReader;
import edu.washington.cs.knowitall.extractor.conf.featureset.BooleanFeatureSet;
import edu.washington.cs.knowitall.extractor.conf.opennlp.OpenNlpConfFunction;
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
public class ReVerbOpenNlpConfFunction implements ConfidenceFunction {

    private ReVerbFeatures reverbFeatures;
    private OpenNlpConfFunction<ChunkedBinaryExtraction> conf;

    /**
     * Constructs a new instance of the confidence function.
     *
     * @throws ConfidenceFunctionException
     *             if unable to initialize
     * @throws IOException
     */
    public ReVerbOpenNlpConfFunction() throws ConfidenceFunctionException,
            IOException {
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
    public ReVerbOpenNlpConfFunction(URL url) throws IOException {
        InputStream is = url.openStream();
        try {
            GISModel model;
            try {
                model = (GISModel) new PlainTextGISModelReader(
                        new BufferedReader(new InputStreamReader(
                                new GZIPInputStream(is)))).getModel();
            } catch (Exception e) {
                throw new IOException(e);
            }

            initializeConfFunction(model);

        } catch (IOException e) {
            throw new ConfidenceFunctionException("Unable to load classifier: "
                    + url, e);
        } finally {
            is.close();
        }
    }

    /***
     * Load a model specified as a resource path from the root.
     * @param modelResourcePath
     * @throws IOException
     */
    public ReVerbOpenNlpConfFunction(String modelResourcePath)
            throws IOException {
        this(ReVerbOpenNlpConfFunction.class.getClassLoader().getResource(
                modelResourcePath));
    }

    /***
     * Load a model from the specified file.
     * @param modelFile
     * @throws IOException
     */
    public ReVerbOpenNlpConfFunction(File modelFile) throws IOException {
        this(modelFile.toURI().toURL());
    }

    /* Assumes that this.classifier is valid */
    private void initializeConfFunction(GISModel model) throws IOException {
        this.reverbFeatures = new ReVerbFeatures();
        BooleanFeatureSet<ChunkedBinaryExtraction> featureSet = reverbFeatures.getFeatureSet();
        this.conf = new OpenNlpConfFunction<ChunkedBinaryExtraction>(model, featureSet);
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
        return this.conf.getConf(extr);
    }
}
