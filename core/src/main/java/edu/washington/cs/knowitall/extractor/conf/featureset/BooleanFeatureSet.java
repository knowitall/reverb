package edu.washington.cs.knowitall.extractor.conf.featureset;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/***
 * Represents a set of boolean feature functions that can be applied to objects
 * of type <code>T</code>. Each feature is represented as a
 * <code>Predicate<T></code> object, and has a String name.
 *
 * @author afader
 *
 * @param <T>
 */
public class BooleanFeatureSet<T> extends FeatureSet<T> {

    private Map<String, Predicate<T>> features;

    /**
     * Constructs a new feature set from the given features.
     *
     * @param givenFeatures
     */
    public BooleanFeatureSet(Map<String, Predicate<T>> givenFeatures) {
        super(ImmutableList.copyOf(givenFeatures.keySet()));

        features = ImmutableMap.copyOf(givenFeatures);
    }

    /**
     * Returns a feature representation of the given object.
     *
     * @param object
     * @return a sorted map, mapping feature names to boolean values
     */
    public SortedMap<String, Boolean> featurize(T object) {
        TreeMap<String, Boolean> values = new TreeMap<String, Boolean>();
        for (int i = 0; i < this.getNumFeatures(); i++) {
            String featureName = featureNames.get(i);
            boolean featureValue = features.get(featureName).apply(object);
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
        int i = 0;
        for (String featureName : this.featureNames) {
            if (features.get(featureName).apply(object)) {
                values[i] = 1.0;
            } else {
                values[i] = 0.0;
            }

            i++;
        }
        return values;
    }

    /**
     * Returns the feature representation of object as an array of booleans. The
     * ith value corresponds to the ith feature.
     *
     * @param object
     * @return
     */
    public boolean[] featurizeToBool(T object) {
        boolean[] values = new boolean[features.size()];
        int i = 0;
        for (String featureName : this.featureNames) {
            values[i] = features.get(featureName).apply(object);
            i++;
        }
        return values;
    }

    /**
     * Returns the features as an ImmutableList, sorted using the default String
     * comparator on the features' names.
     *
     * @return
     */
    public Map<String, Predicate<T>> getFeatures() {
        return this.features;
    }

    @Override
    public double featurize(String featureName, T object) {
        return this.features.get(featureName).apply(object) == true ? 1.0 : 0.0;
    }
    
    public boolean featurizeToBool(String featureName, T object) {
        return this.features.get(featureName).apply(object);
    }
}
