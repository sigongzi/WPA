package mypta.graph;

import mypta.util.benchmark.BenchmarkId;
import pascal.taie.ir.exp.NewExp;
import pascal.taie.language.type.ReferenceType;
import pascal.taie.language.type.Type;


public class MemoryObj {
    BenchmarkId benchmarkId;
    ReferenceType type;

    MyField fieldPointer;
    int id;
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

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("<memory object: id ");
        res.append(this.id);
        res.append(" benchmarkId ");
        res.append(this.benchmarkId.getBenchmarkId());
        res.append(">");
        return res.toString();
    }
}
