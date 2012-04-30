package edu.washington.cs.knowitall.extractor.conf.weka;

import edu.washington.cs.knowitall.extractor.conf.featureset.BooleanFeatureSet;
import weka.classifiers.Classifier;
import weka.core.Instance;

/***
 * A confidence function that takes an object of type <code>T</code> as input,
 * and returns a real valued number. This function operates in two steps.
 *
 * First, it computes a boolean feature representation of the object using a
 * <code>BooleanFeatureSet</code> object.
 *
 * Second, it passes the featurized object to a binary classifier and returns
 * the confidence of the object belonging to the positive class. The binary
 * classifier is represented by a Weka <code>Classifier</code> object.
 *
 * The caller is responsible for making sure that the features defined by the
 * <code>BooleanFeatureSet</code> object are consistent with the feature
 * representation used by the <code>Classifier</code> object. If the
 * <code>BooleanFeatureSet</code> object has n features, then the
 * <code>Classifier</code> object must have n numeric attributes and one nominal
 * attribute (corresponding to the class value). The order of the features in
 * <code>Classifier</code> must be the same as the order defined by
 * <code>BooleanFeatureSet</code>, with the class attribute appearing last.
 *
 * @author afader
 *
 * @param <T>
 */
public class WekaClassifierConfFunction<T> {

    private WekaFeatureSet<T> wekaFeatureSet;
    private Classifier classifier;

    /**
     * Constructs a new classifier confidence function that uses the given
     * feature set and classifier.
     *
     * @param featureSet
     * @param classifier
     */
    public WekaClassifierConfFunction(BooleanFeatureSet<T> featureSet,
            Classifier classifier) {
        this.wekaFeatureSet = new WekaFeatureSet<T>(featureSet);
        this.classifier = classifier;
    }

    /**
     * Computes the confidence that the given object belongs to the positive
     * class according to the classifier.
     *
     * @param object
     * @return the confidence score
     * @throws Exception
     *             if the classifier is unable to compute the confidence
     */
    public double getConf(T object) throws Exception {
        Instance inst = wekaFeatureSet.getInstanceFrom(object);
        double[] distr = classifier.distributionForInstance(inst);
        // return the positive class distribution value
        return distr[0];
    }

}
