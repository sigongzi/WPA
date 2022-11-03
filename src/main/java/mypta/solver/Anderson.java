package mypta.solver;
import pascal.taie.analysis.ProgramAnalysis;
import pascal.taie.config.AnalysisConfig;
import mypta.util.PointerAnalysisResult;
public class Anderson extends ProgramAnalysis<PointerAnalysisResult> {
    public static final String ID = "myAnderson";

    public Anderson(AnalysisConfig config) {
        super(config);
    }

    @Override
    public PointerAnalysisResult analyze() {

    }

}
