package mypta.handler;

import mypta.graph.MemoryObj;
import mypta.solver.InfoHandler;
import mypta.solver.InfoLevel;
import mypta.util.benchmark.BenchmarkId;
import mypta.util.benchmark.BenchmarkInfo;
import mypta.solver.Solver;
import mypta.util.benchmark.TestId;
import pascal.taie.World;
import pascal.taie.analysis.pta.core.cs.element.CSMethod;
import pascal.taie.ir.exp.*;
import pascal.taie.ir.proginfo.MethodRef;
import pascal.taie.ir.stmt.*;
import pascal.taie.language.classes.JField;
import pascal.taie.language.classes.JMethod;

public class MethodHandler {
    public static MethodSummary handleNewMethod(Solver solver, JMethod method) {
        InfoHandler.get().printMessage(InfoLevel.DEBUG,
                "---------------------------------------------------------");
        InfoHandler.get().printMessage(InfoLevel.DEBUG, method.toString());
        method.getIR().forEach(stmt -> {
            boolean isBenchMark = false;
            if (stmt instanceof Invoke i) {
                JMethod invokeMethod = i.getMethodRef().resolveNullable();
                if (invokeMethod != null && (invokeMethod.equals(BenchmarkInfo.get().getAlloc()) || invokeMethod.equals(BenchmarkInfo.get().getTest()))) {
                    isBenchMark = true;
                }
            }
            if (!isBenchMark) {
                InfoHandler.get().printMessage(InfoLevel.DEBUG, "[IR] %s", stmt.toString());
            }
        });
        InfoHandler.get().printMessage(InfoLevel.DEBUG,
                "---------------------------------------------------------");
        MethodSummary res = new MethodSummary();
        res.setMethodParams(method.getIR().getParams());
        int benchmarkId = 0;
        for (Stmt s : method.getIR()) {
            if (s instanceof AssignStmt<?, ?> a) {
                if (a instanceof New nw) {
                    Var l = nw.getLValue();
                    BenchmarkId bid = new BenchmarkId(benchmarkId);
                    MemoryObj m = new MemoryObj(bid, nw.getRValue().getType());
                    InfoHandler.get().printMessage(InfoLevel.DEBUG,
                            "a new statement %s, with left var %s", nw.toString(), l.toString());
                    if (benchmarkId != 0) {
                        res.addAllocMap(bid, m);
                    }


                    res.addNewRelation(l, m);

                    benchmarkId = 0;
                } else if (a instanceof Copy cp) {
                    res.addCopyRelation(cp.getLValue(), cp.getRValue());
                } else if (a instanceof Cast c) {
                    res.addCastRelation(c.getLValue(), c.getRValue().getValue());
                } else if (a instanceof LoadField load) {
                    if (load.getFieldAccess() instanceof InstanceFieldAccess ifa) {
                        // maybe nullable
                        res.addFieldLoad(load.getLValue(), ifa.getBase(), ifa.getFieldRef().resolveNullable());
                    } else if (load.getFieldAccess() instanceof StaticFieldAccess sfa) {
                        res.addFieldLoad(load.getLValue(), null, sfa.getFieldRef().resolveNullable());
                    }
                } else if (a instanceof StoreField store) {
                    JField field = store.getFieldRef().resolveNullable();
                    if (store.getFieldAccess() instanceof InstanceFieldAccess ifa) {
                        // maybe nullable
                        res.addFieldStore(ifa.getBase(), ifa.getFieldRef().resolveNullable(), store.getRValue());
                    } else if (store.getFieldAccess() instanceof StaticFieldAccess sfa) {
                        res.addFieldStore(null, sfa.getFieldRef().resolveNullable(), store.getRValue());
                    }
                } else if (a instanceof LoadArray load) {
                    Var base = load.getArrayAccess().getBase();
                    Var index = load.getArrayAccess().getIndex();
                    res.addArrayLoad(load.getLValue(), base, index);
                } else if (a instanceof StoreArray store) {
                    Var base = store.getArrayAccess().getBase();
                    Var index = store.getArrayAccess().getIndex();
                    res.addArrayStore(base, index, store.getRValue());
                }
            } else if (s instanceof Invoke i) {
                JMethod invokeMethod = i.getMethodRef().resolveNullable();
                boolean isBenchMark = false;
                if (invokeMethod != null) {
                    if (invokeMethod.equals(BenchmarkInfo.get().getAlloc())) {
                        if (i.getInvokeExp().getArg(0).getConstValue() instanceof IntLiteral num) {
                            benchmarkId = num.getValue();
                        }
                        isBenchMark = true;
                    } else if (invokeMethod.equals(BenchmarkInfo.get().getTest())) {
                        Var l = i.getInvokeExp().getArg(0);
                        Var r = i.getInvokeExp().getArg(1);
                        if (l.getConstValue() instanceof IntLiteral num) {
                            int testId = num.getValue();

                            res.addTestMap(new TestId(testId), r);
                        }
                        isBenchMark = true;
                    }
                }
                if (!isBenchMark) {
                    // It is not benchmark invoke
                    Exp e = i.getInvokeExp();
                    Var v = i.getLValue();
                    // Var may be null here
                    String mainclass = World.get().getMainMethod().getDeclaringClass().toString();
                    mainclass = mainclass.substring(0, mainclass.indexOf("."));
                    if (e instanceof InvokeStatic is) {
                        JMethod m = is.getMethodRef().resolveNullable();
                        if (m != null && (m.getDeclaringClass().toString().indexOf(mainclass) != -1)) {
                            res.addStaticMethod(i);

                        }

                    } else if (e instanceof InvokeVirtual iv) {
                        Var base = iv.getBase();
                        MethodRef methodref = iv.getMethodRef();
                        // use dispatch for method ref
                        res.addCallRelation(v, base, methodref);
                        res.addVarInvoke(base, i);
                    } else if (e instanceof InvokeDynamic id) {
                        // TODO: What is it?
                    } else if (e instanceof InvokeSpecial is) {
                        Var base = is.getBase();
                        MethodRef methodref = is.getMethodRef();
                        res.addCallRelation(v, base, methodref);
                        res.addVarInvoke(base, i);
                    } else if (e instanceof InvokeInterface ii) {
                        Var base = ii.getBase();
                        MethodRef methodref = ii.getMethodRef();
                        res.addCallRelation(v, base, methodref);
                        res.addVarInvoke(base, i);
                    }
                }
            } else if (s instanceof Return r) {
                Var a = r.getValue();
                if (a != null) {
                    res.addReturnVar(a);
                }
            }
        }
        InfoHandler.get().printMessage(InfoLevel.DEBUG,
                "---------------------------------------------------------");
        return res;
    }
}
