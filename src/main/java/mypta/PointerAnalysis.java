package mypta;

import mypta.util.benchmark.BenchmarkInfo;
import mypta.util.misc.AnswerPrinter;
import pascal.taie.World;
import pascal.taie.analysis.ProgramAnalysis;
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

    private void PrintIRForMainMethod() {
        Iterator<Stmt> iter = World.get().getMainMethod().getIR().getStmts().iterator();
        while(iter.hasNext()) {
            Stmt s = iter.next();
            System.out.println(s);
            System.out.println(s.getClass());
        }
    }


    @Override
    public PointerAnalysisResult analyze() {
        BenchmarkInfo.initalize();

        AnalysisOptions options = getOptions();

        Solver solver = InitializeSolverByName((String) options.get("name"));
        solver.solve();


        PointerAnalysisResult res = solver.getResult();
        AnswerPrinter.printAnswer(res);

        return res;
    }

    private Solver InitializeSolverByName(String name) {
        if (name.equals("Anderson")) {
            return new Anderson();
        }
        else {
            throw new IllegalArgumentException(
                    "Unsupported solve algorithm:" + name);
        }
    }

}
