package edu.washington.cs.knowitall.normalization;

import java.io.ByteArrayInputStream;
import java.io.StringReader;

import uk.ac.susx.informatics.Morpha;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;

/**
 * A field normalizer that applies the stemmer to every token and strips
 * nothing.
 * 
 * @author Rob
 * 
 */
public class BasicFieldNormalizer implements FieldNormalizer {

    private Morpha lexer;

    public BasicFieldNormalizer() {

        lexer = new Morpha(new ByteArrayInputStream("".getBytes()));
    }

    @Override
    public NormalizedField normalizeField(ChunkedExtraction field) {

        String[] normTokens = new String[field.getLength()];

        for (int i = 0; i < field.getLength(); ++i) {

            normTokens[i] = stem(field.getToken(i), field.getPosTag(i));
        }

        return new NormalizedField(field, normTokens, field.getPosTags()
                .toArray(new String[normTokens.length]));
    }

    /**
     * A wrapper for the call to Morpha. If morpha returns null, token is
     * returned unchanged.
     * 
     * @param token
     * @param posTag
     * @return
     */
    private String stem(String token, String posTag) {
        token = token.toLowerCase();
        String wordTag = token + "_" + posTag;
        try {
            lexer.yyreset(new StringReader(wordTag));
            lexer.yybegin(Morpha.scan);
            String tokenNorm = lexer.next();
            if (tokenNorm == null) {
                return token;
            } else {
                return tokenNorm;
            }
        } catch (Throwable e) {
            return token;
        }
    }

    public String stemSingleToken(String token, String posTag) {

        return stem(token, posTag);
    }
}
