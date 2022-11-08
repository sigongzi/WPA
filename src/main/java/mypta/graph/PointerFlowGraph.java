package mypta.graph;

import pascal.taie.ir.exp.Var;
import pascal.taie.language.classes.JField;
import pascal.taie.language.classes.JMethod;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;

public class PointerFlowGraph {
    public HashMap<Pointer, HashSet<Pointer>> outEdge;

    public HashSet<Pointer> totalPointer;

    HashMap<JMethod, MyMethod> getPointerByMethod;

    HashMap<JField, MyField> getPointerByField;

    HashMap<Var, MyVar> getPointerByVar;

    HashSet<MemoryObj> totalMemoryObject;
    int totalPointerCount = 0;
    int totalMemoryCount = 0;

    public PointerFlowGraph() {

        outEdge = new HashMap<>();
        totalPointer = new HashSet<>();
        totalMemoryObject = new HashSet<>();
        getPointerByVar = new HashMap<>();
        getPointerByMethod = new HashMap<>();
        getPointerByField = new HashMap<>();
        totalPointerCount = 0;
        totalMemoryCount = 0;
    }

    public void addOutEdge(Pointer p1, Pointer p2) {
        if(!outEdge.containsKey(p1)) {
            outEdge.put(p1, new HashSet<>());
        }
        outEdge.get(p1).add(p2);
    }
    public HashSet<Pointer> getOutEdge(Pointer p) {
        if(outEdge.containsKey(p)) {
            return outEdge.get(p);
        }
        else return new HashSet<>();
    }

    public void addPointer(Pointer p) {

        totalPointer.add(p);
        p.setId(++totalPointerCount);
    }
    public void addMemoryObject(MemoryObj mObj) {
        totalMemoryObject.add(mObj);
        mObj.setId(++totalMemoryCount);
    }
    public void addPointerByMethod(MyMethod p, JMethod method) {
        getPointerByMethod.put(method, p);
    }

    public void addPointerByVar(MyVar p, Var v) {
        getPointerByVar.put(v, p);
    }

    public void addPointerByField(MyField p, JField v) {
        getPointerByField.put(v,p);
    }
    @Nullable
    public MyMethod getPointerByMethod(JMethod method) {
        if (getPointerByMethod.containsKey(method)) {
            return getPointerByMethod.get(method);
        }
        return null;
    }


    @Nullable
    public MyVar getPointerByVar(Var v) {
        if (getPointerByVar.containsKey(v)) {
            return getPointerByVar.get(v);
        }
        return null;
    }

    @Nullable
    public MyField getPointerByField(JField f) {
        if (getPointerByField.containsKey(f)) {
            return getPointerByField.get(f);
        }
        return null;
    }
    public MyMethod getPointerByMethodOrSet(JMethod method) {
        MyMethod p = this.getPointerByMethod(method);
        if (p == null) {
            p = this.createByMethod(method);
            this.addPointerByMethod(p, method);
        }
        return p;
    }
    public MyVar getPointerByVarOrSet(Var v) {
        MyVar p = this.getPointerByVar(v);
        if (p == null) {
            p = this.createByVar(v);
            this.addPointerByVar(p, v);
        }
        return p;
    }

    public MyField getPointerByFieldOrSet(JField f) {
        MyField p = this.getPointerByField(f);
        if (p == null) {
            p = this.createByField(f);
            if (f != null) {
                this.addPointerByField(p, f);
            }
        }
        return p;
    }

    MyMethod createByMethod(JMethod m) {
        MyMethod p = new MyMethod(m);
        this.addPointer(p);
        return p;
    }

    MyVar createByVar(Var v) {
        MyVar p = new MyVar(v);
        this.addPointer(p);
        return p;
    }

    MyField createByField(JField f) {
        MyField p = new MyField(f);
        this.addPointer(p);
        return p;
    }

    MyField createByField() {
        MyField p = new MyField(null);
        this.addPointer(p);
        return p;
    }

}
