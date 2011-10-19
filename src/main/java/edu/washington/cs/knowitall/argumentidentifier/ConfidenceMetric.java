package edu.washington.cs.knowitall.argumentidentifier;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import weka.classifiers.functions.Logistic;
import weka.core.Instances;
import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.commonlib.ResourceUtils;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedArgumentExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;

/***
 * An extraction confidence function that uses a logistic regression
 * classifier. It assigns an extraction a real valued number between
 * 0 and 1 according to the logistic regression model.
 * 
 * @author janara
 *
 */

public class ConfidenceMetric {
	private static String TRAINING_DATA = "confidence-training.arff";
	
    private Logistic classifier = new Logistic();
	private PatternExtractor pattern_extractor;
	private String header;
	
	public ConfidenceMetric(){
		this.pattern_extractor = new PatternExtractor();
        Reader trainreader;
        try {
            trainreader = new InputStreamReader(ResourceUtils.loadResource(TRAINING_DATA, this.getClass()));
        }
    	catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return;
    	}
    	try {
			Instances traininginstances = new Instances(trainreader);
			trainreader.close();
			traininginstances.setClassIndex(traininginstances.numAttributes() - 1);
			classifier.buildClassifier(traininginstances);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		header = getHeader();
		
	}
	public double getConfidence(ChunkedBinaryExtraction extr){
		String features = getAllMetrics(extr);
		StringReader testreader = new StringReader(header+features+",true\n");
		Instances testinginstances = null;
		try {
			testinginstances = new Instances(testreader);
		} catch (IOException e) {
			e.printStackTrace();
		}
		testinginstances.setClassIndex(testinginstances.numAttributes() - 1);
		testreader.close();
		
		try {
			classifier.classifyInstance(testinginstances.firstInstance());
			return classifier.distributionForInstance(testinginstances.firstInstance())[0];
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return .5;
	}
	
	public String getHeader(){
		String header = "";
			header += "@RELATION conf\n";
			header += "@ATTRIBUTE correct_end {true,false}\n";
			header += "@ATTRIBUTE pred_starts_w_np {true,false}\n";
			header += "@ATTRIBUTE to_before_pred {true,false}\n";
			
			header += "@ATTRIBUTE conj_before_rel {true,false}\n";
			header += "@ATTRIBUTE which_before_rel {true,false}\n";
			header += "@ATTRIBUTE rel_one_verb {true,false}\n";
			header += "@ATTRIBUTE rel_to {true,false}\n";
			header += "@ATTRIBUTE rel_for {true,false}\n";
			header += "@ATTRIBUTE rel_in {true,false}\n";
			header += "@ATTRIBUTE rel_of {true,false}\n";
			header += "@ATTRIBUTE rel_on {true,false}\n";
			
			header += "@ATTRIBUTE pp_before_arg1 {true,false}\n";
			header += "@ATTRIBUTE words_till_start NUMERIC\n";
			header += "@ATTRIBUTE arg1_conf NUMERIC\n";
			header += "@ATTRIBUTE arg1_proper {true,false}\n";
			header += "@ATTRIBUTE np_before_arg1 {true,false}\n";
			header += "@ATTRIBUTE arg1_length NUMERIC\n";
			
			header += "@ATTRIBUTE adj {true,false}\n";
			header += "@ATTRIBUTE comp {true,false}\n";
			header += "@ATTRIBUTE nest1 {true,false}\n";
			header += "@ATTRIBUTE nest2 {true,false}\n";
			header += "@ATTRIBUTE rel  {true,false}\n";
			header += "@ATTRIBUTE npinf {true,false}\n";
			header += "@ATTRIBUTE doublenp {true,false}\n";
			
			header += "@ATTRIBUTE arg2_proper {true,false}\n";
			header += "@ATTRIBUTE verb_after_arg2 {true,false}\n";
			header += "@ATTRIBUTE np_after_arg2 {true,false}\n";
			header += "@ATTRIBUTE pp_after_arg2 {true,false}\n";
			header += "@ATTRIBUTE words_till_end NUMERIC\n";
			header += "@ATTRIBUTE arg2_conf NUMERIC\n";

			header += "@ATTRIBUTE sent_less_than_10 {true,false}\n";
			header += "@ATTRIBUTE sent_less_than_20 {true,false}\n";
			header += "@ATTRIBUTE sent_more_than_20 {true,false}\n";
			header += "@ATTRIBUTE extr_covers_phrase {true,false}\n";			
			
			header += "@ATTRIBUTE class {true,false}\n@DATA\n";
			return header;
	}

	public String getAllMetrics(ChunkedBinaryExtraction extr){
		
		String toprint = "";

		//relation metrics
		boolean pred_starts_w_np = predStartsWithNP(extr);
		boolean to_before_pred = toBeforePred(extr);
		boolean conj_before_rel = conjBeforeRel(extr);
		boolean which_before_rel = whichBeforeRel(extr);
		boolean rel_one_verb = relOneVerb(extr);
		boolean rel_to = relEndsWithToken(extr, "to");
		boolean rel_for = relEndsWithToken(extr, "for");
		boolean rel_in = relEndsWithToken(extr, "in");
		boolean rel_of = relEndsWithToken(extr, "of");
		boolean rel_on = relEndsWithToken(extr, "on");

		//arg1 metrics
		boolean correct_end = correctArg1End(extr);
		boolean pp_before_arg1 = ppBeforeArg1(extr);
		int words_till_start = wordsTillStart(extr);
		double arg1_conf = extr.getArgument1().getConfidence();
		boolean arg1_proper = arg1IsProper(extr);
		boolean np_before_arg1 = npBeforeArg1(extr);
		int arg1_length = arg1Length(extr);

		//arg2 metrics
		boolean adj = pattern_extractor.adjRelation(extr.getRelation());
		boolean comp = pattern_extractor.complementClause(extr.getRelation());
		boolean nest1 = pattern_extractor.nestedRelation1(extr.getRelation());
		boolean nest2 = pattern_extractor.nestedRelation2(extr.getRelation());
		boolean rel = pattern_extractor.npRelativeClause(extr.getRelation());
		boolean npinf = pattern_extractor.npInfinitiveClause(extr.getRelation());
		boolean doublenp = pattern_extractor.doubleNP(extr.getRelation());
		boolean arg2_proper = arg2IsProper(extr);
		boolean verb_after_arg2 = verbAfterArg2(extr);
		boolean np_after_arg2 = npAfterArg2(extr);
		boolean pp_after_arg2 = ppAfterArg2(extr);
		int words_till_end = wordsTillStart(extr);
		double arg2_conf = extr.getArgument2().getConfidence();

		//sentence metric
		boolean sent_less_than_10 = sentLength(extr,0,11);
		boolean sent_less_than_20 = sentLength(extr,11,21);
		boolean sent_more_than_20 = sentLength(extr,21,Integer.MAX_VALUE);
		boolean extr_covers_phrase = extrCoversPhrase(extr);

		toprint += correct_end+","+pred_starts_w_np+","+to_before_pred+",";
		toprint += conj_before_rel+","+which_before_rel+","+rel_one_verb+","+rel_to+","+rel_for+","+rel_in+","+rel_of+","+rel_on+",";
		toprint += pp_before_arg1+","+words_till_start+","+arg1_conf+","+arg1_proper+","+np_before_arg1+","+arg1_length+",";
		toprint += adj+","+comp+","+nest1+","+nest2+","+rel+","+npinf+","+doublenp+",";
		toprint += arg2_proper+","+verb_after_arg2+","+np_after_arg2+","+pp_after_arg2+","+words_till_end+","+arg2_conf+",";
		toprint += sent_less_than_10+","+sent_less_than_20+","+sent_more_than_20+","+extr_covers_phrase;

		return toprint;
	}
	
	
	public int getIntValue(boolean bool, boolean dir){
		if(bool == dir){
			return 1;
		}
		else {
			return 0;
		}
	}

	
	
	private boolean extrCoversPhrase(ChunkedBinaryExtraction e) {
		
		ChunkedSentence sent = e.getSentence();
		List<String> tokens = sent.getTokens();
		
		Range x = e.getArgument1().getRange();
		Range y = e.getArgument2().getRange();
		Range r = e.getRelation().getRange();
		boolean adj = x.isAdjacentTo(r) && r.isAdjacentTo(y);
		
		int xs = x.getStart();
		boolean leftOk = xs == 0 || tokens.get(xs-1).equals(",") || tokens.get(xs-1).equals(".");
			
		int l = sent.getLength() - 1;
		int yr = y.getLastIndex();
		boolean rightOk = yr == l || tokens.get(yr+1).equals(",") || tokens.get(yr+1).equals(".");
		
		return adj && leftOk && rightOk;
	}
	private boolean sentLength(ChunkedBinaryExtraction e, int lower, int upper) {
		final int a = lower;
		final int b = upper;
		ChunkedSentence sent = e.getSentence();
		int len = sent.getLength();
		return a <= len && len < b;
	}
	private boolean npAfterArg2(ChunkedBinaryExtraction e) {
		ChunkedArgumentExtraction arg2 = e.getArgument2();
		int lastArg2 = arg2.getRange().getLastIndex();
		ChunkedSentence sent = arg2.getSentence();
		return lastArg2 + 1 < sent.getLength() && 
			sent.getChunkTags().get(lastArg2+1).equals("B-NP");
	}
	private boolean verbAfterArg2(ChunkedBinaryExtraction e) {
		ChunkedArgumentExtraction arg2 = e.getArgument2();
        int pastArg2 = arg2.getStart() + arg2.getLength();
        if (pastArg2 < e.getSentence().getLength()) {
            String pastPosTag = e.getSentence().getPosTags().get(pastArg2);
            if (pastPosTag.equals("MD") || pastPosTag.startsWith("V")) {
                return true;
            }
        }
        return false;
	}
	private boolean npBeforeArg1(ChunkedBinaryExtraction e) {
		ChunkedExtraction arg1 = e.getArgument1();
		int start = arg1.getRange().getStart();
		if (start == 0) {
			return false;
		} else {
			ChunkedSentence sent = arg1.getSentence();
			return sent.getChunkTags().get(start-1).endsWith("-NP");
		}
		
	}
	private boolean arg1IsProper(ChunkedBinaryExtraction e) {
		return isProperNp(e.getArgument2());
	}
	private boolean arg2IsProper(ChunkedBinaryExtraction e) {
		return isProperNp(e.getArgument2());
	}
	private boolean isProperNp(ChunkedExtraction e) {
		for (String tag : e.getPosTags()) {
			if (!tag.startsWith("NNP") && !tag.equals("DT") && !tag.equals("IN")) {
				return false;
			}
		}
		return true;
	}
	private boolean relEndsWithToken(ChunkedBinaryExtraction e, String t) {
		final String token = t;
		List<String> tokens = e.getRelation().getTokens();
		return tokens.get(tokens.size()-1).equals(token);
	}
	private boolean relOneVerb(ChunkedBinaryExtraction e){
		ChunkedExtraction rel = e.getRelation();
		List<String> posTags = rel.getPosTags();
		return posTags.size() == 1 && posTags.get(0).startsWith("V");
	}
	private boolean whichBeforeRel(ChunkedBinaryExtraction e) {
		ChunkedExtraction pred = e.getRelation();
        int predStart = pred.getStart();
        if (predStart > 0) {
            String precPosTag = e.getSentence().getPosTags().get(predStart-1);
            String precPosToken = e.getSentence().getToken(predStart-1);
            if (precPosTag.equals("WP") || precPosTag.equals("WDT") || precPosToken.equals("that")) {
                return true;
            }
        } 
        return false;
	}
	private boolean conjBeforeRel(ChunkedBinaryExtraction e) {
		ChunkedExtraction pred = e.getRelation();
        int predStart = pred.getStart();
        if (predStart > 0) {
            String precPosTag = e.getSentence().getPosTags().get(predStart-1);
            if (precPosTag.equals("CC")) {
                return true;
            }
        } 
        return false;
	}
	
	public int wordsTillStart(ChunkedBinaryExtraction extr){
		if(extr.getArgument1()==null || extr.getArgument1().getLength()<1){
			return -1;
		}
		int words_till_start = extr.getArgument1().getStart();
		return words_till_start;
	}
	
	public int arg1Length(ChunkedBinaryExtraction extr){
		if(extr.getArgument1()==null || extr.getArgument1().getLength()<1){
			return -1;
		}
		return  extr.getArgument1().getLength();
	}
	
	public int wordsTillEnd(ChunkedBinaryExtraction extr){
		if(extr.getArgument2()==null || extr.getArgument2().getLength()<1){
			return -1;
		}
		int words_till_end = extr.getSentence().getLength()-(extr.getArgument2().getStart()+extr.getArgument2().getLength());
		return words_till_end;
	}
	
	public boolean ppAfterArg2(ChunkedBinaryExtraction extr){
		if(wordsTillEnd(extr)>0){
			int end = extr.getArgument2().getStart()+extr.getArgument2().getLength();
			if(extr.getSentence().getChunkTag(end).equals("B-PP")){
				return true;
			}
		}
		return false;
	}
	
	public boolean correctArg1End(ChunkedBinaryExtraction extr){
		int i = extr.getArgument1().getStart();
		int start = i;
		while(i < extr.getArgument1().getStart()+extr.getArgument1().getLength()){
			if(extr.getSentence().getChunkTag(i).equals("B-NP")){
				start = i;
			}
			i++;
		}
		return pattern_extractor.findSubj(extr.getRelation(),start);
	}
	
	public boolean ppBeforeArg1(ChunkedBinaryExtraction extr){
		if(wordsTillStart(extr)>0){
			int start = extr.getArgument1().getStart();
			if(extr.getSentence().getChunkTag(start-1).equals("B-PP")){
				return true;
			}
		}
		return false;
	}
	
	public boolean npBeforeArg2(ChunkedBinaryExtraction extr, boolean train){
		for(int i = extr.getRelation().getStart()+extr.getRelation().getLength(); i < extr.getArgument2().getStart(); i++){
			if(extr.getSentence().getChunkTag(i).contains("NP")){
				return true;
			}
		}
    	return false;
	}
	
	public boolean predStartsWithNP(ChunkedBinaryExtraction extr){
		//check that the relation is in a vp
		if(extr.getSentence().getPosTag(extr.getRelation().getStart()).contains("N")){
			return true;
		}
		return false;
	}
	
	public boolean toInPred(ChunkedBinaryExtraction extr, boolean train){
		//check for to in current pred
		if(train){
			for(int i = extr.getRelation().getStart(); i < extr.getArgument2().getStart(); i++){
				if(extr.getSentence().getChunkTag(i).equals("B-VP") && extr.getSentence().getPosTag(i).equals("TO")){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean toBeforePred(ChunkedBinaryExtraction extr){
		//find appropriate vp;
		boolean in_conj = false;
		int i = extr.getRelation().getStart()-1;
		while(i > -1){
			if(extr.getSentence().getPosTag(i).equals("CC") || extr.getSentence().getPosTag(i).equals(",")){
				in_conj = true;
				break;
			}
			if(extr.getSentence().getChunkTag(i).contains("NP")){
				break;
			}
			i--;
		}
		int last_vp = extr.getRelation().getStart();
		if(in_conj){
			boolean seen_vp = false;
			while(i > -1){
				if(extr.getSentence().getPosTag(i).equals("CC") || extr.getSentence().getPosTag(i).equals(",")){
					seen_vp = false;
				}
				else if(extr.getSentence().getChunkTag(i).equals("B-VP")){
					seen_vp = true;
					last_vp = i;
				}
				else if((extr.getSentence().getChunkTag(i).equals("B-NP") || extr.getSentence().getChunkTag(i).equals("I-NP")) && seen_vp){
					break;
				}
				if(extr.getSentence().getChunkTag(i).equals("B-VP") && extr.getSentence().getPosTag(i).equals("TO")){
					return true;
				}
				i--;
			}
		}
		//check for a to
		i = last_vp;
		boolean foundnp = false;
		while(i > -1){
			if(extr.getSentence().getToken(i).equals("to")){
				return true;
			}
			if(extr.getSentence().getChunkTag(i).equals("B-NP") || extr.getSentence().getChunkTag(i).equals("I-NP")){
				foundnp = true;
				break;
			}
			i--;
		}
		
		if(foundnp){
			return false;
		}
		
		return false;
	}

}
