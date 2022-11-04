package mypta;

import mypta.util.benchmark.BenchmarkInfo;
import mypta.util.misc.AnswerPrinter;
import pascal.taie.World;
import pascal.taie.analysis.ProgramAnalysis;
import pascal.taie.analysis.pta.core.cs.selector.ContextSelector;
import pascal.taie.analysis.pta.core.cs.selector.ContextSelectorFactory;
import pascal.taie.analysis.pta.core.heap.AllocationSiteBasedModel;
import pascal.taie.analysis.pta.core.heap.HeapModel;
import pascal.taie.config.AnalysisConfig;
import mypta.util.misc.PointerAnalysisResult;
import mypta.solver.Solver;
import mypta.solver.Anderson;
import pascal.taie.config.AnalysisOptions;
import pascal.taie.ir.stmt.Stmt;

import java.util.Iterator;

public class PointerAnalysis extends ProgramAnalysis<PointerAnalysisResult> {
    public static final String ID = "mypta";

    public PointerAnalysis(AnalysisConfig config) {
        super(config);
    }

    private void PrintIRForMainClass() {
        Iterator<Stmt> iter = World.get().getMainMethod().getIR().getStmts().iterator();
        while(iter.hasNext()) {
            Stmt s = iter.next();
            System.out.println(s);
            System.out.println(s.getClass());
        }
    }

    private void PrintCallGraph() {
        // no call graph?
    }

    @Override
    public PointerAnalysisResult analyze() {
        System.out.println("now enter the analysis");

        BenchmarkInfo.initalize();

        AnalysisOptions options = getOptions();
        HeapModel heapModel = new AllocationSiteBasedModel(options);
        ContextSelector selector = ContextSelectorFactory.makeCISelector();
        Solver solver = InitializeSolverByName((String) options.get("name"),options, heapModel, selector);
        solver.solve();
        //this.PrintIRForMainClass();


        PointerAnalysisResult res = solver.getResult();
        AnswerPrinter.printAnswer(res);

        return res;
    }

    private Solver InitializeSolverByName(String name, AnalysisOptions options, HeapModel heapModel,
                                          ContextSelector selector) {
        if (name.equals("Anderson")) {
            return new Anderson();
        }
        else {
            throw new IllegalArgumentException(
                    "Unsupported solve algorithm:" + name);
        }
    }

}
