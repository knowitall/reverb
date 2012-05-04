package edu.washington.cs.knowitall.extractor.conf.opennlp;

import java.io.IOException;

import opennlp.maxent.GISModel;
import edu.washington.cs.knowitall.extractor.conf.ConfidenceFunctionException;
import edu.washington.cs.knowitall.extractor.conf.featureset.BooleanFeatureSet;

/***
 * An extraction confidence function that is backed by a logistic regression
 * classifier. This function will assign an extraction a real valued number
 * between 0 and 1 according to the logistic regression model.
 *
 * It represents an extraction using a boolean feature set.
 *
 * @author schmmd
 *
 */
public class OpenNlpConfFunction<E> {

    // the set of boolean features
    private BooleanFeatureSet<E> featureSet;

    // the underlying OpenNlp model
    private GISModel model;

    /**
     * Used to optimize featurization by turning string operations
     * into a map lookup.
     */
    private OpenNlpAlphabet<E> alphabet;

    public OpenNlpConfFunction(GISModel model, BooleanFeatureSet<E> featureSet) throws IOException {
        this.model = model;
        this.featureSet = featureSet;
        this.alphabet = new OpenNlpAlphabet<E>(this.featureSet);
    }

    /**
     * Turn an extraction into the feature representation used by OpenNlp.
     * @param  extr  the extraction to featurize
     * @return a featurized representation
     */
    public String[] featurize(E extr) {
        String[] stringFeatures = new String[this.featureSet.getNumFeatures()];

        int i = 0;
        for (String feature : this.featureSet.getFeatureNames()) {
            boolean value = this.featureSet.featurizeToBool(feature, extr);
            stringFeatures[i++] = this.alphabet.lookup
                    .get(new OpenNlpAlphabet.Key(feature, value));
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
    public double getConf(E extr)
            throws ConfidenceFunctionException {
        int i = 0;
        while (!this.model.getOutcome(i).equals("1")) {
            i++;
        }

        return this.model.eval(this.featurize(extr))[i];
    }
}
