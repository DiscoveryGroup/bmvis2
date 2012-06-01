package biomine.bmvis2.ui;

import biomine.bmvis2.GraphArea;
import biomine.bmvis2.Logging;
import biomine.bmvis2.pipeline.Pipeline;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * Abstract class to handle GraphVisualizer and Pipeline management
 *
 * @author ahinkka
 */
public abstract class GraphControls extends JPanel {
    private Pipeline pipeline;

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public Pipeline getPipeline() {
        return this.pipeline;
    }

    public abstract void updateControls();

    public abstract void initElements (GraphArea area);

    public void addInterestNode(String n) {
    }

    public void removeInterestNode(String n) {
    }

    public void setInterestNodes(Set<String> interestNodes) {
    }

    public Set<String> getInterestNodes() {
        return null;
    }
}
