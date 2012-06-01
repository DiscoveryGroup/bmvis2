package biomine.bmvis2;

import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;

/**
 * Draws bezier-curves for edges.
 *
 * @author alhartik
 */

public class BezierEdgeRenderer extends EdgeRenderer {
    private VisualEdge edge;
    private QuadCurve2D curve;

    /**
     * Mutable Vec2 to reduce copy operations.
     * Be careful with this.
     */
    private MutableVec2 mutableVec = new MutableVec2(0, 0);

    /**
     * Stores point on curve to mutableVec and returns it
     *
     * @param t
     * @return mv now containing curve point
     */
    MutableVec2 getCurvePoint(double t) {
        Vec2 p0 = edge.getFrom().getVisibleAncestor().getPos();
        Vec2 p1 = new Vec2(curve.getCtrlPt());
        Vec2 p2 = edge.getTo().getVisibleAncestor().getPos();

        // Derived from cubic bezier equations (http://en.wikipedia.org/wiki/Bezier_curve)
        double a = (1 - t) * (1 - t);
        double b = 2 * (1 - t) * t;
        double c = t * t;
        double x = a * p0.x + b * p1.x + c * p2.x;
        double y = a * p0.y + b * p1.y + c * p2.y;
        mutableVec.set(x, y);
        return mutableVec;
    }

    /**
     * Initializes object to draw given edge.
     *
     * @param e
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

    /**
     * Path used to draw arrowheads
     */
    Path2D.Double arrowheadPath = new Path2D.Double();
    /**
     * MutableVecs used in computations. Again to reduce memory
     * allocations.
     */
    private MutableVec2 dir = new MutableVec2(0, 0);
    private MutableVec2 dirNormal = new MutableVec2(0, 0);

    /**
     * Draws edge on given canvas
     */
    public void drawEdge(Graphics2D g, VisualEdge edge) {
        this.edge = edge;
        VisualNode fromAncestor = edge.getFrom().getVisibleAncestor();
        VisualNode toAncestor = edge.getTo().getVisibleAncestor();
        Vec2 toPos = toAncestor.getPos();

        if (fromAncestor == toAncestor)
            return;

        this.setWeightAndColor(g, edge);

        this.updateCurve(edge);
        g.draw(curve);

        // Arrowhead
        if (!edge.isSymmetric()) {
            double left = 0.0;
            double right = 1;
            MutableVec2 curvePoint = null;

            // Binary search to find the location of the arrowhead
            for (int i = 0; i < 40; i++) {
                double ct = (left + right) / 2;
                curvePoint = getCurvePoint(ct);

                if (toAncestor.containsPoint(curvePoint.x, curvePoint.y)) {
                    right = ct;
                } else {
                    left = ct;
                }
            }

            curvePoint = getCurvePoint(right);

            double headX = curvePoint.x;
            double headY = curvePoint.y;

            arrowheadPath.reset();
            arrowheadPath.moveTo(headX, headY);

            MutableVec2 cp2 = getCurvePoint(left - 0.1);

            dir.set(toPos);
            dir.subtract(cp2);
            dir.scale(1 / dir.length());

            dirNormal.set(dir.y, -dir.x);

            dir.scale(12);

            dirNormal.scale(6);
            arrowheadPath.lineTo(headX - dir.x + dirNormal.x, headY - dir.y + dirNormal.y);
            arrowheadPath.lineTo(headX - dir.x - dirNormal.x, headY - dir.y - dirNormal.y);
            arrowheadPath.lineTo(headX, headY);
            g.fill(arrowheadPath);
        }
    }

}
