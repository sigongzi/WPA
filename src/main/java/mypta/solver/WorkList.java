package mypta.worklist;

import pascal.taie.analysis.pta.core.cs.element.Pointer;
import pascal.taie.analysis.pta.pts.PointsToSet;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Represents work list in pointer analysis.
 */
public class WorkList {

    private final Queue<WorkListEntry> entries = new ArrayDeque<>();

    /**
     * Adds an entry to the work list.
     */
    public void addEntry(Pointer pointer, PointsToSet pointsToSet) {
        entries.add(new WorkListEntry(pointer, pointsToSet));
    }

    /**
     * Retrieves and removes an entry from this queue, or returns null
     * if this work list is empty.
     */
    public WorkListEntry pollEntry() {
        return entries.poll();
    }

    /**
     * @return true if the work list is empty, otherwise false.
     */
    public boolean isEmpty() {
        return entries.isEmpty();
    }

}
