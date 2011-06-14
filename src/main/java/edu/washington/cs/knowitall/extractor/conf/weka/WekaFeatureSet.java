package edu.washington.cs.knowitall.extractor.conf.weka;

import java.util.List;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import edu.washington.cs.knowitall.extractor.conf.featureset.FeatureSet;

/***
 * Used for converting an object of type <code>T</code> into a Weka
 * <code>Instance</code> object.
 * @author afader
 *
 * @param <T>
 */
public class WekaFeatureSet<T> {
	
	private FeatureSet<T> featureSet;
	private int numFeatures;
	
	private Instances instances;
	private FastVector attributes;
	private final String INST_NAME = "test";
	
	
	/**
	 * Constructs a new Weka feature set from the given feature set.
	 * @param featureSet
	 */
	public WekaFeatureSet(FeatureSet<T> featureSet) {
		this.featureSet = featureSet;
		initializeWekaObjects();
	}
	
	private void initializeWekaObjects() {
		numFeatures = featureSet.getNumFeatures();
		List<String> featureNames = featureSet.getFeatureNames();
		attributes = new FastVector(numFeatures + 1); // +1 for class attribute
		// Construct a numeric attribute for each feature in the set
		for (int i = 0; i < numFeatures; i++) {
			Attribute featureAttr = new Attribute(featureNames.get(i));
			attributes.addElement(featureAttr);
		}
		FastVector classVals = new FastVector(2);
		classVals.addElement("positive");
		classVals.addElement("negative");
		Attribute classAttr = new Attribute("class", classVals);
		attributes.addElement(classAttr);
		instances = new Instances(INST_NAME, attributes, 0);
		instances.setClassIndex(numFeatures);
	}
	
	/***
	 * @return a copy of the Weka attributes for this feature representation.
	 */
	public FastVector getAttributesCopy() {
		FastVector attributesCopy = new FastVector(attributes.size());
		for (int i = 0; i < numFeatures + 1; i++) {
			attributesCopy.addElement((Attribute)attributes.elementAt(i));
		}
		return attributesCopy;
	}
	
	/**
	 * Returns the feature representation of the given object as 
	 * a Weka <code>Instance</code> object. Does not add a class value attribute
	 * to the instance object. 
	 * @param object
	 * @return
	 */
	public Instance getInstanceFrom(T object) {
		Instance inst = new Instance(numFeatures);
		double[] featureVals = featureSet.featurizeToDouble(object);
		for (int i = 0; i < numFeatures; i++) {
			inst.setValue((Attribute)attributes.elementAt(i), featureVals[i]);
		}
		inst.setDataset(instances);
		return inst;
	}
	
	/**
	 * Returns the feature representation of the given object as
	 * a Weka <code>Instance</code> object. Sets the class attribute to the given
	 * label.
	 * @param object
	 * @param label
	 * @return
	 */
	public Instance getInstanceFrom(T object, int label) {
		Instance inst = new Instance(numFeatures + 1);
		double[] featureVals = featureSet.featurizeToDouble(object);
		for (int i = 0; i < numFeatures; i++) {
			inst.setValue((Attribute)attributes.elementAt(i), featureVals[i]);
		}
		inst.setValue((Attribute)attributes.elementAt(numFeatures), 1-label);
		inst.setDataset(instances);
		return inst;
	}
}
