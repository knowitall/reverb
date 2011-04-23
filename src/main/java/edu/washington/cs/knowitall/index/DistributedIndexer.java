package edu.washington.cs.knowitall.index;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * <p>
 * A Hadoop driver for taking a set of {@link NormalizedSpanExtraction} objects
 * from disk (serialized via {@link ExtractionSerializer}), converting them to
 * Lucene {@link Document} objects, and then indexing them across some number
 * of shards. The extractions are converted to document objects using the 
 * {@link LuceneExtractionSerializer} class.
 * </p>
 * 
 * <p>
 * This can be run on Hadoop using the following command:
 * </p>
 * 
 * <p>
 * <tt>hadoop jar  
 * this.jar {@link edu.washington.cs.knowitall.index.DistributedIndexer} 
 * -Dmapred.reduce.tasks=20 input output</tt>
 * </p>
 * 
 * <p>
 * This will create 20 index shards in the given output directory. The job
 * makes one index shard per reduce task, so the number of resulting shards
 * can be controlled by using the {@code mapred.reduce.tasks} parameter. 
 * </p>
 * 
 * <p>
 * Because Lucene's {@link IndexWriter} class requires access to a local 
 * filesystem (not HDFS), the reducer class creates a temporary local directory
 * and writes the index there. After indexing, it moves the index directory
 * into HDFS.
 * </p>
 * @author afader
 *
 */
public class DistributedIndexer extends Configured implements Tool {
    
    /**
     * Runs the indexer, reading extractions from args[0] and writing the 
     * index shards to args[1].
     * @param args
     * @throws Exception
     */
    public static void main(String args[]) throws Exception {
        System.exit(ToolRunner.run(new DistributedIndexer(), args));
    }
    
    public int run(String[] args) throws Exception {
        
        /*
         * These two lines make it so the user can pass standard hadoop
         * commands on the command line (things like mapred.job.queue.name)
         */
        Configuration conf = getConf();
        String[] otherArgs = 
            new GenericOptionsParser(conf, args).getRemainingArgs();
        
        if (otherArgs.length != 2) {
            System.out.println("Usage: DistributedIndexer input output");
            return -1;
        }
        
        // Setup the job
        Job job = new Job(conf);
        job.setJarByClass(DistributedIndexer.class);
        
        // The input and output come from args[0] and args[1]
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        
        /*
         * DistributedIndexer uses the identity mappers and reducers. All
         * of the index-writing logic takes place inside the IndexOutputFormat
         * class. The Mapper and Reducer read/write (LongWritable, Text) pairs,
         * where the Text value is the serialized version of the extraction,
         * and LongWritable is the offset of that line in the original file
         * split (Hadoop's default behavior).  
         */
        job.setMapperClass(Mapper.class);
        job.setReducerClass(Reducer.class);
        
        /*
         * The IndexOutputFormat receives the (key, value) pairs that the 
         * Reducer writes. It ignores the key and converts the value (which
         * is a String representation of a {@link NormalizedSpanExtraction})
         * into a Lucene document object. This is then written to a Lucene
         * index. Once the job completes, the index is optimized, then closed,
         * and moved into HDFS.
         */
        job.setOutputFormatClass(IndexOutputFormat.class);
        
        return job.waitForCompletion(true) ? 0 : 1;
        
    }
    
    /**
     * The static class responsible for writing extractions to an index, 
     * instead of just printing them to storage.
     * @author afader
     *
     */
    static class IndexOutputFormat 
    extends FileOutputFormat<LongWritable, Text> {
        
        /**
         * Returns a RecordWriter object, which is responsible for taking
         * (key, value) pairs and writing them to HDFS. However, in this
         * implementation, they are not written directly to HDFS. Instead,
         * a temporary directory is created in the local filesystem. Then,
         * an index is written to the temporary directory, and finally copied
         * over to HDFS.
         */
        public RecordWriter<LongWritable, Text> 
        getRecordWriter(TaskAttemptContext context) 
        throws IOException, InterruptedException {
            
            // Create a temporary directory on the local filesystem
            Configuration conf = context.getConfiguration();
            String tmp = conf.get("hadoop.tmp.dir");
            long millis = System.currentTimeMillis();
            String shardName = "" + millis + "-" + new Random().nextInt();
            final File file = new File(tmp, shardName);
            
            // Keep reporting progress, or else the task will get killed!
            context.progress();
            
            // Set up the index writer to write to the local temp directory
            Directory dir = FSDirectory.open(file);
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
            final IndexWriter iwriter = new IndexWriter(dir, analyzer, true, 
                    IndexWriter.MaxFieldLength.LIMITED);
            iwriter.setMergeFactor(100000);
            
            // This is the record writer that the reducer has access to, and
            // where the actual indexing code is.
            return new RecordWriter<LongWritable, Text>() {

                /**
                 * Takes a key (just the offset of the line in the mapper's
                 * split) and a value (the String representation of the 
                 * extraction), and writes it to the index.
                 */
                public void write(LongWritable key, Text val) 
                throws IOException, InterruptedException {
                    String str = val.toString();
                    try {
                        NormalizedSpanExtraction extr = 
                            ExtractionSerializer.fromString(str);
                        Document doc = 
                            LuceneExtractionSerializer.toDocument(extr);
                        iwriter.addDocument(doc);
                    } catch (ExtractionFormatException e) {
                        return;
                    }
                }
                
                /**
                 * When the reducer is done writing, the index needs to be
                 * optimized, closed, and then copied to HDFS.
                 */
                public void close(TaskAttemptContext context) 
                throws IOException, InterruptedException {
                    
                    /*
                     * Optimizing the index can take a long time. Hadoop will
                     * automatically kill a task if it doesn't report progress
                     * after some amount of time. In order to keep reporting
                     * progress as the index is being optimized, we need to 
                     * span a new thread, which will report progress every 10
                     * seconds.
                     */
                    final TaskAttemptContext contextCpy = context;
                    Thread t = new Thread() {
                        public boolean stop = false;
                        public void run() {
                            while (!stop) {
                                contextCpy.progress();
                                try {
                                    sleep(10000);
                                } catch (InterruptedException e) {
                                    stop = true;
                                }
                            }
                        }
                    };
                    t.start();
                    
                    // Optimize and close the index
                    iwriter.optimize();
                    iwriter.close();
                    
                    // Copy from the local filesystem to HDFS
                    FileSystem fs = FileSystem.get(context.getConfiguration());
                    Path dest = getDefaultWorkFile(context, "");
                    Path source = new Path(file.getAbsolutePath());
                    fs.copyFromLocalFile(source, dest);
                    
                    // Delete the temporary index directory
                    FileUtil.fullyDelete(file);
                    
                    // Stop the progress thread
                    t.interrupt();
                    
                }
                
            };
        }
    }

}
