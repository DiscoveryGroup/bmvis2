package biomine.bmvis2.ui;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Collections;

import javax.script.ScriptEngineManager;
import javax.swing.BoxLayout;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import biomine.bmvis2.GraphArea;
import biomine.bmvis2.Logging;
import biomine.bmvis2.Vis;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.pipeline.*;
import biomine.bmvis2.pipeline.sources.FileGraphSource;
import biomine.bmvis2.ui.simple.GroupModalGraphControls;

public class GraphTab extends JPanel {
    private Pipeline pipeline;
    private GraphVisualizer visualizer;  // wrapper for the two left components (node browser + visualized graph)


    public GraphTab(Vis vis) {
        Vis vis1 = vis;

        GraphControls controls;
        if (!vis.useSimpleUI()) {
            controls = new PipelineControls();
            this.pipeline = new Pipeline(controls, vis);
            this.visualizer = new GraphView();
            this.pipeline.setGraphVisualizer(this.visualizer);

            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            HidingSplitPane pipeLineSplit = new HidingSplitPane(false, vis.useSimpleUI());
            pipeLineSplit.setRightComponent(controls);
            pipeLineSplit.setLeftComponent(visualizer);
            pipeLineSplit.setOneTouchExpandable(true);
            pipeLineSplit.setResizeWeight(1);
            pipeLineSplit.setDividerLocation(1.0);

            this.add(pipeLineSplit);
        } else {
            // this.controls = new ExperimentalGraphControls();
            controls = new GroupModalGraphControls();

            this.pipeline = new Pipeline(controls, vis);

            this.visualizer = new GraphArea(VisualGraph.EMPTY, this.pipeline);
            this.pipeline.setGraphVisualizer(this.visualizer);

            this.setLayout(new GridLayout(0, 1));
            this.add(this.visualizer);
        }

        FocusListener focusListener = new FocusListener() {
            public void focusGained(FocusEvent focusEvent) {
                Logging.debug("focus", GraphTab.this.getClass() + " gained focus!");
            }

            public void focusLost(FocusEvent focusEvent) {
                Logging.debug("focus", GraphTab.this.getClass() + " lost focus!");
            }
        };
        this.addFocusListener(focusListener);
    }

    public GraphVisualizer getVisualizer() {
        return visualizer;
    }

    private void updateJS() {
        try {
            ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
            if (scriptEngineManager != null
                    && JavaScriptConsole.hasJavaScript()) {
                System.err.println("Loading JavaScript console...");
                // this.consoles = new ArrayList<JavaScriptConsole>();
                JavaScriptConsole console = new JavaScriptConsole(pipeline.getCurrentGraph(),
                        scriptEngineManager);
                System.err.println("JavaScript console loaded.");
                // this.consoles.add(c);
            }
        } catch (Exception e) {
            System.err
                    .println("Class ScriptingEngineManager not found. Scripting console disabled.");
        }

    }

    /**
     * Initialization can be optimized by providing an operations list to Pipeline instead of adding them separately.
     * @param fileName
     */
    public void addFile(String fileName) {
        assert this.visualizer != null;
        pipeline.addSource(new FileGraphSource(fileName));
    }

    public void addGraphSource(FileGraphSource fileGraphSource) {
        pipeline.addSource(fileGraphSource);
    }

    private JMenuBar menuBar;

    public JMenuBar getMenuBar() {
        if (menuBar != null)
            return menuBar;

        JMenuBar bar = new JMenuBar();
        Menus m = Menus.getInstance(pipeline);
        m.setPipeline(pipeline);

        m.buildMenuBar(bar, Collections.EMPTY_LIST);

        return menuBar = bar;
    }

    public VisualGraph getVisualGraph() {
        return pipeline.getCurrentGraph();

    }

    public Pipeline getPipeline() {
        return pipeline;
    }
}
