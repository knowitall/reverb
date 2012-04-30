package edu.washington.cs.knowitall.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

public class HtmlUtils {

    private static HashSet<Pattern> removePatterns;
    private static HashSet<Pattern> breakPatterns;
    private static boolean initialized = false;

    private static final String[] breakTags = { "blockquote", "br", "center",
            "dd", "div", "dt", "fieldset", "h\\d", "hr", "img", "input",
            "isindex", "li", "noframes", "noscript", "p", "pre", "q", "table",
            "td", "textarea", "th", "xmp" };

    private static final String[] removeTags = { "applet", "form", "head",
            "iframe", "legend", "map", "object", "script", "select", "style",
            "title" };

    private static Pattern tag = Pattern.compile("<[^<]*?>");
    private static Pattern whiteSpace = Pattern.compile("\\s+");
    private static Pattern multiSpace = Pattern.compile("  +");
    private static Pattern multiBreaks = Pattern.compile("\n\n+");

    public static String removeHtml(String content) {

        if (!initialized)
            initPatterns();

        // Normalize whitespace
        content = whiteSpace.matcher(content).replaceAll(" ");

        // Remove and break text
        content = applyPatterns(removePatterns, content);
        content = applyPatterns(breakPatterns, content);

        // Remove tags
        content = tag.matcher(content).replaceAll("");

        // Escape HTML codes
        content = StringEscapeUtils.unescapeCsv(content);

        // Fix more whitespace
        content = multiSpace.matcher(content).replaceAll(" ");
        content = multiBreaks.matcher(content).replaceAll("\n");
        content = content.replace(';', '\n');

        return content;

    }

    public static void main(String[] args) throws Exception {
        BufferedReader in;
        if (args.length == 1) {
            in = new BufferedReader(new FileReader(args[0]));
        } else {
            in = new BufferedReader(new InputStreamReader(System.in));
        }
        StringBuffer sb = new StringBuffer();
        for (String line = in.readLine(); line != null; line = in.readLine()) {
            sb.append(line);
        }
        System.out.println(removeHtml(sb.toString()));
    }

    private static String applyPatterns(HashSet<Pattern> patterns, String s) {
        for (Pattern pat : patterns) {
            s = pat.matcher(s).replaceAll("\n");
        }
        return s;
    }

    private static void initPatterns() {
        removePatterns = new HashSet<Pattern>();
        breakPatterns = new HashSet<Pattern>();
        for (int i = 0; i < removeTags.length; i++) {
            Pattern p = Pattern.compile("(?is)<" + removeTags[i]
                    + "[^<]*?>.*?</" + removeTags[i] + ">");
            removePatterns.add(p);
            p = Pattern.compile("(?i)</?" + removeTags[i] + "[^<]*?>");
            breakPatterns.add(p);
        }
        for (int i = 0; i < breakTags.length; i++) {
            Pattern p = Pattern.compile("(?i)</?" + breakTags[i] + "[^<]*?>");
            breakPatterns.add(p);
        }
        initialized = true;
    }

}
