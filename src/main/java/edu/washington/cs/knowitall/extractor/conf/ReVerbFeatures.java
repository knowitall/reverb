package edu.washington.cs.knowitall.extractor.conf;

import java.util.HashMap;
import java.util.List;

import com.google.common.base.Predicate;

import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.extractor.ReVerbExtractor;
import edu.washington.cs.knowitall.extractor.conf.featureset.BooleanFeatureSet;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedArgumentExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;
import edu.washington.cs.knowitall.sequence.LayeredTokenMatcher;
import edu.washington.cs.knowitall.sequence.LayeredTokenPattern;
import edu.washington.cs.knowitall.sequence.SequenceException;

/***
 * This class defines the features used by the ReVerb confidence function. A reference
 * to the feature set can be obtained by calling the <code>getFeatureSet()</code> method
 * after constructing an instance of this class.
 * @author afader
 *
 */
public class ReVerbFeatures {
	
	private HashMap<String, Predicate<ChunkedBinaryExtraction>> featureMap;
	private BooleanFeatureSet<ChunkedBinaryExtraction> featureSet;
	
	public ReVerbFeatures() throws ConfidenceFunctionException {
		try {
			initFeatureSet();
		} catch (SequenceException e) {
			throw new ConfidenceFunctionException(
				"Unable to initialize features", e);
		}
	}
	
	/**
	 * @return the feature set used for the ReVerb confidence function
	 */
	public BooleanFeatureSet<ChunkedBinaryExtraction> getFeatureSet() {
		return featureSet;
	}
	
	private void initFeatureSet() throws SequenceException {
		initFeatureMap();
		featureSet = new BooleanFeatureSet<ChunkedBinaryExtraction>(featureMap);
	}
	
	private void initFeatureMap() throws SequenceException {
		featureMap = new HashMap<String, Predicate<ChunkedBinaryExtraction>>();
		featureMap.put("sent starts w/arg1", startArg1());
		featureMap.put("sent ends w/arg2", endArg2());
		featureMap.put("prep before arg1", prepBeforeArg1());
		featureMap.put("conj before rel", conjBeforeRel());
		featureMap.put("which|who|that before rel", relPronounBeforeRel());
		featureMap.put("verb after arg2", verbAfterArg2());	
		featureMap.put("np after arg2", npAfterArg2());
		featureMap.put("prep after arg2", prepAfterArg2());
		featureMap.put("0 < len(sent) <= 10", sentLength(0, 11));
		featureMap.put("10 < len(sent) <= 20", sentLength(11, 21));
		featureMap.put("20 < len(sent)", sentLength(11, Integer.MAX_VALUE));
		featureMap.put("arg2 is proper", arg2IsProper());
		featureMap.put("arg1 is proper", arg1IsProper());
		featureMap.put("arg2 contains pronoun", arg2Pronoun());
		featureMap.put("arg1 contains pronoun", arg1Pronoun());
		featureMap.put("arg2 contains pos pronoun", arg2PosPronoun());
		featureMap.put("arg1 contains pos pronoun", arg1PosPronoun());
		featureMap.put("rel is a single verb", relIsOneVerb());
		featureMap.put("rel is VW+P", relIsVWP());
		featureMap.put("rel ends with to", relEndsWithToken("to"));
		featureMap.put("rel ends with in", relEndsWithToken("in"));
		featureMap.put("rel ends with for", relEndsWithToken("for"));
		featureMap.put("rel ends with of", relEndsWithToken("of"));
		featureMap.put("rel ends with on", relEndsWithToken("on"));
		featureMap.put("rel contains vbz", relContainsPos("VBZ"));
		featureMap.put("rel contains vbg", relContainsPos("VBG"));
		featureMap.put("rel contains vbd", relContainsPos("VBD"));
		featureMap.put("rel contains vbn", relContainsPos("VBN"));
		featureMap.put("rel contains vbp", relContainsPos("VBP"));
		featureMap.put("rel contains vb", relContainsPos("VB"));
		featureMap.put("np before arg1", npBeforeArg1());
		featureMap.put("extr covers phrase", extrCoversPhrase());
	}
	
	/**
	 * Each of the private methods below defines a feature. 
	 */
	
	// Used for features related to the relation string
	private String VERB = ReVerbExtractor.VERB;
	private String WORD = ReVerbExtractor.WORD;
	private String PREP = ReVerbExtractor.PREP;
	
	private Predicate<ChunkedBinaryExtraction> startArg1() {
		return new Predicate<ChunkedBinaryExtraction>() {
			public boolean apply(ChunkedBinaryExtraction e) {
				return e.getArgument1().getRange().getStart() == 0;
			}
		};
	}
	
	private Predicate<ChunkedBinaryExtraction> endArg2() {
		return new Predicate<ChunkedBinaryExtraction>() {
			public boolean apply(ChunkedBinaryExtraction e) {
				int arg2End = e.getArgument2().getRange().getLastIndex(); 
				int sentEnd = e.getSentence().getLength() - 2;
				return arg2End == sentEnd;
			}
		};
	}
	
	private Predicate<ChunkedBinaryExtraction> prepBeforeArg1() {
		return new Predicate<ChunkedBinaryExtraction>() {
			public boolean apply(ChunkedBinaryExtraction e) {
				ChunkedArgumentExtraction arg1 = e.getArgument1();
		        int arg1Start = arg1.getStart();
		        if (arg1Start > 0) {
		            String precPosTag = e.getSentence().getPosTags().get(arg1Start-1);
		            if (precPosTag.equals("IN") || precPosTag.equals("TO")) {
		                return true;
		            }
		        } 
		        return false;
			}
		};
	}
	
	private Predicate<ChunkedBinaryExtraction> conjBeforeRel() {
		return new Predicate<ChunkedBinaryExtraction>() {
			public boolean apply(ChunkedBinaryExtraction e) {
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
		};
	}
	
	private Predicate<ChunkedBinaryExtraction> relPronounBeforeRel() {
		return new Predicate<ChunkedBinaryExtraction>() {
			public boolean apply(ChunkedBinaryExtraction e) {
				ChunkedExtraction pred = e.getRelation();
		        int predStart = pred.getStart();
		        if (predStart > 0) {
		            String precToken = e.getSentence().getTokens().get(predStart-1).toLowerCase();
		            if (precToken.equals("which") || precToken.equals("who") || precToken.equals("that")) {
		                return true;
		            }
		        } 
		        return false;
			}
		};
	}
	
	private Predicate<ChunkedBinaryExtraction> verbAfterArg2() {
		return new Predicate<ChunkedBinaryExtraction>() {
			public boolean apply(ChunkedBinaryExtraction e) {
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
		};
	}
	
	private Predicate<ChunkedBinaryExtraction> npAfterArg2() {
		return new Predicate<ChunkedBinaryExtraction>() {
			public boolean apply(ChunkedBinaryExtraction e) {
				ChunkedArgumentExtraction arg2 = e.getArgument2();
				int lastArg2 = arg2.getRange().getLastIndex();
				ChunkedSentence sent = arg2.getSentence();
				return lastArg2 + 1 < sent.getLength() && 
					sent.getChunkTags().get(lastArg2+1).equals("B-NP");
			}
		};
	}
	
	private Predicate<ChunkedBinaryExtraction> prepAfterArg2() {
		return new Predicate<ChunkedBinaryExtraction>() {
			public boolean apply(ChunkedBinaryExtraction e) {
				ChunkedArgumentExtraction arg2 = e.getArgument2();
				int lastArg2 = arg2.getRange().getLastIndex();
				ChunkedSentence sent = arg2.getSentence();
				if (lastArg2+1 >= sent.getLength()) return false;
				String tag = sent.getPosTag(lastArg2+1);
				return tag.equals("TO") || tag.equals("IN");
			}
		};
	}

	private Predicate<ChunkedBinaryExtraction> sentLength(int lower, int upper) {
		final int a = lower;
		final int b = upper;
		return new Predicate<ChunkedBinaryExtraction>() {
			public boolean apply(ChunkedBinaryExtraction e) {
				ChunkedSentence sent = e.getSentence();
				int len = sent.getLength();
				return a <= len && len < b;
			}
		};
	}
	
	private static boolean isProperNp(ChunkedExtraction e) {
		for (String tag : e.getPosTags()) {
			if (tag.equalsIgnoreCase("NNP") || tag.equalsIgnoreCase("NNPS")) {
				return true;
			}
			/*
			if (!tag.startsWith("NNP") && !tag.equals("DT") && !tag.equals("IN")) {
				return false;
			}
			*/
		}
		return false;
	}
	
	private static boolean containsPronoun(ChunkedExtraction e) {
		for (String tag : e.getPosTags()) {
			if (tag.equals("PRP")) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean containsPosPronoun(ChunkedExtraction e) {
		for (String tag : e.getPosTags()) {
			if (tag.equals("PRP$")) {
				return true;
			}
		}
		return false;
	}
	
	private Predicate<ChunkedBinaryExtraction> arg1IsProper() {
		return new Predicate<ChunkedBinaryExtraction>() {
			public boolean apply(ChunkedBinaryExtraction e) {
				return isProperNp(e.getArgument1());
			}
		};
	}
	
	private Predicate<ChunkedBinaryExtraction> arg2IsProper() {
		return new Predicate<ChunkedBinaryExtraction>() {
			public boolean apply(ChunkedBinaryExtraction e) {
				return isProperNp(e.getArgument2());
			}
		};
	}
	
	private Predicate<ChunkedBinaryExtraction> arg1Pronoun() {
		return new Predicate<ChunkedBinaryExtraction>() {
			public boolean apply(ChunkedBinaryExtraction e) {
				return containsPronoun(e.getArgument1());
			}
		};
	}
	
	private Predicate<ChunkedBinaryExtraction> arg2Pronoun() {
		return new Predicate<ChunkedBinaryExtraction>() {
			public boolean apply(ChunkedBinaryExtraction e) {
				return containsPronoun(e.getArgument2());
			}
		};
	}
	
	private Predicate<ChunkedBinaryExtraction> arg1PosPronoun() {
		return new Predicate<ChunkedBinaryExtraction>() {
			public boolean apply(ChunkedBinaryExtraction e) {
				return containsPosPronoun(e.getArgument1());
			}
		};
	}
	
	private Predicate<ChunkedBinaryExtraction> arg2PosPronoun() {
		return new Predicate<ChunkedBinaryExtraction>() {
			public boolean apply(ChunkedBinaryExtraction e) {
				return containsPosPronoun(e.getArgument2());
			}
		};
	}
	
	private Predicate<ChunkedBinaryExtraction> relIsOneVerb() {
		return new Predicate<ChunkedBinaryExtraction>() {
			public boolean apply(ChunkedBinaryExtraction e) {
				ChunkedExtraction rel = e.getRelation();
				List<String> posTags = rel.getPosTags();
				return posTags.size() == 1 && posTags.get(0).startsWith("V");
			}
		};
	}
	
	private Predicate<ChunkedBinaryExtraction> relIsVWP() throws SequenceException {
		final String patternStr = String.format("(%s (%s+ (%s)+)?)+", VERB, WORD, PREP);
		final LayeredTokenPattern pattern = new LayeredTokenPattern(patternStr);
		return new Predicate<ChunkedBinaryExtraction>() {
			public boolean apply(ChunkedBinaryExtraction e) {
				try {
					LayeredTokenMatcher m = pattern.matcher(e.getRelation());
					int n = 0;
					while (m.find()) n++;
					return n == 1;
				} catch (SequenceException ex) {
					throw new IllegalStateException(ex);
				}
			}
		};
	}
	
	private Predicate<ChunkedBinaryExtraction> relEndsWithToken(String t) {
		final String token = t;
		return new Predicate<ChunkedBinaryExtraction>() {
			public boolean apply(ChunkedBinaryExtraction e) {
				List<String> tokens = e.getRelation().getTokens();
				return tokens.get(tokens.size()-1).equals(token);
			}
		};
	}
	
	private Predicate<ChunkedBinaryExtraction> relContainsPos(final String pos) {
		return new Predicate<ChunkedBinaryExtraction>() {
			public boolean apply(ChunkedBinaryExtraction e) {
				if (e.getRelation().getPosTags().contains(pos)) {
					return true;
				}
				else {
					return false;
				}
			}
		};
	}
	
	private Predicate<ChunkedBinaryExtraction> npBeforeArg1() {
		return new Predicate<ChunkedBinaryExtraction>() {
			public boolean apply(ChunkedBinaryExtraction e) {
				ChunkedExtraction arg1 = e.getArgument1();
				int start = arg1.getRange().getStart();
				if (start == 0) {
					return false;
				} else {
					ChunkedSentence sent = arg1.getSentence();
					return sent.getChunkTags().get(start-1).endsWith("-NP");
				}
				
			}
		};
	}
	
	/**
	 * A feature that returns true when the following are all true:
	 * - there are no tokens between arg1 and rel, and rel and arg2.
	 * - the token to the left of arg1 is a comma or the sentence start
	 * - the token to the rigth of arg2 is a period, comma, or sentence end
	 * @return the feature
	 */
	private Predicate<ChunkedBinaryExtraction> extrCoversPhrase() {
		return new Predicate<ChunkedBinaryExtraction>() {
			public boolean apply(ChunkedBinaryExtraction e) {
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
		};
	}

}
