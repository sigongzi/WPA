package mypta;


import mypta.solver.InfoHandler;

/**
 * @author ivory
 * usage java -jar xxx.jar [src] [test_class]
 * example
 * code/
 * test.Hello
 */
public class Main {
    public static void main(String[] args) {
        String srcDirectory = args[0];
        String mainClass = args[1];

        InfoHandler.setInfoHandler();
        String[] taieArgs = new String[]{"-pp","-cp",srcDirectory,"-m",mainClass, "-a",
                "mypta=name:Anderson"};
        pascal.taie.Main.main(taieArgs);
    }
}