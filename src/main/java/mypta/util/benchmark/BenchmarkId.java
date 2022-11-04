package mypta.util.benchmark;

public class BenchmarkId implements  Comparable<BenchmarkId>{
    int benchmarkId;
    public BenchmarkId(int benchmarkId) {
        this.benchmarkId = benchmarkId;
    }
    public int getBenchmarkId() {
        return benchmarkId;
    }
    @Override
    public int compareTo(BenchmarkId compareBenchmarkId) {
        return this.benchmarkId - compareBenchmarkId.benchmarkId;
    }
}
