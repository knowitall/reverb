package edu.washington.cs.knowitall.extractor.conf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import weka.classifiers.functions.Logistic;
import weka.core.SerializationHelper;

public class PrintClassifier {
    public static void main(String[] args) throws FileNotFoundException, Exception {
        Logistic logistic = (Logistic)SerializationHelper.read(
                new FileInputStream(args[0]));
        System.out.println(logistic);
        double[][] doubles = logistic.coefficients();
        System.out.println(doubles);
        System.out.println(new ReVerbFeatures().getFeatureSet().getFeatureNames().size());
    }
}
