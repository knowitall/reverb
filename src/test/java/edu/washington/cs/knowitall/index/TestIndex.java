package edu.washington.cs.knowitall.index;

import static edu.washington.cs.knowitall.nlp.extraction.TestExtractions.extractions;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.washington.cs.knowitall.nlp.extraction.SpanExtraction;

public abstract class TestIndex {
    
    public static void createIndex(File f) throws IOException {
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
        Directory dir = FSDirectory.open(f);
        IndexWriter iwriter = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        SpanExtractionNormalizer normalizer = new BinaryVerbalExtractionNormalizer();
        for (SpanExtraction extr : extractions) {
            NormalizedSpanExtraction nextr = normalizer.normalizeExtraction(extr);
            iwriter.addDocument(LuceneExtractionSerializer.toDocument(nextr));
        }
        iwriter.close();
    }
}
