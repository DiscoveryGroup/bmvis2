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

package biomine.bmvis2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple and fast data structure for VisualGraphs. Use this when you are
 * writing time-sensitive (=slow,hard) algorithms.
 *
 * @author alhartik
 */

public class SimpleVisualGraph {
    public class SimpleEdge {
        public int to;
        public double weight;
        public VisualEdge visualEdge;

        public String toString() {
            return "" + to;
        }
    }

    public class SimpleTwoDirEdge {
        public int from;
        public int to;
        public double weight;
        public VisualEdge visualEdge;
    }

    public final int n;
    public SimpleEdge[][] edges;

    private VisualNode[] nodeArray;
    private HashMap<VisualNode, Integer> nodes;

    public Collection<VisualNode> getVisualNodes() {
        return Arrays.asList(nodeArray);
    }

    public VisualNode getVisualNode(int i) {
        return nodeArray[i];
    }

    public int getInt(VisualNode n) {
        Integer ret = nodes.get(n);
        if (ret == null) {
            Logging.warning("algorithms", "Couldn't find VisualNode " + n);
            return -1;
        }
        return ret;
    }

    /*
      * These aren't fastest possible implementations. This is due to initial
      * (bad?) design. -alhartik
      *
      * I wonder if we'd get better performance out of a "simple" graph
      * implementation using collections?  The lookup performance of arrays
      * is not as optimal as it could be.  The memory savings of this
      * implementation might still be significant... -ahinkka
      */
    public void addEdge(int from, int to, VisualEdge e) {
        //	System.err.println("add "+from+" "+to);
        for (int j = 0; j < 2; j++) {
            boolean found = false;
            for (int i = 0; i < edges[from].length; i++) {
                if (edges[from][i].to == to) {
                    edges[from][i].weight = e.getGoodness();
                    found = true;
                    break;
                }
            }
            if (!found) {
                edges[from] = Arrays.copyOf(edges[from], edges[from].length + 1);
                SimpleEdge ne = new SimpleEdge();
                ne.to = to;
                ne.weight = e.getGoodness();
                ne.visualEdge = e;
                edges[from][edges[from].length - 1] = ne;
            }
            int t = to;
            to = from;
            from = t;
        }
    }

    public void removeEdge(int from, int to) {
        realRemoveEdge(from, to);
        realRemoveEdge(to, from);
    }

    public void removeEdge(VisualEdge e) {
        int from = getInt(e.getFrom());
        int to = getInt(e.getTo());

        realRemoveEdge(from, to);
        realRemoveEdge(to, from);
    }

    /**
     * Previously remove edge was a kludge with two loops loops (pun
     * intended). Now the implementation is separated from the public methods.
     */
    private void realRemoveEdge(int from, int to) {
        for (int i = 0; i < edges[from].length; i++) {
            if (edges[from][i].to == to) {
                int newLastEdgeIndex = edges[from].length - 1;
                edges[from][i] = edges[from][newLastEdgeIndex];
                edges[from] = Arrays.copyOf(edges[from], newLastEdgeIndex);
                break;
            }
        }
    }

    public Collection<SimpleEdge> getEdges(int from) {
        return Arrays.asList(edges[from]);
    }

    private void initWithNodes(Collection<VisualNode> ns) {
        HashSet<VisualNode> nodes = new HashSet<VisualNode>(ns);
        edges = new SimpleEdge[n][];
        nodeArray = new VisualNode[n];
        this.nodes = new HashMap<VisualNode, Integer>();

        {
            int i = 0;
            for (VisualNode n : ns) {
                this.nodes.put(n, i);
                nodeArray[i++] = n;
            }
        }

        ArrayList<HashSet<VisualEdge>> edgesAL = new ArrayList<HashSet<VisualEdge>>();
        for (int i = 0; i < n; i++) {
            edgesAL.add(new HashSet<VisualEdge>());
        }

        for (int i = 0; i < n; i++) {

            //ArrayList<SimpleEdge> edgesList = new ArrayList<SimpleEdge>();
            for (VisualEdge e : nodeArray[i].getEdges()) {
                VisualNode o = e.getOther(nodeArray[i]);
                if (!(nodes.contains(o)))
                    continue;

                if (o != nodeArray[i]) {
                    try {
                        assert o.getEdges().contains(e) : "inconsistent getEdges() " + o + " doesn't have (" + e + "). only " + o.getEdges();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        System.out.println("o=" + o + " e=" + e);
                    }
                }

                edgesAL.get(i).add(e);

                //se.to=i;
                //edgesAL.get(i).add(e);
            }

        }

        for (int i = 0; i < n; i++) {
            edges[i] = new SimpleEdge[edgesAL.get(i).size()];
            int k = 0;
            for (VisualEdge ve : edgesAL.get(i)) {
                SimpleEdge se = new SimpleEdge();
                se.to = this.nodes.get(ve.getOther(nodeArray[i]));
                se.weight = ve.getGoodness();
                se.visualEdge = ve;
                edges[i][k++] = se;
            }
        }
    }

    public SimpleVisualGraph(VisualGroupNode g) {
        n = g.getChildren().size();
        initWithNodes(g.getChildren());
    }

    public SimpleVisualGraph(VisualGraph g) {
        n = g.getRootNode().getDescendants().size();
        initWithNodes(g.getRootNode().getDescendants());
    }

    public int getEdgeCount() {
        int c = 0;
        for (int i = 0; i < n; i++)
            c += getEdges(i).size();
        return c;
    }

    public Set<SimpleTwoDirEdge> getAllEdges() {
        ArrayList<SimpleTwoDirEdge> ret = new ArrayList<SimpleTwoDirEdge>();
        for (int i = 0; i < n; i++)
            for (SimpleEdge e : getEdges(i)) {

                if (e.to > i)
                    continue;

                SimpleTwoDirEdge t = new SimpleTwoDirEdge();
                t.from = i;
                t.to = e.to;
                t.weight = e.weight;
                t.visualEdge = e.visualEdge;


                ret.add(t);
            }
        return new HashSet<SimpleTwoDirEdge>(ret);
    }

    public SimpleVisualGraph(Collection<VisualNode> nodes) {
        n = nodes.size();
        initWithNodes(nodes);
    }

    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append("V=[");
        for (int i = 0; i < n; i++)
            b.append("" + nodeArray[i] + ",");
        b.append("]\n");
        b.append("E=[");
        for (int i = 0; i < n; i++) {

            b.append("" + getEdges(i));
        }
        b.append("]\n");

        return b.toString();
    }
}
