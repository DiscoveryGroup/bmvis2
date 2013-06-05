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

import biomine.bmvis2.VisualEdge;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.utils.StringUtils;

import java.util.*;

public class Path implements Comparable {
    private VisualNode fromNode;
    private VisualNode toNode;

    public List<VisualEdge> edges;

    public Path(List<VisualEdge> edges) {
        this.edges = edges;
    }

    public Path(Path previous, VisualEdge extension, VisualNode to) {
        this.edges = new ArrayList<VisualEdge>();
        for (VisualEdge edge : previous.edges)
            this.edges.add(edge);
        this.edges.add(extension);

        this.fromNode = previous.getFrom();
        this.toNode = to;
    }

    public Path(VisualNode from, VisualNode to) {
        this.edges = new ArrayList<VisualEdge>();

        this.fromNode = from;
        this.toNode = to;
    }

    public double getGoodness() {
        if (this.edges.size() == 0)
            return 0.0;

        double goodness = 1.0;
        for (VisualEdge edge : this.edges)
            goodness = goodness * edge.getGoodness();
        return goodness;
    }

    public Set<VisualNode> nodes() {
        HashSet<VisualNode> ret = new HashSet<VisualNode>();

        for (VisualEdge edge : this.edges) {
            ret.add(edge.getFrom());
            ret.add(edge.getTo());
        }

        return ret;
    }

    public Set<VisualEdge> edges() {
        return new HashSet<VisualEdge>(this.edges);
    }


    /**
     * Returns the cost (edge count) of adding this path to a graph.
     *
     * @param paths
     * @return edge count
     */
    public int costWith(Collection<Path> paths) {
        Set<VisualEdge> allEdges = this.edges();

        for (Path p : paths)
            allEdges.addAll(p.edges());

        return allEdges.size();
    }

    public VisualNode getFrom() {
        return this.fromNode;
    }

    public VisualNode getTo() {
        return this.toNode;
    }

    public int compareTo(Object o) {
        if (o != null && o instanceof Path) {
            Path other = (Path) o;

            if (this.getGoodness() > other.getGoodness())
                return -1;
            else if (other.getGoodness() > other.getGoodness())
                return 1;
            else
                return 0;
        } else {
            return 0;
        }
    }

    public String toString() {
        String head = "Path ";
        if (this.getFrom() != null && this.getTo() != null)
            head = "Path " + this.getFrom() + "->" + this.getTo() + " ";

        return head + StringUtils.joinObjects(new ArrayList(this.edges), " ") + this.getGoodness();
    }
}
