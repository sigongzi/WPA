package mypta.solver;

import mypta.graph.*;
import mypta.handler.MethodHandler;
import mypta.handler.MethodSummary;
import mypta.handler.Pair;
import mypta.util.benchmark.BenchmarkId;
import mypta.util.benchmark.TestId;
import mypta.util.misc.PointerAnalysisResult;
import pascal.taie.World;
import pascal.taie.ir.exp.Var;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.language.classes.JField;
import pascal.taie.language.classes.JMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


public class Anderson extends Solver{



    public Anderson() {
        this.pointerFlowGraph = new PointerFlowGraph();
        this.reachableMethod = new HashSet<>();
        this.testSet = new HashSet<>();

        this.workList = new WorkList();
    }



    private PointsToSet getPointsToSetOf(Pointer pointer) {
        PointsToSet pts = pointer.getPointsToSet();
        if (pts == null) {
            pts = new PointsToSet();
        }
        return pts;
    }

    private void addPointsTo(Pointer pointer, PointsToSet pts) {
        workList.addEntry(pointer, pts);
    }

    private void addPointsTo(Pointer pointer, MemoryObj mObj) {
        PointsToSet pts = new PointsToSet();
        pts.add(mObj);
        workList.addEntry(pointer, pts);
    }

    private void addNewMethod(JMethod method) {
        reachableMethod.add(method);


        MyMethod p = pointerFlowGraph.getPointerByMethodOrSet(method);
        pointerFlowGraph.addPointerByMethod(p, method);

        // add the return value to the pointer
        workList.addEntry(p, method);
    }
    @Override
    public PointerAnalysisResult getResult() {
        ArrayList<Pair<TestId, HashSet<BenchmarkId>>> res = new ArrayList<>();

        for (Pair<TestId, MyVar> t : this.testSet) {
            HashSet<BenchmarkId> a = new HashSet<>();
            t.getSecond().getPointsToSet().getMemoryObject().forEach(
                memoryObj -> {
                    a.add(memoryObj.getBenchmarkId());
                }

            );
            res.add(new Pair<>(t.getFirst(), a));
        }

        return new PointerAnalysisResult(res);
    }

    @Override
    public void initialize() {
        System.out.println("the initialize begins");
        // add the main Method
        this.addNewMethod(World.get().getMainMethod());
        System.out.println("now the worklist is:");
        System.out.println(workList.toString());
    }
    @Override
    public void analyze() {
        while(!workList.isEmpty()) {
            WorkList.Entry entry = workList.pollEntry();
            if (entry instanceof WorkList.PointerEntry pEntry) {
                System.out.println(String.format("pointer entry %s", pEntry.toString()));
                Pointer p = pEntry.pointer();
                PointsToSet pts = pEntry.pointsToSet();
                PointsToSet diff = propagate(p, pts);
                System.out.println(String.format("DIFF set %s", diff.toString()));
                if (!diff.isEmpty() && p instanceof MyVar myvar) {
                    // In Anderson Collapse the Field
                    myvar.addPointsToSet(diff);
                    System.out.println(String.format("MYVAR pointer: %s pointstoset: %s",
                            myvar.toString(),
                            getPointsToSetOf(myvar).toString()));
                    processStore(myvar, pts);
                    processLoad(myvar, pts);
                    processCall(myvar, pts);
                }
            } else if (entry instanceof WorkList.CallEdgeEntry cEntry) {
                addReachable(cEntry.method());
            }
        }
    }

    /**
     * add a new method with parsing the intermediate method
     * @param method
     */
    @Override
    public void addReachable(JMethod method) {
        MethodSummary summary = MethodHandler.handleNewMethod(this, method);

        System.out.println(summary.newRel.toString());
        System.out.println(summary.copyRel.toString());
        System.out.println(summary.testSet.toString());



        for(Pair<Var, MemoryObj> t: summary.newRel) {
            Var v = t.getFirst();
            MemoryObj mObj = t.getSecond();
            pointerFlowGraph.addMemoryObject(mObj);
            Pointer p = pointerFlowGraph.getPointerByVarOrSet(v);
            addPointsTo(p, mObj);
        }

        for(Pair<Var, Var> t: summary.copyRel) {
            Var v1 = t.getFirst();
            Var v2 = t.getSecond();
            Pointer p1 = pointerFlowGraph.getPointerByVarOrSet(v1);
            Pointer p2 = pointerFlowGraph.getPointerByVarOrSet(v2);

            pointerFlowGraph.addOutEdge(p2, p1);
            if (p2.getPointsToSet() != null) {
                addPointsTo(p1, p2.getPointsToSet().copy());
            }
        }

        for(Pair<Var, Var> t: summary.castRel) {
            Var v1 = t.getFirst();
            Var v2 = t.getSecond();
            Pointer p1 = pointerFlowGraph.getPointerByVarOrSet(v1);
            Pointer p2 = pointerFlowGraph.getPointerByVarOrSet(v2);

            pointerFlowGraph.addOutEdge(p2, p1);
            if (p2.getPointsToSet() != null) {
                addPointsTo(p1, p2.getPointsToSet().copy());
            }
        }

        for(Pair<Var, Pair<Var, Var>> t : summary.arrayLoad) {
            Var v1 = t.getFirst();
            Var v2 = t.getSecond().getFirst();

            MyVar p1 = pointerFlowGraph.getPointerByVarOrSet(v1);
            MyVar p2 = pointerFlowGraph.getPointerByVarOrSet(v2);

            p2.addLoadFields(p1);
        }

        for(Pair<Pair<Var, Var>, Var> t : summary.arrayStore) {
            Var v1 = t.getFirst().getFirst();
            Var v2 = t.getSecond();

            MyVar p1 = pointerFlowGraph.getPointerByVarOrSet(v1);
            MyVar p2 = pointerFlowGraph.getPointerByVarOrSet(v2);

            p1.addStoreFields(p2);
        }

        for(Pair<Var, Pair<Var, JField>> t : summary.fieldLoad) {
            Var v1 = t.getFirst();
            Var v2 = t.getSecond().getFirst();

            MyVar p1 = pointerFlowGraph.getPointerByVarOrSet(v1);

            if (v2 != null) {
                // not a static field
                MyVar p2 = pointerFlowGraph.getPointerByVarOrSet(v2);

                p2.addLoadFields(p1);
            } else {
                // a static field
                MyField p2 = pointerFlowGraph.getPointerByFieldOrSet(t.getSecond().getSecond());
                pointerFlowGraph.addOutEdge(p2, p1);
                if (p2.getPointsToSet() != null) {
                    addPointsTo(p1, p2.getPointsToSet().copy());
                }
            }
        }

        for(Pair<Pair<Var, JField>, Var> t : summary.fieldStore) {
            Var v1 = t.getFirst().getFirst();
            Var v2 = t.getSecond();

            MyVar p2 = pointerFlowGraph.getPointerByVarOrSet(v2);
            if (v1 != null) {
                // not a static field
                MyVar p1 = pointerFlowGraph.getPointerByVarOrSet(v1);

                p1.addStoreFields(p2);
            } else {
                // a static field
                MyField p1 = pointerFlowGraph.getPointerByFieldOrSet(t.getFirst().getSecond());
                pointerFlowGraph.addOutEdge(p2, p1);
                if(p2.getPointsToSet() != null) {
                    addPointsTo(p1, p2.getPointsToSet().copy());
                }
            }
        }

        for(Map.Entry<Var, ArrayList<Invoke>> entry : summary.invokes.entrySet()) {
            Var v1 = entry.getKey();
            MyVar p = pointerFlowGraph.getPointerByVarOrSet(v1);
            p.addInvokes(entry.getValue());
        }

        // add return value for this JMethod

        if (!summary.returnVar.isEmpty()) {
            MyMethod m = pointerFlowGraph.getPointerByMethodOrSet(method);
            for(Var v : summary.returnVar) {
                Pointer p1= pointerFlowGraph.getPointerByVarOrSet(v);
                pointerFlowGraph.addOutEdge(p1,m);
                if (p1.getPointsToSet() != null) {
                    addPointsTo(m, p1.getPointsToSet().copy());
                }
            }
        }

        for (Pair<TestId, Var> t : summary.testSet) {
            MyVar p = pointerFlowGraph.getPointerByVarOrSet(t.getSecond());
            this.testSet.add(new Pair<>(t.getFirst(), p));
        }
        System.out.println(String.format("the worklist is %s", workList.toString()));

        System.out.println(String.format("the pointerflowgraph the pointer set is %s",
            this.pointerFlowGraph.totalPointer.toString()));
        System.out.println(String.format("the pointerflowgraph the edge set is %s",
                this.pointerFlowGraph.outEdge.toString()));

        System.out.println(String.format("the test set is %s", this.testSet.toString()));
    }

    @Override
    public void addPFGEdge(Pointer p1, Pointer p2) {
        pointerFlowGraph.addOutEdge(p1, p2);
    }

    @Override
    public void processCall(MyVar v, PointsToSet pts) {
        for (Invoke invoke : v.getInvokes()) {
            pts.getMemoryObject().forEach(memoryObj -> {
                JMethod callee = World.get().getClassHierarchy().dispatch(memoryObj.getType()
                        , invoke.getMethodRef());
                if (callee != null) {
                    if (!reachableMethod.contains(callee)) {
                        addNewMethod(callee);
                    }
                    Pointer p = pointerFlowGraph.getPointerByMethod(callee);

                    // add a node as function from this node to the var
                    pointerFlowGraph.addOutEdge(p, v);

                }
            });
        }
    }

    public void processStore(MyVar v, PointsToSet pts) {
        for (MyVar source : v.getStoreFields()) {
            pts.getMemoryObject().forEach(memoryObj -> {
                MyField p = memoryObj.getFieldPointer();
                if (p == null) {
                    p = pointerFlowGraph.getPointerByFieldOrSet(null);
                    memoryObj.setFieldPointer(p);
                }
                pointerFlowGraph.addOutEdge(source ,p);
                addPointsTo(memoryObj.getFieldPointer(), source.getPointsToSet().copy());
            });
        }
    }

    public void processLoad(MyVar v, PointsToSet pts) {
        for(MyVar target : v.getLoadFields()) {
            pts.getMemoryObject().forEach(memoryObj -> {
                MyField p = memoryObj.getFieldPointer();
                if (p == null) {
                    p = pointerFlowGraph.getPointerByFieldOrSet(null);
                    memoryObj.setFieldPointer(p);
                }
                pointerFlowGraph.addOutEdge(p, target);
                addPointsTo(target, p.getPointsToSet().copy());
            });
        }
    }

    @Override
    public PointsToSet propagate(Pointer p, PointsToSet set) {
        System.out.println(String.format("the pointstoset is %s", getPointsToSetOf(p).toString()));
        System.out.println(String.format("the newly added is %s", set.toString()));
        PointsToSet diff = getPointsToSetOf(p).allDiff(set);
        if (!diff.isEmpty()) {
            pointerFlowGraph.getOutEdge(p).forEach(tar ->
                    addPointsTo(tar, diff));
        }
        return diff;
    }

}
