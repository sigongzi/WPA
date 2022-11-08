package mypta.graph;

import mypta.graph.PointsToSet;

public abstract class Pointer {
    public PointsToSet pointsToSet;
    PointerType type;

    int id;

    public PointsToSet getPointsToSet() {
        if (this.pointsToSet == null) {
            this.pointsToSet = new PointsToSet();
        }
        return this.pointsToSet;
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
        if (this.pointsToSet == null) {
            this.pointsToSet = new PointsToSet();
        }
        this.pointsToSet.add(mObj);
    }
    public PointerType getType() {
        return type;
    }
    void setId(int id) {
        this.id = id;
    }

    public abstract String getRef();
    @Override
    public String toString() {
        return String.format("<Pointer PointerId: %d PointerRef: %s>", id, this.getRef());
    }
}
