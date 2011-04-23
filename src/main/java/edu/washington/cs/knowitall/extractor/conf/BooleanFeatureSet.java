package edu.washington.cs.knowitall.extractor.conf;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

/***
 * Represents a set of boolean feature functions that can be applied to objects of type <code>T</code>.
 * Each feature is represented as a <code>Predicate<T></code> object, and has a String name. 
 * @author afader
 *
 * @param <T>
 */
public class BooleanFeatureSet<T> {
	
	private int numFeatures;
	private ImmutableList<String> featureNames;
	private ImmutableList<Predicate<T>> features;
	
	/**
	 * Constructs a new feature set from the given features. 
	 * @param givenFeatures
	 */
	public BooleanFeatureSet(Map<String, Predicate<T>> givenFeatures) {
		TreeMap<String, Predicate<T>> sortedFeatures = new TreeMap<String, Predicate<T>>();
		for (String featureName : givenFeatures.keySet()) {
			Predicate<T> feature = givenFeatures.get(featureName);
			sortedFeatures.put(featureName, feature);
		}
		featureNames = ImmutableList.copyOf(sortedFeatures.keySet());
		features = ImmutableList.copyOf(sortedFeatures.values());
		numFeatures = features.size();
	}
	
	/**
	 * Returns a feature representation of the given object. 
	 * @param object
	 * @return a sorted map, mapping feature names to boolean values
	 */
	public SortedMap<String, Boolean> featurize(T object) {
		TreeMap<String, Boolean> values = new TreeMap<String, Boolean>();
		for (int i = 0; i < numFeatures; i++) {
			String featureName = featureNames.get(i);
			boolean featureValue = features.get(i).apply(object);
			values.put(featureName, featureValue);
		}
		return values;
	}
	
	/**
	 * Returns the feature representation of object as an array of doubles. Each
	 * value in the array corresponds to a feature (where 1.0 is true and
	 * 0.0 is false). The ith value corresponds to the ith feature.
	 * @param object
	 * @return
	 */
	public double[] featurizeToDouble(T object) {
		double[] values = new double[features.size()];
		for (int i = 0; i < features.size(); i++) {
			if (features.get(i).apply(object)) {
				values[i] = 1.0;
			} else {
				values[i] = 0.0;
			}
		}
		return values;
	}
	
	/**
	 * Returns the feature representation of object as an array of booleans.
	 * The ith value corresponds to the ith feature.
	 * @param object
	 * @return
	 */
	public boolean[] featurizeToBool(T object) {
		boolean[] values = new boolean[features.size()];
		for (int i = 0; i < features.size(); i++) {
			values[i] = features.get(i).apply(object);
		}
		return values;
	}
	
	/**
	 * Returns the feature names as an ImmutableList, sorted using the default String
	 * comparator.
	 * @return
	 */
	public List<String> getFeatureNames() {
		return featureNames;
	}
	
	/**
	 * Returns the features as an ImmutableList, sorted using the default String
	 * comparator on the features' names.
	 * @return
	 */
	public List<Predicate<T>> getFeatures() {
		return features;
	}
	
	/**
	 * @return the number of features in this feature set
	 */
	public int getNumFeatures() {
		return numFeatures;
	}

}
