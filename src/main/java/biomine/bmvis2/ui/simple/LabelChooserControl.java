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

package biomine.bmvis2.ui.simple;

import biomine.bmvis2.LabeledItem;
import biomine.bmvis2.Logging;
import biomine.bmvis2.pipeline.operations.view.EdgeLabelOperation;
import biomine.bmvis2.pipeline.operations.view.NodeLabelOperation;
import biomine.bmvis2.pipeline.Pipeline;
import biomine.bmvis2.utils.StringUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LabelChooserControl extends TranslucentControl {
    private JComboBox toggleComboBox;
    private Map<String, Runnable> nodeOperationMap = new HashMap<String, Runnable>();
    private Map<String, Runnable> edgeOperationMap = new HashMap<String, Runnable>();
    NodeLabelOperation nodeLabelOperation = new NodeLabelOperation();
    EdgeLabelOperation edgeLabelOperation = new EdgeLabelOperation();


    public LabelChooserControl(Pipeline pipeline) {
        super(pipeline);

        // Attribute drop-down

        this.toggleComboBox = new JComboBox();
        this.populateToggleComboBox(this.toggleComboBox);
        this.toggleComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Runnable r = nodeOperationMap.get(toggleComboBox.getSelectedItem());
                Runnable r2 = edgeOperationMap.get(toggleComboBox.getSelectedItem());
                if (r != null)
                    SwingUtilities.invokeLater(r);
                if (r2 != null)
                    SwingUtilities.invokeLater(r2);

            }
        });

        this.add(this.toggleComboBox);
    }

    public void updateControl() {
        populateToggleComboBox(this.toggleComboBox);
    }

    private void populateToggleComboBox(final JComboBox p) {
        p.removeAllItems();

        p.addItem("");

        populateNodeLabels(p);
        populateEdgeLabels(p);
    }


    private void populateNodeLabels(JComboBox p) {
        Set<String> nodeLabels = this.getPipeline().getCurrentGraph().getAvailableNodeLabels();
        Logging.debug("ui", "Available node labels: " + StringUtils.joinObjects(new HashSet(nodeLabels), ", "));

        if (!this.getPipeline().getCurrentGraph().getShowNodeTypes())
            nodeLabels.add("type");

        Set<String> enabledNodeLabels = new HashSet<String>();
        for (String label : this.nodeLabelOperation.getEnabledLabels())
            enabledNodeLabels.add(label);

        for (String label : nodeLabels) {
            if (LabeledItem.nonVisibleAttributes.contains(label.toLowerCase()))
                continue;

            final String nodeLabel = label;
            if (enabledNodeLabels.contains(label)) {
                String item = " ☒ node: " + label.replace("_", " ");
                p.addItem(item);
                this.nodeOperationMap.put(item, new Runnable() {
                    public void run() {
                        nodeLabelOperation.disableLabel(nodeLabel);
                        getPipeline().settingsChanged(false);
                        updateControl();
                    }
                });
            } else if (!enabledNodeLabels.contains(label)) {
                String item = " ☐ node: " + label.replace("_", " ");
                p.addItem(item);
                this.nodeOperationMap.put(item, new Runnable() {
                    public void run() {
                        nodeLabelOperation.enableLabel(nodeLabel);
                        getPipeline().settingsChanged(false);
                        updateControl();
                    }
                });
            }
        }
    }

    private void populateEdgeLabels(JComboBox p) {
        Set<String> edgeLabels = this.getPipeline().getCurrentGraph().getAvailableEdgeLabels();

        Set<String> enabledEdgeLabels = new HashSet<String>();
        for (String label : this.edgeLabelOperation.getEnabledLabels())
            enabledEdgeLabels.add(label);

        for (String label : edgeLabels) {
            if (LabeledItem.nonVisibleAttributes.contains(label.toLowerCase()))
                continue;

            final String edgeLabel = label;
            if (enabledEdgeLabels.contains(label)) {
                String item = " ☒ edge: " + label.replace("_", " ");
                p.addItem(item);
                this.edgeOperationMap.put(item, new Runnable() {
                    public void run() {
                        edgeLabelOperation.disableLabel(edgeLabel);
                        getPipeline().settingsChanged(false);
                        updateControl();
                    }
                });

            } else if (!enabledEdgeLabels.contains(label)) {
                String item = " ☐ edge: " + label.replace("_", " ");
                p.addItem(item);
                this.edgeOperationMap.put(item, new Runnable() {
                    public void run() {
                        edgeLabelOperation.enableLabel(edgeLabel);
                        getPipeline().settingsChanged(false);
                        updateControl();
                    }
                });
            }

        }
    }
}
