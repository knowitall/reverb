package edu.washington.cs.knowitall.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.google.common.base.Joiner;

import edu.washington.cs.knowitall.argumentidentifier.ConfidenceMetric;
import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.extractor.ExtractorException;
import edu.washington.cs.knowitall.extractor.R2A2;
import edu.washington.cs.knowitall.extractor.ReVerbExtractor;
import edu.washington.cs.knowitall.extractor.ReVerbRelationExtractor;
import edu.washington.cs.knowitall.extractor.conf.ConfidenceFunction;
import edu.washington.cs.knowitall.extractor.conf.ConfidenceFunctionException;
import edu.washington.cs.knowitall.extractor.conf.ReVerbOpenNlpConfFunction;
import edu.washington.cs.knowitall.extractor.mapper.PronounArgumentFilter;
import edu.washington.cs.knowitall.io.BufferedReaderIterator;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.ChunkedSentenceIterator;
import edu.washington.cs.knowitall.nlp.ChunkedSentenceReader;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;
import edu.washington.cs.knowitall.normalization.BinaryExtractionNormalizer;
import edu.washington.cs.knowitall.normalization.NormalizedBinaryExtraction;

/***
 * A command line wrapper for ReVerbExtractor. Run with -h to see the usage
 * information.
 *
 * @author afader
 *
 */
public class CommandLineReVerb {

    private static final String NAME = "CommandLineReVerb";

    private ReVerbRelationExtractor extractor;
    private ConfidenceFunction confFunc;
    private BufferedReaderIterator stdinLineIterator;
    private BinaryExtractionNormalizer normalizer;

    private long startAtTime;
    private boolean dataStdin = false;
    private boolean fileListStdin = false;
    private boolean stripHtml = false;
    private boolean quiet = false;
    private boolean timing = false;
    private boolean filterPronouns = false;
    private boolean mergeOverlapRels = false;
    private boolean useSynLexConstraints = false;
    private boolean allowUnary = false;
    private boolean useArgLearner = false;
    private int minFreq = 20;

    private int messageEvery = 1000;
    private int numSents = 0;
    private int numExtrs = 0;
    private int numFiles = 0;
    private String currentFile;
    private Queue<String> fileArgs;

    long chunkTime = 0;
    long extractTime = 0;
    long confTime = 0;

    private static String[] colNames = { "filename", "sentence number", "arg1",
            "rel", "arg2", "arg1 start", "arg1 end", "rel start", "rel end",
            "arg2 start", "arg2 end", "conf", "sentence words",
            "sentence pos tags", "sentence chunk tags", "arg1 normalized",
            "rel normalized", "arg2 normalized" };

    public static void main(String[] args) throws ExtractorException {

        Options options = new Options();
        options.addOption("h", "help", false, "Print help and exit");
        options.addOption("f", "files", false,
                "Read file list from standard input");
        options.addOption("s", "strip-html", false,
                "Strip HTML before extracting");
        options.addOption("p", "filter-pronouns", false,
                "Filter out arguments that contain a pronoun");
        options.addOption("q", "quiet", false,
                "Quiet mode (don't print messages to standard error)");
        options.addOption("t", "timing", false,
                "Provide detailed timing information");
        options.addOption(
                "m",
                "minFreq",
                true,
                "Each relation must have at a minimum this many number of distinct arguments in a large corpus.");
        options.addOption(
                "a",
                "argLearner",
                false,
                "Use ArgLearner to identify extraction arguments (experimental, slower but more accurate). If you use this setting, the minFreq, noConstraints, keepOverlap, and allowUnary values will be ignored.");
        options.addOption("K", "keepOverlap", false,
                "Do not merge overlapping relations (Default is to merge.)");
        options.addOption(
                "U",
                "allowUnary",
                false,
                "Allow relations with a single argument to be output. (Default setting is to disallow unary relations.)");
        options.addOption("N", "noConstraints", false,
                "Do not enforce the syntactic and lexical constraints that are part of ReVerb.");

        CommandLineParser parser = new PosixParser();

        try {

            CommandLine params = parser.parse(options, args);
            if (params.hasOption("h")) {
                usage(options);
                return;
            } else {
                CommandLineReVerb clReVerb = new CommandLineReVerb(params);
                clReVerb.runExtractor();
            }

        } catch (ParseException e) {
            System.err.println("Could not parse command line arguments: "
                    + e.getMessage());
            usage(options);
            return;
        } catch (IOException e) {
            System.err.println("Encountered IOException: " + e.getMessage());
            return;
        }
    }

    public static void usage(Options options) {
        HelpFormatter help = new HelpFormatter();
        help.printHelp(String.format("%s [OPTIONS] [FILES]", NAME), options);
        System.out.println();
        printOutputFormatHelp();
        System.out.println();
    }

    private static void printOutputFormatHelp() {
        System.out.println("Output Columns:");
        for (int i = 0; i < colNames.length; i++) {
            int j = i + 1;
            System.out.println("    " + j + ". " + colNames[i]);
        }
    }

    public CommandLineReVerb(CommandLine params) throws ExtractorException {

        quiet = params.hasOption("quiet");

        timing = params.hasOption("timing");

        if (params.hasOption("files")) {
            dataStdin = false;
            fileListStdin = true;
            stdinLineIterator = new BufferedReaderIterator(new BufferedReader(
                    new InputStreamReader(System.in)));
        } else if (params.getArgs().length > 0) {
            dataStdin = false;
            fileListStdin = false;
            fileArgs = new LinkedList<String>();
            for (String arg : params.getArgs())
                fileArgs.add(arg);
        } else {
            dataStdin = true;
            fileListStdin = false;
        }

        stripHtml = params.hasOption("strip-html");
        filterPronouns = params.hasOption("filter-pronouns");

        minFreq = Integer.parseInt(params.getOptionValue("minFreq", "20"));
        mergeOverlapRels = !params.hasOption("keepOverlap");
        useSynLexConstraints = !params.hasOption("noConstraints");
        allowUnary = params.hasOption("allowUnary");

        useArgLearner = params.hasOption("argLearner");

        normalizer = new BinaryExtractionNormalizer();

        try {

            if (useArgLearner) {
                messageInc("Initializing ReVerb+ArgLearner extractor...");
                extractor = new R2A2();
                message("Done.");
                messageInc("Initializing confidence function...");
                confFunc = new ConfidenceMetric();
                message("Done.");
            } else {
                messageInc("Initializing ReVerb extractor...");
                extractor = new ReVerbExtractor(minFreq, useSynLexConstraints,
                        mergeOverlapRels, allowUnary);
                message("Done.");
                messageInc("Initializing confidence function...");
                confFunc = new ReVerbOpenNlpConfFunction();
                message("Done.");
            }

            if (filterPronouns) {
                extractor.getArgument1Extractor().addMapper(
                        new PronounArgumentFilter());
                extractor.getArgument2Extractor().addMapper(
                        new PronounArgumentFilter());
            }

            messageInc("Initializing NLP tools...");
            DefaultObjects.initializeNlpTools();
            message("Done.");

        } catch (ConfidenceFunctionException e) {
            throw new ExtractorException(e);
        } catch (IOException e) {
            throw new ExtractorException(e);
        }

    }

    public void runExtractor() throws IOException, ExtractorException {

        message("Starting extraction.");

        startAtTime = System.currentTimeMillis();
        if (dataStdin) {
            extractFromStdin();
        } else {
            while (haveNextFile()) {
                try {
                    extractFromNextFile();
                } catch (ExtractorException e) {
                    message("Error during extraction: " + e.getMessage());
                } catch (IOException e) {
                    message("Error reading file: " + e.getMessage());
                }
                numFiles++;
            }
        }

        message("Done with extraction.");
        summary();
    }

    private void summary() {
        long currentTime = System.currentTimeMillis();
        long runTimeSecs = (currentTime - startAtTime) / 1000;
        messageInc("Summary: ");
        messageInc(numExtrs + " extractions, ");
        messageInc(numSents + " sentences, ");
        messageInc(numFiles + " files, ");
        message(runTimeSecs + " seconds");

        if (timing) {
            DecimalFormat fmt = new DecimalFormat("#.##");

            messageInc("Timing: ");
            messageInc("chunking: "
                    + fmt.format(chunkTime / 1000.0 / 1000.0 / 1000.0) + " s, ");
            messageInc("extraction: "
                    + fmt.format(extractTime / 1000.0 / 1000.0 / 1000.0)
                    + " s, ");
            messageInc("confidence: "
                    + fmt.format(confTime / 1000.0 / 1000.0 / 1000.0) + " s");
        }
    }

    private void message(String msg) {
        if (!quiet) {
            System.err.println(msg);
        }
    }

    private void messageInc(String msg) {
        if (!quiet) {
            System.err.print(msg);
        }
    }

    private boolean haveNextFile() throws IOException {
        if (fileListStdin) {
            return stdinLineIterator.hasNext();
        } else {
            return fileArgs.size() > 0;
        }
    }

    private File getNextFile() throws IOException {
        if (fileListStdin) {
            return new File(stdinLineIterator.next());
        } else {
            return new File(fileArgs.remove());
        }
    }

    private void extractFromNextFile() throws IOException, ExtractorException {
        File f = getNextFile();
        currentFile = f.getAbsolutePath();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream(f)));
        ChunkedSentenceReader reader = getSentenceReader(in);
        message("Extracting from " + f);
        extractFromSentReader(reader);
    }

    private void extractFromStdin() throws IOException, ExtractorException {
        currentFile = "stdin";
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        ChunkedSentenceReader reader = getSentenceReader(in);
        extractFromSentReader(reader);
    }

    private ChunkedSentenceReader getSentenceReader(BufferedReader in)
            throws IOException {
        if (stripHtml) {
            return DefaultObjects.getDefaultSentenceReaderHtml(in);
        } else {
            return DefaultObjects.getDefaultSentenceReader(in);
        }
    }

    private double getConf(ChunkedBinaryExtraction extr) {
        try {
            return confFunc.getConf(extr);
        } catch (ConfidenceFunctionException e) {
            System.err.println("Could not compute confidence for " + extr
                    + ": " + e.getMessage());
            return 0;
        }
    }

    private void extractFromSentReader(ChunkedSentenceReader reader)
            throws ExtractorException {
        long start;

        ChunkedSentenceIterator sentenceIt = reader.iterator();

        while (sentenceIt.hasNext()) {
            // get the next chunked sentence
            ChunkedSentence sent = sentenceIt.next();
            chunkTime += sentenceIt.getLastComputeTime();

            numSents++;

            // make the extractions
            start = System.nanoTime();
            Iterable<ChunkedBinaryExtraction> extractions = extractor
                    .extract(sent);
            extractTime += System.nanoTime() - start;

            for (ChunkedBinaryExtraction extr : extractions) {
                numExtrs++;

                // run the confidence function
                start = System.nanoTime();
                double conf = getConf(extr);
                confTime += System.nanoTime() - start;

                NormalizedBinaryExtraction extrNorm = normalizer
                        .normalize(extr);
                printExtr(extrNorm, conf);
            }
            if (numSents % messageEvery == 0)
                summary();
        }
    }

    private void printExtr(NormalizedBinaryExtraction extr, double conf) {
        String arg1 = extr.getArgument1().toString();
        String rel = extr.getRelation().toString();
        String arg2 = extr.getArgument2().toString();

        ChunkedSentence sent = extr.getSentence();
        String toks = sent.getTokensAsString();
        String pos = sent.getPosTagsAsString();
        String chunks = sent.getChunkTagsAsString();
        String arg1Norm = extr.getArgument1Norm().toString();
        String relNorm = extr.getRelationNorm().toString();
        String arg2Norm = extr.getArgument2Norm().toString();

        Range arg1Range = extr.getArgument1().getRange();
        Range relRange = extr.getRelation().getRange();
        Range arg2Range = extr.getArgument2().getRange();
        String a1s = String.valueOf(arg1Range.getStart());
        String a1e = String.valueOf(arg1Range.getEnd());
        String rs = String.valueOf(relRange.getStart());
        String re = String.valueOf(relRange.getEnd());
        String a2s = String.valueOf(arg2Range.getStart());
        String a2e = String.valueOf(arg2Range.getEnd());

        String row = Joiner.on("\t").join(
                new String[] { currentFile, String.valueOf(numSents), arg1,
                        rel, arg2, a1s, a1e, rs, re, a2s, a2e,
                        String.valueOf(conf), toks, pos, chunks, arg1Norm,
                        relNorm, arg2Norm });

        System.out.println(row);
    }

}
