package edu.washington.cs.knowitall.index;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;
import edu.washington.cs.knowitall.sequence.SequenceException;
import edu.washington.cs.knowitall.util.Morpha;

/***
 * A class that can be used to normalize verbal relation strings. It performs
 * the following normalization procedure on a {@link ChunkedExtraction} object:
 * <ul>
 * <li>Removes inflection in each token using the {@link Morpha} class.</li>
 * <li>Removes auxiliary verbs, determiners, adjectives, and adverbs.</li>
 * </ul>
 * 
 * @author afader
 *
 */
public class VerbalRelationNormalizer implements FieldNormalizer {
	
	private Morpha lexer;
	private HashSet<String> ignorePosTags;
	private HashSet<String> auxVerbs;
	
	/**
	 * Constructs a new instance.
	 */
	public VerbalRelationNormalizer() {
		
		lexer = new Morpha(new ByteArrayInputStream("".getBytes()));
		
		ignorePosTags = new HashSet<String>();
		ignorePosTags.add("MD"); // can, must, should
		ignorePosTags.add("DT"); // the, an, these
		ignorePosTags.add("PDT"); // predeterminers 
		ignorePosTags.add("WDT"); // wh-determiners
		ignorePosTags.add("JJ"); // adjectives
		ignorePosTags.add("RB"); // adverbs
		ignorePosTags.add("PRP$"); // my, your, our
		
		auxVerbs = new HashSet<String>();
		auxVerbs.add("be");
		auxVerbs.add("have");
		auxVerbs.add("do");
	}
	
	/**
	 * Normalizes the given field.
	 */
	public NormalizedField normalizeField(ChunkedExtraction field) {
		
		List<String> tokens = field.getTokens();
		List<String> posTags = field.getPosTags();

		ArrayList<String> tokensCopy = new ArrayList<String>(tokens.size());
		tokensCopy.addAll(tokens);
		ArrayList<String> posTagsCopy = new ArrayList<String>(posTags.size());
		posTagsCopy.addAll(posTags);

		normalizeModify(tokensCopy, posTagsCopy);
		
		try {
			return new NormalizedField(field, tokensCopy, posTagsCopy);
		} catch (SequenceException e) {
			String msg = String.format(
				"tokens and posTags are not the same length for field %s", 
				field);
			throw new IllegalStateException(msg, e);
		}
	}
	
	private void normalizeModify(List<String> tokens, List<String> posTags) {
		stemAll(tokens, posTags);
		removeIgnoredPosTags(tokens, posTags);
		removeLeadingBeHave(tokens, posTags);
	}
	
	private String stem(String token, String posTag) {
		token = token.toLowerCase();
		String wordTag = token + "_" + posTag;
		try {
	        lexer.yyreset(new StringReader(wordTag));
	        lexer.yybegin(Morpha.scan);
	        String tokenNorm = lexer.next();
	        return tokenNorm;
	    } catch (Throwable e) {
	        return token;
	    }
	}
	
	private void stemAll(List<String> tokens, List<String> posTags) {
		for (int i = 0; i < tokens.size(); i++) {
			String tok = tokens.get(i);
			String tag = posTags.get(i);
			String newTok = stem(tok, tag);
			tokens.set(i, newTok);
		}
	}
	
	private void removeIgnoredPosTags(List<String> tokens, List<String> posTags) {
		int i = 0;
		while (i < posTags.size()) {
			if (ignorePosTags.contains(posTags.get(i))) {
				tokens.remove(i);
				posTags.remove(i);
			} else {
				i++;
			}
		}
	}
	
	private void removeLeadingBeHave(List<String> tokens, List<String> posTags) {
		int lastVerbIndex = -1;
		int n = tokens.size();
		for (int i = 0; i < n; i++) {
			String tag = posTags.get(n-i-1);
			if (tag.startsWith("V")) {
				lastVerbIndex = n-i-1;
				break;
			}
		}
		if (lastVerbIndex < 0) return;
		int i = 0;
		while (i < lastVerbIndex) {
			String tok = tokens.get(i);
			if (i+1 < posTags.size() && !posTags.get(i+1).startsWith("V")) break;
			if (auxVerbs.contains(tok)) {
				tokens.remove(i);
				posTags.remove(i);
				lastVerbIndex--;
			} else {
				i++;
			}
		}
	}
	


}