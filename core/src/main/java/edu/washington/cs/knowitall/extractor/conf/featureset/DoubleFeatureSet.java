package edu.washington.cs.knowitall.extractor.conf.featureset;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

/***
 * Represents a set of boolean feature functions that can be applied to objects
 * of type <code>T</code>. Each feature is represented as a
 * <code>Predicate<T></code> object, and has a String name.
 *
 * @author afader
 *
 * @param <T>
 */
public class DoubleFeatureSet<T> extends FeatureSet<T> {

    private ImmutableList<Function<T, Double>> features;

    /**
     * Constructs a new feature set from the given features.
     *
     * @param givenFeatures
     */
    public DoubleFeatureSet(Map<String, Function<T, Double>> givenFeatures) {
        super(ImmutableList.copyOf(givenFeatures.keySet()));

        TreeMap<String, Function<T, Double>> sortedFeatures = new TreeMap<String, Function<T, Double>>(
                givenFeatures);
        features = ImmutableList.copyOf(sortedFeatures.values());
    }

    /**
     * Returns a feature representation of the given object.
     *
     * @param object
     * @return a sorted map, mapping feature names to boolean values
     */
    public SortedMap<String, Double> featurize(T object) {
        TreeMap<String, Double> values = new TreeMap<String, Double>();
        for (int i = 0; i < this.getNumFeatures(); i++) {
            String featureName = featureNames.get(i);
            double featureValue = features.get(i).apply(object);
            values.put(featureName, featureValue);
        }
        return values;
    }

    /**
     * Returns the feature representation of object as an array of doubles. Each
     * value in the array corresponds to a feature (where 1.0 is true and 0.0 is
     * false). The ith value corresponds to the ith feature.
     *
     * @param object
     * @return
     */
    public double[] featurizeToDouble(T object) {
        double[] values = new double[features.size()];
        for (int i = 0; i < features.size(); i++) {
            values[i] = features.get(i).apply(object);
        }
        return values;
    }

    /**
     * Returns the features as an ImmutableList, sorted using the default String
     * comparator on the features' names.
     *
     * @return
     */
    public List<Function<T, Double>> getFeatures() {
        return features;
    }

    @Override
    public double featurize(String featureName, T object) {
        return this.features.get(this.featureNames.indexOf(featureName)).apply(
                object);
    }
}
