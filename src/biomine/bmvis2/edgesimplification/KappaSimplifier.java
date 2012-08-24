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

import java.util.*;

import biomine.bmvis2.Logging;
import biomine.bmvis2.SimpleVisualGraph;
import biomine.bmvis2.VisualEdge;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.SimpleVisualGraph.SimpleTwoDirEdge;
import biomine.bmvis2.algorithms.ProbDijkstra;

/**
 * Path simplification which selects greedily the edge with largest kappa value.
 * <p/>
 * PS
 *
 * @author alhartik
 */
public class KappaSimplifier extends Simplifier {
    static final double MYINF = 1000000;

    SimpleVisualGraph graph;

    public double getKappa(SimpleTwoDirEdge edge) {
        double edgeProbability = edge.weight;
        int from = edge.from;
        int to = edge.to;

        // Remove the trivial path, why?
        graph.removeEdge(from, to);

        ProbDijkstra bestPathAlg = new ProbDijkstra(graph, from);
        double bestPathProbability = bestPathAlg.getProbTo(to);

        graph.addEdge(from, to, edge.visualEdge);

        if (bestPathProbability > edgeProbability) return 1.0;

        double ret = bestPathProbability / edgeProbability;

        if (ret <= 0)
            return -MYINF;
        return ret;
    }

    private double startQuality = 0;

    public double getStartQuality() {
        return startQuality;
    }


    public void setStartQuality(double startQuality) {
        this.startQuality = startQuality;
    }


    public double getResultQuality() {
        return resultQuality;
    }


    public void setResultQuality(double resultQuality) {
        this.resultQuality = resultQuality;
    }

    private double resultQuality = 0;

    @Override
    public List<VisualEdge> getRemovedEdges(VisualGraph g, int removeK) {
        Logging.info("enduser", "Starting simplification initial calculation.");
        long t = System.currentTimeMillis();
        graph = new SimpleVisualGraph(g);
        // startQuality = SimplificationUtils.graphConnectivity(graph);

        ArrayList<VisualEdge> removed = new ArrayList<VisualEdge>();
        HashSet<VisualEdge> cutters = new HashSet<VisualEdge>();

        d("removeK=" + removeK);

        // First remove max removeK parallel edges from the graph:
        Collection<VisualEdge> parallel = SimplificationUtils.removeParallelEdges(g, removeK);
        removed.addAll(parallel);

        for (VisualEdge rem : parallel)
            graph.removeEdge(rem);

        removeK -= parallel.size();
        d("removeK=" + removeK);


        int remove = Math.min(removeK,
                graph.getEdgeCount() - (graph.n - 1));//gotta leave graph.n-1 edges


        removeLoop:
        for (int r = 0; r < remove; r++) {

            SimpleTwoDirEdge bestEdge = null;
            double bestKappa = -1000;
            Set<SimpleTwoDirEdge> edges = graph.getAllEdges();

            for (SimpleTwoDirEdge e : edges) {
                if (cutters.contains(e.visualEdge)) continue;
                double kappa = getKappa(e);

                if (kappa >= 1) {
                    // We can remove this as it does not reduce any best paths
                    graph.removeEdge(e.from, e.to);
                    removed.add(e.visualEdge);
                    d(e.visualEdge + " has kappa = " + kappa + ". removing...");
                    r++;
                    if (r >= remove)
                        break removeLoop;
                } else if (kappa < 0) {
                    // cutter
                    d(e.visualEdge + " is a cutter (" + kappa + ")");
                    cutters.add(e.visualEdge);
                } else {
                    if (bestKappa < kappa) {
                        bestKappa = kappa;
                        bestEdge = e;
                    }
                }
            }
            if (bestEdge == null) break;

            d("Iteration " + r + ", bestKappa: " + bestKappa + ", removed " + bestEdge.visualEdge);

            graph.removeEdge(bestEdge.from, bestEdge.to);
            removed.add(bestEdge.visualEdge);
        }

        // resultQuality = SimplificationUtils.graphConnectivity(graph);
        long time = System.currentTimeMillis() - t;
        Logging.info("enduser", "Simplification calculation took " + time * 0.001 + " seconds.");
        return removed;
    }

    private void d(Object o) {
        Logging.debug("kappasimplifier", "" + o);
    }

    @Override
    public String getTitle() {
        return "Simplify edges";
    }
}
