/*
 * Copyright 2012 University of Helsinki.
 *
 * This file is part of BMVis².
 *
 * BMVis² is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * BMVis² is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BMVis².  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package biomine.bmvis2.edgesimplification;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import biomine.bmvis2.*;
import biomine.bmvis2.algorithms.ProbDijkstra;

public class SimplificationUtils {
    public static Collection<VisualEdge> removeParallelEdges(VisualGraph vg, int removeK) {
        HashSet<VisualEdge> remove = new HashSet<VisualEdge>();

        for (VisualNode n : vg.getAllNodes()) {
            HashMap<VisualNode, HashSet<VisualEdge>> es = new HashMap<VisualNode, HashSet<VisualEdge>>();

            for (VisualEdge e : n.getEdges()) {
                HashSet<VisualEdge> s = es.get(e.getOther(n));
                if (s == null) {
                    s = new HashSet<VisualEdge>();
                }
                s.add(e);
                es.put(e.getOther(n), s);
            }
            for (Entry<VisualNode, HashSet<VisualEdge>> ent : es.entrySet()) {
                if (ent.getValue().size() > 1) {
                    VisualEdge bestEdge = getBestVisualEdge(ent.getValue());

                    for (VisualEdge e : ent.getValue())
                        if (!e.equals(bestEdge))
                            remove.add(e);
                }
            }
        }

        while (remove.size() > removeK)
            remove.remove(remove.iterator().next());
        return remove;
    }

    public static VisualEdge getBestVisualEdge(Collection<VisualEdge> edges) {
        VisualEdge ret = null;

        for (VisualEdge e : edges)
            if (ret == null || e.getGoodness() > ret.getGoodness())
                ret = e;

        return ret;
    }

    public static int countNormalEdges(VisualGraph visualGraph) {
        HashSet<VisualEdge> edges = new HashSet<VisualEdge>();
        for (VisualNode n : visualGraph.getRootNode().getDescendants()) {
            for (VisualEdge e : n.getEdges()) {
                if (e.getOther(n).getClass().equals(VisualNode.class)) {
                    edges.add(e);
                }
            }

        }
        return edges.size();

    }

    public static double graphConnectivity(SimpleVisualGraph g) {
        Logging.debug("simplifier", "Calculating graph connectivity...");
        double total = 0;
        int div = 0;
        for (int i = 0; i < g.n; i++) {
            ProbDijkstra d = new ProbDijkstra(g, i);
            for (int j = i + 1; j < g.n; j++) {
                div++;
                double p = d.getProbTo(j);
                total += p;
            }
        }
        Logging.debug("simplifier", "Done calculating graph connectivity.");
        return total / div;
    }

    public static double graphConnectivity(VisualGraph g) {
        SimpleVisualGraph sg = new SimpleVisualGraph(g.getRootNode().getDescendants());
        for (VisualEdge e : g.getHiddenEdges())
            sg.removeEdge(e);
        return graphConnectivity(sg);
    }
}
