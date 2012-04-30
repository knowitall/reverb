package edu.washington.cs.knowitall.extractor.conf.weka;

import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import edu.washington.cs.knowitall.extractor.conf.featureset.FeatureSet;

/***
 * A wrapper for the Weka <code>Instances</code> class. This class allows the
 * caller to add instances to a data set by directly passing an object of type
 * <code>T</code> and a label. The object is then featurized using a
 * <code>BooleanFeatureSet</code> and added to a Weka <code>Instances</code>
 * object.
 *
 * @author afader
 *
 * @param <T>
 */
public class WekaDataSet<T> {

    private WekaFeatureSet<T> wekaFeatureSet;
    private Instances instances;

    /**
     * Constructs a new data set
     *
     * @param name
     *            the name of the data set
     * @param featureSet
     *            the feature representation of the data set
     */
    public WekaDataSet(String name, FeatureSet<T> featureSet) {
        wekaFeatureSet = new WekaFeatureSet<T>(featureSet);
        FastVector attributes = wekaFeatureSet.getAttributesCopy();
        instances = new Instances(name, attributes, 0);
        instances.setClassIndex(attributes.size() - 1);
    }

    /**
     * Adds a new instance to the data set with the given label (0 for negative,
     * 1 for positive).
     *
     * @param object
     * @param label
     */
    public void addInstance(T object, int label) {
        Instance instance = wekaFeatureSet.getInstanceFrom(object, label);
        instances.add(instance);
    }

    /**
     * Adds a new unlabeled instance to the data set with the given label (0 for
     * negative, 1 for positive).
     *
     * @param object
     */
    public void addInstance(T object) {
        Instance instance = wekaFeatureSet.getInstanceFrom(object);
        instances.add(instance);
    }

    /**
     * @return the underlying Weka <code>Instances</code> object
     */
    public Instances getWekaInstances() {
        return instances;
    }
}
