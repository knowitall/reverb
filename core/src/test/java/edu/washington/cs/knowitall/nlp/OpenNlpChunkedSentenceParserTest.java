package edu.washington.cs.knowitall.nlp;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import com.google.common.collect.Iterables;

import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.OpenNlpChunkedSentenceParser;

public class OpenNlpChunkedSentenceParserTest {

    @Test
    public void testParseSentence() throws Exception {

        String sentStr = " [NP Numerous/JJ Palestinian/JJ churches/NNS] [VP are/VBP holding/VBG] [NP special/JJ prayers/NNS] [NP this/DT morning/NN] [PP for/IN] [NP all/PDT those/DT suffering/VBG and/CC mourning/VBG] [PP in/IN] [NP the/DT wake/NN] [PP of/IN] [NP this/DT unthinkable/JJ tragedy/NN] ./.";
        int expectedLength = 23;
        String[] expectedPos = "JJ JJ NNS VBP VBG JJ NNS DT NN IN PDT DT VBG CC VBG IN DT NN IN DT JJ NN .".split(" ");
        String[] expectedToks = "Numerous Palestinian churches are holding special prayers this morning for all those suffering and mourning in the wake of this unthinkable tragedy .".split(" ");
        ArrayList<Range> expectedRanges = new ArrayList<Range>();
        expectedRanges.add(new Range(0, 3)); // Numerouse Palestinian churches
        expectedRanges.add(new Range(5, 2)); // special prayers
        expectedRanges.add(new Range(7, 2)); // this morning
        expectedRanges.add(new Range(10, 5)); // all those suffering and mourning
        expectedRanges.add(new Range(16, 2)); // the wake
        expectedRanges.add(new Range(19, 3)); // this unthinkable tragedy

        OpenNlpChunkedSentenceParser reader = new OpenNlpChunkedSentenceParser();
        reader.attachOfs(false);
        ChunkedSentence sent = reader.parseSentence(sentStr);

        ArrayList<Range> gotRanges = new ArrayList<Range>();
        Iterables.addAll(gotRanges, sent.getNpChunkRanges());

        assertEquals(expectedLength, sent.getLength());
        assertArrayEquals(expectedPos, sent.getPosTags().toArray());
        assertArrayEquals(expectedToks, sent.getTokens().toArray());
        assertEquals(expectedRanges, gotRanges);


    }

}
