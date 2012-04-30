package edu.washington.cs.knowitall.extractor.mapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrdinalPreprocessor extends IndependentMapper<String> {

    private static final Pattern pattern = Pattern.compile("(nd|rd|th)-(most|least)");

    @Override
    public String doMap(String sent) {
        Matcher m = pattern.matcher(sent);
        String result = m.replaceAll("$1 $2");
        return result;
    }
}
