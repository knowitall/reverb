package edu.washington.cs.knowitall.argumentidentifier;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import edu.washington.cs.knowitall.regex.Match;
import edu.washington.cs.knowitall.regex.RegularExpression;
import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.nlp.ChunkedSentencePattern;
import edu.washington.cs.knowitall.nlp.ChunkedSentenceToken;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;

/**
 * PatternExtractor contains regular expressions for common syntactic patterns
 *
 * @author janara
 *
 */

public class PatternExtractor {
    public static String CAPS = "[A-Zçêòéñì]";
    public static String ALPHA = "[A-Zçêòéñìa-z]";
    public static String ALPHANUM = "[A-Zçêòéñìa-z0-9]";
    public static String PUNT = "[,\\.;:?!()-]";

    public static String np = "(?:<chunk='B-NP'> <chunk='I-NP'>*)";
    public static String vp = "(?:<chunk='B-VP'> <chunk='I-VP'>* <chunk='B-PRT'>?)";
    public static String comma = "(?:<string=','>)";
    public static String and = "(?:<string='and'>)";
    public static String or = "(?:<string='or'>)";
    public static String adjp = "(?:<chunk='B-ADJP'> <chunk='I-ADJP'>*)";
    public static String advp = "(?:<chunk='B-ADVP'> <chunk='I-ADVP'>*)";

    public static String np_pp = "(?:<chunk='B-NP'> <chunk='I-NP'>* (?:<chunk='B-PP'> <chunk='B-NP'> <chunk='I-NP'>*)*)";
    public static String pp_np1 = "(?:<chunk='B-PP'> <chunk='B-NP'> <chunk='I-NP'>* (?:<chunk='B-PP'> <chunk='B-NP'> <chunk='I-NP'>*)*)";
    public static String pp_np2 = "(?:<chunk='B-PP'>? <chunk='B-NP'> <chunk='I-NP'>* (?:<chunk='B-PP'> <chunk='B-NP'> <chunk='I-NP'>*)*)";
    public static String vp1 = "(?:(?:<pos='VBD'> | <pos='VB'> | <pos='VBZ'> | <pos='VBP'> | <pos='MD'> | <pos='VBG'> | <pos='VBN'>) <chunk='I-VP'>* <chunk='B-PRT'>?)";
    public static String vp2 = "(?:(?:<pos='VBD'> | <pos='VB'> | <pos='VBZ'> | <pos='VBP'> | <pos='MD'>) <chunk='I-VP'>* <chunk='B-PRT'>?)";
    public static String vp3 = "(?:<pos='TO'> <chunk='I-VP'>+ <chunk='B-PRT'>?)";
    public static String advp_vp_advp = "(?:" + advp + "? " + vp1 + " " + advp
            + "?)";
    public static String pp = "(?:<chunk='B-PP'> <chunk='I-PP'>*)";
    public static String introduces_if = "(?:<string='if'> | <string='as'> | <string='while'> | <string='so'> | <string='although'> | <string='whether'> | <string='why'> | <string='where'> | <string='because'> | <string='but'>)";
    public static String vp_np = "(?:" + advp + "? " + vp + " (?:" + pp_np2
            + " (?:<string=','>? <pos='CC'>? " + pp_np2 + ")*)*)";

    private HashMap<String, String> patternMapArg1 = new HashMap<String, String>();
    private HashMap<String, RegularExpression<ChunkedSentenceToken>> compiledPatternMapArg1 = new HashMap<String, RegularExpression<ChunkedSentenceToken>>();

    private HashMap<String, String> patternMapArg2 = new HashMap<String, String>();
    private HashMap<String, RegularExpression<ChunkedSentenceToken>> compiledPatternMapArg2 = new HashMap<String, RegularExpression<ChunkedSentenceToken>>();

    public PatternExtractor() {
        initPatternMap();
        initCompiledPatternMap();
    }

    private void initPatternMap() {
        patternMapArg1.put("between_commas", "<>* " + comma + " <>* " + comma
                + " <>*");
        patternMapArg1.put("between_quotes",
                "<>* <string='``'> <>* <string=''''> <>*");
        patternMapArg1.put("verb_np", "<>* " + vp2 + " " + np);
        patternMapArg1.put("double_np", "<>* " + np + " " + np);
        patternMapArg1.put("if", "<>* <string='if'> <>");
        patternMapArg1.put("app_strict", np_pp + " " + comma + " (" + np + " "
                + pp + ")* " + np + " " + comma + "?");
        patternMapArg1.put("app_1", np + " " + comma + " (" + adjp + "? " + pp
                + "? " + vp + "? " + pp + "?)? " + "(" + np + " " + advp + "? "
                + pp + "? " + comma + "? <pos='CC'>?)? " + np
                + " <string='.'>? " + comma
                + " (<pos='WDT'> | <pos='WP'> | <string='that'>)? " + advp
                + "? ");
        patternMapArg1.put("app_2", np + " " + comma + " (<pos='VBN'> " + pp
                + "*)? " + "(" + np + " " + advp + "? " + pp + "? " + comma
                + "? <pos='CC'>?)? " + np + " <string='.'>? " + comma + " "
                + advp + "? ");
        patternMapArg1.put("app_3", np_pp + " " + comma + " " + "((" + pp + " "
                + np + ") " + np + " " + comma + ")* (" + pp + " " + np + ")* "
                + np);
        patternMapArg1.put("verb_conj_simple1", np + " " + advp + "* " + vp2
                + " " + "(" + pp + "? " + np + ")* " + comma + "* " + "("
                + comma + " | <pos='CC'>) <pos='RB'>? " + advp + "*");
        patternMapArg1.put("statement", "<>* " + comma + " " + np + " " + vp
                + " " + advp + "* <string='.'>");

        patternMapArg1.put("list_3", "(" + np_pp + " " + comma + ")* " + np_pp
                + " " + comma + "? (" + and + " | " + or + ") " + np_pp);
        patternMapArg1.put("list_1", "(" + np + " " + comma + ")* " + np + " "
                + comma + "? (" + and + " | " + or + ") " + np);
        patternMapArg1.put("list_2", "(" + np + " " + comma + ")* " + np + " "
                + comma + "? (" + and + " | " + or
                + ") <chunk='I-NP'> (<chunk='I-NP'>)*");
        patternMapArg1.put("obj", "<>* " + vp + " " + pp + "? " + np_pp);
        patternMapArg1.put("contains_interv_clause1", np + " " + comma + " "
                + pp + "+ " + np + " (" + pp + "? " + np + ")* " + comma + " "
                + advp + "? " + vp + "? ");
        patternMapArg1.put("contains_interv_clause2", np + " " + comma + " "
                + np + " " + vp + " " + comma + " " + advp + "? " + vp + "? ");
        patternMapArg1.put("to_verb",
                " <>* <pos='TO'> <chunk='I-VP'>* <chunk='B-PRT'>? <>* ");
        patternMapArg1.put("subj_simple", np + " " + advp + "*");
        patternMapArg1.put("subj_rel", np + " " + comma
                + "* (<pos='WP'> | <pos='WDT'> | <string='that'>) " + advp
                + "*");
        patternMapArg1.put("subj_quotes1", "<> " + comma
                + "* (<string=''''> | <string='``'>) " + advp + "*");
        patternMapArg1.put("subj_quotes2", np + " " + comma
                + "* (<string=''''> | <string='``'>) " + advp + "*");
        patternMapArg1.put("subj_4", "(" + np + " " + vp + " " + "(" + pp
                + "* " + np + " (" + pp + " " + np + ")*)* " + comma + ")* "
                + vp + " (" + pp + "* " + np + " (" + pp + " " + np + ")*)* "
                + comma + "* (" + comma + " | " + and + "| " + or + ") " + advp
                + "*");
        patternMapArg1.put("subj_5a", comma + " " + pp + "* " + np + " (" + pp
                + "* " + np + ")* " + adjp + "* " + comma + " " + advp + "*");
        patternMapArg1.put("subj_5b", comma + " <pos='VBN'> (" + pp + "* " + np
                + ")* " + adjp + "* " + comma + " " + advp + "*");
        patternMapArg1.put("subj_5c", comma + " " + np + "* <pos='CC'> (" + pp
                + "* " + np + ")* " + adjp + "* " + comma + " " + advp + "*");
        patternMapArg1.put("subj_6", np + "* " + comma + " " + advp + "*");
        patternMapArg1.put("relative_clause", "<>* " + np + " " + comma
                + "? (<pos='WDT'> | <pos='WP'> | <string='that'>) " + vp + " "
                + pp + "? " + np_pp);// +vp+" "+pp_np2+"? "+comma+"? ");

        patternMapArg2.put("np_list", np + " (" + comma + " " + np + ")* "
                + comma + " " + np + " <>*");
        patternMapArg2.put("np_list_cc", np + " <pos='CC'> " + np + " <>* ");
        patternMapArg2
                .put("relative_clause",
                        (vp1 + " " + pp + "? " + np_pp + " " + comma + "? "
                                + pp + "? (<pos='WP'> | <pos='WDT'> | <pos='WRB'> | <string='that'>) <>* "));
        patternMapArg2
                .put("contains_relative_clause",
                        "<>* <chunk='B-NP'> <>* (<string='that'> | <chunk='B-SBAR'> | <pos='WDT'> | <pos='WP'> | <pos='WRB'>) <>* ");
        patternMapArg2
                .put("contains_relative_clause2",
                        "<>* <chunk='B-NP'> <>* (<chunk='B-SBAR'> | <pos='WDT'> | <pos='WRB'>) <>* ");

        patternMapArg2
                .put("nested_relation1",
                        (advp_vp_advp
                                + " (("
                                + comma
                                + " | <string=':'>)? (<string='``'> | <string='\"'>))? "
                                + pp + "? " + np + " (" + pp + " " + np + ")? "
                                + advp + "? " + vp2 + " <>*"));
        patternMapArg2.put("nested_relation2",
                (advp_vp_advp + " " + np + " <chunk='B-SBAR'> <>*"));
        patternMapArg2.put("nested_relation3", (advp_vp_advp + " " + pp
                + "? ((" + comma
                + " | <string=':'>)? (<string='``'> | <string='\"'>))? "
                + np_pp + " " + advp + "? " + vp2 + " <>*"));
        patternMapArg2
                .put("nested_relation4",
                        "<>* (<chunk='B-SBAR'> | <pos='WRB'> | <pos='WDT'> | <pos='WP'> | <string='that'>) <>* "
                                + vp1 + " <>*");
        patternMapArg2.put("double_np1", np + " " + np + " <>*");
        patternMapArg2.put("double_np2", np + " " + np + " <chunk='B-VP'> <>*");

        patternMapArg2.put("infinitive_clause", advp + "? " + vp
                + "? <chunk='I-VP'>* <pos='TO'> <chunk='I-VP'>+ <>*");
        patternMapArg2.put("np_infinitive_clause", advp + "? " + vp1 + " "
                + advp + "? " + pp + "? ((" + comma
                + " | <string=':'>)? (<string='``'> | <string='\"'>))? " + np
                + " " + advp + "? " + vp3 + " <>*");

        patternMapArg2.put("complement_clause1", advp + "? " + vp1
                + " (<chunk='B-SBAR'> |<pos='WRB'> | <pos='WP'>) <>*");
        patternMapArg2.put("complement_clause2", advp + "? " + vp1 + " " + advp
                + "? <string='that'> " + np + " <>*");
        patternMapArg2.put("objNestedClause", advp + "? " + vp1 + " " + advp
                + "? " + np + " <string='that'> " + np + " " + vp + " <>*");
        patternMapArg2
                .put("adj_relation",
                        advp
                                + "? (<pos='VBD'> | <pos='VB'> | <pos='VBZ'> | <pos='VBN'> | <pos='VBP'> | <pos='MD'>) <chunk='I-VP'>* "
                                + advp
                                + "? "
                                + " <chunk='B-NP'> <chunk='I-NP'>* <chunk='B-PP'>? <chunk='B-ADJP'> <>*");
        patternMapArg2
                .put("list1",
                        "<chunk='B-NP'> <chunk='I-NP'>* (<string=','> (<chunk='B-PP'>)? <chunk='B-NP'> <chunk='I-NP'>*)* (<string=','> | (<string=','> <pos='CC'>)) <chunk='B-NP'> <>*");
        patternMapArg2
                .put("list3",
                        "<chunk='B-NP'> <chunk='I-NP'>* (<string=','> <chunk='B-NP'> <chunk='I-NP'>*)* <string=','>* <pos='CC'> <chunk='B-NP'> <>*");
        patternMapArg2
                .put("compound1",
                        "<chunk='B-NP'> <chunk='I-NP'>* ((<string=','> <chunk='B-NP'> <chunk='I-NP'>* (<chunk='B-PP'>)?)* (<string=','> | (<string=','> <pos='CC'>)) <chunk='B-NP'> <chunk='I-NP'>)* <string=','>* <pos='CC'> "
                                + advp + "? <chunk='B-VP'> <>*");
        patternMapArg2
                .put("compound2",
                        "<chunk='B-NP'> <chunk='I-NP'>* (<chunk='B-PP'> <chunk='B-NP'> <chunk='I-NP'>)* <string=','>* <pos='CC'> "
                                + advp + "? <chunk='B-VP'> <>*");
        patternMapArg2
                .put("ifclause1",
                        "<chunk='B-NP'> <chunk='I-NP'>* ((<string=','> <chunk='B-NP'> <chunk='I-NP'>* (<chunk='B-PP'>)?)* (<string=','> | (<string=','> <pos='CC'>)) <chunk='B-NP'> <chunk='I-NP'>)* <string=','>* "
                                + introduces_if + " <>*");
        patternMapArg2
                .put("ifclause2",
                        "<chunk='B-NP'> <chunk='I-NP'>* (<chunk='B-PP'> <chunk='B-NP'> <chunk='I-NP'>)* "
                                + introduces_if + " <>*");
        patternMapArg2
                .put("relclause1",
                        "<chunk='I-NP'>* <string=','>? (<string='``'> | <string=''''>)? <string=','>? (<pos='WDT'> | <pos='WP'> | <string='that'>) <>* ");
        patternMapArg2
                .put("relclause2",
                        "<chunk='I-NP'>* <string=','> <string=''''> (<pos='WDT'> | <pos='WP'> | <string='that'>) <>* ");
        patternMapArg2
                .put("relclause3",
                        "<chunk='I-NP'>* <string=','> (<pos='WDT'> | <pos='WP'> | <string='that'>) <>* ");
        patternMapArg2.put("vbg_1",
                "<chunk='I-NP'>* <string=','> <pos='VBG'> <>* ");
        patternMapArg2.put("vbg_2", "<chunk='I-NP'>* <pos='VBG'> <>* ");
        patternMapArg2
                .put("app_1",
                        "<chunk='I-NP'>* (<string='``'> | <string=''''>)? <string=','> <chunk='B-NP'> <chunk='I-NP'>* (<chunk='B-PP'>? <chunk='B-NP'> <chunk='I-NP'>*)* (<string=','> | <string='.'>) <>* ");
        patternMapArg2
                .put("app_2",
                        "<chunk='I-NP'>* <string=','> (<string='``'> | <string=''''>)? <chunk='B-NP'> <chunk='I-NP'>* (<chunk='B-PP'>? <chunk='B-NP'> <chunk='I-NP'>*)* (<string=','> | <string='.'>) <>* ");

    }

    private void initCompiledPatternMap() {
        Set<String> keys = patternMapArg1.keySet();
        Iterator<String> iter = keys.iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            String pattern = patternMapArg1.get(key);
            compiledPatternMapArg1.put(key,
                    ChunkedSentencePattern.compile(pattern));
        }

        keys = patternMapArg2.keySet();
        iter = keys.iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            String pattern = patternMapArg2.get(key);
            compiledPatternMapArg2.put(key,
                    ChunkedSentencePattern.compile(pattern));
        }
    }

    public boolean prevStop(ChunkedExtraction extr, int current) {

        if (!matches(ChunkedSentenceToken.tokenize(extr.getSentence(), new Range(0, extr.getStart())),
                "between_commas", true)
                && !matches(
                        ChunkedSentenceToken.tokenize(extr.getSentence(), new Range(0, extr.getStart())),
                        "between_quotes", true)) {
            if ((!extr.getSentence().getPosTag(current).equals("WDT")
                    && !extr.getSentence().getPosTag(current).equals("WRB")
                    && !extr.getSentence().getPosTag(current).equals("WP") && matches(
                        ChunkedSentenceToken.tokenize(extr.getSentence(), new Range(0, current + 1)),
                        "double_np", true))
                    || matches(ChunkedSentenceToken.tokenize(extr.getSentence(),
                            new Range(0, current + 1)), "if", true)
                    || matches(ChunkedSentenceToken.tokenize(extr.getSentence(),
                            new Range(0, current + 1)), "verb_np", true)) {
                return true;
            }
        }
        return false;
    }

    public int getInterveningNPCount(ChunkedExtraction extr, int k) {
        int count = 0;
        for (int i = k + 1; i < extr.getStart(); i++) {
            if (extr.getSentence().getChunkTag(i).equals("B-NP")
                    && !(extr.getSentence().getToken(i).equals("which")
                            || extr.getSentence().getToken(i).equals("who") || extr
                            .getSentence().getToken(i).equals("that"))) {
                count++;
            }
        }
        return count;
    }

    public int getPunctuationCount(ChunkedExtraction extr, int current) {
        Pattern punt_pattern = Pattern.compile(PUNT);
        int punctuation_count = 0;
        for (int i = extr.getStart() - 1; i >= current; i--) {
            if (punt_pattern.matcher(extr.getSentence().getToken(i)).matches()) {
                punctuation_count++;
            }
        }
        return punctuation_count;
    }

    public boolean getCapitalized(ChunkedExtraction extr, int current) {
        char first = extr.getSentence().getToken(current).charAt(0);
        if (first > 64 && first < 91) {
            return true;
        }
        return false;
    }

    public boolean wordBeforePredIsConj(ChunkedExtraction extr, int current) {
        int pred_start = extr.getStart();
        if (extr.getSentence().getToken(pred_start - 1).equals("but")
                || extr.getSentence().getToken(pred_start - 1).equals("and")
                || extr.getSentence().getToken(pred_start - 1).equals("or")) {
            return true;
        }
        return false;
    }

    public boolean getInterveningConj(ChunkedExtraction extr, int current) {
        boolean intervening_and = false;
        for (int i = extr.getStart() - 1; i >= current; i--) {
            if (extr.getSentence().getPosTag(i).equals("CC")) {
                intervening_and = true;
            }
        }
        return intervening_and;
    }

    public boolean wordAfterIsVP(ChunkedExtraction extr, int current) {
        int i = current;
        while (i < extr.getStart()
                && (extr.getSentence().getChunkTag(i).contains("NP") || extr
                        .getSentence().getChunkTag(i).contains("PP"))) {
            i++;
        }
        extr.getSentence().getChunkTag(i).contains("VP");
        return extr.getSentence().getChunkTag(i).contains("VP");
    }

    public boolean wordBeforeIsVP(ChunkedExtraction extr, int current) {
        boolean word_before_vp = false;
        int i = current;
        while (i > -1
                && (extr.getSentence().getChunkTag(i).contains("NP")
                        || extr.getSentence().getChunkTag(i).contains("PP") || extr
                        .getSentence().getChunkTag(i).contains("RB"))) {
            i--;
        }
        if (i > -1) {
            word_before_vp = (extr.getSentence().getChunkTag(i).contains("VP") && !extr
                    .getSentence().getChunkTag(i).contains("ADVP"));
        }
        return word_before_vp;
    }

    public boolean nextIsThat(ChunkedExtraction extr, int current) {
        current++;
        while (current < extr.getStart()
                && (extr.getSentence().getChunkTag(current).equals("I-NP") || extr
                        .getSentence().getToken(current).equals(","))) {
            current++;
        }
        if (current >= extr.getStart()) {
            return false;
        }
        if (extr.getSentence().getPosTag(current).equals("WP")
                || extr.getSentence().getPosTag(current).equals("WDT")
                || extr.getSentence().getToken(current).equals("that")) {
            return true;
        }
        return false;
    }

    public int getNPCountBefore(ChunkedExtraction extr, int current) {
        int np_count = 0;
        current = current - 1;
        while (current > -1) {
            if (extr.getSentence().getChunkTag(current).equals("B-NP")) {
                np_count++;
            }
            current--;
        }
        return np_count;
    }

    public boolean matchesPPBeforeVerb(ChunkedExtraction extr) {
        int i = extr.getStart();
        while (i > 0 && extr.getSentence().getChunkTag(i).contains("VP")) {
            i--;
            if (extr.getSentence().getPosTag(i).equals("IN")
                    || extr.getSentence().getPosTag(i).equals("TO")) {
                return true;
            }
        }
        return false;
    }

    public boolean matchesToVerb(ChunkedExtraction extr, boolean debug) {
        int i = extr.getStart();
        while (i > 0 && extr.getSentence().getChunkTag(i).contains("VP")) {
            i--;
            if (extr.getSentence().getToken(i).equals("to")) {
                return true;
            }
        }
        return false;
    }

    public boolean matchesNPVerb(ChunkedExtraction extr) {
        int i = extr.getStart();
        if (extr.getSentence().getChunkTag(i).contains("N")) {
            return true;
        }
        return false;
    }

    public boolean matchesAppositiveClause2(ChunkedExtraction extr, int current) {
        if (!extr.getSentence().getChunkTag(current).equals("B-NP")) {
            return false;
        }
        int length = extr.getStart() - current;
        if (matches(ChunkedSentenceToken.tokenize(extr.getSentence(), new Range(current, length)),
                "app_3", true)) {
            return true;
        }
        return false;
    }

    public boolean matchesRelativeClause(ChunkedExtraction extr, int argend) {
        if (matches(ChunkedSentenceToken.tokenize(extr.getSentence(), new Range(0, argend)),
                "relative_clause", true)
                && !(extr.getSentence().getChunkTag(argend - 1).contains("NP") && extr
                        .getSentence().getChunkTag(argend).equals("B-VP"))) {
            return true;
        }
        return false;
    }

    public boolean matchesQuotes(ChunkedExtraction extr) {
        if (extr.getStart() < 4) {
            return false;
        }
        if (extr.getSentence().getToken(extr.getStart() - 1).equals("''")
                && extr.getSentence().getToken(extr.getStart() - 2).equals(",")) {
            return true;
        }
        return false;
    }

    public boolean matchesStatement(ChunkedExtraction extr) {
        if (extr.getStart() < 2) {
            return false;
        }
        int length = extr.getSentence().getLength();
        if (matches(ChunkedSentenceToken.tokenize(extr.getSentence().getSubSequence(0, length)), "statement",
                true)) {
            return true;
        }
        return false;
    }

    public boolean matchesAppositiveStrict(ChunkedExtraction extr) {
        if (matches(
                ChunkedSentenceToken.tokenize(extr.getSentence(), new Range(extr.getStart(),
                        extr.getLength())), "app_strict", true)) {
            return true;
        }
        return false;
    }

    public boolean matchesListStrict(ChunkedExtraction extr) {
        if (matches(
                ChunkedSentenceToken.tokenize(extr.getSentence(), new Range(extr.getStart(),
                        extr.getLength())), "list_1", true)
                && !extr.getSentence()
                        .getSubSequence(extr.getStart(), extr.getLength())
                        .getPosTagsAsString().contains("IN")) {
            return true;
        }
        return false;
    }

    public boolean matchesList(ChunkedExtraction extr, int argend, int current) {
        while (current > -1
                && (extr.getSentence().getChunkTag(current).contains("NP")
                        || extr.getSentence().getChunkTag(current)
                                .contains("PP")
                        || extr.getSentence().getPosTag(current).contains(",") || extr
                        .getSentence().getPosTag(current).contains("CC"))) {
            int length = argend - current;
            if ((matches(ChunkedSentenceToken.tokenize(extr.getSentence(), new Range(current, length)),
                    "list_1", true)
                    || matches(
                            ChunkedSentenceToken.tokenize(extr.getSentence(), new Range(current, length)),
                            "list_3", true) || matches(ChunkedSentenceToken.tokenize(extr.getSentence(),
                    new Range(current, length)), "list_2", true))
                    && (current == 0
                            || extr.getSentence().getChunkTag(current)
                                    .equals(",")
                            || !extr.getSentence().getChunkTag(current - 1)
                                    .contains("B-VP") || !extr.getSentence()
                            .getChunkTag(current - 1).contains("I-VP"))
                    && (current <= 1 || (!extr.getSentence()
                            .getChunkTag(current - 1).contains("PP")
                            && !extr.getSentence().getChunkTag(current - 2)
                                    .contains("V") && !matchesObj(extr, current)))) {
                return true;
            }
            current--;
        }
        return false;
    }

    public boolean matchesObj(ChunkedExtraction extr, int current) {
        int length = current + 1;
        if ((matches(ChunkedSentenceToken.tokenize(extr.getSentence(), new Range(0, length)), "obj", true))) {
            return true;
        }
        return false;
    }

    public boolean matchesCommaBeforeVerb(ChunkedExtraction extr) {
        int i = extr.getStart();
        while (i > 0 && extr.getSentence().getChunkTag(i).contains("VP")) {
            i--;
            if (extr.getSentence().getToken(i).equals(",")) {
                return true;
            }
        }
        return false;
    }

    public boolean nextNPWhich(ChunkedExtraction extr, int current) {
        int i = current + 1;
        while (i < extr.getStart()) {
            if (extr.getSentence().getChunkTag(i).equals("B-NP")) {
                break;
            }
            i++;
        }
        if (extr.getSentence().getToken(i).equals("who")
                || extr.getSentence().getToken(i).equals("which")
                || extr.getSentence().getToken(i).equals("that")) {
            return true;
        }
        return false;
    }

    public boolean nextIsDash(ChunkedExtraction extr, int current) {
        current++;
        while (current < extr.getStart()
                && extr.getSentence().getChunkTag(current).contains("NP")) {
            current++;
        }
        if (extr.getSentence().getToken(current).equals("-")) {
            return true;
        }
        return false;
    }

    public boolean vpStartsWithTo(ChunkedExtraction extr, int current) {
        int length = extr.getStart() - current;
        return (matches(ChunkedSentenceToken.tokenize(extr.getSentence(), new Range(current, length)),
                "to_verb", true));
    }

    public boolean matchesVerbConjSimple(ChunkedExtraction extr, int current) {
        if ((matches(
                ChunkedSentenceToken.tokenize(extr.getSentence(), new Range(current,
                        extr.getStart() - current)), "verb_conj_simple1", true))) {
            return true;
        }
        return false;
    }

    public boolean matchesAppositiveClause(ChunkedExtraction extr, int current) {
        // check if there's a comma before the next np
        int i = current - 1;
        boolean notseencomma = true;
        while (i > 0
                && (notseencomma || !extr.getSentence().getChunkTag(i)
                        .equals("B-NP"))) {
            if (extr.getSentence().getToken(i).equals(",")) {
                notseencomma = false;
            }
            i--;
        }

        if (i < 0 || notseencomma
                || !extr.getSentence().getChunkTag(i).equals("B-NP")) {
            return false;
        }

        int length = extr.getStart() - i;
        if (matches(ChunkedSentenceToken.tokenize(extr.getSentence(), new Range(i, length)), "app_1", true)
                || matches(ChunkedSentenceToken.tokenize(extr.getSentence(), new Range(i, length)),
                        "app_2", true)) {
            return true;
        }
        return false;
    }

    public boolean simpleSubj(ChunkedExtraction extr, int i) {
        if (!extr.getSentence().getPosTag(i).startsWith("W")
                && (matches(
                        ChunkedSentenceToken.tokenize(extr.getSentence(), new Range(i,
                                extr.getStart() - i)), "subj_simple", true))) {
            return true;
        }
        return false;
    }

    public boolean quotesSubj(ChunkedExtraction extr, int i) {
        if (matches(ChunkedSentenceToken.tokenize(extr.getSentence(), new Range(i, extr.getStart() - i)),
                "subj_quotes1", true)
                || matches(
                        ChunkedSentenceToken.tokenize(extr.getSentence(), new Range(i,
                                extr.getStart() - i)), "subj_quotes2", true)) {
            return true;
        }
        return false;
    }

    public boolean relSubj(ChunkedExtraction extr, int i) {
        if (matches(ChunkedSentenceToken.tokenize(extr.getSentence(), new Range(i, extr.getStart() - i)),
                "subj_rel", true)) {
            return true;
        }
        return false;
    }

    public boolean findSubj(ChunkedExtraction extr, int i) {
        int npcount = 0;
        int index = i + 1;
        while (index < extr.getStart()) {
            if (extr.getSentence().getChunkTag(index).equals("B-NP")) {
                npcount++;
            }
            index++;
        }
        while (i > -1) {
            int start = i;
            int length = extr.getStart() - i;
            if ((matches(ChunkedSentenceToken.tokenize(extr.getSentence(),
                    new Range(start, length)), "subj_4", true))
                    || (extr.getSentence().getPosTag(extr.getStart())
                            .equals("VBN")
                            && (matches(ChunkedSentenceToken.tokenize(
                                    extr.getSentence(),
                                    new Range(start, length)), "subj_6", true)) || (npcount < 1
                            && (matches(ChunkedSentenceToken.tokenize(
                                    extr.getSentence(),
                                    new Range(start, length)), "subj_5a", true))
                            || (matches(ChunkedSentenceToken.tokenize(
                                    extr.getSentence(),
                                    new Range(start, length)), "subj_5b", true)) || (matches(
                                ChunkedSentenceToken.tokenize(extr
                                        .getSentence(),
                                        new Range(start, length)), "subj_5c",
                                true))))) {
                return true;
            }
            i--;
        }
        return false;
    }

    public boolean appClause(ChunkedExtraction extr, int current) {
        List<ChunkedSentenceToken> tocheck = ChunkedSentenceToken.tokenize(extr.getSentence(), new Range(current,
                extr.getSentence().getLength() - current));
        // matches pattern
        if (matches(tocheck, "app_1", false)
                || matches(tocheck, "app_2", false)) {
            return true;
        }
        return false;
    }

    public boolean vbgIsNext(ChunkedExtraction extr, int current) {
        List<ChunkedSentenceToken> tocheck = ChunkedSentenceToken.tokenize(extr.getSentence(), new Range(current,
                extr.getSentence().getLength() - current));
        // matches pattern
        if (matches(tocheck, "vbg_1", false)
                || matches(tocheck, "vbg_2", false)) {
            return true;
        }
        return false;
    }

    public boolean relClause(ChunkedExtraction extr, int current) {
        List<ChunkedSentenceToken> tocheck = ChunkedSentenceToken.tokenize(extr.getSentence(), new Range(current,
                extr.getSentence().getLength() - current));
        // matches pattern
        if (matches(tocheck, "relclause1", false)
                || matches(tocheck, "relclause2", false)
                || matches(tocheck, "relclause3", false)) {
            return true;
        }
        return false;
    }

    public boolean ifClause(ChunkedExtraction extr) {
        List<ChunkedSentenceToken> tocheck = getChunkedSentenceFromPredEnd(extr);
        // matches pattern
        if (matches(tocheck, "ifclause1", false)
                || matches(tocheck, "ifclause2", false)) {
            return true;
        }
        return false;
    }

    public boolean compoundVerb(ChunkedExtraction extr) {
        List<ChunkedSentenceToken> tocheck = getChunkedSentenceFromPredEnd(extr);
        // matches pattern
        if (matches(tocheck, "compound1", false)
                || matches(tocheck, "compound2", false)) {
            return true;
        }
        return false;
    }

    public boolean startsList(ChunkedExtraction extr) {
        // find start of pattern
        List<ChunkedSentenceToken> tocheck = getChunkedSentenceFromPredEnd(extr);
        // matches pattern
        if (matches(tocheck, "list1", false)
                || matches(tocheck, "list3", false)) {
            return true;
        }
        return false;
    }

    public boolean adjRelation(ChunkedExtraction extr) {
        // find start of pattern
        List<ChunkedSentenceToken> tocheck = getChunkedSentenceFromPred(extr);
        // matches pattern
        if (matches(tocheck, "adj_relation", false)) {
            return true;
        }
        return false;
    }

    public boolean objNestedClause(ChunkedExtraction extr) {
        // find start of pattern
        List<ChunkedSentenceToken> tocheck = getChunkedSentenceFromPred(extr);
        // matches pattern
        if (matches(tocheck, "objNestedClause", false)) {
            return true;
        }
        return false;
    }

    public boolean complementClause(ChunkedExtraction extr) {
        // find start of pattern
        List<ChunkedSentenceToken> tocheck = getChunkedSentenceFromPred(extr);
        // matches pattern
        if (matches(tocheck, "complement_clause1", false)
                || matches(tocheck, "complement_clause2", false)) {
            return true;
        }
        return false;

    }

    public boolean npInfinitiveClause(ChunkedExtraction extr) {
        // find start of pattern
        List<ChunkedSentenceToken> tocheck = getChunkedSentenceFromPred(extr);
        // matches pattern
        if (matches(tocheck, "np_infinitive_clause", false)) {
            return true;
        }
        return false;

    }

    public boolean infinitiveClause(ChunkedExtraction extr) {
        // find start of pattern
        List<ChunkedSentenceToken> tocheck = getChunkedSentenceFromPred(extr);
        // matches pattern
        if (matches(tocheck, "infinitive_clause", false)) {
            return true;
        }
        return false;
    }

    public boolean doubleNP(ChunkedExtraction extr) {
        // find start of pattern
        List<ChunkedSentenceToken> tocheck = getChunkedSentenceFromPredEnd(extr);
        // matches pattern
        if (matches(tocheck, "double_np1", false)
                && !matches(tocheck, "double_np2", false)) {
            return true;
        }
        return false;
    }

    public boolean nestedRelation1(ChunkedExtraction extr) {
        // find start of pattern
        List<ChunkedSentenceToken> tocheck = getChunkedSentenceFromPred(extr);
        // matches pattern
        if (matches(tocheck, "nested_relation1", false)) {
            int count = 0;
            for (int i = extr.getStart(); i < extr.getStart()
                    + extr.getLength(); i++) {
                if (extr.getSentence().getPosTag(i).startsWith("V")
                        || extr.getSentence().getChunkTag(i).equals("B-NP")) {
                    count++;
                }
            }
            if (count < 2) {
                return true;
            }
        }
        return false;
    }

    public boolean nestedRelation2(ChunkedExtraction extr) {
        // find start of pattern
        List<ChunkedSentenceToken> tocheck = getChunkedSentenceFromPred(extr);
        // matches pattern
        if (matches(tocheck, "nested_relation2", false)) {
            return true;
        }
        if (matches(tocheck, "nested_relation1", false)
                && !complementClause(extr)) {
            int count = 0;
            for (int i = extr.getStart(); i < extr.getStart()
                    + extr.getLength(); i++) {
                if (extr.getSentence().getPosTag(i).startsWith("V")
                        || extr.getSentence().getChunkTag(i).equals("B-NP")) {
                    count++;
                }
            }
            if (count >= 2) {
                return true;
            }
        }
        return false;
    }

    public boolean npRelativeClause(ChunkedExtraction extr) {
        // find start of pattern
        List<ChunkedSentenceToken> tocheck = getChunkedSentenceFromPred(extr);
        // match patterns
        if (matches(tocheck, "relative_clause", false)) {
            return true;
        }
        return false;
    }

    public boolean matchesNPList(ChunkedExtraction extr) {
        boolean innplist = false;

        // find start of pattern
        int pred_end = extr.getStart() + extr.getLength();
        int start = -1;
        for (int i = pred_end; i < extr.getSentence().getLength(); i++) {
            if (extr.getSentence().getChunkTag(i).equals("B-NP")) {
                start = i;
                break;
            }
        }
        if (start < 0) {
            return false;
        }
        int length = extr.getSentence().getLength() - start;
        List<ChunkedSentenceToken> tocheck = ChunkedSentenceToken.tokenize(extr.getSentence(), new Range(start,
                length));

        // match patterns
        boolean matches1 = matches(tocheck, "np_list", false);
        boolean matches2 = matches(tocheck, "np_list_cc", false);
        if (matches1 || matches2) {
            innplist = true;
        }
        return innplist;
    }

    public static List<ChunkedSentenceToken> getChunkedSentenceFromPred(
            ChunkedExtraction extr) {
        int pred_start_orig = extr.getStart();
        int start = pred_start_orig;
        for (int i = pred_start_orig; i < pred_start_orig + extr.getLength(); i++) {
            if (extr.getSentence().getChunkTag(i).contains("I-VP")
                    || extr.getSentence().getChunkTag(i).contains("B-VP")) {
                start = i;
            }
        }
        int end = extr.getSentence().getLength();
        return ChunkedSentenceToken.tokenize(extr.getSentence(), new Range(start, end - start));
    }

    public static List<ChunkedSentenceToken> getChunkedSentenceFromPredEnd(
            ChunkedExtraction extr) {
        int start = extr.getStart() + extr.getLength();
        int end = extr.getSentence().getLength();
        return ChunkedSentenceToken.tokenize(extr.getSentence(), new Range(start, end - start));
    }

    public boolean matches(List<ChunkedSentenceToken> tokens, String type, boolean arg1) {
        Match<ChunkedSentenceToken> match;
        if (arg1) {
            match = compiledPatternMapArg1.get(type).match(
                    tokens);
        } else {
            match = compiledPatternMapArg2.get(type).match(
                    tokens);
        }
        return match != null;
    }

    public static boolean inNPList(ChunkedExtraction extr, int current) {
        int pred_end = extr.getStart() + extr.getLength();
        int start = -1;
        for (int i = pred_end; i < extr.getSentence().getLength(); i++) {
            if (extr.getSentence().getChunkTag(i).equals("B-NP")) {
                start = i;
                break;
            }
        }
        if (start < 0 || start > current - 1) {
            return false;
        }
        int comma_cc_count = 0;
        for (int i = start; i < current + 1; i++) {
            if ((extr.getSentence().getToken(i).equals("which")
                    || extr.getSentence().getToken(i).equals("that") || extr
                    .getSentence().getToken(i).equals("who"))
                    || (!extr.getSentence().getChunkTag(i).equals("B-NP")
                            && !extr.getSentence().getChunkTag(i)
                                    .equals("I-NP")
                            && !extr.getSentence().getToken(i).equals("and")
                            && !extr.getSentence().getToken(i).equals("or") && !extr
                            .getSentence().getToken(i).equals(","))) {
                return false;
            }
            if (extr.getSentence().getToken(i).equals("and")
                    || extr.getSentence().getToken(i).equals("or")
                    || extr.getSentence().getToken(i).equals(",")) {
                comma_cc_count++;
            }
        }
        if (comma_cc_count > 0) {
            return true;
        }
        return false;
    }

    public static boolean inPPList(ChunkedExtraction extr, int current) {
        int pred_end = extr.getStart() + extr.getLength();
        int start = -1;
        for (int i = pred_end; i < extr.getSentence().getLength(); i++) {
            if (extr.getSentence().getChunkTag(i).equals("B-NP")) {
                start = i;
                break;
            }
        }
        if (start < 0 || start > current - 1) {
            return false;
        }
        int pp_count = 0;
        for (int i = start; i < current + 1; i++) {
            if ((extr.getSentence().getToken(i).equals("which")
                    || extr.getSentence().getToken(i).equals("that") || extr
                    .getSentence().getToken(i).equals("who"))
                    || (!extr.getSentence().getChunkTag(i).equals("B-NP")
                            && !extr.getSentence().getChunkTag(i)
                                    .equals("I-NP")
                            && !extr.getSentence().getChunkTag(i)
                                    .equals("B-PP") && !extr.getSentence()
                            .getChunkTag(i).equals("I-PP"))) {
                return false;
            }
            if (extr.getSentence().getChunkTag(i).equals("B-PP")) {
                pp_count++;
            }
        }
        if (pp_count > 0) {
            return true;
        }
        return false;
    }

    public static boolean previousIsOf(ChunkedExtraction extr, int current) {
        current--;
        while (current > extr.getStart() + extr.getLength()) {
            if (extr.getSentence().getChunkTag(current).contains("NP")
                    || extr.getSentence().getChunkTag(current).contains("VP")) {
                break;
            } else if (extr.getSentence().getToken(current).equals("of")) {
                return true;
            }
            current--;
        }
        return false;
    }

    public static boolean nextIsVP(ChunkedExtraction extr, int current) {
        int next = -1;
        for (int i = current + 1; i < extr.getSentence().getLength(); i++) {
            if (!extr.getSentence().getChunkTag(i).equals("I-NP")) {
                next = i;
                break;
            }
        }
        if (next > -1 && next < extr.getSentence().getLength()) {
            if (extr.getSentence().getPosTag(next).contains("V")
                    || extr.getSentence().getChunkTag(next).contains("V")) {
                return true;
            }
        }
        return false;
    }

    public static boolean nextIsPP(ChunkedExtraction extr, int current) {
        int next = -1;
        for (int i = current + 1; i < extr.getSentence().getLength(); i++) {
            if (!extr.getSentence().getChunkTag(i).equals("I-NP")) {
                next = i;
                break;
            }
        }
        if (next > -1 && next < extr.getSentence().getLength()) {
            if (extr.getSentence().getChunkTag(next).contains("PP")) {
                return true;
            }
        }
        return false;
    }

    public static boolean nextisNP(ChunkedExtraction extr, int current) {
        int next = -1;
        for (int i = current + 1; i < extr.getSentence().getLength(); i++) {
            if (!extr.getSentence().getChunkTag(i).equals("I-NP")) {
                next = i;
                break;
            }
        }
        if (next > -1 && next < extr.getSentence().getLength()) {
            if (extr.getSentence().getChunkTag(next).contains("B-NP")) {
                return true;
            }
        }
        return false;
    }

    public static int getNPToEndCount(ChunkedExtraction extr, int current) {
        int count = 0;
        for (int i = current; i < extr.getSentence().getLength(); i++) {
            if (extr.getSentence().getChunkTag(i).equals("B-NP")) {
                count++;
            }
        }
        return count;
    }

}
