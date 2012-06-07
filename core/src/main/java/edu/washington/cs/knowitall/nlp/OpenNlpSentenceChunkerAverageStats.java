package edu.washington.cs.knowitall.nlp;

public class OpenNlpSentenceChunkerAverageStats extends
        OpenNlpSentenceChunkerStats {
    private int count;

    public OpenNlpSentenceChunkerAverageStats() {
        super(0, 0, 0, 0);
        count = 0;
    }

    public void add(OpenNlpSentenceChunkerStats stats) {
        this.tagTime += stats.tagTime();
        this.tokenizeTime += stats.tokenizeTime();
        this.chunkTime += stats.chunkTime();
        this.totalTime += stats.totalTime();
        count++;
    }

    public OpenNlpSentenceChunkerStats totalStats() {
        return new OpenNlpSentenceChunkerStats(this.totalTime,
                this.tokenizeTime, this.tagTime, this.chunkTime);
    }

    public long tagTime() {
        return super.tagTime() / count;
    }

    public long tokenizeTime() {
        return super.tokenizeTime() / count;
    }

    public long chunkTime() {
        return super.chunkTime() / count;
    }

    public long totalTime() {
        return super.totalTime() / count;
    }
}
