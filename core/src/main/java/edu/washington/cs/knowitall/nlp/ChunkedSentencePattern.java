package edu.washington.cs.knowitall.nlp;

import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.washington.cs.knowitall.logic.ArgFactory;
import edu.washington.cs.knowitall.logic.LogicExpression;
import edu.washington.cs.knowitall.logic.Expression.Arg;
import edu.washington.cs.knowitall.regex.Expression;
import edu.washington.cs.knowitall.regex.ExpressionFactory;
import edu.washington.cs.knowitall.regex.Match;
import edu.washington.cs.knowitall.regex.RegularExpression;

public class ChunkedSentencePattern {
    /***
     * This class compiles regular expressions over the ChunkedSentenceTokens in
     * a sentence into an NFA. There is a lot of redundancy in their
     * expressiveness. This is largely because it supports pattern matching on
     * the fields This is not necessary but is an optimization and a shorthand
     * (i.e. {@code <pos="NNPS?"> is equivalent to "<pos="NNP" | pos="NNPS">}
     * and {@code (?:<pos="NNP"> | <pos="NNPS">)}.
     * <p>
     * Here are some equivalent examples:
     * <ol>
     * <li> {@code <pos="JJ">* <pos="NNP.">+}
     * <li> {@code <pos="JJ">* <pos="NNPS?">+}
     * <li> {@code <pos="JJ">* <pos="NNP" | pos="NNPS">+}
     * <li> {@code <pos="JJ">* (?:<pos="NNP"> | <pos="NNPS">)+}
     * </ol>
     * Note that (3) and (4) are not preferred for efficiency reasons. Regex OR
     * (in example (4)) should only be used on multi-ChunkedSentenceToken
     * sequences.
     * <p>
     * The Regular Expressions support named groups (<name>: ... ), unnamed
     * groups (?: ... ), and capturing groups ( ... ). The operators allowed are
     * +, ?, *, and |. The Logic Expressions (that describe each
     * ChunkedSentenceToken) allow grouping "( ... )", not '!', or '|', and and
     * '&'.
     *
     * @param regex
     * @return
     */
    public static RegularExpression<ChunkedSentenceToken> compile(String regex) {
        return RegularExpression.compile(regex,
                new ExpressionFactory<ChunkedSentenceToken>() {

                    @Override
                    public Expression.BaseExpression<ChunkedSentenceToken> create(
                            final String expression) {
                        final Pattern valuePattern = Pattern
                                .compile("([\"'])(.*)\\1");
                        return new Expression.BaseExpression<ChunkedSentenceToken>(
                                expression) {
                            private final LogicExpression<ChunkedSentenceToken> logic;

                            {
                                this.logic = LogicExpression.compile(
                                        expression,
                                        new ArgFactory<ChunkedSentenceToken>() {
                                            @Override
                                            public Arg<ChunkedSentenceToken> create(
                                                    final String argument) {
                                                return new Arg<ChunkedSentenceToken>() {
                                                    private final ChunkedSentenceToken.Expression expression;

                                                    {
                                                        String[] parts = argument
                                                                .split("=");

                                                        String base = parts[0];

                                                        Matcher matcher = valuePattern
                                                                .matcher(parts[1]);
                                                        if (!matcher.matches()) {
                                                            throw new IllegalArgumentException(
                                                                    "Value not enclosed in quotes (\") or ('): "
                                                                            + argument);
                                                        }
                                                        String string = matcher
                                                                .group(2);

                                                        if (base.equalsIgnoreCase("stringCS")) {
                                                            this.expression = new ChunkedSentenceToken.StringExpression(
                                                                    string, 0);
                                                        } else if (base
                                                                .equalsIgnoreCase("string")) {
                                                            this.expression = new ChunkedSentenceToken.StringExpression(
                                                                    string);
                                                        } else if (base
                                                                .equalsIgnoreCase("pos")) {
                                                            this.expression = new ChunkedSentenceToken.PosTagExpression(
                                                                    string);
                                                        } else if (base
                                                                .equalsIgnoreCase("chunk")) {
                                                            this.expression = new ChunkedSentenceToken.ChunkTagExpression(
                                                                    string);
                                                        } else {
                                                            throw new IllegalStateException(
                                                                    "unknown argument specified: "
                                                                            + base);
                                                        }
                                                    }

                                                    @Override
                                                    public boolean apply(
                                                            ChunkedSentenceToken entity) {
                                                        return this.expression
                                                                .apply(entity);
                                                    }
                                                };
                                            }
                                        });
                            }

                            @Override
                            public boolean apply(ChunkedSentenceToken entity) {
                                return logic.apply(entity);
                            }
                        };
                    }
                });
    }

    public static void main(String[] args) throws ChunkerException, IOException {
        System.out.println("Compiling the expression... ");
        RegularExpression<ChunkedSentenceToken> expression = ChunkedSentencePattern
                .compile(args[0]);
        System.out.println(expression);
        OpenNlpSentenceChunker chunker = new OpenNlpSentenceChunker();

        System.out
                .println("Please enter a sentence to match with the above expression.");
        Scanner scan = new Scanner(System.in);
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            ChunkedSentence chunked = chunker.chunkSentence(line);
            Match<ChunkedSentenceToken> match = expression
                    .match(ChunkedSentenceToken.tokenize(chunked));
            if (match != null) {
                System.out.println(match.groups().get(0));
            }
        }
    }
}
