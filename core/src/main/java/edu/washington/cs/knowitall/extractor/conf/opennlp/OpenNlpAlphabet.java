package edu.washington.cs.knowitall.extractor.conf.opennlp;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import edu.washington.cs.knowitall.extractor.conf.featureset.BooleanFeatureSet;

/***
 * OpenNLP requires features to have a unique text representation. This class
 * facilitates a lookup from feature name and feature value to a unique string
 * of the form featureName=featureValue.
 *
 * This is for a performance boost from avoiding string concatenations.
 *
 * @author schmmd
 *
 * @param <T>
 */
public class OpenNlpAlphabet<T> {
    public final ImmutableMap<Key, String> lookup;

    public static class Key {
        public final String featureName;
        public final boolean value;

        public Key(String featureName, boolean value) {
            this.featureName = featureName;
            this.value = value;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Key)) {
                return false;
            } else {
                Key that = (Key) other;
                return that.featureName == this.featureName
                        && that.value == this.value;
            }
        }

        @Override
        public int hashCode() {
            return this.featureName.hashCode() + (this.value ? 1 : 0);
        }
    }

    public OpenNlpAlphabet(BooleanFeatureSet<T> features) {
        Map<Key, String> lookup = new HashMap<Key, String>();

        for (String featureName : features.getFeatureNames()) {
            lookup.put(new Key(featureName, true), featureName + "=true");
            lookup.put(new Key(featureName, false), featureName + "=false");
        }

        this.lookup = ImmutableMap.copyOf(lookup);
    }
}
