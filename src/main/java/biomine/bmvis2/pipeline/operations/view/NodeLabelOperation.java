/*
 * Copyright 2012-2016 University of Helsinki.
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

import biomine.bmgraph.BMNode;
import biomine.bmvis2.Logging;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.pipeline.GraphOperation;
import biomine.bmvis2.pipeline.SettingsChangeCallback;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Sets default node labels, and provides list of attributes to be shown as labels.
 *
 * @author alhartik
 */
public class NodeLabelOperation implements GraphOperation {
    private HashSet<String> enabledLabels = new HashSet<String>();

    public void doOperation(VisualGraph g) throws GraphOperationException {
        Set<String> avail = g.getAvailableNodeLabels();
        for (VisualNode n : g.getNodes()) {
            BMNode nod = n.getBMNode();
            ArrayList<String> labels = new ArrayList<String>();
            labels.ensureCapacity(enabledLabels.size());
            if (enabledLabels.contains("type") && !n.getShowNodeType())
                labels.add(n.getType());
            if (nod != null) {
                for (String lbl : avail) {
                    if (enabledLabels.contains(lbl)) {
                        String l = nod.get(lbl);
                        if (l != null)
                            labels.add(lbl.substring(0, 3) + ": " + l);
                    }
                }
            }
            n.setExtraLabels(labels);
        }
    }

    public void enableLabel(String label) {
        Logging.debug("ui", "Enabled labels: " + this.enabledLabels);
        this.enabledLabels.add(label);
    }

    public void disableLabel(String label) {
        Logging.debug("ui", "Enabled labels: " + this.enabledLabels);
        this.enabledLabels.remove(label);
    }

    public JComponent getSettingsComponent(final SettingsChangeCallback v,
                                           VisualGraph graph) {
        JPanel ret = new JPanel();
        ret.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 0;
        c.gridy = 0;
        final Set<String> avail = graph.getAvailableNodeLabels();

        for (final String str : avail) {
            final JCheckBox box = new JCheckBox();
            box.setSelected(enabledLabels.contains(str));
            box.setAction(new AbstractAction(str) {
                public void actionPerformed(ActionEvent arg0) {
                    if (box.isSelected() != enabledLabels.contains(str)) {
                        if (box.isSelected())
                            enabledLabels.add(str);
                        else
                            enabledLabels.remove(str);
                        v.settingsChanged(false);
                    }
                }
            });
            ret.add(box, c);
            c.gridy++;
        }

        return ret;
    }

    public String getTitle() {
        return "Node labels";
    }

    public String getToolTip() {
        return null;
    }

    public JSONObject toJSON() {
        JSONObject ret = new JSONObject();
        JSONArray enabled = new JSONArray();
        enabled.addAll(enabledLabels);
        ret.put("enabled", enabled);
        return ret;
    }

    public void fromJSON(JSONObject o) throws Exception {
        JSONArray enabled = (JSONArray) o.get("enabled");
        enabledLabels.clear();
        enabledLabels.addAll(enabled);
    }

    public HashSet<String> getEnabledLabels() {
        return enabledLabels;
    }
}
