package edu.washington.cs.knowitall.argumentidentifier;

public class ExtractionParameters {
    public static final boolean NEG_TRAINING = false;
    public static final boolean ALL_VNCLS = false;
    public static boolean USE_VLCN_FEATURES = false;
    public static boolean USE_ENTITY_FEATURES = true;
    public static boolean USE_PATTERN_FEATURES = true;
    public static boolean USE_CONJUNCTIVE_FEATURES = true;

    // lexical features (never set for open ie)
    public static boolean USE_LEX_FEATURES = true;
    public static boolean USE_CONTEXTUAL_FEATURES = true;
    // dependency parse features
    public static boolean USE_PARSER_FEATURES = false;
    // must all E1s be a subject? (during training)
    public static boolean REQUIRE_SBJ = true;

    public static int MAX_REL_WORDS = 5; // max words in an extracted relation
    public static int MAX_DIST = 10; // max distance (words) between two
                                     // possible entities
    public static int MAX_SENTENCE_LENGTH = 45;
    public static int WINDOW = 6; // from how many positions out from E1,E2 do
                                  // we extract features?

    // true if want to extract about proper nouns only
    public static boolean ARG1_IS_PROPER = false;

    public static boolean DISCARD_PRONOUNS = true;

    // only process this many ARG1s relative to start of sentence
    public static int MAX_NP_POSITION = 3;

    // include part-of-speech-info in output
    public static boolean PRINT_POS = true;
}