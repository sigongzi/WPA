package test;

import benchmark.internal.BenchmarkN;

public class test_2000012957 {
    public static void main(String[] args) {
        A a = S.geta();
        BenchmarkN.test(1, a); // 1: 1
    }
}

class S {
    private static final A a;

    static {
        BenchmarkN.alloc(1);
        a = new A();
    }

    public static A geta() {
        return a;
    }

}