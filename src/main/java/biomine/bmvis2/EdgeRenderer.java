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

package biomine.bmvis2;

import biomine.bmvis2.ui.GraphVisualizer;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;


public abstract class EdgeRenderer {
    /**
     * Draw's the edge.
     */
    public abstract void drawEdge(Graphics2D graphics, VisualEdge edge);

    public JComponent getSettingsComponent(final GraphVisualizer visualizer) {
        return new JPanel();
    }

    protected static void setWeightAndColor(Graphics2D g, VisualEdge edge) {
        g.setColor(Color.BLACK);

        double weight = 0.1 + 1 * edge.getGoodness();

        if (weight > 1)
            weight = 1;

        double notGood = 1 - edge.getGoodness();

        if (notGood < 0)
            notGood = 0;

        g.setColor(new Color((int) (notGood * 255), (int) (notGood * 255), (int) (notGood * 255)));

        if (edge.isHighlighted() || edge.getFrom().isHighlighted()
                || edge.getTo().isHighlighted()) {
            g.setColor(Color.RED);
            weight *= 2;
        }

        BasicStroke stroke = new BasicStroke((float) (weight));
        g.setStroke(stroke);
    }
}
