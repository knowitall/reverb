package edu.washington.cs.knowitall.argumentidentifier;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import edu.washington.cs.knowitall.extractor.conf.ConfidenceFunction;
import edu.washington.cs.knowitall.extractor.conf.ConfidenceFunctionException;
import edu.washington.cs.knowitall.extractor.conf.classifier.LogisticRegression;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;

/***
 * An extraction confidence function that uses a logistic regression classifier.
 * It assigns an extraction a real valued number between 0 and 1 according to
 * the logistic regression model.
 *
 * @author janara
 *
 */

public class ConfidenceMetric implements ConfidenceFunction {
    private static URL MODEL_URL =
            ConfidenceMetric.class.getResource("/r2a2-conf.weights");

    private final LogisticRegression<ChunkedBinaryExtraction> logreg;

    public ConfidenceMetric() throws IOException {
        this(MODEL_URL);
    }

    public ConfidenceMetric(LogisticRegression<ChunkedBinaryExtraction> logreg) throws IOException {
        this.logreg = logreg;
    }

    public ConfidenceMetric(URL url) throws IOException {
        if (url == null) {
            throw new IllegalArgumentException("url cannot be null");
        }

        InputStream in = new BufferedInputStream(url.openStream());
        try {
            this.logreg = new LogisticRegression<ChunkedBinaryExtraction>(new ArgLearnerFeatureSet(), in);
        }
        finally {
            in.close();
        }
    }

    @Override
    public double getConf(ChunkedBinaryExtraction extr)
            throws ConfidenceFunctionException {
        return logreg.confidence(extr);
    }
}
