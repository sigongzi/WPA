package mypta.graph;

import pascal.taie.ir.exp.Var;
import pascal.taie.ir.stmt.*;

import java.util.ArrayList;
import java.util.List;

public class MyVar extends Pointer {
    Var var;
    List<Invoke> invokes;
    List<MyVar> loadFields;
    List<MyVar> storeFields;

    public MyVar(Var v) {
        this.var = v;
        invokes = new ArrayList<>();
        loadFields = new ArrayList<>();
        storeFields = new ArrayList<>();
    }
    public Var getVar() {
        return var;
    }
    public List<Invoke> getInvokes() {
        return this.invokes;
    }

    public void addLoadFields(MyVar v) {
        loadFields.add(v);
    }

    public void addStoreFields(MyVar v) {
        storeFields.add(v);
    }

    public void addInvokes(ArrayList<Invoke> l) {
        this.invokes.addAll(l);
    }

    public List<MyVar> getStoreFields() {
        return this.storeFields;
    }

    public  List<MyVar> getLoadFields() {
        return this.loadFields;
    }

    @Override
    public String getRef() {
        return String.format("%s from %s", var.toString(), var.getMethod().toString());
    }
}
