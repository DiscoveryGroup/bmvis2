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

import biomine.bmvis2.color.DefaultNodeColoring;
import biomine.bmvis2.color.GroupColoring;
import biomine.bmvis2.color.NodeColoring;
import biomine.bmvis2.color.NodeGraderColoring;
import biomine.bmvis2.group.NodeAttributeGrader;
import biomine.bmvis2.pipeline.GraphOperation;
import biomine.bmvis2.pipeline.operations.view.NodeColoringOperation;
import biomine.bmvis2.pipeline.Pipeline;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class ColoringControl extends TranslucentControl {
    private ColoringComboBox coloringComboBox;

    public ColoringControl(Pipeline pipeline) {
        super(pipeline);

        this.coloringComboBox = new ColoringComboBox();
        JLabel coloringLabel = new JLabel("Coloring");

        // EmptyBorder(int top, int left, int bottom, int right)
        coloringLabel.setBorder(new EmptyBorder(5, 5, 0, 5));
        this.coloringComboBox.setBorder(new EmptyBorder(0, 5, 5, 5));

        SpringLayout layout = new SpringLayout();
        this.setLayout(layout);
        layout.putConstraint(SpringLayout.NORTH, this.coloringComboBox, 0, SpringLayout.SOUTH, coloringLabel);

        this.add(coloringLabel);
        this.add(this.coloringComboBox);
    }

    public class ColoringComboBox extends JComboBox implements ActionListener {
        private Collection<NodeColoring> nodeColorings = new HashSet<NodeColoring>();

        public ColoringComboBox() {
            super();

            this.nodeColorings.add(new DefaultNodeColoring());
            this.nodeColorings.add(new GroupColoring());
            this.nodeColorings.add(new NodeGraderColoring(NodeAttributeGrader.BEST_PATH));
            for (NodeColoring nodeColoring : this.nodeColorings)
                this.addItem(nodeColoring.getByName());

            this.addActionListener(this);
        }

        protected NodeColoringOperation getCurrentNodeColoringOperation() {
            Collection<GraphOperation> coloringOps = new ArrayList<GraphOperation>();
            for (GraphOperation go : ColoringControl.this.getPipeline().getFastOps()) {
                if (go instanceof NodeColoringOperation) {
                    coloringOps.add(go);
                }
            }

            if (coloringOps.size() == 1)
                return (NodeColoringOperation) coloringOps.iterator().next();
            return null;
        }

        protected String getSelectedColoringName() {
            return (String) this.getItemAt(this.getSelectedIndex());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ColoringComboBox c = (ColoringComboBox) e.getSource();
            String coloringName = (String) c.getSelectedItem();

            final NodeColoringOperation op = this.getCurrentNodeColoringOperation();

            if (op == null)
                return;

            NodeColoring nc = null;
            for (NodeColoring coloring : this.nodeColorings)
                if (coloring.getByName().equals(coloringName))
                    nc = coloring;
            if (nc == null)
                return;

            final NodeColoring n = nc;

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Pipeline pipeline = ColoringControl.this.getPipeline();
                    pipeline.removeFastOp(op);
                    pipeline.addFastOp(new NodeColoringOperation(n));
                }
            });
        }
    }

    public void updateControl() {
        NodeColoringOperation op = this.coloringComboBox.getCurrentNodeColoringOperation();

        if (op == null) {
            this.coloringComboBox.setEnabled(false);
        } else {
            this.coloringComboBox.setEnabled(true);
            if (!op.getColoringSimpleUIName().equals(this.coloringComboBox.getSelectedColoringName())) {
                for (int i = 0; i < this.coloringComboBox.getItemCount(); i++) {
                    String coloringName = (String) this.coloringComboBox.getItemAt(i);
                    if (coloringName.equals(op.getColoringSimpleUIName()))
                        this.coloringComboBox.setSelectedIndex(i);
                }
            }
        }
    }
}
