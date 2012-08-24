/*
 * Copyright 2012 University of Helsinki.
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

package biomine.bmvis2.ui;

import java.awt.*;
import java.awt.geom.AffineTransform;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import biomine.bmvis2.*;
import biomine.bmvis2.GraphArea.GraphAreaSettings;
import biomine.bmvis2.pipeline.Pipeline;

/**
 * This is the GraphVisualizer used in the classic mode with sidepanes.
 * This provides, in addition to the graph visualization, the left sidepane
 * with hierarchical node browser.
 */
public class GraphView extends GraphVisualizer {
    private GraphArea graphArea;
    private NodeBrowser nodeBrowser;

    private JSplitPane splitPane;
    private VisualGraph graph = null;
    private Pipeline pipeline;

    public GraphView() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;

        this.graphArea = new GraphArea(this.pipeline.getCurrentGraph(), pipeline);
        this.graphArea.setPipeline(pipeline);

        if (splitPane != null)
            this.remove(splitPane);

        this.nodeBrowser = new NodeBrowser(this.pipeline);
        this.areaSettings.removeAll();
        this.areaSettings.setLayout(new GridLayout(1, 1));

        this.splitPane = new HidingSplitPane(pipeline.getVis().useSimpleUI(), false);
        this.splitPane.setLeftComponent(nodeBrowser);
        this.splitPane.setRightComponent(graphArea);
        this.add(splitPane);

        this.revalidate();

        this.setGraph(pipeline.getCurrentGraph(), true);
    }

    public void setGraph(VisualGraph newGraph, boolean initialZoom) {
        GraphAreaSettings settings = null;
        AffineTransform transform = null;
        if (this.graphArea != null) {
            settings = this.graphArea.getSettings();
            transform = this.graphArea.getTransform();
        }

        if (settings != null)
            this.graphArea.setSettings(settings);

        if (transform != null)
            this.graphArea.setTransform(transform);

        this.graphArea.setGraph(newGraph, initialZoom);
        newGraph.addObserver(this.nodeBrowser);

        if (this.graph != null)
            this.graph.removeObserver(this.nodeBrowser);
        this.nodeBrowser.graphStructureChanged(newGraph);
    }

    private JPanel areaSettings = new JPanel();

    public VisualGraph getGraph() {
        return graph;
    }

    public GraphArea getGraphArea() {
        return this.graphArea;
    }
}
