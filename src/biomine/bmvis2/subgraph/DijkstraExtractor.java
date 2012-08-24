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

package biomine.bmvis2.subgraph;

import biomine.bmvis2.*;
import biomine.bmvis2.pipeline.SettingsChangeCallback;

import javax.swing.*;
import java.util.*;

public class DijkstraExtractor extends Extractor {
    // { VisualGraph => { VisualNode from => { VisualNode to => [ Path, ... ] } } }
    private Map<VisualNode, Map<VisualNode, List<Path>>> pathCache = null;

    @Override
    public JComponent getSettingsComponent(SettingsChangeCallback v, VisualGraph graph) {
        return null;
    }

    /**
     * Finds best paths from a node to all others from a graph.
     *
     * @param graph
     * @param from
     * @param visited parameter can be used to exclude some nodes from the path search; it should normally be empty
     * @return
     */
    public Map<VisualNode, List<Path>> calculateBestPaths(VisualGraph graph, VisualNode from, Set<VisualNode> visited) {
        Logging.debug("graph_operation", "Calculating best paths from " + from);
        Map<VisualNode, List<Path>> bestPath = new HashMap<VisualNode, List<Path>>();
        PriorityQueue<Path> pathHeap = new PriorityQueue<Path>();

        for (VisualNode node : graph.getAllNodes())
            bestPath.put(node, new LinkedList<Path>(Arrays.asList(new Path(from, node))));

        Set<VisualNode> unvisited = new HashSet<VisualNode>();
        if (visited.size() == 0)
            visited = new HashSet<VisualNode>();

        unvisited.add(from);
        VisualNode current = from;
        while (current != null) {
            // d("Current node: " + current);
            Path currentPath = bestPath.get(current).get(0);
            // d("Current path: " + currentPath);

            for (VisualEdge edge : current.getEdges()) {
                VisualNode neighbor = edge.getOther(current);

                if (visited.contains(neighbor))
                    continue;

                unvisited.add(neighbor);

                Path currentPathToNeighbor = bestPath.get(neighbor).get(0);
                Path candidatePath = new Path(currentPath, edge, neighbor);

                if (candidatePath.getGoodness() > currentPathToNeighbor.getGoodness()) {
                    bestPath.get(neighbor).add(0, candidatePath);
                    pathHeap.add(candidatePath);
                } else
                    pathHeap.add(currentPathToNeighbor);

            }

            visited.add(current);
            unvisited.remove(current);

            // Pick best connected unvisited
            current = null;
            if (unvisited.size() > 0) {
                while (pathHeap.size() > 0 && visited.contains(pathHeap.peek().getTo()))
                    pathHeap.remove();

                if (pathHeap.size() > 0)
                    current = pathHeap.remove().getTo();
            }
        }

        Map<VisualNode, List<Path>> ret = new HashMap<VisualNode, List<Path>>();
        for (VisualNode node : bestPath.keySet()) {
            if (node.equals(from) || node.equals(graph.getRootNode()))
                continue;

            LinkedList<Path> list = new LinkedList<Path>();
            for (Path p : bestPath.get(node))
                if (p.edges.size() > 0)
                    list.add(p);

            if (list.size() > 0)
                ret.put(node, list);
        }

        Logging.debug("graph_operation", "Done calculating best paths from " + from);

        return ret;
    }

    private Path getNthBestPathBetweenNodes(VisualGraph g, int nth, VisualNode node1, VisualNode node2) {
        try {
            Logging.debug("graph_operation", "Path count from " + node1 + ": " + this.pathCache.get(node1).size());
            return this.pathCache.get(node1).get(node2).get(nth - 1);
        } catch (NullPointerException npe) {
            Logging.debug("graph_operation", "" + nth + " + best path not found between " + node1.toString() + " " + node2.toString());
        } catch (IndexOutOfBoundsException i) {
            Logging.debug("graph_operation", "" + nth + " best path not found between " + node1.toString() + " " + node2.toString());
        }


        return null;
    }

    private Set<VisualEdge> calculateShowableEdges(VisualGraph g, List<VisualNode> interests) {
        Set<VisualEdge> ret = new HashSet<VisualEdge>();

        int budgetLeft = this.getEdgeBudget();

        List<VisualNode> positiveInterests = interests;
        List<VisualNode> negativeInterests = new LinkedList<VisualNode>();

        for (VisualNode node : g.getNodesOfInterest().keySet()) {
            Double score = g.getNodesOfInterest().get(node);
            if (score != null && score < 0)
                negativeInterests.add(node);
        }

        HashMap<VisualEdge, Integer> onHowManyBestPaths = new HashMap<VisualEdge, Integer>();


        for (VisualNode node1 : positiveInterests) {
            for (VisualNode node2 : positiveInterests) {
                Path bestPath = getNthBestPathBetweenNodes(g, 1, node1, node2);

                if (bestPath == null)
                    continue;

                for (VisualEdge edge : bestPath.edges) {
                    Integer current = onHowManyBestPaths.get(edge);
                    if (current == null)
                        current = 0;

                    current++;

                    onHowManyBestPaths.put(edge, current);
                }
            }
        }

        for (VisualNode node1 : positiveInterests) {
            for (VisualNode node2 : positiveInterests) {
                Path bestPath = getNthBestPathBetweenNodes(g, 2, node1, node2);

                if (bestPath == null)
                    continue;

                for (VisualEdge edge : bestPath.edges) {
                    Integer current = onHowManyBestPaths.get(edge);
                    if (current == null)
                        current = 0;

                    current++;

                    onHowManyBestPaths.put(edge, current);
                }
            }
        }

        class ScoredCandidate implements Comparable {
            public Double score;
            public VisualEdge edge;

            ScoredCandidate(Double score, VisualEdge node) {
                this.score = score;
                this.edge = node;
            }

            public int compareTo(Object o) {
                if (o instanceof ScoredCandidate)
                    return Double.compare(this.score, ((ScoredCandidate) o).score);

                return 0;
            }
        }

        ArrayList<ScoredCandidate> candidates = new ArrayList<ScoredCandidate>();

        int maxPathCount = 0;
        for (Integer i : onHowManyBestPaths.values())
            if (i > maxPathCount)
                maxPathCount = i;

        if (maxPathCount == 0) {
            for (VisualEdge edge : g.getAllEdges()) {
                candidates.add(new ScoredCandidate(edge.getGoodness(), edge));
            }
        } else {
            for (VisualEdge edge : g.getAllEdges()) {
                Integer pathCount = onHowManyBestPaths.get(edge);

                if (pathCount == null)
                    pathCount = 0;

                double score = edge.getGoodness();

                if (pathCount != maxPathCount)
                    score *= 1.0 / (maxPathCount - pathCount);

                candidates.add(new ScoredCandidate(score, edge));
            }
        }

        Collections.sort(candidates);
        Collections.reverse(candidates);

        while (budgetLeft > 0 && candidates.size() > 0) {
            ret.add(candidates.remove(0).edge);
            budgetLeft--;
        }

        return ret;
    }

    private Set<VisualNode> calculateShowableNodes(VisualGraph g, List<VisualNode> interests) {
        Set<VisualNode> ret = new HashSet<VisualNode>();

        int budgetLeft = this.getNodeBudget();

        Map<VisualNode, Double> nodeScores = new HashMap<VisualNode, Double>();
        for (VisualNode node : g.getAllNodes())
            nodeScores.put(node, 0.0);

        List<VisualNode> positiveInterests = interests;

        if (interests.size() == 0)
            return ret;

        List<VisualNode> negativeInterests = new LinkedList<VisualNode>();
        for (VisualNode node : g.getNodesOfInterest().keySet()) {
            Double score = g.getNodesOfInterest().get(node);
            if (score != null && score < 0)
                negativeInterests.add(node);
        }

        for (VisualNode interest : positiveInterests)
            nodeScores.put(interest, nodeScores.get(interest) + g.getInterestingness(interest));
        for (VisualNode interest : positiveInterests)
            nodeScores.put(interest, nodeScores.get(interest) - g.getInterestingness(interest));

        // Add all nodes on best paths between nodes
        for (VisualNode node1 : positiveInterests) {
            for (VisualNode node2 : positiveInterests) {
                Path bestPath = getNthBestPathBetweenNodes(g, 1, node1, node2);

                if (bestPath == null)
                    continue;

                for (VisualNode member : bestPath.nodes())
                    nodeScores.put(member, nodeScores.get(member) + 1);
            }
        }

        // Add all nodes on 2nd best paths between nodes
        for (VisualNode node1 : positiveInterests) {
            for (VisualNode node2 : positiveInterests) {
                Path bestPath = getNthBestPathBetweenNodes(g, 2, node1, node2);

                boolean negativeOnPath = false;
                if (bestPath != null) {
                    for (VisualNode neg : negativeInterests)
                        if (bestPath.nodes().contains(neg)) {
                            negativeOnPath = true;
                            break;
                        }
                }

                if (negativeOnPath)
                    bestPath = getNthBestPathBetweenNodes(g, 3, node1, node2);

                if (bestPath == null)
                    continue;

                for (VisualNode member : bestPath.nodes())
                    nodeScores.put(member, nodeScores.get(member) + 0.5);
            }
        }

        class ScoredCandidate implements Comparable {
            public Double score;
            public VisualNode node;

            ScoredCandidate(Double score, VisualNode node) {
                this.score = score;
                this.node = node;
            }

            public int compareTo(Object o) {
                if (o instanceof ScoredCandidate)
                    return Double.compare(this.score, ((ScoredCandidate) o).score);

                return 0;
            }
        }

        ArrayList<ScoredCandidate> candidates = new ArrayList<ScoredCandidate>();

        // Add neighbors of interesting nodes
        for (VisualNode interest : positiveInterests)
            for (VisualNode neighbor : interest.getNeighbors())
                nodeScores.put(neighbor, nodeScores.get(neighbor) + 1.0 / neighbor.getDegree());

        for (VisualNode node : nodeScores.keySet()) {
            Double currentScore = nodeScores.get(node);

            if (currentScore == 0.0)
                continue;

            for (VisualNode neighbor : node.getNeighbors())
                nodeScores.put(neighbor, nodeScores.get(neighbor) + 0.5 / neighbor.getDegree());
        }


        for (VisualNode node : nodeScores.keySet()) {
            Double currentScore = nodeScores.get(node);

            candidates.add(new ScoredCandidate(currentScore, node));
        }


        Collections.sort(candidates);
        Collections.reverse(candidates);

        while (budgetLeft > 0 && candidates.size() > 0) {
            ret.add(candidates.remove(0).node);
            budgetLeft -= 1;
        }

        return ret;
    }

    @Override
    public void doOperation(VisualGraph g) throws GraphOperationException {
        // { VisualNode from => { VisualNode to => [ Path, ... ] } }
        this.pathCache = new HashMap<VisualNode, Map<VisualNode, List<Path>>>();

        this.setInterestMap(g.getNodesOfInterest());
        List<VisualNode> interesting = new ArrayList<VisualNode>();
        for (VisualNode n : this.getInterestMap().keySet())
            if (this.getInterestMap().get(n) > 0.0)
                interesting.add(n);

        // Map<VisualNode, Integer> onBestPathCount = new HashMap<VisualNode, Integer>();

        // Calculates best paths from all nodes of interest to other nodes
        for (VisualNode node : interesting) {
            if (this.pathCache.containsKey(node)) {
                Logging.debug("graph_operation", "Using cached values for paths from " + node);
            } else {
                Map<VisualNode, List<Path>> temp = calculateBestPaths(g, node, Collections.<VisualNode>emptySet());
                this.pathCache.put(node, temp);
            }
        }

        Set<VisualNode> showableNodes = calculateShowableNodes(g, interesting);
        Extractor.removeAllExceptNodes(g, showableNodes);


        // Extractor.removeAllExceptEdges(g, calculateShowableEdges(g, interesting));
    }

    private void d(String s) {
        System.err.println(s);
    }
}
