package mypta.handler;


import fj.Hash;
import mypta.graph.MemoryObj;
import mypta.util.benchmark.BenchmarkId;
import mypta.util.benchmark.TestId;
import pascal.taie.ir.exp.Var;
import pascal.taie.ir.proginfo.MethodRef;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.ir.stmt.Stmt;
import pascal.taie.language.classes.JField;
import pascal.taie.language.classes.JMethod;

import javax.management.relation.Relation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;



public class MethodSummary {
    public HashSet<Var> methodParams;
    public HashSet<Var> returnVar;
    public HashSet<JMethod> newStaticMethod;
    public HashSet<Pair<TestId, Var>> testSet;

    public HashMap<BenchmarkId, MemoryObj> allocMap;
    public HashSet<Pair<Var, Var>> copyRel;

    public HashSet<Pair<Var, Var>> castRel;
    public HashSet<Pair<Var, MemoryObj>> newRel;
    public HashSet<Pair<Var, Pair<Var,JMethod>>> callRel;

    public HashSet<Pair<Var, Pair<Var, JField>>> fieldLoad;

    public HashSet<Pair<Pair<Var, JField>,Var>> fieldStore;

    public HashSet<Pair<Var, Pair<Var, Var>>> arrayLoad;

    public HashSet<Pair<Pair<Var,Var>, Var>> arrayStore;

    public HashMap<Var, ArrayList<Stmt>> fieldAccess;

    public HashMap<Var, ArrayList<Invoke>> invokes;

    public MethodSummary() {
        methodParams = new HashSet<>();
        returnVar = new HashSet<>();
        copyRel = new HashSet<>();
        castRel = new HashSet<>();
        newRel = new HashSet<>();
        callRel = new HashSet<>();
        fieldLoad = new HashSet<>();
        fieldStore = new HashSet<>();
        arrayLoad = new HashSet<>();
        arrayStore = new HashSet<>();
        newStaticMethod  = new HashSet<>();

        testSet = new HashSet<>();
        allocMap = new HashMap<>();
        fieldAccess = new HashMap<>();
        invokes = new HashMap<>();

    }
    public void setMethodParams(Iterable<Var> varSet) {
        for(Var v : varSet) {
            methodParams.add(v);
        }
    }

    public void addReturnVar(Var v) {
        returnVar.add(v);
    }
    public void addCopyRelation(Var a, Var b) {
        copyRel.add(new Pair(a,b));
    }

    public void addNewRelation(Var a, MemoryObj b) {
        newRel.add(new Pair(a,b));
    }

    public void addCallRelation(Var a, Var b, MethodRef methodref) {
        callRel.add(new Pair(a, new Pair(b, methodref)));
    }

    public void addFieldLoad(Var a, Var b, JField c) {
        fieldLoad.add(new Pair(a, new Pair<>(b,c)));
    }

    public void addFieldStore(Var b, JField c, Var a) {
        fieldStore.add(new Pair<>(new Pair<>(b,c), a));
    }

    public void addArrayLoad(Var a, Var base, Var index) {
        arrayLoad.add(new Pair<>(a, new Pair<>(base, index)));
    }
    public void addArrayStore(Var a, Var base, Var index) {
        arrayStore.add(new Pair<>(new Pair<>(base, index), a));
    }

    public void addCastRelation(Var a, Var b) {
        castRel.add(new Pair<>(a, b));
    }
    public void addStaticMethod(JMethod m) {
        newStaticMethod.add(m);
    }
    public void addTestMap(TestId t,Var a) {
        testSet.add(new Pair<>(t, a));
    }
    public void addAllocMap(BenchmarkId t, MemoryObj a) {allocMap.put(t, a);}

    public void addVarInvoke(Var v, Invoke iv) {
        if (!this.invokes.containsKey(v)) {
            this.invokes.put(v, new ArrayList<>());
        }
        this.invokes.get(v).add(iv);
    }

    public void addVarFieldAccess(Var v, Stmt s) {
        if(!this.fieldAccess.containsKey(v)) {
            this.invokes.put(v, new ArrayList<>());
        }
        this.fieldAccess.get(v).add(s);
    }
}
