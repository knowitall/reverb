package edu.washington.cs.knowitall.argumentidentifier;

import java.util.List;

import edu.washington.cs.knowitall.extractor.conf.classifier.DoubleFeatures;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedArgumentExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;

/**
 * Generates features for Arg1LocationClassifier
 *
 * @author janara
 *
 */
public class Arg1LocationFeatureGenerator {
    PatternExtractor patternExtractor;

    public Arg1LocationFeatureGenerator() {
        this.patternExtractor = new PatternExtractor();
    }

    public String getHeader() {
        String header = "@RELATION np_head\n"
                + "@ATTRIBUTE simple_subj   {true,false}\n"
                + "@ATTRIBUTE quotes_subj   {true,false}\n"
                + "@ATTRIBUTE relative_subj   {true,false}\n"
                + "@ATTRIBUTE verb_conj   {true,false}\n"
                + "@ATTRIBUTE app   {true,false}\n"
                + "@ATTRIBUTE which_who   {true,false}\n"
                + "@ATTRIBUTE cap   {true,false}\n"
                + "@ATTRIBUTE punt_count  NUMERIC\n"
                + "@ATTRIBUTE intervening_np_count  NUMERIC\n"
                + "@ATTRIBUTE np_count_before  NUMERIC\n"
                + "@ATTRIBUTE word_before_pred_conj   {true,false}\n"
                + "@ATTRIBUTE intervening_and  {true,false}\n"
                + "@ATTRIBUTE word_after_vp  {true,false}\n"
                + "@ATTRIBUTE word_before_vp  {true,false}\n"
                + "@ATTRIBUTE class        {closest_np,not_closest_np}\n"
                + "@DATA\n";
        return header;

    }

    public double toDouble(boolean bool) {
        if (bool) return 1.0;
        else return 0.0;
    }

    public double toDouble(int num) {
        return num + 0.0;
    }

    public double toDouble(double num) {
        return num;
    }

    public DoubleFeatures extractFeatures(ChunkedExtraction extr,
            ChunkedArgumentExtraction arg1, int current, boolean train) {
        List<String> words = extr.getSentence().getTokens();
        List<String> chunks = extr.getSentence().getChunkTags();

        int pred_start = extr.getRange().getStart();

        int end = current - 1;
        if (current == -1) {
            current = pred_start - 1;
            end = -1;
        }
        for (int k = current; k > end; k--) {
            if (chunks.get(k).equals("B-NP")) {

                boolean simple_subj = false;
                boolean quotes_subj = patternExtractor.quotesSubj(extr, k);
                boolean relative_subj = false;
                boolean verb_conj = false;
                boolean app_clause = false;
                boolean which_who = false;
                boolean capitalized = false;
                boolean word_before_pred_conj = false;
                boolean intervening_and = false;
                boolean word_after_vp = false;
                boolean word_before_vp = false;
                int np_count_before = patternExtractor.getNPCountBefore(extr,
                        k);
                int intervening_np = patternExtractor.getInterveningNPCount(
                        extr, k);
                int punctuation_count = patternExtractor.getPunctuationCount(
                        extr, k);

                simple_subj = patternExtractor.simpleSubj(extr, k);
                relative_subj = patternExtractor.relSubj(extr, k);
                verb_conj = patternExtractor.matchesVerbConjSimple(extr, k);
                app_clause = patternExtractor.matchesAppositiveClause(extr,
                        current);

                which_who = (words.get(k).equals("which")
                        || words.get(k).equals("who") || words.get(k).equals(
                        "that"));
                capitalized = patternExtractor.getCapitalized(extr, k);
                punctuation_count = patternExtractor.getPunctuationCount(extr,
                        k);
                word_before_pred_conj = patternExtractor.wordBeforePredIsConj(
                        extr, k);
                intervening_and = patternExtractor.getInterveningConj(extr, k);
                word_after_vp = patternExtractor.wordAfterIsVP(extr, k);
                np_count_before = patternExtractor.getNPCountBefore(extr, k);
                word_before_vp = patternExtractor.wordBeforeIsVP(extr, k);
                intervening_np = patternExtractor.getInterveningNPCount(extr,
                        k);

                DoubleFeatures featureMap = new DoubleFeatures();
                featureMap.put("simple_subj", toDouble(simple_subj));
                featureMap.put("quotes_subj", toDouble(quotes_subj));
                featureMap.put("relative_subj", toDouble(relative_subj));
                featureMap.put("verb_conj", toDouble(verb_conj));
                featureMap.put("app", toDouble(app_clause));
                featureMap.put("which_who", toDouble(which_who));
                featureMap.put("capitalized", toDouble(capitalized));
                featureMap.put("punct_count", toDouble(punctuation_count));
                featureMap.put("intervening_np_count", toDouble(intervening_np));
                featureMap.put("np_count_before", toDouble(np_count_before));
                featureMap.put("word_before_pred_conj", toDouble(word_before_pred_conj));
                featureMap.put("intervening_and", toDouble(intervening_and));
                featureMap.put("word_after_vp", toDouble(word_after_vp));
                featureMap.put("word_before_vp", toDouble(word_before_vp));

                return featureMap;
            }
        }

        throw new IllegalStateException();
    }
}
