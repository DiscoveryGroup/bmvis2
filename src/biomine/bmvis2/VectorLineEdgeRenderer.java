package biomine.bmvis2;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

/**
 * @author ahinkka
 */
public class VectorLineEdgeRenderer extends EdgeRenderer {
    public void drawEdge(Graphics2D g, VisualEdge edge) {
        VisualNode fromAnc = edge.getFrom().getVisibleAncestor();
        VisualNode toAnc = edge.getTo().getVisibleAncestor();

        if (fromAnc == toAnc)
            return;

        Vec2 toPos = toAnc.getPos();
        Vec2 fromPos = fromAnc.getPos();

        setWeightAndColor(g, edge);

        Line2D.Double lineFrom = new Line2D.Double(fromPos.toPoint(), edge.getPos().toPoint());
        g.draw(lineFrom);

        if (!edge.isSymmetric()) {
            GeneralPath p = new GeneralPath();
            p.moveTo(edge.getPos().toPoint().getX(), edge.getPos().toPoint().getY());

            Vec2 normal = toPos.scaled(1 / toPos.length()).normal();

            Vec2 left = toPos.minus(normal.scaled(3.0));
            Vec2 right = toPos.plus(normal.scaled(3.0));

            p.lineTo(left.toPoint().getX(), left.toPoint().getY());
            p.lineTo(right.toPoint().getX(), right.toPoint().getY());

            p.closePath();

            g.draw(p);

            Paint gradient = new GradientPaint(new Float(edge.getPos().toPoint().getX()),
                    new Float(edge.getPos().toPoint().getY()),
                    Color.WHITE,
                    new Float(toPos.toPoint().getX()),
                    new Float(toPos.toPoint().getY()),
                    g.getColor());
            g.setPaint(gradient);
            g.fill(p);
        } else {
            Line2D.Double lineTo = new Line2D.Double(toPos.toPoint(), edge.getPos().toPoint());
            g.draw(lineTo);
        }
    }
}
