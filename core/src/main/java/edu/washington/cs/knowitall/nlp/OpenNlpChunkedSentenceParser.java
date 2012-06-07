package edu.washington.cs.knowitall.nlp;

import java.text.ParseException;
import java.util.ArrayList;

import edu.washington.cs.knowitall.sequence.SequenceException;

/**
 * <p>
 * A utility class for "parsing" the output of the OpenNLP command line chunker.
 * The command line chunker returns strings in this form:
 * </p>
 * <p align="center">
 * {@code [NP JFK/NNP] [VP was/VBD elected/VBN] [NP president/NN] [PP in/IN] [NP 1960/CD] ./.}
 * </p>
 * <p>
 * This class converts that String representation into a {@link ChunkedSentence}
 * object.
 * </p>
 *
 * @author afader
 *
 */
public class OpenNlpChunkedSentenceParser {

    private boolean attachOfs = true;
    private boolean attachPossessives = true;

    /**
     * @return true if this object will attach NPs beginning with "of" with the
     *         previous NP.
     */
    public boolean attachOfs() {
        return attachOfs;
    }

    /**
     * @return true if this object will attach NPs beginning with the tag POS
     *         with the previous NP.
     */
    public boolean attachPossessives() {
        return attachPossessives;
    }

    /**
     * @param attachOfs
     */
    public void attachOfs(boolean attachOfs) {
        this.attachOfs = attachOfs;
    }

    /**
     * @param attachPossessives
     */
    public void attachPossessives(boolean attachPossessives) {
        this.attachPossessives = attachPossessives;
    }

    /**
     * Converts sent into a {@link ChunkedSentence} object.
     *
     * @param sent
     * @return the chunked representation
     * @throws ParseException
     *             if sent is malformed.
     */
    public ChunkedSentence parseSentence(String sent) throws ParseException {

        // Spaces before square brackets goof things up for some reason
        sent = sent.replace(" ]", "]");

        ArrayList<String> tokensList = new ArrayList<String>();
        ArrayList<String> posTagsList = new ArrayList<String>();
        ArrayList<String> npChunkTagsList = new ArrayList<String>();

        String[] parts = sent.trim().split(" ");
        String currentChunk = null;
        boolean atChunkStart = false;
        for (String part : parts) {

            if (isChunk(part)) {
                currentChunk = getChunkType(part);
                atChunkStart = true;
            } else {
                String[] tokTag = getTokenTag(part);
                String token = tokTag[0];
                String posTag = tokTag[1];
                tokensList.add(token);
                posTagsList.add(posTag);
                if (currentChunk == null) {
                    npChunkTagsList.add("O");
                } else if (atChunkStart) {
                    npChunkTagsList.add("B-" + currentChunk);
                    atChunkStart = false;
                } else {
                    npChunkTagsList.add("I-" + currentChunk);
                }
                if (isEndChunk(part))
                    currentChunk = null;
            }

        }

        String[] tokens = tokensList.toArray(new String[0]);
        String[] posTags = posTagsList.toArray(new String[0]);
        String[] npChunkTags = npChunkTagsList.toArray(new String[0]);

        if (attachOfs) {
            OpenNlpUtils.attachOfs(tokens, npChunkTags);
        } else {
            OpenNlpUtils.detatchOfs(tokens, npChunkTags);
        }
        if (attachPossessives) {
            OpenNlpUtils.attachPossessives(posTags, npChunkTags);
        } else {
            OpenNlpUtils.detatchPossessives(posTags, npChunkTags);
        }

        try {
            return new ChunkedSentence(tokens, posTags, npChunkTags);
        } catch (SequenceException e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }

    private boolean isChunk(String part) {
        return part.startsWith("[");
    }

    private String getChunkType(String part) throws ParseException {
        if (part.length() >= 2) {
            return part.subSequence(1, part.length()).toString();
        } else {
            throw new ParseException("Couldn't parse part: " + part, 0);
        }
    }

    private boolean isEndChunk(String part) {
        return part.endsWith("]");
    }

    private String[] getTokenTag(String piece) throws ParseException {
        if (piece.endsWith("]")) {
            piece = piece.substring(0, piece.length() - 1);
        }
        int i = piece.lastIndexOf("/");
        if (i > 0) {
            String token = piece.substring(0, i);
            String posTag = piece.substring(i + 1);
            return new String[] { token, posTag };
        } else {
            throw new ParseException("Couldn't get token/tag: " + piece, 0);
        }
    }

}
