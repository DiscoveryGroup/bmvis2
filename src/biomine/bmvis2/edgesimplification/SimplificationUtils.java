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
