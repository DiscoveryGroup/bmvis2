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

package biomine.bmvis2.pipeline.operations.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import biomine.bmvis2.pipeline.GraphOperation;
import biomine.bmvis2.pipeline.SettingsChangeCallback;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import biomine.bmgraph.BMEdge;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualEdge;


public class EdgeLabelOperation implements GraphOperation {
    private HashSet<String> enabledLabels = new HashSet<String>();
    private boolean showEdgeTypes = true;

    public void doOperation(VisualGraph g) throws GraphOperationException {
        Set<String> avail = g.getAvailableEdgeLabels();
        for (VisualEdge n : g.getEdges()) {
            BMEdge bmEdge = (BMEdge) g.getBMEntity(n);
            ArrayList<String> labels = new ArrayList<String>();
            labels.ensureCapacity(enabledLabels.size());
            if (bmEdge != null) {
                for (String lbl : avail) {
                    if (enabledLabels.contains(lbl)) {
                        String l = bmEdge.get(lbl);
                        if (l != null)
                            labels.add(lbl.substring(0, 3) + ": " + l);
                    }
                }
                n.setExtraLabels(labels);
                n.setShowEdgeType(showEdgeTypes);
            }
        }
    }

    public void enableLabel(String label) {
        this.enabledLabels.add(label);
    }

    public void disableLabel(String label) {
        this.enabledLabels.remove(label);
    }

    public JComponent getSettingsComponent(final SettingsChangeCallback v,
                                           VisualGraph graph) {
        JPanel ret = new JPanel();
        ret.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = c.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 0;
        c.gridy = 0;

        // First a checkbox for type
        final JCheckBox typeCheckbox = new JCheckBox();
        typeCheckbox.setSelected(showEdgeTypes);
        typeCheckbox.setAction(new AbstractAction("type") {
            public void actionPerformed(ActionEvent event) {
                if (typeCheckbox.isSelected() != showEdgeTypes) {
                    if (typeCheckbox.isSelected()) {
                        showEdgeTypes = true;
                    } else {
                        showEdgeTypes = false;
                    }
                    v.settingsChanged(false);
                }

            }
        });
        ret.add(typeCheckbox, c);
        c.gridy++;

        // Then for other attributes
        final Set<String> availableLabels = graph.getAvailableEdgeLabels();
        for (final String label : availableLabels) {
            final JCheckBox cb = new JCheckBox();
            cb.setSelected(enabledLabels.contains(label));
            cb.setAction(new AbstractAction(label) {
                public void actionPerformed(ActionEvent event) {
                    if (cb.isSelected() != enabledLabels.contains(label)) {
                        if (cb.isSelected()) {
                            enabledLabels.add(label);
                        } else {
                            enabledLabels.remove(label);
                        }
                        v.settingsChanged(false);
                    }
                }
            });
            ret.add(cb, c);
            c.gridy++;
        }

        return ret;
    }

    public String getTitle() {
        return "Edge labels";
    }

    public String getToolTip() {
        return null;
    }

    public JSONObject toJSON() {
        JSONObject ret = new JSONObject();
        JSONArray enabled = new JSONArray();
        enabled.addAll(enabledLabels);
        ret.put("enabled", enabled);
        ret.put("showTypes", showEdgeTypes);
        return ret;
    }

    public void fromJSON(JSONObject o) throws Exception {
        JSONArray enabled = (JSONArray) o.get("enabled");
        enabledLabels.clear();
        enabledLabels.addAll(enabled);
        showEdgeTypes = (Boolean) o.get("showTypes");
    }

    public HashSet<String> getEnabledLabels() {
        return enabledLabels;
    }
}
