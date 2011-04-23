package edu.washington.cs.knowitall.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import edu.washington.cs.knowitall.extractor.ExtractorException;
import edu.washington.cs.knowitall.extractor.ReVerbExtractor;
import edu.washington.cs.knowitall.extractor.conf.ConfidenceFunctionException;
import edu.washington.cs.knowitall.extractor.conf.ReVerbConfFunction;
import edu.washington.cs.knowitall.extractor.mapper.PronounArgumentFilter;
import edu.washington.cs.knowitall.io.BufferedReaderIterator;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.ChunkedSentenceReader;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;

/***
 * A command line wrapper for ReVerbExtractor. Run with -h to see the usage information.
 * @author afader
 *
 */
public class CommandLineReVerb {
	
	private static final String NAME = "CommandLineReVerb";
	
	private ReVerbExtractor reverb;
	private ReVerbConfFunction confFunc;
	private BufferedReaderIterator stdinLineIterator;
	
	private long startAtTime;
	private boolean dataStdin = false;
	private boolean fileListStdin = false;
	private boolean stripHtml = false;
	private boolean printSents = true;
	private boolean quiet = false;
	private boolean filterPronouns = false;
	
	private int messageEvery = 1000;
	private int numSents = 0;
	private int sentInFile = 0;
	private int numExtrs = 0;
	private int numFiles = 0;
	private String currentFile;
	private Queue<String> fileArgs;
	
	public static void main(String[] args) throws ExtractorException {
		
		Options options = new Options();
		options.addOption("h", "help", false, "Print help and exit");
		options.addOption("f", "files", false, "Read file list from standard input");
		options.addOption("s", "strip-html", false, "Strip HTML before extracting");
		options.addOption("n", "no-sents", false, "Don't print sentences");
		options.addOption("p", "filter-pronouns", false, "Filter out arguments that contain a pronoun");
		options.addOption("q", "quiet", false, "Quiet mode (don't print messages to standard error)");
		
		
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
			System.err.println("Could not parse command line arguments: " + e.getMessage());
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
	}
	
	public CommandLineReVerb(CommandLine params) throws ExtractorException {
		
		quiet = params.hasOption("quiet");
		
		if (params.hasOption("files")) {
			dataStdin = false;
			fileListStdin = true;
			stdinLineIterator = new BufferedReaderIterator(new BufferedReader(new InputStreamReader(System.in)));
		} else if (params.getArgs().length > 0) {
			dataStdin = false;
			fileListStdin = false;
			fileArgs = new LinkedList<String>();
			for (String arg : params.getArgs()) fileArgs.add(arg);
		} else {
			dataStdin = true;
			fileListStdin = false;
		}
		
		stripHtml = params.hasOption("strip-html");
		printSents = !params.hasOption("no-sents");
		filterPronouns = params.hasOption("filter-pronouns");
		
		try {
			
			messageInc("Initializing extractor...");
		
			reverb = new ReVerbExtractor();
		
			if (filterPronouns) {
				reverb.getArgument1Extractor().addMapper(new PronounArgumentFilter());
				reverb.getArgument2Extractor().addMapper(new PronounArgumentFilter());
			}
			message("Done.");
			
			messageInc("Initializing confidence function...");
			confFunc = new ReVerbConfFunction();
			message("Done.");
			
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
				sentInFile = 0;
				try {
					extractFromNextFile();
				} catch (ExtractorException e) {
					message("Error during extraction: " + e.getMessage());
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
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
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
	
	private ChunkedSentenceReader getSentenceReader(BufferedReader in) throws IOException {
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
			System.err.println("Could not compute confidence for " + extr + ": " + e.getMessage());
			return 0;
		}
	}
	
	private void extractFromSentReader(ChunkedSentenceReader reader) throws ExtractorException {
		for (ChunkedSentence sent : reader.getSentences()) {
			numSents++;
			sentInFile++;
			if (printSents) printSent(sent);
			for (ChunkedBinaryExtraction extr : reverb.extract(sent)) {
				numExtrs++;
				double conf = getConf(extr);
				printExtr(extr, conf);
			}
			if (numSents % messageEvery == 0) summary();
		}
	}
	
	private void printSent(ChunkedSentence sent) {
		String sentString = sent.getTokensAsString();
        System.out.println(String.format("sentence\t%s\t%s\t%s", currentFile, numSents, sentString));
	}
	
	private void printExtr(ChunkedBinaryExtraction extr, double conf) {
		String arg1 = extr.getArgument1().toString();
        String rel = extr.getRelation().toString();
        String arg2 = extr.getArgument2().toString();
        String extrString = String.format("%s\t%s\t%s\t%s\t%s\t%s", currentFile, numSents, arg1, rel, arg2, conf);
        System.out.println("extraction\t" + extrString);
	}

}
