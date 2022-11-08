package mypta.graph;


import pascal.taie.util.Copyable;

import java.util.HashSet;

public class PointsToSet implements Copyable<PointsToSet> {

    HashSet<MemoryObj> edge;

    public PointsToSet() {
        edge = new HashSet<>();
    }

    public PointsToSet(PointsToSet pts) {
        this.edge = new HashSet<>(pts.edge);
    }

    public PointsToSet(HashSet e) {
        this.edge = e;
    }

    public void addAll(PointsToSet pts) {
        this.edge.addAll(pts.edge);
    }

    public PointsToSet allDiff(PointsToSet another) {
        PointsToSet diff = another.copy();
        diff.edge.removeAll(this.edge);

        return diff;

    }
    public void add(MemoryObj mObj) {
        edge.add(mObj);
    }

    public HashSet<MemoryObj> getMemoryObject() {
        return this.edge;
    }
    public boolean isEmpty() {
        return this.edge.isEmpty();
    }
    @Override
    public PointsToSet copy()
    {
        return new PointsToSet(this); //invoke copy constructor
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder("<PointsToSet: ");
        edge.forEach(memoryObj -> {
            res.append(memoryObj.toString());
        });
        res.append(">");
        return res.toString();
    }
}
