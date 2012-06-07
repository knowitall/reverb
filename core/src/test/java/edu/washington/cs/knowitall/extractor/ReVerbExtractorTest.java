package edu.washington.cs.knowitall.extractor;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;

import edu.washington.cs.knowitall.extractor.ReVerbExtractor;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;

public class ReVerbExtractorTest  {

    private static ReVerbExtractor reverb;
    private static ReVerbExtractor regReverb;
    private static ReVerbExtractor relaxedReverb;
    private static HashSet<String> expected, got;
    
    
    @Before
    public void setUp() throws Exception {
    	if(regReverb == null) {
        	regReverb = new ReVerbExtractor();
        }
        if (relaxedReverb == null) {
        	relaxedReverb = new ReVerbExtractor(0, false, false, true);
        }
        expected = new HashSet<String>();
    }

    private static List<ChunkedBinaryExtraction> extract(ChunkedSentence sent) throws Exception {
        ArrayList<ChunkedBinaryExtraction> results = new ArrayList<ChunkedBinaryExtraction>();
        Iterables.addAll(results, reverb.extract(sent));
        return results;
    }

    private static HashSet<String> extractRels(String ts, String ps, String cs) throws Exception {
        List<ChunkedBinaryExtraction> extrs = extract(asSentence(ts, ps, cs));
        HashSet<String> results = new HashSet<String>();
        for (ChunkedBinaryExtraction extr : extrs) {
            results.add(extr.getRelation().toString());
        }
        return results;
    }

    private static HashSet<String> extractTriples(String ts, String ps, String cs) throws Exception {
        List<ChunkedBinaryExtraction> extrs = extract(asSentence(ts, ps, cs));
        HashSet<String> results = new HashSet<String>();
        for (ChunkedBinaryExtraction extr : extrs) {
            results.add("("+ extr.getArgument1() + ", " + extr.getRelation() + ", " + extr.getArgument2() + ")");
        }
        return results;
    }

    public static ChunkedSentence asSentence(String tokensStr, String posTagsStr, String npChunkTagsStr) throws Exception {
        String[] tokens = tokensStr.split(" ");
        String[] posTags = posTagsStr.split(" ");
        String[] npChunkTags = npChunkTagsStr.split(" ");
        return new ChunkedSentence(tokens, posTags, npChunkTags);
    }

    @Test
    public void testExtract1() throws Exception {
        reverb = regReverb;
    	got = extractRels(
                "He brought forward the idea of independence from Britain .",
                "PRP VBD RB DT NN IN NN IN NNP .",
                "B-NP O O B-NP B-NP O B-NP O B-NP O"
        );
        expected.add("brought forward the idea of");
        assertEquals(expected, got);
    }

    @Test
    /**
     * No change with no filtering.
     * @throws Exception
     */
    public void testNoFiltersExtract1() throws Exception {
        reverb = regReverb;
    	got = extractRels(
                "He brought forward the idea of independence from Britain .",
                "PRP VBD RB DT NN IN NN IN NNP .",
                "B-NP O O B-NP B-NP O B-NP O B-NP O"
        );
        expected.add("brought forward the idea of");
        assertEquals(expected, got);
        
    }
    
    @Test
    public void testExtract2() throws Exception {
    	reverb = regReverb;	
        got = extractRels(
                "Five years ago , I was working for just over minimum wage creating PDF forms for a small company .",
                "CD NNS RB , PRP VBD VBG IN RB IN JJ NN VBG NNP NNS IN DT JJ NN .",
                "B-NP I-NP O O B-NP O O O O O B-NP I-NP O B-NP I-NP O B-NP I-NP I-NP O"
        );
        expected.add("was working for just over");
        assertEquals(expected, got);
    }
    
    @Test
    /**
     * No change with no filtering.
     * @throws Exception
     */
    public void testNoFiltersExtract2() throws Exception {
    	reverb = regReverb;	
        got = extractRels(
                "Five years ago , I was working for just over minimum wage creating PDF forms for a small company .",
                "CD NNS RB , PRP VBD VBG IN RB IN JJ NN VBG NNP NNS IN DT JJ NN .",
                "B-NP I-NP O O B-NP O O O O O B-NP I-NP O B-NP I-NP O B-NP I-NP I-NP O"
        );
        expected.add("was working for just over");
        assertEquals(expected, got);
    }
    @Test
    public void testExtract3() throws Exception {
    	
    	reverb = regReverb;	
        got = extractRels(
                "They simply open a web browser and listen as their screen reader reads the newspaper to them , " +
                "and they do it when they want to and as soon as the content is published .",
                "PRP RB VB DT NN NN CC VB IN PRP$ NN NN VBZ DT NN TO PRP , CC PRP VBP PRP WRB PRP VBP TO CC RB " +
                "RB IN DT NN VBZ VBN .",
                "B-NP O O B-NP I-NP I-NP O O O B-NP I-NP I-NP O B-NP I-NP O B-NP O O B-NP O B-NP O B-NP O O O O O O B-NP " +
                "I-NP O O O"
        );
        expected.add("simply open");
        expected.add("listen as");
        expected.add("reads the newspaper to");
        expected.add("do");
        assertEquals(expected, got);
    }

    @Test
    /**
     * No filtering retains overlapping relations, relations with TO, and allows unary relations.
     * @throws Exception
     */
    public void testNoFiltersExtract3() throws Exception {
    	reverb = relaxedReverb;	
        got = extractRels(
                "They simply open a web browser and listen as their screen reader reads the newspaper to them , " +
                "and they do it when they want to and as soon as the content is published .",
                "PRP RB VB DT NN NN CC VB IN PRP$ NN NN VBZ DT NN TO PRP , CC PRP VBP PRP WRB PRP VBP TO CC RB " +
                "RB IN DT NN VBZ VBN .",
                "B-NP O O B-NP I-NP I-NP O O O B-NP I-NP I-NP O B-NP I-NP O B-NP O O B-NP O B-NP O B-NP O O O O O O B-NP " +
                "I-NP O O O"
        );

        expected.add("simply open");
        expected.add("listen as");
        expected.add("reads");//overlapping relation retained.
        expected.add("reads the newspaper to");
        expected.add("do");
        expected.add("want to");//relation with TO retained.
        expected.add("is published");//unary relation added.
        assertEquals(expected, got);
    }
    
    @Test
    public void testExtract4() throws Exception {
    	reverb = regReverb;	
        got = extractRels(
                "This chassis supports up to six fans , has a complete black interior , and has plenty of higher end features packed into a small case .",
                "DT NN VBZ RP TO CD NNS , VBZ DT JJ JJ NN , CC VBZ RB IN JJR NN NNS VBN IN DT JJ NN .",
                "B-NP I-NP O O O B-NP I-NP O O B-NP I-NP I-NP I-NP O O O O O B-NP I-NP I-NP O O B-NP I-NP I-NP O"
        );
        expected.add("supports up to");
        expected.add("has plenty of");
        expected.add("has");
        assertEquals(expected, got);
    }

    @Test
    /**
     * No filtering adds relations that contain a VBN.
     * @throws Exception
     */
    public void testNoFiltersExtract4() throws Exception {
    	reverb = relaxedReverb;	
        got = extractRels(
                "This chassis supports up to six fans , has a complete black interior , and has plenty of higher end features packed into a small case .",
                "DT NN VBZ RP TO CD NNS , VBZ DT JJ JJ NN , CC VBZ RB IN JJR NN NNS VBN IN DT JJ NN .",
                "B-NP I-NP O O O B-NP I-NP O O B-NP I-NP I-NP I-NP O O O O O B-NP I-NP I-NP O O B-NP I-NP I-NP O"
        );
        expected.add("supports up to");
        expected.add("packed into");//adds relation containing VBN.
        expected.add("has plenty of");
        expected.add("has");
        assertEquals(expected, got);
    }

    @Test
    public void testExtract5() throws Exception {
    	reverb = regReverb;	
        got = extractRels(
                "The key schedule for decryption is similar - the subkeys are in reverse order compared to encryption .",
                "DT JJ NN IN NN VBZ JJ : DT NNS VBP IN NN NN VBN TO NN .",
                "B-NP I-NP I-NP O B-NP O O O B-NP I-NP O O B-NP I-NP O O B-NP O"
        );
        expected.add("are in");
        assertEquals(expected, got);
    }

    @Test
    /**
     * No filtering adds relations that contain TO, and unary relations.
     * @throws Exception
     */
    public void testNoFiltersExtract5() throws Exception {
    	reverb = new ReVerbExtractor(0, false, true, true);//relaxedReverb;	
        got = extractRels(
                "The key schedule for decryption is similar - the subkeys are in reverse order compared to encryption .",
                "DT JJ NN IN NN VBZ JJ : DT NNS VBP IN NN NN VBN TO NN .",
                "B-NP I-NP I-NP O B-NP O O O B-NP I-NP O O B-NP I-NP O O B-NP O"
        );
        expected.add("is");//unary relation.
        expected.add("compared to");//retains relation with TO
        expected.add("are in");
        assertEquals(expected, got);
    }
    
    @Test
    public void testExtract6() throws Exception {
    	reverb = regReverb;	
        got = extractRels(
                "In over-the-counter trading yesterday , Benjamin Franklin rose 25 cents to $ 4.25 .",
                "IN JJ NN NN , NNP NNP VBD CD NNS TO $ CD .",
                "O B-NP I-NP B-NP O B-NP I-NP O B-NP I-NP O B-NP I-NP O"
        );
        expected.add("rose 25 cents to");
        assertEquals(expected, got);
    }
    
    @Test
    /**
     * No filtering adds overlapping relation.
     * @throws Exception
     */
    public void testNoFiltersExtract6() throws Exception {
    	reverb = relaxedReverb;	
        got = extractRels(
                "In over-the-counter trading yesterday , Benjamin Franklin rose 25 cents to $ 4.25 .",
                "IN JJ NN NN , NNP NNP VBD CD NNS TO $ CD .",
                "O B-NP I-NP B-NP O B-NP I-NP O B-NP I-NP O B-NP I-NP O"
        );
        expected.add("rose 25 cents to");
        expected.add("rose");//overlapping relation
        assertEquals(expected, got);
    }

    @Test
    public void testExtract7() throws Exception {
    	reverb = regReverb;	
        got = extractTriples(
                "Preliminary research at Jiwa found that the temple was built between the fifth and sixth centuries .",
                "JJ NN IN NNP VBD IN DT NN VBD VBN IN DT JJ CC JJ NNS .", 
                "B-NP I-NP O B-NP O O B-NP I-NP O O O B-NP I-NP I-NP I-NP I-NP O"
        );
        expected.add("(the temple, was built between, the fifth and sixth centuries)");
        assertEquals(expected, got);
    }

    @Test
    /**
     * No filtering allows relations with lexical stopword "that".
     * @throws Exception
     */
    public void testNoFiltersExtract7() throws Exception {
    	reverb = relaxedReverb;	
        got = extractTriples(
                "Preliminary research at Jiwa found that the temple was built between the fifth and sixth centuries .",
                "JJ NN IN NNP VBD IN DT NN VBD VBN IN DT JJ CC JJ NNS .", 
                "B-NP I-NP O B-NP O O B-NP I-NP O O O B-NP I-NP I-NP I-NP I-NP O"
        );
        expected.add("(Jiwa, found that, the temple)");
        expected.add("(the temple, was built between, the fifth and sixth centuries)");
        assertEquals(expected, got);
    }
    
    @Test
    public void testExtract8() throws Exception {
    	reverb = regReverb;	
        got = extractRels(
                "Property Capital Trust dropped its plan to liquidate because it was n't able to realize the value it had expected .",
                "NNP NNP NNP VBD PRP$ NN TO VB IN PRP VBD RB JJ TO VB DT NN PRP VBD VBN .",
        "B-NP I-NP I-NP O B-NP I-NP O O O B-NP O O O O O B-NP I-NP B-NP O O O");
        expected.add("was n't able to realize");
        expected.add("dropped");
        assertEquals(expected, got);
    }

    @Test
    /**
     * No filtering adds over-specified relations, relations with lexical stopword "because", relations with VBD and retains smaller overlapping relations.
     * @throws Exception
     */
    public void testNoFiltersExtract8() throws Exception {
    	reverb = relaxedReverb;	
        got = extractRels(
                "Property Capital Trust dropped its plan to liquidate because it was n't able to realize the value it had expected .",
                "NNP NNP NNP VBD PRP$ NN TO VB IN PRP VBD RB JJ TO VB DT NN PRP VBD VBN .",
        "B-NP I-NP I-NP O B-NP I-NP O O O B-NP O O O O O B-NP I-NP B-NP O O O");
        expected.add("liquidate because");//relation with lexical stop word "because" is allowed.
        expected.add("realize");
        expected.add("had expected"); //relation with VBN is allowed.
        expected.add("was n't able to realize");
        expected.add("was n't");//overlapping smaller relation not merged.
        expected.add("dropped");
        expected.add("dropped its plan to liquidate because"); // over-specified relation with no support.
        assertEquals(expected, got);
    }
    
    @Test
    public void testExtract9() throws Exception {
    	reverb = regReverb;	
        got = extractRels(
                "Remote servers can serve up files via SMB.",
                "VB NNS MD VB RP NNS IN NNP",
                "B-NP I-NP O O O B-NP O B-NP"
        );
        expected.add("can serve up");
        assertEquals(expected, got);
    }
    @Test
    /**
     * No filtering adds unary relations, and over-specified relations that have little or no support in a large corpus.
     * @throws Exception
     */
    public void testNoFiltersExtract9() throws Exception {
    	reverb = relaxedReverb;	
        got = extractRels(
                "Remote servers can serve up files via SMB.",
                "VB NNS MD VB RP NNS IN NNP",
                "B-NP I-NP O O O B-NP O B-NP"
        );
        expected.add("can serve up files via"); // overspecified relation with no support added.
        expected.add("Remote"); //unary relation added.
        expected.add("can serve up");
        assertEquals(expected, got);
    }

    @Test
    public void testExtract10() throws Exception {
    	reverb = regReverb;	
        got = extractRels(
                "Spaghetti Westerns were a name given to low-budget Western films , which were made by Italian movie companies in the 1960s .", 
                "NNP NNP VBD DT NN VBN TO JJ JJ NNS , WDT VBD VBN IN JJ NN NNS IN DT NNS .", 
                "B-NP I-NP B-VP B-NP I-NP B-VP B-PP B-NP I-NP I-NP O B-NP B-VP I-VP B-PP B-NP I-NP I-NP B-PP B-NP I-NP O"
        );
        expected.add("were made by");
        expected.add("were a name given to");
        assertEquals(expected, got);
    }

    @Test
    /**
     * No filtering does not modify the extraction of relations from relative clauses.
     * No filtering however, adds relations that begin with VBN and allows relations that contain TO.
     * @throws Exception
     */
    public void testNoFiltersExtract10() throws Exception {
    	reverb = relaxedReverb;	
        got = extractRels(
                "Spaghetti Westerns were a name given to low-budget Western films , which were made by Italian movie companies in the 1960s .", 
                "NNP NNP VBD DT NN VBN TO JJ JJ NNS , WDT VBD VBN IN JJ NN NNS IN DT NNS .", 
                "B-NP I-NP B-VP B-NP I-NP B-VP B-PP B-NP I-NP I-NP O B-NP B-VP I-VP B-PP B-NP I-NP I-NP B-PP B-NP I-NP O"
        );
        expected.add("were"); //relations that begin with VBN are allowed.
        expected.add("given to"); //relations that contain TO are allowed.
        expected.add("were made by"); 
        expected.add("were a name given to");
        assertEquals(expected, got);
    }
    
    @Test
    /**
     * 
     * @throws Exception
     */
    public void testExtract11() throws Exception {
    	reverb = regReverb;	
        got = extractRels(
                "XDH gene mutation is the underlying cause of classical xanthinuria .", 
                "NNP NN NN VBZ DT VBG NN IN JJ NN .", 
                "B-NP I-NP I-NP B-VP B-NP I-NP I-NP B-PP B-NP I-NP O"     
        );
        expected.add("is the underlying cause of");
        assertEquals(expected, got);
    }

    @Test
    public void testNoFiltersExtract11() throws Exception {
    	reverb = relaxedReverb;	
        got = extractRels(
                "XDH gene mutation is the underlying cause of classical xanthinuria .", 
                "NNP NN NN VBZ DT VBG NN IN JJ NN .", 
                "B-NP I-NP I-NP B-VP B-NP I-NP I-NP B-PP B-NP I-NP O"     
        );
       
        expected.add("is");//overlapping relations are not merged.
        expected.add("underlying");//relation beginning with a VBG is  retained.
        expected.add("is the underlying cause of");//same as no filter case.       
        assertEquals(expected, got);
    }

    @Test
    public void testExtract12() throws Exception {
    	reverb = regReverb;	
        got = extractRels(
                "He is the author of FooBar and is the capital of Spain .",
                "PRP VBZ DT NN IN NNP CC VBZ DT NN IN NNP .",
                "B-NP O O B-NP I-NP I-NP O O B-NP I-NP I-NP I-NP O"
        );
        expected.add("is the author of");
        expected.add("is the capital of");
        assertEquals(expected, got);
    }

    @Test
    /**
     * The extraction of relations in co-ordinate clauses is unchanged. 
     * However, noFilters adds an overlapping relation which are not merged.
     * @throws Exception
     */
    public void testNoFiltersExtract12() throws Exception {
    	reverb = relaxedReverb;
    	got = extractRels(
                "He is the author of FooBar and is the capital of Spain .",
                "PRP VBZ DT NN IN NNP CC VBZ DT NN IN NNP .",
                "B-NP O O B-NP I-NP I-NP O O B-NP I-NP I-NP I-NP O"
        );
    	expected.add("is");
        expected.add("is the author of");
        expected.add("is the capital of");
        assertEquals(expected, got);        
    }

    
    @Test
    /**
     * Relations whose previous tag is an existential (EX) are not allowed.
     * @throws Exception
     */
    public void testExtract13() throws Exception {
    	reverb = regReverb;	
        got = extractRels(
                "There are five types of food .",
                "EX VBP CD NNS IN NN .",
                "B-NP O B-NP I-NP I-NP I-NP O"
        );
        
        assertEquals(expected, got);
    }
    
    @Test
    /**
     * With noFilters, relations whose previous tag is an existential (EX) are also allowed.
     * @throws Exception
     */
    public void testNoFiltersExtract13() throws Exception {
    	reverb = relaxedReverb;
    	got = extractRels(
                "There are five types of food .",
                "EX VBP CD NNS IN NN .",
                "B-NP O B-NP I-NP I-NP I-NP O"
        );
    	expected.add("are");
    	expected.add("are five types of");
    	assertEquals(expected, got);
        
    }

    @Test
    public void testExtract14() throws Exception {
    	reverb = regReverb;	
        got = extractTriples(
                "Fluoropolymer resin , which was discovered by Plunkett , is a type of resin .",
                "NNP NN , WDT VBD VBN IN NNP , VBZ DT NN IN NN .",
                "B-NP I-NP O B-NP O O O B-NP O O B-NP I-NP I-NP I-NP O"
        );
        String extr = "(Fluoropolymer resin, was discovered by, Plunkett)";
        assertTrue(got.contains(extr));
        extr = "(Fluoropolymer resin, is, a type of resin)";
        assertFalse(got.contains(extr));
    }

    @Test
    /**
     * No filtering does not alter the dropping of "wh" determiners.
     * However, no filtering adds overlapping relations.
     * @throws Exception
     */
    public void testNoFiltersExtract14() throws Exception {
        reverb = relaxedReverb;
    	got = extractTriples(
                "Fluoropolymer resin , which was discovered by Plunkett , is a type of resin .",
                "NNP NN , WDT VBD VBN IN NNP , VBZ DT NN IN NN .",
                "B-NP I-NP O B-NP O O O B-NP O O B-NP I-NP I-NP I-NP O"
        );
        String extr = "(Fluoropolymer resin, was discovered by, Plunkett)";
        assertTrue(got.contains(extr));
        extr = "(Fluoropolymer resin, is, a type of resin)";
        assertTrue(got.contains(extr));
    }

    
    @Test
    public void testExtractOneChar() throws Exception {
        // This is a real example from Wikipedia text.
    	reverb = regReverb;	
        got = extractTriples(
            ". ^ a b c d e f g h i j k l m Clinton , Bill .",
            ". IN DT NN NN VBN NN NN NN NN IN NNS VBP JJ NN NNP , NNP .",
            "O B-PP B-NP I-NP I-NP I-NP I-NP I-NP I-NP I-NP B-PP B-NP B-VP B-NP I-NP I-NP O B-NP O"
        );
        assertEquals(0, got.size());
    }
    
    
    @Test
    public void testRelPronounError() throws Exception {
        /*
         * Relations should not contain pronouns.
         */
    	reverb = regReverb;
        got = extractTriples(
            "I picked it up at noon .",
            "PRP VBD PRP RP IN NN .",
            "B-NP O B-NP O O B-NP O"
        );
        assertFalse(got.contains("(I, picked it up at, noon)"));
    }
    
    @Test
    /*
     * With no filtering, relations can contain pronouns.
     */
    public void testNoFilterRelPronounError() throws Exception {
        
    	reverb = relaxedReverb;
        got = extractTriples(
            "I picked it up at noon .",
            "PRP VBD PRP RP IN NN .",
            "B-NP O B-NP O O B-NP O"
        );
        
        assertTrue(got.contains("(I, picked it up at, noon)"));
        assertTrue(got.contains("(I, picked, it)"));
        		
    }
    
    @Test
    /**
     * 
     * @throws Exception
     */
    public void testReflexivePronounArg1() throws Exception {
    	reverb = regReverb;	
        got = extractTriples(
            "Edison himself invented the phonograph .",
            "NNP PRP VBD DT NN .",
            "B-NP B-NP O B-NP I-NP O"
        );
        assertFalse(got.contains("(himself, invented, the phonograph)"));
        assertTrue(got.contains("(Edison, invented, the phonograph)"));
    }

    @Test
    /**
     * Relations should include the noun and not the reflexive pronoun.
     * No filtering should not affect this behavior.
     * Question: Should the said text be quoted for this test? 
     * @throws Exception
     */
    public void testNoFilterReflexivePronounArg1() throws Exception {
        reverb = relaxedReverb;
    	got = extractTriples(
            "Edison himself invented the phonograph , he himself said .",
            "NNP PRP VBD DT NN O PRP PRP VBD .",
            "B-NP B-NP O B-NP I-NP O B-NP B-NP O O"
        );
    	assertFalse(got.contains("(himself, invented, the phonograph)"));
        assertTrue(got.contains("(Edison, invented, the phonograph)"));
    }

    @Test
    public void testUnaryCases() throws Exception{
        reverb = relaxedReverb;
        got = extractTriples("people on earth slow down",
                "NNS IN NN VB RP",
                "B-NP B-PP B-NP B-VP B-PP");
        assertTrue(got.contains("(earth, slow down, )"));

        got = extractTriples("It rained",
                "PRP VBD",
                "B-NP B-VP");
        assertTrue(got.contains("(It, rained, )"));

    }

}
