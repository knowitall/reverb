package edu.washington.cs.knowitall.nlp.extraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;

/**
 * Some extractions that are used by other test cases.
 * @author afader
 *
 */
public abstract class TestExtractions {
    
    public static List<ChunkedSentence> sentences;
    public static List<ChunkedBinaryExtraction> extractions;
    
    
    static {
        
        sentences = new ArrayList<ChunkedSentence>();
        extractions = new ArrayList<ChunkedBinaryExtraction>();
        
        try {
        
            addSentExtr(
                    "Mike is the mayor of Seattle .",
                    "NNP VBZ DT NN IN NNP .",
                    "B-NP O B-NP I-NP O B-NP O",
                    0,1, // Mike
                    1,4, // is the mayor of
                    5,1,  // Seattle
                    0.5,
                    "doc1"
            );
            
            addSentExtr(
                    "He brought forward the idea of independence .",
                    "PRP VBD RB DT NN IN NN .",
                    "B-NP O O B-NP B-NP O B-NP O",
                    0,1, // He
                    1,5, // brought forward the idea of
                    6,1, // independence
                    0.9,
                    "doc1"
            );
            
            addSentExtr(
                    "XDH gene mutation is the underlying cause of classical xanthinuria .", 
                    "NNP NN NN VBZ DT VBG NN IN JJ NN .", 
                    "B-NP I-NP I-NP B-VP B-NP I-NP I-NP B-PP B-NP I-NP O",
                    0,3, // XDH gene mutation
                    3,5, // is the underlying cause of
                    8,2, // classical xanthinuria
                    0.3,
                    "doc2"
            );
            
            addSentExtr(
                    "Construction of the church began in 1900 .",
                    "NN IN DT NN VBD IN CD .",
                    "B-NP O B-NP I-NP O O B-NP O",
                    0,4, // Construction of the church
                    4,2, // began in
                    6,1, // 1900,
                    0.1,
                    "doc3"
            );
        
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    
    }
    
    private static void addSentExtr(String a, String b, String c, 
            int xs, int xl, int rs, int rl, int ys, int yl, double conf, 
            String docId) throws Exception {
        
        ChunkedSentence sent = toSent(a,b,c);
        sentences.add(sent);
        ChunkedBinaryExtraction extr = toExtr(sent, xs,xl, rs,rl, ys,yl);
        extr.setProperty("docId", docId);
        extr.setProperty("conf", Double.toString(conf));
        extractions.add(extr);
        
    }
    
    private static List<String> split(String s) {
        return Arrays.asList(s.split(" "));
    }
    
    private static ChunkedSentence toSent(String toks, String pos, 
            String chunks) throws Exception {
        return new ChunkedSentence(split(toks), split(pos), split(chunks));
    }
    
    private static ChunkedBinaryExtraction toExtr(ChunkedSentence sent, 
        int xs, int xl, int rs, int rl, int ys, int yl) {
        ChunkedExtraction rel = new ChunkedExtraction(sent, new Range(rs, rl));
        ChunkedArgumentExtraction x = new ChunkedArgumentExtraction(sent, 
                new Range(xs,xl), rel);
        ChunkedArgumentExtraction y = new ChunkedArgumentExtraction(sent, 
                new Range(ys,yl), rel);
        return new ChunkedBinaryExtraction(rel, x, y);
    }
    
    
}
