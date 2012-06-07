package edu.washington.cs.knowitall.argumentidentifier;

import java.util.Vector;
import java.util.Iterator;

public class PositionInstance {

    private boolean isMidInstance;
    private boolean isRelInstance;
    private int i;
    private Vector<String> features;

    public PositionInstance(int i) {
        this.i = i;
        features = new Vector<String>();
        isMidInstance = false;
        isRelInstance = false;
    }

    public void addFeature(String f) {
        features.add(f);
    }

    public void addFeature(int p, String f) {
        features.add(p, f);
    }

    public void setFeature(int p, String f) {
        features.add(p, f);
        features.remove(p + 1);
    }

    public Vector<String> features() {
        return features;
    }

    public void setIsMidInstance(boolean b) {
        isMidInstance = b;
    }

    public boolean isMidInstance() {
        return isMidInstance;
    }

    public void setIsRelInstance(boolean b) {
        isRelInstance = b;
    }

    public boolean isRelInstance() {
        return isRelInstance;
    }

    public String label() {
        return features.lastElement();
    }

    public int size() {
        return features.size();
    }

    public String get(int i) {
        return features.get(i);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (isMidInstance)
            buf.append('*');
        buf.append(' ');
        buf.append(i);
        buf.append('\t');
        for (Iterator<String> it = features.iterator(); it.hasNext();) {
            buf.append(it.next());
            buf.append(' ');
        }
        return buf.toString().trim();
    }

}