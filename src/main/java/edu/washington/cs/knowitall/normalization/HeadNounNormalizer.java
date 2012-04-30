package edu.washington.cs.knowitall.normalization;

import java.io.ByteArrayInputStream;
import java.io.StringReader;

import uk.ac.susx.informatics.Morpha;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;
import edu.washington.cs.knowitall.sequence.SequenceException;

/***
 * A normalizer function that heuristically finds the head noun phrase of an
 * argument field. It uses the following heuristic method:
 * <ul>
 * <li>If the field contains a proper noun, don't normalize</li>
 * <li>If the field contains a tag starting with N, return the rightmost one,
 * stemmed</li>
 * <li>Otherwise, don't normalize</li>
 * </ul>
 * This is a much simpler version of Magerman's rules for finding head words in
 * a syntactic parse tree.
 * 
 * @author afader
 * 
 */
public class HeadNounNormalizer implements FieldNormalizer {

    private static Morpha lexer;

    public HeadNounNormalizer() {
        lexer = new Morpha(new ByteArrayInputStream("".getBytes()));
    }

    @Override
    public NormalizedField normalizeField(ChunkedExtraction field) {

        boolean containsProperNoun = false;
        int lastNounIndex = -1;
        for (int i = 0; i < field.getLength(); i++) {
            String tag = field.getPosTag(i);
            if (tag.equals("NNP") || tag.equals("NNPS")) {
                containsProperNoun = true;
            }
            if (tag.startsWith("N")) {
                lastNounIndex = i;
            }
        }

        NormalizedField norm;
        if (containsProperNoun || lastNounIndex == -1) {
            try {
                norm = new NormalizedField(field, field.getTokens(),
                        field.getPosTags());
            } catch (SequenceException e) {
                String msg = String
                        .format("tokens and posTags are not the same length for field %s",
                                field);
                throw new IllegalStateException(msg, e);
            }
        } else {
            String token = field.getToken(lastNounIndex);
            String posTag = field.getPosTag(lastNounIndex);
            String normToken = stem(token, posTag);
            String[] tokens = { normToken };
            String[] posTags = { posTag };
            try {
                norm = new NormalizedField(field, tokens, posTags);
            } catch (SequenceException e) {
                String msg = String
                        .format("tokens and posTags are not the same length for field %s",
                                field);
                throw new IllegalStateException(msg, e);
            }
        }

        return norm;

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

}
