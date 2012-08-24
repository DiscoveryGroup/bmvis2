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

import biomine.bmvis2.*;
import biomine.bmvis2.pipeline.Pipeline;
import biomine.bmvis2.pipeline.SettingsChangeCallback;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MouseClickModeControl extends TranslucentControl {

    public MouseClickModeControl(Pipeline pipeline) {
        super(pipeline);

        MouseModeComboBox mouseModeComboBox = new MouseModeComboBox();
        JLabel mouseModeLabel = new JLabel("Click to");

        mouseModeComboBox.setBorder(new EmptyBorder(0, 5, 5, 5));
        mouseModeLabel.setBorder(new EmptyBorder(5, 5, 0, 5));

        SpringLayout layout = new SpringLayout();
        this.setLayout(layout);
        layout.putConstraint(SpringLayout.NORTH, mouseModeComboBox, 0, SpringLayout.SOUTH, mouseModeLabel);

        this.add(mouseModeLabel);
        this.add(mouseModeComboBox);
    }


    public class MouseModeComboBox extends JComboBox implements ActionListener {
        public MouseModeComboBox() {
            super();

            this.addItem(GraphArea.MOUSE_CLICK_MODE_SELECT);
            this.addItem(GraphArea.MOUSE_CLICK_MODE_TOGGLE_GROUP);
            // this.addItem(GraphArea.MOUSE_CLICK_MODE_NODE_EXPAND);

            this.setEnabled(true);
            this.addActionListener(this);
        }


        @Override
        public void actionPerformed(ActionEvent e) {
            MouseClickModeControl.this.getPipeline().getVisualizer().getGraphArea().setMouseClickMode((String) this.getItemAt(this.getSelectedIndex()));
        }
    }
}
