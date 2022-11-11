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
import pascal.taie.ir.stmt.InstanceOf;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.language.classes.JField;
import pascal.taie.language.classes.JMethod;

import java.lang.reflect.Parameter;
import java.util.*;


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
            InfoHandler.get().printMessage(InfoLevel.DEBUG,
                    "--------------------------");
            InfoHandler.get().printMessage(InfoLevel.DEBUG,
                    "WORKLIST: process a worklist entry");
            WorkList.Entry entry = workList.pollEntry();
            if (entry instanceof WorkList.PointerEntry pEntry) {
                InfoHandler.get().printMessage(InfoLevel.DEBUG,
                        "WORKLIST: get a pointer entry");
                Pointer p = pEntry.pointer();
                PointsToSet pts = pEntry.pointsToSet();

                InfoHandler.get().printMessage(InfoLevel.DEBUG,
                        "WORKLIST: process \n Pointer <%s> with \n Points to Set <%s>",
                                p, pts);
                PointsToSet diff = propagate(p, pts);
                InfoHandler.get().printMessage(InfoLevel.DEBUG,
                        "WORKLIST: process \n Pointer <%s> with \n Diff <%s>",
                                p, diff);
                if (!diff.isEmpty() && p instanceof MyVar myvar) {
                    // In Anderson Collapse the Field
                    myvar.addPointsToSet(diff);
                    processStore(myvar, pts);
                    processLoad(myvar, pts);
                    processCall(myvar, pts);
                }
            } else if (entry instanceof WorkList.CallEdgeEntry cEntry) {
                addReachable(cEntry.method());
            }


            InfoHandler.get().printMessage(InfoLevel.DEBUG,
                    "WORKLIST: process a worklist entry ends");
            InfoHandler.get().printMessage(InfoLevel.DEBUG,
                    "--------------------------");
        }
    }

    /**
     * add a new method with parsing the intermediate method
     * @param method
     */
    @Override
    public void addReachable(JMethod method) {
        InfoHandler.get().printMessage(InfoLevel.DEBUG,
                "----------------------------------------------");
        InfoHandler.get().printMessage(InfoLevel.DEBUG,
                "ADDREACHABLE: start a new method process %s", method);
        MethodSummary summary = MethodHandler.handleNewMethod(this, method);




        for(Pair<Var, MemoryObj> t: summary.newRel) {
            Var v = t.getFirst();
            MemoryObj mObj = t.getSecond();
            pointerFlowGraph.addMemoryObject(mObj);
            Pointer p = pointerFlowGraph.getPointerByVarOrSet(v);
            InfoHandler.get().printMessage(InfoLevel.DEBUG,
                    "ADDREACHABLE: get a new object %s from %s", mObj, method);
            addPointsTo(p, mObj);
        }

        for(Pair<Var, Var> t: summary.copyRel) {
            Var v1 = t.getFirst();
            Var v2 = t.getSecond();
            Pointer p1 = pointerFlowGraph.getPointerByVarOrSet(v1);
            Pointer p2 = pointerFlowGraph.getPointerByVarOrSet(v2);

            addPFGEdge(p2, p1);
        }

        for(Pair<Var, Var> t: summary.castRel) {
            Var v1 = t.getFirst();
            Var v2 = t.getSecond();
            Pointer p1 = pointerFlowGraph.getPointerByVarOrSet(v1);
            Pointer p2 = pointerFlowGraph.getPointerByVarOrSet(v2);

            addPFGEdge(p2, p1);

        }

        for(Pair<Var, Pair<Var, Var>> t : summary.arrayLoad) {
            Var v1 = t.getFirst();
            Var v2 = t.getSecond().getFirst();

            MyVar p1 = pointerFlowGraph.getPointerByVarOrSet(v1);
            MyVar p2 = pointerFlowGraph.getPointerByVarOrSet(v2);

            p2.addLoadFields(p1);
            processLoad(p2, p2.getPointsToSet());
        }

        for(Pair<Pair<Var, Var>, Var> t : summary.arrayStore) {
            Var v1 = t.getFirst().getFirst();
            Var v2 = t.getSecond();

            MyVar p1 = pointerFlowGraph.getPointerByVarOrSet(v1);
            MyVar p2 = pointerFlowGraph.getPointerByVarOrSet(v2);
            InfoHandler.get().printMessage(InfoLevel.DEBUG,
                    "ADDREACHABLE: array store to %s with value %s", v1, v2);
            p1.addStoreFields(p2);
            processStore(p1, p1.getPointsToSet());
        }

        for(Pair<Var, Pair<Var, JField>> t : summary.fieldLoad) {
            Var v1 = t.getFirst();
            Var v2 = t.getSecond().getFirst();

            MyVar p1 = pointerFlowGraph.getPointerByVarOrSet(v1);

            if (v2 != null) {
                // not a static field
                MyVar p2 = pointerFlowGraph.getPointerByVarOrSet(v2);

                p2.addLoadFields(p1);
                processLoad(p2, p2.getPointsToSet());
            } else {
                // a static field
                assert (t.getSecond().getSecond() != null);
                MyField p2 = pointerFlowGraph.getPointerByFieldOrSet(t.getSecond().getSecond());
                addPFGEdge(p2, p1);
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
                processStore(p1, p1.getPointsToSet());
            } else {
                // a static field
                assert (t.getFirst().getSecond() != null);
                MyField p1 = pointerFlowGraph.getPointerByFieldOrSet(t.getFirst().getSecond());
                addPFGEdge(p2, p1);
            }
        }
        // call relationship is useless because we add invoke statement to the variable

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
                addPFGEdge(p1, m);
            }
        }

        for (Pair<TestId, Var> t : summary.testSet) {
            MyVar p = pointerFlowGraph.getPointerByVarOrSet(t.getSecond());
            this.testSet.add(new Pair<>(t.getFirst(), p));
        }


        // add process for static method

        for (Invoke t : summary.newStaticMethod) {
            Var v = t.getLValue();
            JMethod callee = t.getRValue().getMethodRef().resolve();
            List<Var> para_list = t.getRValue().getArgs();
            MyMethod m_pointer = pointerFlowGraph.getPointerByMethodOrSet(callee);
            // add this method to worklist
            addNewMethod(callee);
            setParametersRelationship(callee, para_list);
            if (v != null) {
                MyVar v_pointer = pointerFlowGraph.getPointerByVarOrSet(v);
                addPFGEdge(m_pointer, v_pointer);
            }
        }


        InfoHandler.get().printMessage(InfoLevel.DEBUG,
                "ADDREACHABLE: end a new method process %s", method);

        InfoHandler.get().printMessage(InfoLevel.DEBUG,
                "----------------------------------------------");

    }

    public void setParametersRelationship(JMethod callee, List<Var> paraList) {
        assert (paraList.size() == callee.getIR().getParams().size());

        Iterator<Var> para = paraList.iterator();
        Iterator<Var> iter_callee = callee.getIR().getParams().iterator();
        while (para.hasNext() && iter_callee.hasNext()) {
            MyVar v1 = pointerFlowGraph.getPointerByVarOrSet(para.next());
            MyVar v2 = pointerFlowGraph.getPointerByVarOrSet(iter_callee.next());
            addPFGEdge(v1, v2);

        }
    }

    @Override
    public void addPFGEdge(Pointer p1, Pointer p2) {
        InfoHandler.get().printMessage(InfoLevel.DEBUG,
                "ADDPFGEDGE: add a edge from %s to %s", p1, p2);
        pointerFlowGraph.addOutEdge(p1, p2);
        if (p1.getPointsToSet() != null) {
            propagate(p2, p1.getPointsToSet().copy());
        }
    }

    @Override
    public void processCall(MyVar v, PointsToSet pts) {
        for (Invoke invoke : v.getInvokes()) {
            pts.getMemoryObject().forEach(memoryObj -> {

                JMethod callee = World.get().getClassHierarchy().dispatch(memoryObj.getType()
                        , invoke.getMethodRef());

                if (callee != null) {
                    setParametersRelationship(
                            callee, invoke.getInvokeExp().getArgs());
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
                InfoHandler.get().printMessage(InfoLevel.DEBUG,
                        "PROCESSSTORE:%s store the memoryObj %s's field", source, memoryObj);
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
                InfoHandler.get().printMessage(InfoLevel.DEBUG,
                        "PROCESSLOAD: %s load from the memoryObj %s's field", target, memoryObj);
                MyField p = memoryObj.getFieldPointer();
                if (p == null) {
                    p = pointerFlowGraph.getPointerByFieldOrSet(null);
                    memoryObj.setFieldPointer(p);
                }


                addPFGEdge(p, target);

            });
        }
    }

    @Override
    public PointsToSet propagate(Pointer p, PointsToSet set) {
        PointsToSet diff = getPointsToSetOf(p).allDiff(set);
        if (!diff.isEmpty()) {
            pointerFlowGraph.getOutEdge(p).forEach(tar ->
                    addPointsTo(tar, diff));
        }
        return diff;
    }

}
