package edu.washington.cs.knowitall.argumentidentifier;

import java.util.ArrayList;
import java.util.List;

import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.extractor.conf.featureset.FeatureSet;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedArgumentExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;

public class ArgLearnerFeatureSet extends FeatureSet<ChunkedBinaryExtraction> {
    private final PatternExtractor patternExtractor;

    public ArgLearnerFeatureSet() {
        super(featureNames());
        this.patternExtractor = new PatternExtractor();
    }

    public static List<String> featureNames() {
        List<String> names = new ArrayList<String>(34);
        names.add("correct_end");
        names.add("pred_starts_w_np");
        names.add("to_before_pred");

        names.add("conj_before_rel");
        names.add("which_before_rel");
        names.add("rel_one_verb");
        names.add("rel_to");
        names.add("rel_for");
        names.add("rel_in");
        names.add("rel_of");
        names.add("rel_on");

        names.add("pp_before_arg1");
        names.add("words_till_start");
        names.add("arg1_conf");
        names.add("arg1_proper");
        names.add("np_before_arg1");
        names.add("arg1_length");

        names.add("adj");
        names.add("comp");
        names.add("nest1");
        names.add("nest2");
        names.add("rel");
        names.add("npinf");
        names.add("doublenp");

        names.add("arg2_proper");
        names.add("verb_after_arg2");
        names.add("np_after_arg2");
        names.add("pp_after_arg2");
        names.add("words_till_end");
        names.add("arg2_conf");

        names.add("sent_less_than_10");
        names.add("sent_less_than_20");
        names.add("sent_more_than_20");
        names.add("extr_covers_phrase");

        return names;
    }

    private double toDouble(boolean bool) {
        // of course, weka featurizes booleans inversely
        if (bool) {
            return 0.0;
        }
        else {
            return 1.0;
        }
    }

    private double toDouble(int num) {
        return 0.0 + num;
    }

    private double toDouble(double num) {
        return num;
    }

    @Override
    public double featurize(String featureName, ChunkedBinaryExtraction extr) {
        // relation metrics
        if (featureName.equals("pred_starts_w_np")) {
            return toDouble(predStartsWithNP(extr));
        } else if (featureName.equals("to_before_pred")) {
            return toDouble(toBeforePred(extr));
        } else if (featureName.equals("conj_before_rel")) {
            return toDouble(conjBeforeRel(extr));
        } else if (featureName.equals("which_before_rel")) {
            return toDouble(whichBeforeRel(extr));
        } else if (featureName.equals("rel_one_verb")) {
            return toDouble(relOneVerb(extr));
        } else if (featureName.equals("rel_to")) {
            return toDouble(relEndsWithToken(extr, "to"));
        } else if (featureName.equals("rel_for")) {
            return toDouble(relEndsWithToken(extr, "for"));
        } else if (featureName.equals("rel_in")) {
            return toDouble(relEndsWithToken(extr, "in"));
        } else if (featureName.equals("rel_of")) {
            return toDouble(relEndsWithToken(extr, "of"));
        } else if (featureName.equals("rel_on")) {
            return toDouble(relEndsWithToken(extr, "on"));
        }

        // arg1 metrics
        else if (featureName.equals("correct_end")) {
            return toDouble(correctArg1End(extr));
        } else if (featureName.equals("pp_before_arg1")) {
            return toDouble(ppBeforeArg1(extr));
        } else if (featureName.equals("words_till_start")) {
            return toDouble(wordsTillStart(extr));
        } else if (featureName.equals("arg1_conf")) {
            return toDouble(extr.getArgument1().getConfidence());
        } else if (featureName.equals("arg1_proper")) {
            return toDouble(arg1IsProper(extr));
        } else if (featureName.equals("np_before_arg1")) {
            return toDouble(npBeforeArg1(extr));
        } else if (featureName.equals("arg1_length")) {
            return toDouble(arg1Length(extr));
        }

        // arg2 metrics
        else if (featureName.equals("adj")) {
            return toDouble(patternExtractor.adjRelation(extr.getRelation()));
        } else if (featureName.equals("comp")) {
            return toDouble(patternExtractor.complementClause(extr
                    .getRelation()));
        } else if (featureName.equals("nest1")) {
            return toDouble(patternExtractor
                    .nestedRelation1(extr.getRelation()));
        } else if (featureName.equals("nest2")) {
            return toDouble(patternExtractor
                    .nestedRelation2(extr.getRelation()));
        } else if (featureName.equals("rel")) {
            return toDouble(patternExtractor.npRelativeClause(extr
                    .getRelation()));
        } else if (featureName.equals("npinf")) {
            return toDouble(patternExtractor.npInfinitiveClause(extr
                    .getRelation()));
        } else if (featureName.equals("doublenp")) {
            return toDouble(patternExtractor.doubleNP(extr.getRelation()));
        } else if (featureName.equals("arg2_proper")) {
            return toDouble(arg2IsProper(extr));
        } else if (featureName.equals("verb_after_arg2")) {
            return toDouble(verbAfterArg2(extr));
        } else if (featureName.equals("np_after_arg2")) {
            return toDouble(npAfterArg2(extr));
        } else if (featureName.equals("pp_after_arg2")) {
            return toDouble(ppAfterArg2(extr));
        } else if (featureName.equals("words_till_end")) {
            return toDouble(wordsTillStart(extr));
        } else if (featureName.equals("arg2_conf")) {
            return toDouble(extr.getArgument2().getConfidence());
        }

        // sentence metric
        else if (featureName.equals("sent_less_than_10")) {
            return toDouble(sentLength(extr, 0, 11));
        } else if (featureName.equals("sent_less_than_20")) {
            return toDouble(sentLength(extr, 11, 21));
        } else if (featureName.equals("sent_more_than_20")) {
            return toDouble(sentLength(extr, 21, Integer.MAX_VALUE));
        } else if (featureName.equals("extr_covers_phrase")) {
            return toDouble(extrCoversPhrase(extr));
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    public int getIntValue(boolean bool, boolean dir) {
        if (bool == dir) {
            return 1;
        } else {
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
        boolean leftOk = xs == 0 || tokens.get(xs - 1).equals(",")
                || tokens.get(xs - 1).equals(".");

        int l = sent.getLength() - 1;
        int yr = y.getLastIndex();
        boolean rightOk = yr == l || tokens.get(yr + 1).equals(",")
                || tokens.get(yr + 1).equals(".");

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
        return lastArg2 + 1 < sent.getLength()
                && sent.getChunkTags().get(lastArg2 + 1).equals("B-NP");
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
            return sent.getChunkTags().get(start - 1).endsWith("-NP");
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
            if (!tag.startsWith("NNP") && !tag.equals("DT")
                    && !tag.equals("IN")) {
                return false;
            }
        }
        return true;
    }

    private boolean relEndsWithToken(ChunkedBinaryExtraction e, String t) {
        final String token = t;
        List<String> tokens = e.getRelation().getTokens();
        return tokens.get(tokens.size() - 1).equals(token);
    }

    private boolean relOneVerb(ChunkedBinaryExtraction e) {
        ChunkedExtraction rel = e.getRelation();
        List<String> posTags = rel.getPosTags();
        return posTags.size() == 1 && posTags.get(0).startsWith("V");
    }

    private boolean whichBeforeRel(ChunkedBinaryExtraction e) {
        ChunkedExtraction pred = e.getRelation();
        int predStart = pred.getStart();
        if (predStart > 0) {
            String precPosTag = e.getSentence().getPosTags().get(predStart - 1);
            String precPosToken = e.getSentence().getToken(predStart - 1);
            if (precPosTag.equals("WP") || precPosTag.equals("WDT")
                    || precPosToken.equals("that")) {
                return true;
            }
        }
        return false;
    }

    private boolean conjBeforeRel(ChunkedBinaryExtraction e) {
        ChunkedExtraction pred = e.getRelation();
        int predStart = pred.getStart();
        if (predStart > 0) {
            String precPosTag = e.getSentence().getPosTags().get(predStart - 1);
            if (precPosTag.equals("CC")) {
                return true;
            }
        }
        return false;
    }

    public int wordsTillStart(ChunkedBinaryExtraction extr) {
        if (extr.getArgument1() == null || extr.getArgument1().getLength() < 1) {
            return -1;
        }
        int words_till_start = extr.getArgument1().getStart();
        return words_till_start;
    }

    public int arg1Length(ChunkedBinaryExtraction extr) {
        if (extr.getArgument1() == null || extr.getArgument1().getLength() < 1) {
            return -1;
        }
        return extr.getArgument1().getLength();
    }

    public int wordsTillEnd(ChunkedBinaryExtraction extr) {
        if (extr.getArgument2() == null || extr.getArgument2().getLength() < 1) {
            return -1;
        }
        int words_till_end = extr.getSentence().getLength()
                - (extr.getArgument2().getStart() + extr.getArgument2()
                        .getLength());
        return words_till_end;
    }

    public boolean ppAfterArg2(ChunkedBinaryExtraction extr) {
        if (wordsTillEnd(extr) > 0) {
            int end = extr.getArgument2().getStart()
                    + extr.getArgument2().getLength();
            if (extr.getSentence().getChunkTag(end).equals("B-PP")) {
                return true;
            }
        }
        return false;
    }

    public boolean correctArg1End(ChunkedBinaryExtraction extr) {
        int i = extr.getArgument1().getStart();
        int start = i;
        while (i < extr.getArgument1().getStart()
                + extr.getArgument1().getLength()) {
            if (extr.getSentence().getChunkTag(i).equals("B-NP")) {
                start = i;
            }
            i++;
        }
        return patternExtractor.findSubj(extr.getRelation(), start);
    }

    public boolean ppBeforeArg1(ChunkedBinaryExtraction extr) {
        if (wordsTillStart(extr) > 0) {
            int start = extr.getArgument1().getStart();
            if (extr.getSentence().getChunkTag(start - 1).equals("B-PP")) {
                return true;
            }
        }
        return false;
    }

    public boolean npBeforeArg2(ChunkedBinaryExtraction extr, boolean train) {
        for (int i = extr.getRelation().getStart()
                + extr.getRelation().getLength(); i < extr.getArgument2()
                .getStart(); i++) {
            if (extr.getSentence().getChunkTag(i).contains("NP")) {
                return true;
            }
        }
        return false;
    }

    public boolean predStartsWithNP(ChunkedBinaryExtraction extr) {
        // check that the relation is in a vp
        if (extr.getSentence().getPosTag(extr.getRelation().getStart())
                .contains("N")) {
            return true;
        }
        return false;
    }

    public boolean toInPred(ChunkedBinaryExtraction extr, boolean train) {
        // check for to in current pred
        if (train) {
            for (int i = extr.getRelation().getStart(); i < extr.getArgument2()
                    .getStart(); i++) {
                if (extr.getSentence().getChunkTag(i).equals("B-VP")
                        && extr.getSentence().getPosTag(i).equals("TO")) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean toBeforePred(ChunkedBinaryExtraction extr) {
        // find appropriate vp;
        boolean in_conj = false;
        int i = extr.getRelation().getStart() - 1;
        while (i > -1) {
            if (extr.getSentence().getPosTag(i).equals("CC")
                    || extr.getSentence().getPosTag(i).equals(",")) {
                in_conj = true;
                break;
            }
            if (extr.getSentence().getChunkTag(i).contains("NP")) {
                break;
            }
            i--;
        }
        int last_vp = extr.getRelation().getStart();
        if (in_conj) {
            boolean seen_vp = false;
            while (i > -1) {
                if (extr.getSentence().getPosTag(i).equals("CC")
                        || extr.getSentence().getPosTag(i).equals(",")) {
                    seen_vp = false;
                } else if (extr.getSentence().getChunkTag(i).equals("B-VP")) {
                    seen_vp = true;
                    last_vp = i;
                } else if ((extr.getSentence().getChunkTag(i).equals("B-NP") || extr
                        .getSentence().getChunkTag(i).equals("I-NP"))
                        && seen_vp) {
                    break;
                }
                if (extr.getSentence().getChunkTag(i).equals("B-VP")
                        && extr.getSentence().getPosTag(i).equals("TO")) {
                    return true;
                }
                i--;
            }
        }
        // check for a to
        i = last_vp;
        boolean foundnp = false;
        while (i > -1) {
            if (extr.getSentence().getToken(i).equals("to")) {
                return true;
            }
            if (extr.getSentence().getChunkTag(i).equals("B-NP")
                    || extr.getSentence().getChunkTag(i).equals("I-NP")) {
                foundnp = true;
                break;
            }
            i--;
        }

        if (foundnp) {
            return false;
        }

        return false;
    }
}
