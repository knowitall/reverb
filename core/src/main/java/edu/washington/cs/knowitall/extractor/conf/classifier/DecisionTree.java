package edu.washington.cs.knowitall.extractor.conf.classifier;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class DecisionTree {
    private Tree root;

    public DecisionTree(Tree tree) {
        this.root = tree;
    }

    public static class Tree {
        public Tree(Predicate<DoubleFeatures> predicate, String predicateString, List<Tree> children, String outcome) {
            this.predicate = predicate;
            this.predicateString = predicateString;
            this.children = children;
            this.outcome = outcome;
        }

        public Tree(Predicate<DoubleFeatures> predicate, String predicateString, List<Tree> children) {
            this.predicate = predicate;
            this.predicateString = predicateString;
            this.children = children;
            this.outcome = null;
        }

        public Tree(Predicate<DoubleFeatures> predicate, String predicateString, String outcome) {
            this.predicate = predicate;
            this.predicateString = predicateString;
            this.children = null;
            this.outcome = outcome;
        }

        private void print(String indent) {
            System.out.println(indent + this.toString());
            if (this.children != null) {
                for (Tree child : this.children) {
                    child.print("|   " + indent);
                }
            }
        }

        public void print() {
            this.print("");
        }

        @Override public String toString() {
            if (outcome != null) {
                return this.predicateString + ":" + this.outcome;
            }
            else {
                return this.predicateString;
            }
        }

        public final Predicate<DoubleFeatures> predicate;
        public final String predicateString;
        public final List<Tree> children;
        public final String outcome;
    }

    private static class Line {
        public final int depth;
        public final String text;

        public Line(int depth, String text) {
            this.depth = depth;
            this.text = text;
        }

        @Override public String toString() {
            return text;
        }
    }

    public static DecisionTree fromModel(URL url) throws IOException {
        InputStream is = url.openStream();

        Pattern indentation = Pattern.compile("^(?:\\|   )*");

        try {
            List<Line> lines = new ArrayList<Line>();
            Scanner scanner = new Scanner(is);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                Matcher matcher = indentation.matcher(line);
                matcher.find();
                int indentWidth = matcher.group().length();

                lines.add(new Line(indentWidth / 4, line.substring(indentWidth)));
            }

            return new DecisionTree(fromLines(lines));
        }
        finally {
            is.close();
        }
    }

    private static final Pattern outcomePattern = Pattern.compile("(\\w+) ([=<>]+) ([^\\s]+) : (\\w+) .*");
    private static final Pattern testPattern = Pattern.compile("(\\w+) ([=<>]+) ([^\\s]+)");

    private static Predicate<DoubleFeatures> predicate(final String feature, final String comparison, final String value) {
        return new Predicate<DoubleFeatures>() {
            @Override
            public boolean apply(DoubleFeatures features) {
                if (comparison.equals("=")) {
                    return features.get(feature) == Double.parseDouble(value);
                }
                else if (comparison.equals("<=")) {
                    return features.get(feature) <= Double.parseDouble(value);
                }
                else if (comparison.equals(">=")) {
                    return features.get(feature) >= Double.parseDouble(value);
                }
                else if (comparison.equals("<")) {
                    return features.get(feature) < Double.parseDouble(value);
                }
                else if (comparison.equals(">")) {
                    return features.get(feature) < Double.parseDouble(value);
                }
                else {
                    throw new IllegalArgumentException("unknown comparison: " + comparison);
                }
            }
        };
    }

    private static Tree fromLines(List<Line> lines) {
        Line cur = lines.get(0);
        Matcher outcomeMatcher = outcomePattern.matcher(cur.text);
        if (!cur.text.equals("root") && outcomeMatcher.matches()) {
            // base case
            // we are an outcome node
            String feature = outcomeMatcher.group(1);
            String comparison = outcomeMatcher.group(2);
            String value = outcomeMatcher.group(3);
            String outcome = outcomeMatcher.group(4);

            return new Tree(predicate(feature, comparison, value), feature+" "+comparison+" "+value, outcome);
        }
        else {
            lines = lines.subList(1, lines.size());
            List<Tree> children = new ArrayList<Tree>();

            // recurse
            int i = 0;
            for (Line line : lines) {
                if (line.depth <= cur.depth) {
                    break;
                }
                else if (line.depth == cur.depth + 1) {
                    // recurse and add as a child
                    children.add(fromLines(lines.subList(i, lines.size())));
                }

                i++;
            }

            if (cur.text.equals("root")) {
                return new Tree(Predicates.<DoubleFeatures>alwaysTrue(), "root", children);
            }
            else {
                Matcher branchMatcher = testPattern.matcher(cur.text);

                if (!branchMatcher.matches()) {
                    throw new IllegalArgumentException();
                }

                String feature = branchMatcher.group(1);
                String comparison = branchMatcher.group(2);
                String value = branchMatcher.group(3);
                return new Tree(predicate(feature, comparison, value), feature+" "+comparison+" "+value, children);
            }
        }
    }

    public String classify(DoubleFeatures features) {
        Tree loc = root;

        while (loc.children != null) {
            boolean moved = false;
            for (Tree child : loc.children) {
                if (child.predicate.apply(features)) {
                    loc = child;
                    moved = true;
                    break;
                }
            }

            if (!moved) {
                throw new IllegalStateException();
            }
        }

        return loc.outcome;
    }
}
