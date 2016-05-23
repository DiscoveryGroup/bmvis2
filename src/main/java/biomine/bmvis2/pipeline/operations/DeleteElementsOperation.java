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

package biomine.bmvis2.pipeline.operations;

import biomine.bmvis2.Logging;
import biomine.bmvis2.VisualEdge;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.pipeline.GraphOperation;
import biomine.bmvis2.pipeline.SettingsChangeCallback;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/**
 * This needs to be thought through.  This class isn't used anywhere right
 * now.  The implementation of node deletions would probably mean introducing
 * the concept of slow operations in Pipeline (and actually forcibly removing
 * nodes after the graph is built but before any groupers are run).
 *
 * The other, less invasive operation would be HideElementsOperation which
 * would just hide the nodes after all other elements are run.
 */
public class DeleteElementsOperation implements GraphOperation {
    public static final String DELETED_NODES_KEY = "deletedNodes";
    public static final String DELETED_EDGES_KEY = "deletedEdges";

    private Set<String> deletedNodes;
    private Set<String> deletedEdges;

    public DeleteElementsOperation() {
        this.deletedNodes = new HashSet<String>();
        this.deletedEdges = new HashSet<String>();
    }


    public void doOperation(VisualGraph graph) throws GraphOperationException {
                HashMap<String, VisualNode> allNodes = new HashMap<String, VisualNode>();
        for (VisualNode node : graph.getAllNodes())
            allNodes.put(node.getId(), node);

        HashMap<String, VisualEdge> allEdges = new HashMap<String, VisualEdge>();
        for (VisualEdge edge : graph.getAllEdges())
            allEdges.put(edge.toString(), edge);

        for (String nodeId: this.deletedNodes) {
            if (allNodes.containsKey(nodeId))
                graph.deleteNode(allNodes.get(nodeId));
            else
                Logging.warning("graph_operation", "Node " + nodeId + " not in graph; couldn't be deleted.");
        }

        for (String edgeId : this.deletedEdges) {
            if (allEdges.containsKey(edgeId))
                graph.deleteEdge(allEdges.get(edgeId));
            else
                Logging.warning("graph_operation", "Edge " + edgeId + " not in graph; couldn't be deleted.");
        }
    }

    public void unDoOperation(VisualGraph graph) throws GraphOperationException {

    }

    public void deleteNode(VisualNode node) {
        this.deletedNodes.add(node.getId());
    }

    public void deleteEdge(VisualEdge edge) {
        this.deletedEdges.add(edge.toString());
    }

    public String getTitle() {
        return "Deleted elements";
    }

    public String getToolTip() {
        return null;
    }

    public JComponent getSettingsComponent(SettingsChangeCallback cb, VisualGraph graph) {
        return new JLabel("Deleted elements.");
    }

    public void fromJSON(JSONObject object) throws Exception {
        JSONArray deletedNodes = (JSONArray) object.get(DELETED_NODES_KEY);
        JSONArray deletedEdges = (JSONArray) object.get(DELETED_EDGES_KEY);

        for (Object obj : deletedNodes)
            this.deletedNodes.add((String) obj);

        for (Object obj : deletedEdges)
            this.deletedEdges.add((String) obj);
    }

    public JSONObject toJSON() {
        JSONObject ret = new JSONObject();
        JSONArray nodes = new JSONArray();
        JSONArray edges = new JSONArray();

        nodes.addAll(this.deletedNodes);
        edges.addAll(this.deletedEdges);

        ret.put(DELETED_NODES_KEY, nodes);
        ret.put(DELETED_EDGES_KEY, edges);

        return ret;
    }
}
