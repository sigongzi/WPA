package mypta.util.misc;

import mypta.handler.Pair;
import mypta.util.benchmark.BenchmarkId;
import mypta.util.benchmark.TestId;

import java.util.*;
import java.util.stream.Collectors;

public class PointerAnalysisResult {
    List<Pair<TestId, HashSet<BenchmarkId>>> result;
    public PointerAnalysisResult(List<Pair<TestId, HashSet<BenchmarkId>>> result) {
        this.result = result;
    }
    public static Comparator<Pair<TestId, HashSet<BenchmarkId>>> PairComparator
            = new Comparator<Pair<TestId, HashSet<BenchmarkId>>>() {

        public int compare(Pair<TestId, HashSet<BenchmarkId>> pair1
                , Pair<TestId, HashSet<BenchmarkId>> pair2) {


            //ascending order
            return pair1.getFirst().compareTo(pair2.getFirst());

            //descending order
            //return fruitName2.compareTo(fruitName1);
        }

    };
    @Override
    public String toString() {
        String res = "";
        Collections.sort(this.result,  this.PairComparator);
        for (Pair<TestId, HashSet<BenchmarkId>> t : this.result) {
            ArrayList<BenchmarkId> tmp = new ArrayList<>(t.getSecond());
            Collections.sort(tmp);
            String append = String.format("%d: %s\n", t.getFirst().getTestId(),
                    String.join(" ", tmp.stream().map(id ->
                        String.format("%d", id.getBenchmarkId())
                    ).distinct().toList())
            );
            res = res + append;
        }

        return res;
    }
}
