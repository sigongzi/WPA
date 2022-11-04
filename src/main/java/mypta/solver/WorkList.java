/*
 * Tai-e: A Static Analysis Framework for Java
 *
 * Copyright (C) 2022 Tian Tan <tiantan@nju.edu.cn>
 * Copyright (C) 2022 Yue Li <yueli@nju.edu.cn>
 *
 * This file is part of Tai-e.
 *
 * Tai-e is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * Tai-e is distributed in the hope that it will be useful,but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Tai-e. If not, see <https://www.gnu.org/licenses/>.
 */

package mypta.solver;

import mypta.graph.Pointer;
import mypta.graph.PointsToSet;
import mypta.handler.Pair;
import pascal.taie.language.classes.JMethod;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * Represents work list in pointer analysis.
 */
final class WorkList {

    /**
     * Pointer entries to be processed.
     */
    private final Map<Pointer, PointsToSet> pointerEntries = new LinkedHashMap<>();

    /**
     * Call edges to be processed.
     */
    private final Queue<Pair<Pointer, JMethod>> callEdges = new ArrayDeque<>();

    void addEntry(Pointer pointer, PointsToSet pointsToSet) {
        PointsToSet set = pointerEntries.get(pointer);
        if (set != null) {
            set.addAll(pointsToSet);
        } else {
            pointerEntries.put(pointer, pointsToSet.copy());
        }

    }

    void addEntry(Pointer p, JMethod m) {
        callEdges.add(new Pair<>(p, m));
    }

    Entry pollEntry() {
        if (!callEdges.isEmpty()) {
            // for correctness, we need to ensure that any call edges in
            // the work list must be processed prior to the pointer entries
            Pair<Pointer, JMethod> p = callEdges.poll();
            return new CallEdgeEntry(p.getFirst(), p.getSecond());
        } else if (!pointerEntries.isEmpty()) {
            var it = pointerEntries.entrySet().iterator();
            var e = it.next();
            it.remove();
            return new PointerEntry(e.getKey(), e.getValue());
        } else {
            throw new NoSuchElementException();
        }
    }

    boolean isEmpty() {
        return pointerEntries.isEmpty() && callEdges.isEmpty();
    }

    interface Entry {
    }

    record PointerEntry(Pointer pointer, PointsToSet pointsToSet)
            implements Entry {
    }

    record CallEdgeEntry(Pointer pointer, JMethod method)
            implements Entry {
    }
}
