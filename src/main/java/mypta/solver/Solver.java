package mypta.solver;


import mypta.graph.*;
import mypta.handler.Pair;
import mypta.util.benchmark.TestId;
import mypta.util.misc.PointerAnalysisResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pascal.taie.ir.exp.Var;
import pascal.taie.language.classes.JMethod;

import java.util.HashMap;
import java.util.HashSet;

public abstract class Solver {

    protected static final Logger logger = LogManager.getLogger(Solver.class);


    protected PointerFlowGraph pointerFlowGraph;

    protected WorkList workList;

    protected PointerAnalysisResult result;

    protected HashSet<JMethod> reachableMethod;

    protected HashSet<Pair<TestId, MyVar>> testSet;

    public void solve() {
        System.out.println("the analysis begins");
        this.initialize();
        this.analyze();
    }

    public abstract void initialize();

    public abstract void analyze();
    public abstract PointerAnalysisResult getResult();

    public abstract void addReachable(JMethod method);

    public abstract void addPFGEdge(Pointer p1, Pointer p2);


    public abstract void processCall(MyVar v, PointsToSet pts);

    public abstract PointsToSet propagate(Pointer p, PointsToSet set);
}
