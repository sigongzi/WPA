package mypta.graph;

import mypta.graph.PointsToSet;

public abstract class Pointer {
    PointsToSet pointsToSet;
    PointerType type;


    public PointsToSet getPointsToSet() {
        return pointsToSet;
    }

    public void setType(PointerType ty) {
        this.type = ty;
    }

    public void addPointsToSet(PointsToSet pts) {
        if (this.pointsToSet == null) {
            this.pointsToSet = new PointsToSet();
        }
        this.pointsToSet.addAll(pts);
    }

    public void addMemoryObject(MemoryObj mObj) {
        pointsToSet.add(mObj);
    }
    public PointerType getType() {
        return type;
    }
}
