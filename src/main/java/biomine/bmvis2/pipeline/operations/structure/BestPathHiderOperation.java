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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import biomine.bmvis2.Logging;
import biomine.bmvis2.pipeline.GraphOperation;
import biomine.bmvis2.pipeline.SettingsChangeCallback;
import org.json.simple.JSONObject;

import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.color.NodeGraderColoring;
import biomine.bmvis2.graphcontrols.BestPathGrader;

public class BestPathHiderOperation implements GraphOperation, NodeHiderOperation {
    private BestPathGrader grader;
    private long target;
    private long oldCount = 1;
    private boolean matchColoring = false;
    private boolean showLabel = false;
    public final static long INITIAL_TARGET = 100000;

    public BestPathHiderOperation() {
        this.grader = new BestPathGrader();
        this.target = INITIAL_TARGET;
    }

    private void hideNodes(VisualGraph graph, long target) {
        HashSet<VisualNode> hiddenNodes = new HashSet<VisualNode>();

        ArrayList<VisualNode> nodes = new ArrayList<VisualNode>(graph.getAllNodes());
        if (this.grader == null || nodes.size() <= target) {
            graph.hideNodes(hiddenNodes);
            return;
        }

        Collections.sort(nodes, new Comparator<VisualNode>() {
            public int compare(VisualNode node1, VisualNode node2) {
                return Double.compare(grader.getNodeGoodness(node2), grader
                        .getNodeGoodness(node1));
            }
        });

        for (VisualNode node : nodes) {
            if (target == 0)
                break;
            hiddenNodes.add(node);
            target--;
        }

        graph.hideNodes(hiddenNodes);
    }

    public void doOperation(VisualGraph graph) throws GraphOperationException {
        Logging.debug("graph_operation", "BestPathHider target: " + this.target);
        this.hideNodes(graph, this.target);

        NodeGraderColoring coloring = new NodeGraderColoring(this.grader);

        if (this.matchColoring) {
            for (VisualNode vn : graph.getNodes()) {
                vn.setBaseColor(coloring.getFillColor(vn));
            }
        }

        if (this.showLabel) {
            for (VisualNode vn : graph.getNodes())
                vn.addExtraLabel(String.format("BestPath=%.3f", this.grader.getNodeGoodness(vn)));
        }
    }

    public JComponent getSettingsComponent(final SettingsChangeCallback settingsChangeCallback,
                                           final VisualGraph graph) {
        long nodeCount = graph.getAllNodes().size();

        if (oldCount != 0) {
            long newTarget = (target * nodeCount) / oldCount;
            this.target = Math.max(newTarget, target);
        } else
            this.target = nodeCount;

        this.oldCount = nodeCount;

        JPanel ret = new JPanel();

        // if int overflows, we're fucked
        final JSlider sl = new JSlider(0, (int) nodeCount, (int) Math.min(target, nodeCount));
        sl.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                if (target == sl.getValue())
                    return;
                target = sl.getValue();
                settingsChangeCallback.settingsChanged(false);
            }
        });

        ret.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        //c.gridwidth=2;

        ret.add(sl, c);
        c.gridy++;

        int choices = graph.getNodesOfInterest().size() + 1;

        Object[] items = new Object[choices];
        items[0] = "All PoIs";

        for (int i = 1; i < choices; i++) {
            items[i] = i + " nearest";
        }
        final JComboBox pathTypeBox = new JComboBox(items);
        pathTypeBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                int i = pathTypeBox.getSelectedIndex();
                grader.setPathK(i);
                settingsChangeCallback.settingsChanged(false);
            }
        });

        ret.add(new JLabel("hide by best path quality to:"), c);
        c.gridy++;
        ret.add(pathTypeBox, c);
        c.gridy++;

        System.out.println("new confpane nodeCount = " + nodeCount);

        final JCheckBox useMatchingColoring = new JCheckBox();
        ret.add(useMatchingColoring, c);

        useMatchingColoring.setAction(new AbstractAction(
                "Use matching coloring") {
            public void actionPerformed(ActionEvent arg0) {
                matchColoring = useMatchingColoring.isSelected();
                settingsChangeCallback.settingsChanged(false);
            }
        });

        useMatchingColoring.setAlignmentX(JComponent.CENTER_ALIGNMENT);

        c.gridy++;

        final JCheckBox labelsBox = new JCheckBox();
        ret.add(labelsBox, c);

        labelsBox.setAction(new AbstractAction(
                "Show goodness in node labels") {
            public void actionPerformed(ActionEvent arg0) {
                showLabel = labelsBox.isSelected();
                settingsChangeCallback.settingsChanged(false);
            }
        });

        return ret;
    }

    public String getTitle() {
        return "Best path hider";
    }

    public String getToolTip() {
        return null;
    }

    /**
     *
     */
    public void fromJSON(JSONObject o) {
        oldCount = 1000000;
        target = ((Number) (o.get("target"))).intValue();
        int pk = ((Number) (o.get("pathK"))).intValue();

        System.out.println("BPH json loaded target = " + target);
        Boolean match = (Boolean) o.get("matchColor");
        if (!(match == null || match == false)) matchColoring = true;
        else matchColoring = false;

        grader.setPathK(pk);
    }

    public JSONObject toJSON() {
        JSONObject ret = new JSONObject();
        ret.put("target", target);
        ret.put("pathK", grader.getPathK());
        ret.put("matchColor", matchColoring);
        return ret;
    }

    public void setTargetHiddenNodes(long target) {
        this.target = target;
    }

    public long getTargetHiddenNodes() {
        return this.target;
    }
}
