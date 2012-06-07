package edu.washington.cs.knowitall.sequence;

import java.util.regex.Matcher;

public class LayeredTokenMatcher {
    private Matcher m;

    protected LayeredTokenMatcher(Matcher m) {
        this.m = m;
    }

    public int end() {
        return m.end();
    }

    public int end(int group) {
        return m.end(group);
    }

    public boolean find() {
        return m.find();
    }

    public boolean find(int group) {
        return m.find(group);
    }

    public int groupCount() {
        return m.groupCount();
    }

    public boolean matches() {
        return m.matches();
    }

    public int start() {
        return m.start();
    }

    public int start(int group) {
        return m.start(group);
    }

    public void reset() {
        m.reset();
    }

}
