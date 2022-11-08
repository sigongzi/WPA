package mypta.util.benchmark;

public class TestId implements Comparable<TestId>{
    int testId;
    public TestId(int testId) {
        this.testId = testId;
    }
    public int getTestId() {
        return this.testId;
    }
    @Override
    public int compareTo(TestId compareTestId) {
        return this.testId - compareTestId.testId;
    }

    @Override
    public String toString() {
        return String.format("%d", this.testId);
    }
}
