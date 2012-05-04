package edu.washington.cs.knowitall.extractor.conf.opennlp;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import opennlp.model.Event;
import edu.washington.cs.knowitall.extractor.conf.featureset.BooleanFeatureSet;

/***
 * A wrapper for the OpenNlp events. This class allows the caller to add events
 * to a data set by directly passing an instance of type <code>T</code> and a
 * label. The instance is then featurized using a <code>BooleanFeatureSet</code>
 * . instance.
 *
 * @author schmmd
 *
 * @param <T>
 */
public class OpenNlpDataSet<T> {

    public final String name;

    private final BooleanFeatureSet<T> features;
    private final List<Event> instances;
    private final OpenNlpAlphabet<T> alphabet;

    /**
     * Constructs a new data set
     *
     * @param name
     *            the name of the data set
     * @param featureSet
     *            the feature representation of the data set
     */
    public OpenNlpDataSet(String name, BooleanFeatureSet<T> featureSet) {
        this.name = name;
        this.features = featureSet;
        this.instances = new ArrayList<Event>();
        this.alphabet = new OpenNlpAlphabet<T>(featureSet);
    }

    /**
     * Adds a new instance to the data set with the given label (0 for negative,
     * 1 for positive).
     *
     * @param instance
     * @param label
     */
    public void addInstance(T instance, int label) {
        String[] stringFeatures = new String[features.getNumFeatures()];

        int i = 0;
        for (String feature : features.getFeatureNames()) {
            boolean value = features.featurizeToBool(feature, instance);
            stringFeatures[i++] = this.alphabet.lookup
                    .get(new OpenNlpAlphabet.Key(feature, value));
        }

        Event event = new Event(Integer.toString(label), stringFeatures);
        this.instances.add(event);
    }

    public ImmutableList<Event> getInstances() {
        return ImmutableList.copyOf(this.instances);
    }
}
