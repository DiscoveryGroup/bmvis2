package biomine.bmvis2;

import biomine.bmvis2.ui.GraphVisualizer;

import javax.swing.*;
import java.awt.*;

public abstract class EdgeRenderer {
    private VisualEdge edge;

    /**
     * Draw's the edge.
     *
     * @param graphics
     * @param edge
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
