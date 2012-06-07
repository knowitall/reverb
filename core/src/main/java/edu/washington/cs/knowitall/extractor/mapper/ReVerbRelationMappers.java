package edu.washington.cs.knowitall.extractor.mapper;

import java.io.IOException;

import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;

/**
 * A list of mappers for <code>ReVerbExtractor</code>'s relations.
 *
 * @author afader
 *
 */
public class ReVerbRelationMappers extends MapperList<ChunkedExtraction> {

    /**
     * Default construction of ReVerbRelationMappers. Uses Lexical and Syntactic
     * constraints, merges overlapping relations, and requires a minimum of 20
     * distinct arguments for each relation on a large corpus.
     *
     * @throws IOException
     */
    public ReVerbRelationMappers() throws IOException {
        init();
    }

    public ReVerbRelationMappers(int minFreq) throws IOException {
        init(minFreq);
    }

    public ReVerbRelationMappers(int minFreq, boolean useLexSynConstraints,
            boolean mergeOverlapRels) throws IOException {
        init(minFreq, useLexSynConstraints, mergeOverlapRels);
    }

    private void init() throws IOException {

        // Add lexical and syntactic constraints on the relations.
        addLexicalAndSyntacticConstraints();

        // The relation should have a minimum number of distinct arguments in a
        // large corpus
        addMapper(new ReVerbRelationDictionaryFilter());

        // Overlapping relations should be merged together
        addMapper(new MergeOverlappingMapper());

    }

    private void init(int minFreq) throws IOException {

        // Add lexical and syntactic constraints on the relation.
        addLexicalAndSyntacticConstraints();

        // The relation should have a minimum number of distinct arguments in a
        // large corpus
        addMapper(new ReVerbRelationDictionaryFilter(minFreq));

        // Overlapping relations should be merged together
        addMapper(new MergeOverlappingMapper());

    }

    private void init(int minFreq, boolean useLexSynConstraints,
            boolean mergeOverlapRels) throws IOException {
        // Add lexical and syntactic constraints on the relation.
        if (useLexSynConstraints) {
            addLexicalAndSyntacticConstraints();
        }
        // The relation should have a minimum number of distinct arguments in a
        // large corpus
        if (minFreq > 0) {
            addMapper(new ReVerbRelationDictionaryFilter(minFreq));
        }
        // Overlapping relations should be merged together
        if (mergeOverlapRels) {
            addMapper(new MergeOverlappingMapper());

        }
    }

    private void addLexicalAndSyntacticConstraints() {
        /*
         * The relation shouldn't just be a single character. This usually
         * happens due to errors in the various NLP tools (sentence detector,
         * tokenizer, POS tagger, chunker).
         */
        addMapper(new FilterMapper<ChunkedExtraction>() {
            public boolean doFilter(ChunkedExtraction rel) {
                if (rel.getLength() == 1) {
                    return rel.getToken(0).length() > 1;
                } else {
                    return true;
                }
            }
        });

        // These pos tags and tokens cannot appear in the relation
        StopListFilter relStopList = new StopListFilter();
        relStopList.addStopPosTag("CC");
        relStopList.addStopPosTag(",");
        relStopList.addStopPosTag("PRP");
        relStopList.addStopToken("that");
        relStopList.addStopToken("if");
        relStopList.addStopToken("because");
        addMapper(relStopList);

        // The POS tag of the first verb in the relation cannot be VBG or VBN
        addMapper(new FilterMapper<ChunkedExtraction>() {
            public boolean doFilter(ChunkedExtraction rel) {
                ChunkedSentence sent = rel.getSentence();
                int start = rel.getStart();
                int length = rel.getLength();
                for (int i = start; i < start + length; i++) {
                    String posTag = sent.getPosTags().get(i);

                    if (posTag.startsWith("VB")) {
                        return !posTag.equals("VBG") && !posTag.equals("VBN");
                    }
                }
                return true;
            }
        });

        // The previous tag can't be an existential "there" or a TO
        addMapper(new FilterMapper<ChunkedExtraction>() {
            public boolean doFilter(ChunkedExtraction rel) {
                int s = rel.getStart();
                if (s == 0) {
                    return true;
                } else {
                    String posTag = rel.getSentence().getPosTag(s - 1);
                    return !posTag.equals("EX") && !posTag.equals("TO");
                }
            }
        });
    }
}
