package mypta.graph;

import mypta.util.benchmark.BenchmarkId;
import pascal.taie.ir.exp.NewExp;
import pascal.taie.language.type.ReferenceType;
import pascal.taie.language.type.Type;


public class MemoryObj {
    BenchmarkId benchmarkId;
    ReferenceType type;

    MyField fieldPointer;
    public MemoryObj(BenchmarkId benchmarkId, ReferenceType type) {
        this.benchmarkId = benchmarkId;
        this.type = type;
    }

    public Type getType() {
        return this.type;
    }

    public MyField getFieldPointer() {
        return this.fieldPointer;
    }

    public BenchmarkId getBenchmarkId() {
        return this.benchmarkId;
    }
    public void setFieldPointer(MyField p) {
        this.fieldPointer = p;
    }
}
