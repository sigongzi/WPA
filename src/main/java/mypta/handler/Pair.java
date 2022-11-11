package mypta.handler;

import mypta.util.benchmark.TestId;

public class Pair<T1, T2> {
    T1 obj1;
    T2 obj2;

    public T1 getFirst() {
        return obj1;
    }

    public T2 getSecond() {
        return obj2;
    }

    public Pair(T1 a, T2 b) {
        obj1 = a;
        obj2 = b;
    }

    @Override
    public String toString() {
        String str1 = "null";
        if (obj1 != null) {
            str1 = obj1.toString();
        }
        String str2 = "null";
        if (obj2 != null) {
            str2 = obj2.toString();
        }
        return String.format("<%s, %s>", str1, str2);
    }
}
