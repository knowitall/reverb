package edu.washington.cs.knowitall.argumentidentifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.StringReader;

import edu.washington.cs.knowitall.argumentidentifier.ArgLearner.Mode;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;
import edu.washington.cs.knowitall.commonlib.ResourceUtils;

import weka.classifiers.trees.REPTree;
import weka.core.Instances;

/**
 * ArgLocationClassifier uses weka to classify the right bound 
 * for Arg1 and heuristics (closest word) for Arg2
 * @author janara
 *
 */
public class ArgLocationClassifier {
    private final String arg1_closest_file = "arg1location-training.arff";
    
    private REPTree classifier = new REPTree();
	private Arg1LocationFeatureGenerator featuregenerator;

    private Mode mode;
	public ArgLocationClassifier(Mode mode){
		this.mode = mode;
		if(mode == ArgLearner.Mode.LEFT){
			this.featuregenerator = new Arg1LocationFeatureGenerator();
			setupClassifier(arg1_closest_file);
		}
	}
	
	public double[] getArgBound(ChunkedExtraction predicate){
		if(mode == ArgLearner.Mode.LEFT){
	    	double[] resultsclassifier = {-1,1};
	    	String header = featuregenerator.getHeader();
	    	int classification = -1;
	    	int k = predicate.getStart()-1;
	    	int rightbound = -1;
	    	
	    	//classify each np
	    	while(k>-1 && classification ==-1){
	    		if(predicate.getSentence().getChunkTag(k).equals("B-NP")){
	    			String features = featuregenerator.extractFeatures(predicate, null, k, false);
	    	    	resultsclassifier = classifyData(header+features);
	    	    	classification = (int) resultsclassifier[0];
	    	    	rightbound=k;
	    		}
	    		k--;
	    	}
	    	
	    	//adjust the np to be the righbound
	    	rightbound++;
	    	while(rightbound < predicate.getStart() && predicate.getSentence().getChunkTag(rightbound).equals("I-NP")){
	    		rightbound++;
	    	}
	    	resultsclassifier[0] = rightbound;
	    	return resultsclassifier;
		}
		else{
			int bound = predicate.getStart()+predicate.getLength();
			if((predicate.getStart()+predicate.getLength())>=predicate.getSentence().getLength() ||
					!(predicate.getSentence().getPosTag(predicate.getStart()+predicate.getLength()).startsWith("N") ||
					predicate.getSentence().getPosTag(predicate.getStart()+predicate.getLength()).startsWith("J") ||
					predicate.getSentence().getPosTag(predicate.getStart()+predicate.getLength()).startsWith("CD") ||
					predicate.getSentence().getPosTag(predicate.getStart()+predicate.getLength()).startsWith("PRP") ||
					predicate.getSentence().getPosTag(predicate.getStart()+predicate.getLength()).startsWith("WRB") ||
					predicate.getSentence().getPosTag(predicate.getStart()+predicate.getLength()).startsWith("IN") ||
					predicate.getSentence().getPosTag(predicate.getStart()+predicate.getLength()).startsWith("DT"))){
				bound = -1;
			}
	    	double[] results = {bound,1};
			return results;
		}
    }
	
    private double[] classifyData(String testingdata){
		StringReader testreader = new StringReader(testingdata);
		int firstposPredicted = -1;
		double confidence = -1;
		try {
			Instances testinginstances = new Instances(testreader);
			testinginstances.setClassIndex(testinginstances.numAttributes() - 1);
			testreader.close();

			double clsLabelPredicted;
			for (int i = 0; i < testinginstances.numInstances(); i++) {
				clsLabelPredicted = classifier.classifyInstance(testinginstances.instance(i));
				if(clsLabelPredicted == 0){
					firstposPredicted = i;
					break;
				}
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}		
		double[] toreturn = {firstposPredicted,confidence};
		return toreturn;
    }
	
	private void setupClassifier(String trainingdata){
    	Reader trainreader = null;
        try {
            trainreader = new InputStreamReader(ResourceUtils.loadResource(trainingdata, this.getClass()));
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
		try {
			Instances traininginstances = new Instances(trainreader);
			traininginstances.setClassIndex(traininginstances.numAttributes() - 1);
			trainreader.close();
			
			classifier.buildClassifier(traininginstances);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
}
