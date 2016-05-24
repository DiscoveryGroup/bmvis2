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

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.geom.GeneralPath;
import java.awt.geom.QuadCurve2D;


class VectorBezierEdgeRenderer extends EdgeRenderer {
    private QuadCurve2D curve;

    private boolean gradientColor = false;
    private boolean strokeLines = false;

    /**
     * Initializes object to draw given edge.
     */
    private void updateCurve(VisualEdge e) {
        Vec2 fromPos = e.getFrom().getVisibleAncestor().getPos();
        Vec2 toPos = e.getTo().getVisibleAncestor().getPos();

        Vec2 curveCenter = e.getPos();
        // second control point
        double px = 2 * (curveCenter.x - 0.25 * fromPos.x - 0.25 * toPos.x);
        double py = 2 * (curveCenter.y - 0.25 * fromPos.y - 0.25 * toPos.y);
        if (curve == null)
            curve = new QuadCurve2D.Double();

        curve.setCurve(fromPos.x, fromPos.y, px, py, toPos.x, toPos.y);
    }

    private GeneralPath getVectorShape(VisualEdge e) {
        Vec2 fromPos = e.getFrom().getVisibleAncestor().getPos();
        Vec2 toPos = e.getTo().getVisibleAncestor().getPos();

        // toPos = toPos.plus(toPos.scaled(1 / toPos.length() * 10.0)); // to make it longer
        Vec2 curveCenter = e.getPos();

        double px = 2 * (curveCenter.x - 0.25 * fromPos.x - 0.25 * toPos.x);
        double py = 2 * (curveCenter.y - 0.25 * fromPos.y - 0.25 * toPos.y);

        Vec2 endNormal = toPos.scaled(1 / toPos.length()).normal();
        Vec2 endLeft = toPos.minus(endNormal.scaled(3.0));  // increase to widen vector heads
        Vec2 endRight = toPos.plus(endNormal.scaled(3.0));

        GeneralPath path = new GeneralPath();

        path.moveTo(fromPos.toPoint().getX(), fromPos.toPoint().getY());
        path.quadTo(px, py, endLeft.toPoint().getX(), endLeft.toPoint().getY());

        path.lineTo(endRight.toPoint().getX(), endRight.toPoint().getY());
        path.quadTo(px, py, fromPos.toPoint().getX(), fromPos.toPoint().getY());

        path.closePath();
        return path;
    }


    public void drawEdge(Graphics2D g, VisualEdge edge) {
        VisualNode fromAnc = edge.getFrom().getVisibleAncestor();
        VisualNode toAnc = edge.getTo().getVisibleAncestor();

        if (fromAnc == toAnc)
            return;

        Vec2 toPos = toAnc.getPos();
        Vec2 fromPos = fromAnc.getPos();

        setWeightAndColor(g, edge);

        this.updateCurve(edge);
        g.draw(curve);

        if (!edge.isSymmetric()) {
            GeneralPath p = this.getVectorShape(edge);
            // g.draw(p);

            if (this.gradientColor) {
                Paint gradient = new GradientPaint((float) fromPos.toPoint().getX(),
                        (float) fromPos.toPoint().getY(),
                        Color.WHITE,
                        (float) toPos.toPoint().getX(),
                        (float) toPos.toPoint().getY(),
                        g.getColor());
                g.setPaint(gradient);
            }

            if (this.strokeLines)
                g.draw(p);

            g.fill(p);
        }
    }

    public JComponent getSettingsComponent(final GraphVisualizer visualizer) {
        JPanel ret = new JPanel();

        GridLayout gl = new GridLayout(2, 1);
        ret.setLayout(gl);

        final JCheckBox gradientColorCB = new JCheckBox("Gradient color");
        final JCheckBox strokeLinesCB = new JCheckBox("Draw stroke lines");
        gradientColorCB.setSelected(this.gradientColor);
        strokeLinesCB.setSelected(this.strokeLines);

        ChangeListener cl = new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                VectorBezierEdgeRenderer.this.gradientColor = gradientColorCB.isSelected();
                VectorBezierEdgeRenderer.this.strokeLines = strokeLinesCB.isSelected();
                visualizer.getGraphArea().repaint();
            }
        };

        gradientColorCB.addChangeListener(cl);
        strokeLinesCB.addChangeListener(cl);

        ret.add(gradientColorCB);
        ret.add(strokeLinesCB);

        return ret;
    }
}
