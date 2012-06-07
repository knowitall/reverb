package edu.washington.cs.knowitall.sequence;


import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.sequence.BIOLayeredSequence;

public class BIOLayeredSequenceTest {
    
    
    public List<String> split(String s) {
        String[] tokens = s.split(" ");
        ArrayList<String> results = new ArrayList<String>(tokens.length);
        for (String t : tokens) results.add(t);
        return results;
    }

    @Test
    public void test1() throws SequenceException {
        List<String> input = split("O B-X I-X O O B-Y I-Y O");
        BIOLayeredSequence seq = new BIOLayeredSequence(input.size());
        seq.addSpanLayer("x", input);
        
        List<Range> ranges = seq.getSpans("x");
        assertEquals(2, ranges.size());
        assertEquals(new Range(1,2), ranges.get(0));
        assertEquals(new Range(5,2), ranges.get(1));
        
        ranges = new ArrayList<Range>(seq.getSpans("x", "X"));
        assertEquals(1, ranges.size());
        assertEquals(new Range(1,2), ranges.get(0));
        
        ranges = new ArrayList<Range>(seq.getSpans("x", "Y"));
        assertEquals(1, ranges.size());
        assertEquals(new Range(5,2), ranges.get(0));
        
        ranges = new ArrayList<Range>(seq.getSpans("x", "Z"));
        assertEquals(0, ranges.size());
        
    }
    
    @Test
    public void test2() throws SequenceException {
        List<String> input = split("O I-X I-X O O");
        BIOLayeredSequence seq = new BIOLayeredSequence(input.size());
        seq.addSpanLayer("x", input); 
        assertEquals(0, seq.getSpans("x").size());
        
        input = split("B-X I-Y B-X");
        seq = new BIOLayeredSequence(input.size());
        seq.addSpanLayer("x", input);
        assertEquals(2, seq.getSpans("x").size());
        
        input = split("B-X I-Y I-X");
        seq = new BIOLayeredSequence(input.size());
        seq.addSpanLayer("x", input);
        assertEquals(1, seq.getSpans("x").size());
        
    }
    
    @Test
    public void test3() throws SequenceException {
        List<String> input = split("O B-X I-X O");
        BIOLayeredSequence seq = new BIOLayeredSequence(input.size());
        seq.addLayer("layer1", input);
        seq.addSpanLayer("layer2", input);
        assertEquals(0, seq.getSpans("layer1").size());
        assertEquals(1, seq.getSpans("layer2").size());
    }
    
    @Test
    public void test4() throws SequenceException {
        BIOLayeredSequence seq = new BIOLayeredSequence(5);
        List<Range> ranges = new ArrayList<Range>();
        ranges.add(new Range(1,2));
        ranges.add(new Range(3,1));
        seq.addSpanLayerRanges("layer", "X", ranges);
        assertEquals(2, seq.getSpans("layer").size());
        List<String> expected = split("O B-X I-X B-X O");
        assertEquals(expected, seq.getLayer("layer"));
    }
    
    @Test(expected=IndexOutOfBoundsException.class)
    public void test5() throws SequenceException {
        BIOLayeredSequence seq = new BIOLayeredSequence(5);
        List<Range> ranges = new ArrayList<Range>();
        ranges.add(new Range(1,10));
        seq.addSpanLayerRanges("layer", "X", ranges);
    }
    
    @Test(expected=SequenceException.class)
    public void test6() throws SequenceException {
        BIOLayeredSequence seq = new BIOLayeredSequence(5);
        List<Range> ranges = new ArrayList<Range>();
        ranges.add(new Range(1,2));
        ranges.add(new Range(2,1));
        seq.addSpanLayerRanges("layer", "X", ranges);
    }

    @Test
    public void test7() throws SequenceException {
        BIOLayeredSequence seq = new BIOLayeredSequence(5);
        List<Range> ranges = new ArrayList<Range>();
        ranges.add(new Range(0,2));
        ranges.add(new Range(3,2));
        seq.addSpanLayerRanges("layer", "X", ranges);
        List<String> expected = split("B-X I-X O B-X I-X");
        assertEquals(expected, seq.getLayer("layer"));
    }
    
    @Test
    public void test8() throws SequenceException {
        BIOLayeredSequence seq = new BIOLayeredSequence(7);
        List<String> input = split("B-X I-X O B-Y I-Y O B-Z");
        seq.addSpanLayer("layer", input);
        BIOLayeredSequence sub = seq.getSubSequence(0, 2);
        assertEquals(2, sub.getLength());
        assertEquals(1, sub.getSpans("layer").size());
        assertEquals(new Range(0,2), sub.getSpans("layer").get(0));
        
        sub = seq.getSubSequence(1, 2);
        assertEquals(2, sub.getLength());
        assertEquals(1, sub.getSpans("layer").size());
        assertEquals(new Range(0,1), sub.getSpans("layer").get(0));
        
        sub = seq.getSubSequence(1, 6);
        assertEquals(6, sub.getLength());
        assertEquals(3, sub.getSpans("layer").size());
        assertEquals(1, sub.getSpans("layer", "X").size());
        assertEquals(1, sub.getSpans("layer", "Y").size());
        assertEquals(1, sub.getSpans("layer", "Z").size());
    }

}
