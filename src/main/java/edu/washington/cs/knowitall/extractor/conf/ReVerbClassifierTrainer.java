package edu.washington.cs.knowitall.extractor.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import edu.washington.cs.knowitall.extractor.conf.featureset.BooleanFeatureSet;
import edu.washington.cs.knowitall.extractor.conf.weka.WekaDataSet;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;
import weka.classifiers.functions.Logistic;
import weka.core.SerializationHelper;
import weka.core.converters.ArffSaver;

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
public class ReVerbClassifierTrainer {

    private BooleanFeatureSet<ChunkedBinaryExtraction> featureSet;
    private Logistic classifier;
    private WekaDataSet<ChunkedBinaryExtraction> dataSet;

    private ReVerbClassifierTrainer() {

        ReVerbFeatures feats = new ReVerbFeatures();
        featureSet = feats.getFeatureSet();
    }

    /**
     * Constructs and trains a new Logistic classifier using the given examples.
     *
     * @param examples
     * @throws Exception
     */
    public ReVerbClassifierTrainer(Iterable<LabeledBinaryExtraction> examples)
            throws Exception {

        this();

        dataSet = new WekaDataSet<ChunkedBinaryExtraction>("train", featureSet);
        loadDataSet(examples);

        train();
    }

    /**
     * Constructs and trains a new Logistic classifier using a previously
     * existing data set (e.g. from an existing confidence function), and
     * additional given examples.
     *
     * @param examples
     * @throws Exception
     */
    public ReVerbClassifierTrainer(
            WekaDataSet<ChunkedBinaryExtraction> existingDataSet,
            Iterable<LabeledBinaryExtraction> examples) throws Exception {

        this();

        dataSet = existingDataSet;
        loadDataSet(examples);

        train();
    }

    /**
     * @return the data set used to train the classifier
     */
    public WekaDataSet<ChunkedBinaryExtraction> getDataSet() {
        return dataSet;
    }

    /**
     * @return the trained classifier.
     */
    public Logistic getClassifier() {
        return classifier;
    }

    private void loadDataSet(Iterable<LabeledBinaryExtraction> examples) {

        for (LabeledBinaryExtraction extr : examples) {
            int label = extr.isPositive() ? 1 : 0;
            dataSet.addInstance(extr, label);
        }
    }

    private void train() throws Exception {
        classifier = new Logistic();
        classifier.buildClassifier(dataSet.getWekaInstances());
    }

    /**
     * Trains a logistic regression classifier using the examples in the given
     * file, and saves the model to disk. The examples must be in the format
     * described in <code>LabeledBinaryExtractionReader</code>.
     *
     * An optional third parameter can be passed that writes the training data
     * in Weka's ARFF file format to disk.
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
        ReVerbClassifierTrainer trainer = new ReVerbClassifierTrainer(
                reader.readExtractions());
        Logistic classifier = trainer.getClassifier();
        SerializationHelper.write(args[1], classifier);

        if (args.length > 2) {
            ArffSaver saver = new ArffSaver();
            saver.setInstances(trainer.getDataSet().getWekaInstances());
            saver.setFile(new File(args[2]));
            saver.writeBatch();
        }
    }
}
