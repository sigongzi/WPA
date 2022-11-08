package mypta.handler;

import mypta.util.benchmark.TestId;

public class Pair<T1,T2>{
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
        return String.format("Pair: <OBJ1>.%s <OBJ2>.%s", obj1.toString(), obj2.toString());
    }
}
