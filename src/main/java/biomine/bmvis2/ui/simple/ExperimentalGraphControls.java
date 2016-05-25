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

import biomine.bmvis2.GraphArea;
import biomine.bmvis2.color.ColorPalette;
import biomine.bmvis2.color.DefaultNodeColoring;
import biomine.bmvis2.pipeline.operations.view.NodeColoringOperation;
import biomine.bmvis2.ui.GraphControls;

import javax.swing.*;
import java.awt.*;

public class ExperimentalGraphControls extends GraphControls {
    private ColoringControl coloringControl;
    private GroupingControl groupingControl;

    public class TranslucentPanel extends JPanel {
        public TranslucentPanel() {
            super();
            this.setOpaque(false);
            this.setLayout(new SpringLayout());
        }

        public void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D g = (Graphics2D) graphics;
            Composite old = g.getComposite();

            g.setComposite(AlphaComposite.SrcOver.derive(0.3f));
            g.setColor(ColorPalette.GMAIL_BLUE);
            g.fill3DRect(0, 0, this.getSize().width, this.getSize().height, true);

            g.setComposite(old);
        }

        @Override
        public Dimension getPreferredSize() {
            double x = 0;
            double y = 0;

            for (Component component : this.getComponents()) {
                if (component.getPreferredSize().width > x)
                    x = component.getPreferredSize().width;

                y = y + component.getPreferredSize().height;
            }
            return new Dimension((int) x, (int) y);
        }
    }

    @Override
    public void updateControls() {
        if (coloringControl != null)
            coloringControl.updateControl();
        if (groupingControl != null)
            groupingControl.updateControl();
    }

    @Override
    public void initElements(GraphArea area) {
        TranslucentPanel panel = new TranslucentPanel();
        area.addLeftReachingComponent(panel);

        this.getPipeline().addFastOp(new NodeColoringOperation(new DefaultNodeColoring()));

        this.coloringControl = new ColoringControl(this.getPipeline());
        this.groupingControl = new GroupingControl(this.getPipeline());
        MouseClickModeControl mouseClickModeControl = new MouseClickModeControl(this.getPipeline());

        ((SpringLayout) panel.getLayout()).putConstraint(SpringLayout.NORTH, this.groupingControl, 0,
                SpringLayout.SOUTH, this.coloringControl);
        ((SpringLayout) panel.getLayout()).putConstraint(SpringLayout.NORTH, mouseClickModeControl, 0,
                SpringLayout.SOUTH, this.groupingControl);

        panel.add(this.coloringControl);
        panel.add(this.groupingControl);
        panel.add(mouseClickModeControl);
    }
}
