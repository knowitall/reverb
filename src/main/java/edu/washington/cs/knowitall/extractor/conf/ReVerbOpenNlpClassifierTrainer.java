package edu.washington.cs.knowitall.extractor.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import opennlp.maxent.GIS;
import opennlp.maxent.GISModel;
import opennlp.maxent.io.GISModelWriter;
import opennlp.maxent.io.SuffixSensitiveGISModelWriter;
import opennlp.model.ListEventStream;

import edu.washington.cs.knowitall.extractor.conf.featureset.BooleanFeatureSet;
import edu.washington.cs.knowitall.extractor.conf.opennlp.OpenNlpDataSet;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;

/***
 * Used to train the ReVerb confidence function using the features described by
 * <code>ReVerbFeatures</code>. Given a set of
 * <code>LabeledBinaryExtraction</code> instances, this class featurizes them
 * and trains a logistic regression classifier using Weka's
 * <code>Logistic</code> class.
 *
 * This class can be called from the command-line to train a classifier and save
 * the resulting model to a file.
 *
 * @author afader
 *
 */
public class ReVerbOpenNlpClassifierTrainer {

    private BooleanFeatureSet<ChunkedBinaryExtraction> featureSet;
    private GISModel model;
    private OpenNlpDataSet<ChunkedBinaryExtraction> dataSet;

    private ReVerbOpenNlpClassifierTrainer() {

        ReVerbFeatures feats = new ReVerbFeatures();
        featureSet = feats.getFeatureSet();
    }

    /**
     * Constructs and trains a new Logistic classifier using the given examples.
     *
     * @param examples
     * @throws Exception
     */
    public ReVerbOpenNlpClassifierTrainer(
            Iterable<LabeledBinaryExtraction> examples) throws Exception {

        this();

        dataSet = new OpenNlpDataSet<ChunkedBinaryExtraction>("train",
                featureSet);
        loadDataSet(examples);

        train();
    }

    /**
     * @return the trained classifier.
     */
    public GISModel getModel() {
        return model;
    }

    private void loadDataSet(Iterable<LabeledBinaryExtraction> examples) {

        for (LabeledBinaryExtraction extr : examples) {
            int label = extr.isPositive() ? 1 : 0;
            dataSet.addInstance(extr, label);
        }
    }

    private void train() throws Exception {
        this.model = GIS.trainModel(new ListEventStream(this.dataSet
                .getInstances()), 100, 0);
    }

    /**
     * Trains a logistic regression classifier using the examples in the given
     * file, and saves the model to disk. The examples must be in the format
     * described in <code>LabeledBinaryExtractionReader</code>.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err
                    .println("Usage: ReVerbClassifierTrainer examples model [features]\n");
            System.err
                    .println("    Trains the model used by ReVerbConfFunction on the given examples file and\n"
                            + "    writes them to the given model file. Optionally, will write out the \n"
                            + "    training data as a Weka ARFF file. The examples must be in the format\n"
                            + "    described in LabeledBinaryExtractionReader. The features used in the\n"
                            + "    classifier are described in ReVerbFeatures.\n");
            return;
        }
        InputStream in = new FileInputStream(args[0]);
        LabeledBinaryExtractionReader reader = new LabeledBinaryExtractionReader(
                in);
        ReVerbOpenNlpClassifierTrainer trainer = new ReVerbOpenNlpClassifierTrainer(
                reader.readExtractions());
        GISModel model = trainer.getModel();

        File outputFile = new File(args[1]);
        GISModelWriter writer = new SuffixSensitiveGISModelWriter(model, outputFile);
        writer.persist();
    }
}
