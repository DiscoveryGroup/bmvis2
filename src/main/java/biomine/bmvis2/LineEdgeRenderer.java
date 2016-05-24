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

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;


/**
 * Draws straight lines between nodes and edge nodes.
 */
public class LineEdgeRenderer extends EdgeRenderer {

    VisualEdge e;

    public void drawEdge(Graphics2D g, VisualEdge e) {
        this.e = e;
        VisualNode fromAnc = e.getFrom();
        VisualNode toAnc = e.getTo();

        if (fromAnc == toAnc) {
            return;
        }

        Vec2 tp = toAnc.getPos();
        Vec2 fp = fromAnc.getPos();

        Line2D.Double lineFrom = new Line2D.Double(fp.toPoint(), e.getPos().toPoint());
        Line2D.Double lineTo = new Line2D.Double(tp.toPoint(), e.getPos().toPoint());

        this.setWeightAndColor(g, e);
        g.draw(lineFrom);
        g.draw(lineTo);

        //ARROWHEAD
        if (!e.isSymmetric()) {
            double left = 0.0;
            double right = 1;
            VisualNode to = e.getTo();
            MutableVec2 cp = new MutableVec2(0, 0);
            for (int i = 0; i < 30; i++) {
                double ct = (left + right) / 2;
                cp.set(e.getPos());
                cp.scale((1 - ct) / ct);
                cp.add(tp);
                cp.scale(ct);

                if (to.containsPoint(cp.toVec2())) {
                    right = ct;
                } else {
                    left = ct;
                }
            }

            Path2D.Double path = new Path2D.Double();
            tp = cp.toVec2();


            path.moveTo(tp.x, tp.y);

            // strange vector calc
            // for proper arrowhead

            Vec2 d = tp.minus(e.getPos().scaled(1 - left).plus(tp.scaled(left)));
            d = d.scaled(1 / d.length());
            Vec2 nd = d.normal();
            d = d.scaled(12);

            nd = nd.scaled(6);
            path.lineTo(tp.x - d.x + nd.x, tp.y - d.y + nd.y);
            path.lineTo(tp.x - d.x - nd.x, tp.y - d.y - nd.y);
            path.lineTo(tp.x, tp.y);
            g.fill(path);
        }
    }
}
