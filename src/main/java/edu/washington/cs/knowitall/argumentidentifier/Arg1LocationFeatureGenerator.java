package edu.washington.cs.knowitall.argumentidentifier;

import java.util.List;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedArgumentExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;

/**
 * Generates features for Arg1LocationClassifier
 * @author janara
 *
 */
public class Arg1LocationFeatureGenerator {
	PatternExtractor pattern_extractor;

	public Arg1LocationFeatureGenerator(){
		this.pattern_extractor = new PatternExtractor();
	}
	
	public String getHeader(){
		String header = "@RELATION np_head\n"+
						"@ATTRIBUTE simple_subj   {true,false}\n"+
						"@ATTRIBUTE quotes_subj   {true,false}\n"+
						"@ATTRIBUTE relative_subj   {true,false}\n"+
						"@ATTRIBUTE verb_conj   {true,false}\n"+
						"@ATTRIBUTE app   {true,false}\n"+
						"@ATTRIBUTE which_who   {true,false}\n"+
						"@ATTRIBUTE cap   {true,false}\n"+
						"@ATTRIBUTE punt_count  NUMERIC\n"+
						"@ATTRIBUTE intervening_np_count  NUMERIC\n"+
						"@ATTRIBUTE np_count_before  NUMERIC\n"+
						"@ATTRIBUTE word_before_pred_conj   {true,false}\n"+
						"@ATTRIBUTE intervening_and  {true,false}\n"+
						"@ATTRIBUTE word_after_vp  {true,false}\n"+
						"@ATTRIBUTE word_before_vp  {true,false}\n"+
						"@ATTRIBUTE class        {closest_np,not_closest_np}\n"+
						"@DATA\n";
		return header;

    }

	public String extractFeatures(ChunkedExtraction extr, ChunkedArgumentExtraction arg1, int current, boolean train){
		String features = "";
		
    	boolean not_seen_yet = true;
    	List<String> words = extr.getSentence().getTokens();
    	List<String> chunks = extr.getSentence().getChunkTags();
    	
    	int pred_start = extr.getRange().getStart();
    	
    	int arg1_start = 0;
    	int arg1_end = 0;
    	
    	if(train){
    		arg1_start = arg1.getRange().getStart();
    		arg1_end = arg1.getRange().getEnd();
    	}
    	int end = current -1;
		if(current == -1){
			current = pred_start-1;
			end = -1;
		}
    	for(int k = current; k > end; k--){
    		if(chunks.get(k).equals("B-NP")){

				boolean simple_subj = false;
				boolean quotes_subj = pattern_extractor.quotesSubj(extr,k);
				boolean relative_subj = false;
				boolean verb_conj = false;
				boolean app_clause = false;
				boolean which_who= false;
				boolean capitalized = false;
				boolean word_before_pred_conj = false; 
				boolean intervening_and = false;
				boolean word_after_vp = false;
				boolean word_before_vp = false;
				int np_count_before = pattern_extractor.getNPCountBefore(extr, k);
				int intervening_np = pattern_extractor.getInterveningNPCount(extr,k);
				int punctuation_count = pattern_extractor.getPunctuationCount(extr, k);

				simple_subj = pattern_extractor.simpleSubj(extr,k);
				relative_subj = pattern_extractor.relSubj(extr,k);
				verb_conj = pattern_extractor.matchesVerbConjSimple(extr, k);
				app_clause = pattern_extractor.matchesAppositiveClause(extr, current);
	
				which_who = (words.get(k).equals("which") || words.get(k).equals("who") || words.get(k).equals("that"));
				capitalized = pattern_extractor.getCapitalized(extr, k);
				punctuation_count = pattern_extractor.getPunctuationCount(extr, k);
				word_before_pred_conj = pattern_extractor.wordBeforePredIsConj(extr, k);
				intervening_and = pattern_extractor.getInterveningConj(extr, k);
				word_after_vp = pattern_extractor.wordAfterIsVP(extr, k);
				np_count_before = pattern_extractor.getNPCountBefore(extr, k);
				word_before_vp = pattern_extractor.wordBeforeIsVP(extr, k);
				intervening_np = pattern_extractor.getInterveningNPCount(extr,k);
				
				boolean correct_np = ((arg1_start<=k && arg1_end >k && not_seen_yet) || (not_seen_yet && k < arg1_start));
				String classification = "not_closest_np";
				
				if(correct_np){
					not_seen_yet = false;
					classification = "closest_np";
				}

				String delim = ",";
				features+=(simple_subj+delim+quotes_subj+delim+relative_subj+delim+verb_conj+delim+app_clause+delim+which_who+delim+capitalized+delim+punctuation_count+delim+(intervening_np)+delim+np_count_before+
						delim+word_before_pred_conj+delim+intervening_and +delim+word_after_vp+delim+word_before_vp+
						delim+classification+"\n");

    		}
    	}
    	return features;
	}
	
	/*
	private static String getNP(ChunkedExtraction extr, int current){
		String np = "";
		while(current < extr.getSentence().getLength() && extr.getSentence().getChunkTag(current).contains("NP")){
			np+=extr.getSentence().getToken(current)+" ";
			current++;
		}
		return np;
	}
	*/
	
}

