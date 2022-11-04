package mypta.util.misc;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class AnswerPrinter {
    public static void printAnswer(PointerAnalysisResult result) {
        try {
            PrintStream ps = new PrintStream(
                    new FileOutputStream(new File("result.txt")));
            ps.println(result.toString());
            ps.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
