package biomine.bmvis2.ui.simple;

import biomine.bmvis2.pipeline.Pipeline;

import javax.swing.*;
import java.awt.*;

abstract class TranslucentControl extends JPanel {
    private Pipeline pipeline;

    public TranslucentControl(Pipeline pipeline) {
        super();
        this.setOpaque(false);
        this.pipeline = pipeline;
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

    public void updateControl() {

    }

    public Pipeline getPipeline() {
        return this.pipeline;
    }
}
