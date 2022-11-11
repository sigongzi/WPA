package mypta.solver;

import com.sun.istack.NotNull;

import javax.annotation.Nullable;

public class InfoHandler {
    InfoLevel level;
    static InfoHandler infoHandler;
    public static void setInfoHandler() {
        infoHandler = new InfoHandler();
    }
    public static InfoHandler get() {
        return infoHandler;
    }
    public InfoHandler() {
        level = InfoLevel.DEBUG;
    }

    public void setInfoLevel(InfoLevel l) {
        this.level = l;
    }
    public void printMessage(InfoLevel l, @NotNull String format, @Nullable Object... args) {
        if (l.compareTo(this.level) <= 0) {
            System.out.println(String.format("[%s] %s", l.name(), String.format(format, args)));
        }
    }
}
