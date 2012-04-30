package edu.washington.cs.knowitall.sequence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import edu.washington.cs.knowitall.commonlib.Range;

/***
 * An immutable implementation of {@link LayeredSequence}. This class represents
 * a sequence with multiple layers (e.g. a sentence with words and
 * part-of-speech tags). In this implementation, the length of the sequence is
 * fixed and the values at each position in the sequence cannot be changed. New
 * layers can be added.
 * 
 * @author afader
 * 
 */
public class SimpleLayeredSequence implements LayeredSequence {

    private HashMap<String, ImmutableList<String>> layers;
    private HashSet<String> layerNames;
    private int numLayers;
    private int length;

    /**
     * Constructs a new layered sequence with the given length
     * 
     * @param length
     */
    public SimpleLayeredSequence(int length) {
        init();
        this.length = length;
        numLayers = 0;
    }

    private void init() {
        layers = new HashMap<String, ImmutableList<String>>();
        layerNames = new HashSet<String>();
    }

    /**
     * @param layerName
     * @return an immutable list of the layer
     */
    public ImmutableList<String> getLayer(String layerName) {
        if (hasLayer(layerName)) {
            return layers.get(layerName);
        } else {
            throw new IllegalArgumentException("Invalid layer name: "
                    + layerName);
        }
    }

    /**
     * @return the number of layers
     */
    public int getNumLayers() {
        return numLayers;
    }

    /**
     * @return the length of the sequence
     */
    public int getLength() {
        return length;
    }

    /**
     * Adds a new layer to the sequence
     * 
     * @param layerName
     * @param layer
     * @throws SequenceException
     *             if a layer with layerName already exists or the given layer
     *             has the incorrect length
     */
    public void addLayer(String layerName, ImmutableList<String> layer)
            throws SequenceException {

        if (hasLayer(layerName)) {
            String msg = String.format(
                    "Cannot add layer '%s': layer already exists", layerName);
            throw new SequenceException(msg);
        } else if (layer.size() != length) {
            String msg = String.format(
                    "Cannot add layer '%s': layer parameter has invalid length "
                            + "(expected %s, but got %s)", layerName, length,
                    layer.size());
            throw new SequenceException(msg);
        }
        layerNames.add(layerName);
        layers.put(layerName, layer);
        numLayers++;
    }

    /**
     * Adds a new layer to the sequence. An immutable list will be instantiated
     * around <code>layer</code>.
     * 
     * @param layerName
     * @param layer
     * @throws SequenceException
     *             if a layer with layerName already exists or the given layer
     *             has the incorrect length
     */
    public void addLayer(String layerName, List<String> layer) {
        this.addLayer(layerName, ImmutableList.copyOf(layer));
    }

    /**
     * Adds a new layer to the sequence
     * 
     * @param layerName
     * @param layer
     * @throws SequenceException
     *             if a layer with layerName already exists or the given layer
     *             has the incorrect length
     */
    public void addLayer(String layerName, String[] layer)
            throws SequenceException {
        List<String> layerList = new ArrayList<String>(layer.length);
        for (int i = 0; i < layer.length; i++)
            layerList.add(layer[i]);
        addLayer(layerName, layerList);
    }

    /**
     * @param layerName
     * @return true if this sequence has a layer with the given name
     */
    public boolean hasLayer(String layerName) {
        return layerNames.contains(layerName);
    }

    /**
     * @return the value of the given layer at the given index
     */
    public String get(String layerName, int index) {
        List<String> layer = getLayer(layerName);
        return layer.get(index);
    }

    /**
     * @param layerName
     * @param start
     * @param length
     * @return an immutable subsequence of the layer
     */
    public ImmutableList<String> getSubSequence(String layerName, int start,
            int length) {
        // if(length < 0) { length = 0;}
        // ImmutableList<String> layer = getLayer(layerName);
        // if(layer.size() < start) { start = layer.size()-1;length = 0;}
        // return layer.subList(start, start+length);
        return getLayer(layerName).subList(start, start + length);
    }

    /**
     * @param layerName
     * @param r
     * @return an immutable subsequence of the layer
     */
    public List<String> getSubSequence(String layerName, Range r) {
        return getSubSequence(layerName, r.getStart(), r.getLength());
    }

    /**
     * Returns the subsequence of this layered sequence starting at the given
     * position with the given length
     * 
     * @param start
     * @param length
     * @return
     */
    public SimpleLayeredSequence getSubSequence(int start, int length) {
        SimpleLayeredSequence sub = new SimpleLayeredSequence(length);
        for (String layerName : getLayerNames()) {

            try {
                sub.addLayer(layerName,
                        getSubSequence(layerName, start, length));
            } catch (SequenceException e) {
                String msg = String.format(
                        "Could not create subsequence of length %s starting at %s "
                                + "for layer %s", start, length, layerName);
                throw new IllegalStateException(msg, e);
            }
        }
        return sub;
    }

    /**
     * Returns the subsequence of this layered sequence starting at the given
     * position with the given length
     * 
     * @param r
     * @return
     */
    public SimpleLayeredSequence getSubSequence(Range r) {
        return getSubSequence(r.getStart(), r.getLength());
    }

    /**
     * @return the layer names
     */
    public Collection<String> getLayerNames() {
        return layerNames;
    }

    /**
     * @param layerName
     * @return the tokens in the given layer name, joined by spaces
     */
    public String getLayerAsString(String layerName) {
        return getLayerAsString(layerName, 0, getLength());
    }

    /**
     * @param layerName
     * @param r
     * @return the tokens in the given layer name in the given range, joined by
     *         spaces
     */
    public String getLayerAsString(String layerName, Range r) {
        return getLayerAsString(layerName, r.getStart(), r.getLength());
    }

    /**
     * @param layerName
     * @param start
     * @param length
     * @return returns the tokens of the given layer name, joined by spaces,
     *         starting at the start position and ending at start+length
     */
    public String getLayerAsString(String layerName, int start, int length) {
        List<String> sub = getSubSequence(layerName, start, length);
        return Joiner.on(" ").join(sub.iterator());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((layerNames == null) ? 0 : layerNames.hashCode());
        result = prime * result + ((layers == null) ? 0 : layers.hashCode());
        result = prime * result + length;
        result = prime * result + numLayers;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof SimpleLayeredSequence))
            return false;
        SimpleLayeredSequence other = (SimpleLayeredSequence) obj;
        if (layerNames == null) {
            if (other.layerNames != null)
                return false;
        } else if (!layerNames.equals(other.layerNames))
            return false;
        if (layers == null) {
            if (other.layers != null)
                return false;
        } else if (!layers.equals(other.layers))
            return false;
        if (length != other.length)
            return false;
        if (numLayers != other.numLayers)
            return false;
        return true;
    }

}
