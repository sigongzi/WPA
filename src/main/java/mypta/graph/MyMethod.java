package mypta.graph;

import pascal.taie.language.classes.JMethod;

public class MyMethod extends Pointer{

    JMethod method;
    public MyMethod(JMethod method) {
        this.method = method;
    }

    @Override
    public String getRef() {
        return method.toString();
    }
}
