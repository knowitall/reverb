package edu.washington.cs.knowitall.normalization;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;
import edu.washington.cs.knowitall.sequence.SequenceException;

import java.io.ByteArrayInputStream;
import java.io.StringReader;

import uk.ac.susx.informatics.Morpha;

/**
 * Created by IntelliJ IDEA. User: niranjan Date: 11/26/11 Time: 11:09 AM To
 * change this template use File | Settings | File Templates.
 */
public class HeadNounExtractor {

    private static Morpha lexer;

    public HeadNounExtractor() {
        lexer = new Morpha(new ByteArrayInputStream("".getBytes()));
    }

    public NormalizedField normalizeField(ChunkedExtraction field) {
        int firstPos = -1;
        int secondPos = -1;
        int thirdPos = -1;
        int fourthPos = -1;
        for (int i = field.getLength() - 1; i >= 0; i--) {
            String tag = field.getPosTag(i);
            if (tag.equals("NN") || tag.equals("NNP") || tag.equals("NNPS")
                    || tag.equals("NNS") || tag.equals("NX")
                    || tag.equals("POS") || tag.equals("JJR")) {

                String token = field.getToken(i);
                String posTag = field.getPosTag(i);
                String norm = stem(token, posTag);
                String[] tokens = { norm };
                String[] posTags = { posTag };
                return new NormalizedField(field, tokens, posTags);

            }
            if (tag.equals("NP") && firstPos == -1) {
                firstPos = i;
            }

            if (secondPos == -1
                    && (tag.equals("$") || tag.equals("ADJP") || tag
                            .equals("PRN"))) {
                secondPos = i;
            }

            if (thirdPos == -1 && (tag.equals("CD"))) {
                thirdPos = i;
            }
            if (fourthPos == -1
                    && ((tag.equals("JJ") || tag.equals("JJS")
                            || tag.equals("RB") || tag.equals("QP")))) {
                fourthPos = i;
            }
        }

        int pos = -1;
        if (firstPos > -1) {
            pos = firstPos;
        } else if (secondPos > -1) {
            pos = secondPos;
        } else if (thirdPos > -1) {
            pos = thirdPos;
        } else if (fourthPos > -1) {
            pos = fourthPos;
        }

        if (pos > -1) {
            String token = field.getToken(pos);
            String posTag = field.getPosTag(pos);
            String norm = stem(token, posTag);
            String[] tokens = { norm };
            String[] posTags = { posTag };
            return new NormalizedField(field, tokens, posTags);
        }
        NormalizedField norm;
        try {
            norm = new NormalizedField(field, field.getTokens(),
                    field.getPosTags());
        } catch (SequenceException e) {
            String msg = String.format(
                    "tokens and posTags are not the same length for field %s",
                    field);
            throw new IllegalStateException(msg, e);
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
