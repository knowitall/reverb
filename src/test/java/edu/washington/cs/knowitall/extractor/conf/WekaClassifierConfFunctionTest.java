package edu.washington.cs.knowitall.extractor.conf;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.washington.cs.knowitall.extractor.conf.featureset.BooleanFeatureSet;
import edu.washington.cs.knowitall.extractor.conf.weka.WekaClassifierConfFunction;

import weka.classifiers.functions.Logistic;

public class WekaClassifierConfFunctionTest {

	@Test
	public void testGetConf() throws Exception {
		Logistic classifier = new Logistic();
		classifier.buildClassifier(StringFeatures.dataSet.getWekaInstances());
		BooleanFeatureSet<String> features = StringFeatures.featureSet;
		WekaClassifierConfFunction<String> func = new WekaClassifierConfFunction<String>(features, classifier);
		double janeConf = func.getConf("jane");
		double ofConf = func.getConf("of");
		assertTrue(ofConf < janeConf);
		assertTrue(ofConf < 1.0);
		assertTrue(0.0 < janeConf);
	}

}
