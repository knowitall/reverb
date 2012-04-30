package edu.washington.cs.knowitall.nlp;

public class OpenNlpSentenceChunkerStats {
    protected long totalTime;
    protected long tokenizeTime;
    protected long tagTime;
    protected long chunkTime;

    public OpenNlpSentenceChunkerStats(long totalTime, long tokenizeTime,
            long tagTime, long chunkTime) {
        this.totalTime = totalTime;
        this.tokenizeTime = tokenizeTime;
        this.tagTime = tagTime;
        this.chunkTime = chunkTime;
    }

    public String toString() {
        return "time(" + this.totalTime() + "): " + "tokenize("
                + this.tokenizeTime() + " ms), " + "tag(" + this.tagTime()
                + " ms), " + "chunk(" + this.chunkTime() + " ms)";
    }

    public long totalTime() {
        return this.totalTime;
    }

    public long tokenizeTime() {
        return this.tokenizeTime;
    }

    public long tagTime() {
        return this.tagTime;
    }

    public long chunkTime() {
        return this.chunkTime;
    }
}
