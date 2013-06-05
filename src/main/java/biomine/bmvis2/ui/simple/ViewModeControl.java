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

import biomine.bmvis2.Logging;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.color.DefaultNodeColoring;
import biomine.bmvis2.color.GroupColoring;
import biomine.bmvis2.group.GirvanNewmanClustering;
import biomine.bmvis2.group.Grouper;
import biomine.bmvis2.pipeline.*;
import discovery.compression.kdd2011.ratio.RatioCompressionGrouper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

public class ViewModeControl extends TranslucentControl {
    private SpringLayout layout;
    private ViewModeComboBox modeComboBox;
    private JComponent settingsComponent;

    private GirvanNewmanClustering grouper = new GirvanNewmanClustering();
    private RatioCompressionGrouper compressor = new RatioCompressionGrouper();

    public static String VIEWMODE_PLAIN = "Plain";
    public static String VIEWMODE_ROLE = "Role";
    public static String VIEWMODE_COMMUNITY = "Community";

    public String currentViewMode = VIEWMODE_PLAIN;

    public ViewModeControl(Pipeline pipeline) {
        super(pipeline);

        layout = new SpringLayout();
        this.setLayout(layout);

        this.modeComboBox = new ViewModeComboBox();
        // top, left, bottom, right
        // this.modeComboBox.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.add(this.modeComboBox);
    }

    private void modeChangeCallback(String newMode) {
        if (currentViewMode.equals(newMode))
            return;
        Logging.info("ui", "View mode " + newMode + " enabled.");

        if (newMode.equals(VIEWMODE_PLAIN)) {
            NodeColoringOperation coloringOp = new NodeColoringOperation(new DefaultNodeColoring());

            this.getPipeline().removeColoringOperations();
            this.getPipeline().addFastOp(coloringOp);
            this.getPipeline().settingsChanged(false);
            this.getPipeline().getCurrentGraph().setHighlightMode(VisualGraph.HighlightMode.NEIGHBORS);

        } else if (newMode.equals(VIEWMODE_ROLE)) {
            NodeColoringOperation coloringOp = new NodeColoringOperation(new GroupColoring());

            this.getPipeline().removeColoringOperations();
            this.getPipeline().addFastOp(coloringOp);

            boolean grouperSet = false;
            List<StructuralOperation> removableOps = new LinkedList<StructuralOperation>();
            for (StructuralOperation op : this.getPipeline().getCurrentStructuralOps()) {
                if (op instanceof GrouperOperation) {
                    GrouperOperation grouperOp = (GrouperOperation) op;
                    Grouper grouper = grouperOp.getGrouper();
                    if (grouper instanceof RatioCompressionGrouper) {
                        grouperSet = true;
                    } else {
                        removableOps.add(op);
                    }
                }
            }
            for (StructuralOperation op : removableOps)
                this.getPipeline().removeStructuralOp(op);

            if (!grouperSet) {
                this.getPipeline().addStructuralOp(new GrouperOperation(this.compressor));
                this.getPipeline().settingsChanged(true);
            } else
                this.getPipeline().settingsChanged(false);

            this.getPipeline().getCurrentGraph().setHighlightMode(VisualGraph.HighlightMode.GROUP);

        } else if (newMode.equals(VIEWMODE_COMMUNITY)) {
            NodeColoringOperation coloringOp = new NodeColoringOperation(new GroupColoring());

            this.getPipeline().removeColoringOperations();
            this.getPipeline().addFastOp(coloringOp);

            boolean grouperSet = false;
            List<StructuralOperation> removableOps = new LinkedList<StructuralOperation>();
            for (StructuralOperation op : this.getPipeline().getCurrentStructuralOps()) {
                if (op instanceof GrouperOperation) {
                    GrouperOperation grouperOp = (GrouperOperation) op;
                    Grouper grouper = grouperOp.getGrouper();
                    if (grouper instanceof GirvanNewmanClustering) {
                        grouperSet = true;
                    } else {
                        removableOps.add(op);
                    }
                }
            }
            for (StructuralOperation op : removableOps)
                this.getPipeline().removeStructuralOp(op);

            this.getPipeline().getCurrentGraph().setHighlightMode(VisualGraph.HighlightMode.GROUP);

            if (!grouperSet) {
                this.getPipeline().addStructuralOp(new GrouperOperation(this.grouper));
                this.getPipeline().settingsChanged(true);
            } else
                this.getPipeline().settingsChanged(false);

            this.getPipeline().getCurrentGraph().setHighlightMode(VisualGraph.HighlightMode.GROUP);
        } else {
            return;
        }

        if (this.settingsComponent != null)
            this.remove(this.settingsComponent);
        this.settingsComponent = null;
        if (newMode.equals(VIEWMODE_COMMUNITY) || newMode.equals(VIEWMODE_ROLE)) {
            this.settingsComponent = this.getPipeline().getCurrentStructuralOps().iterator().next().getSettingsComponent(this.getPipeline(), this.getPipeline().getCurrentGraph());
            this.settingsComponent.setBorder(new EmptyBorder(5, 5, 5, 5));
            this.settingsComponent.setBounds(new Rectangle(200, 200));
            this.settingsComponent.setOpaque(false);

            this.layout.putConstraint(SpringLayout.NORTH, this.settingsComponent, 0, SpringLayout.SOUTH, this.modeComboBox);
            this.add(this.settingsComponent);
        }
        this.revalidate();

        currentViewMode = newMode;
    }

    public class ViewModeComboBox extends JComboBox implements ActionListener {
        public ViewModeComboBox() {
            super();

            this.addItem(VIEWMODE_PLAIN);
            // this.addItem(VIEWMODE_ROLE);
            this.addItem(VIEWMODE_COMMUNITY);

            this.setEnabled(true);
            this.addActionListener(this);
        }


        @Override
        public void actionPerformed(ActionEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ViewModeControl.this.modeChangeCallback((String) ViewModeComboBox.this.getSelectedItem());
                }
            });

        }
    }
}