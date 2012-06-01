package biomine.bmvis2.ui;

import biomine.bmvis2.GraphArea;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.pipeline.Pipeline;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Interface for showing the actual graph visualization.
 *
 * @author ahinkka
 */

public abstract class GraphVisualizer extends JPanel {
    public abstract void setGraph(VisualGraph newGraph, boolean initialZoom);

    public abstract void setPipeline(Pipeline pipeline);

    public abstract GraphArea getGraphArea();
}
