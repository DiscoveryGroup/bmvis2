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

package biomine.bmvis2.pipeline.operations.structure;

import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualGroupNode;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.pipeline.GraphOperation;
import biomine.bmvis2.pipeline.SettingsChangeCallback;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ahinkka
 */
public class TextFilterShower implements GraphOperation {
    String filter = "";

    public String getTitle() {
        return "Filter nodes and edges by text";
    }

    public String getToolTip() {
        return null;
    }

    public TextFilterShower(String filter) {
        this.filter = filter;
    }

    /**
     * TODO: how should this really work?  Should the nodes be made POIs instead of showing them and their neighbors?
     *
     * @param graph Processable graph
     * @throws biomine.bmvis2.pipeline.GraphOperation.GraphOperationException
     *
     */
    public void doOperation(VisualGraph graph) throws GraphOperationException {
        if (this.getFilter().equals(""))
            return;

        Set<VisualNode> shownNodes = new HashSet<VisualNode>();

        for (VisualNode node : graph.getAllNodes()) {
            if (node instanceof VisualGroupNode)
                continue;

            if (this.filter.equals("*")) {
                shownNodes.add(node);
                continue;
            }

            for (String key : node.getBMNode().getAttributes().keySet())
                if (new String(node.getBMNode().getAttributes().get(key)).toLowerCase().contains(this.filter.toLowerCase()))
                    shownNodes.add(node);

            if (node.getName().toLowerCase().contains(this.filter.toLowerCase()) ||
                    node.getType().toLowerCase().contains(this.filter) ||
                    node.getId().toLowerCase().contains(this.filter))
                shownNodes.add(node);

            if (node.getId().equals(this.filter))
                shownNodes.add(node);

            // Logging.debug("graph_operation", "Node: " + node + ", to be shown: " + shownNodes.contains(node));
        }

        HashSet<VisualNode> oneNeighborhood = new HashSet<VisualNode>();
        for (VisualNode node : shownNodes)
            for (VisualNode neighbor : node.getNeighbors())
                oneNeighborhood.add(neighbor);
        shownNodes.addAll(oneNeighborhood);

        graph.unHideNodes(shownNodes);
    }

    public JComponent getSettingsComponent(final SettingsChangeCallback v, VisualGraph graph) {
        JPanel ret = new JPanel();
        final JTextField filterField = new JTextField();
        ret.add(filterField);
        filterField.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                if (filterField.getText().equals(""))
                    return;
                if (filterField.getText().equals(TextFilterShower.this.filter))
                    return;
                TextFilterShower.this.filter = filterField.getText();
                v.settingsChanged(false);
            }

            public void insertUpdate(DocumentEvent documentEvent) {
                this.update();
            }

            public void removeUpdate(DocumentEvent documentEvent) {
                this.update();
            }

            public void changedUpdate(DocumentEvent documentEvent) {
                this.update();
            }
        });

        return ret;
    }

    public JSONObject toJSON() {
        JSONObject ret = new JSONObject();
        ret.put("filter", this.filter);
        return ret;
    }

    public void fromJSON(JSONObject o) throws Exception {
        this.filter = (String) o.get("filter");
    }

    public void setFilter(String currentFilter) {
        this.filter = currentFilter;
    }

    public String getFilter() {
        return this.filter;
    }
}