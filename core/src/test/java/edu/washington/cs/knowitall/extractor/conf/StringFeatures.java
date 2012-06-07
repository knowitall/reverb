package edu.washington.cs.knowitall.extractor.conf;

import java.util.HashMap;

import com.google.common.base.Predicate;

import edu.washington.cs.knowitall.extractor.conf.featureset.BooleanFeatureSet;
import edu.washington.cs.knowitall.extractor.conf.opennlp.OpenNlpDataSet;

public class StringFeatures {

    public static Predicate<String> containsE;
    public static Predicate<String> longerThan3;
    public static Predicate<String> startsWithJ;
    public static HashMap<String, Predicate<String>> allFeatures;
    public static BooleanFeatureSet<String> featureSet;
    public static OpenNlpDataSet<String> dataSet;

    static {

        containsE = new Predicate<String>() {
            public boolean apply(String s) {
                return s.contains("e");
            }
        };

        longerThan3 = new Predicate<String>() {
            public boolean apply(String s) {
                return s.length() > 3;
            }
        };

        startsWithJ = new Predicate<String>() {
            public boolean apply(String s) {
                return s.startsWith("j");
            }
        };

        allFeatures = new HashMap<String, Predicate<String>>();
        allFeatures.put("contains e", containsE);
        allFeatures.put("starts with j", startsWithJ);
        allFeatures.put("longer than 3", longerThan3);

        featureSet = new BooleanFeatureSet<String>(allFeatures);

        dataSet = new OpenNlpDataSet<String>("test", featureSet);
        dataSet.addInstance("of", 0); // 0,0,0
        dataSet.addInstance("eat", 0); // 1,0,0
        dataSet.addInstance("joe", 1); // 1,0,1
        dataSet.addInstance("jane", 1); // 1,1,1
    }
}
