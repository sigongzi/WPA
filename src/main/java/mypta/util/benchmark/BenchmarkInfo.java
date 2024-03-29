package mypta.util.benchmark;

import pascal.taie.World;
import pascal.taie.language.classes.JClass;
import pascal.taie.language.classes.JMethod;

public class BenchmarkInfo {
    JMethod test, alloc;
    private static BenchmarkInfo theInfo;
    public static void initalize() {
        theInfo = new BenchmarkInfo();
        JClass benchmark = World.get().getClassHierarchy().getClass("benchmark.internal.BenchmarkN");
        if (benchmark != null) {
            theInfo.test = benchmark.getDeclaredMethod("test");
            theInfo.alloc = benchmark.getDeclaredMethod("alloc");
        }
    }
    public static BenchmarkInfo get() {
        return theInfo;
    }

    public JMethod getTest() {
        return this.test;
    }

    public JMethod getAlloc() {
        return this.alloc;
    }
}
