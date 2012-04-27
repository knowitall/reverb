package edu.washington.cs.knowitall.argumentidentifier;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StringReader;
import java.util.List;
import java.util.regex.Pattern;

import cc.mallet.fst.CRF;
import cc.mallet.fst.confidence.ViterbiConfidenceEstimator;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Sequence;
import edu.washington.cs.knowitall.argumentidentifier.ArgLearner.Mode;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;
import edu.washington.cs.knowitall.commonlib.ResourceUtils;

/**
 * ArgSubstructureClassifier uses a CRF to classify the left bound for Arg1 and
 * right bound for Arg2
 *
 * @author janara
 *
 */
public class ArgSubstructureClassifier {
    private Mode mode;

    private static final String ARG1_FILE = "arg1substructure-model";
    private static final String ARG2_FILE = "arg2substructure-model";

    private CRF crf = null;
    private Pipe crf_pipe = null;
    private ViterbiConfidenceEstimator crf_estimator;
    private ObjectInputStream crf_input;

    private String[] startTags = { "B-ARG" }; // used by Transducer, indicate
                                              // the start of an argument
                                              // ("B-ARG");
    private String[] inTags = { "I-ARG" }; // used by Transducer, indicate the
                                           // continuation of an argument
                                           // ("I-ARG");

    private ArgSubstructureFeatureGenerator featuregenerator;

    public ArgSubstructureClassifier(Mode mode,
            ArgSubstructureFeatureGenerator featuregenerator) {
        this.mode = mode;
        this.featuregenerator = featuregenerator;
        if (mode == ArgLearner.Mode.LEFT) {
            setupClassifier(ARG1_FILE);
        } else {
            setupClassifier(ARG2_FILE);
        }
    }

    private Pair<Double, Sequence<?>> applyCRF(String testingdata) {
        Sequence<?> input = null;
        Sequence<?> output = null;
        Double conf;

        InstanceList testSequence = null;
        crf_pipe.setTargetProcessing(true);
        testSequence = new InstanceList(crf_pipe);
        testSequence.addThruPipe(new LineGroupIterator(new StringReader(
                testingdata), Pattern.compile("^\\s*$"), true));

        if (testSequence.size() < 1) {
            return new Pair<Double, Sequence<?>>(-1.0, null);
        }

        Instance inst = testSequence.get(0);
        input = (Sequence<?>) inst.getData();

        output = crf.transduce(input);
        conf = crf_estimator.estimateConfidenceFor(inst, startTags, inTags);

        return new Pair<Double, Sequence<?>>(conf, output);
    }

    private int readCRFOutputLeft(ChunkedExtraction extr, int start,
            Sequence<?> output) {
        int s = 0;
        int predstart = extr.getStart();
        int lastnp = -1;
        boolean foundo = false;
        boolean foundarg = false;
        List<String> chunkLabels = extr.getSentence().getChunkTags();

        for (int i = predstart; i > -1; i--) {
            if (chunkLabels.get(i).equals("I-NP")) {
                continue;
            } else {
                if (i < start || i == predstart) {
                    String crflabel = "";
                    crflabel = output.get(s).toString();
                    if (i != predstart && !crflabel.contains("O")) {
                        foundarg = true;
                        lastnp = i;
                    } else if (i != predstart && crflabel.equals("O")) {
                        break;
                    } else if (i != predstart && crflabel.contains("O")) {
                        foundo = true;
                    }
                    s++;
                }
            }
        }
        if (foundo && foundarg) {
            lastnp = -1;
        }
        return lastnp;
    }

    private int readCRFOutputRight(ChunkedExtraction extr, int start,
            Sequence<?> output) {
        int s = 1;
        int lastnp = -1;
        List<String> chunkLabels = extr.getSentence().getChunkTags();

        for (int i = start; i < extr.getSentence().getLength(); i++) {
            if (i > start && chunkLabels.get(i).equals("I-NP")) {
                continue;
            } else {
                String crflabel = "";
                crflabel = output.get(s).toString();
                if (crflabel.equals("O")) {
                    lastnp = i;
                    break;
                }
                s++;
            }
        }
        if (lastnp < 0) {
            lastnp = extr.getSentence().getLength();
        }
        return lastnp;
    }

    private double[] classifyData(String testingdata, ChunkedExtraction extr,
            int start) {

        double[] toreturn = { -1.0, -1.0 };
        if (testingdata == null || testingdata.equals("")) {
            return toreturn;
        }

        // apply crf
        Pair<Double, Sequence<?>> pair = applyCRF(testingdata);
        Double conf = pair.getFirst();
        Sequence<?> output = pair.getSecond();

        if (conf == -1.0) {
            return toreturn;
        }

        // read output
        int lastnp = -1;
        if (mode == ArgLearner.Mode.LEFT) {
            lastnp = readCRFOutputLeft(extr, start, output);
        } else {
            lastnp = readCRFOutputRight(extr, start, output);
        }
        toreturn[0] = lastnp;
        toreturn[1] = conf;
        return toreturn;

    }

    private void setupClassifier(String trainingdata) {
        try {
            crf_input = new ObjectInputStream(ResourceUtils.loadResource(
                    trainingdata, this.getClass()));
            crf = (CRF) crf_input.readObject();
            crf_input.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        crf.getInputAlphabet().stopGrowth();
        crf.getOutputAlphabet().stopGrowth();
        crf_pipe = crf.getInputPipe();
        crf_pipe.setTargetProcessing(false);
        crf_estimator = new ViterbiConfidenceEstimator(crf);
    }

    private String extractFeatures(ChunkedExtraction extr, int argstart,
            int argend, boolean train) {
        String features = featuregenerator.extractCRFFeatures(extr, argstart,
                argend, train);
        return features;
    }

    public double[] getArgBound(ChunkedExtraction predicate, int other_bound) {
        String features = extractFeatures(predicate, other_bound, other_bound,
                false);
        double[] resultsclassifier = classifyData(features, predicate,
                other_bound);
        return resultsclassifier;
    }
}
