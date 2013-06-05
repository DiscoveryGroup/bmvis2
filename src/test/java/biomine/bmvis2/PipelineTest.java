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

package biomine.bmvis2;

import biomine.bmvis2.pipeline.Pipeline;
import biomine.bmvis2.pipeline.sources.StreamGraphSource;
import biomine.bmvis2.ui.GraphControls;
import biomine.bmvis2.ui.GraphVisualizer;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;

public class PipelineTest {
    private Pipeline p;

    public class DummyGraphControls extends GraphControls {
        @Override
        public void updateControls() {
            Logging.debug("ui", this.getClass() + ": updateControls() called!");
        }

        @Override
        public void initElements(GraphArea area) {
            Logging.debug("ui", this.getClass() + ": initElements() called!");
        }
    }

    public class DummyVis extends Vis {
    }

    public class DummyGraphArea extends GraphArea {
        public DummyGraphArea(VisualGraph graph, Pipeline pipeline) {
            super(graph, pipeline);
        }

        public void activateLayoutManager() {
            Logging.debug("ui", this.getClass() + ": activateLayoutManager() called!");
        }
    }

    public class DummyGraphVisualizer extends GraphVisualizer {
        VisualGraph g;
        Pipeline p;
        DummyGraphArea a;

        public DummyGraphVisualizer(VisualGraph graph, Pipeline pipeline) {
            this.a = new DummyGraphArea(graph, pipeline);
        }

        public void setGraph(VisualGraph newGraph, boolean initialZoom) {
            this.g = newGraph;
            Logging.debug("pipeline", this.getClass() + ": setGraph() called!");
        }

        public void setPipeline(Pipeline pipeline) {
            this.p = pipeline;
            Logging.debug("pipeline", this.getClass() + ": setPipeline() called!");
        }

        public GraphArea getGraphArea() {
            Logging.debug("pipeline", this.getClass() + ": getGraphArea() called!");
            return this.a;
        }
    }

    public static StreamGraphSource stringToStreamGraphSource(String graph) {
        ByteArrayInputStream bs = new ByteArrayInputStream(graph.getBytes());
        return new StreamGraphSource(bs, "Graph");
    }

    /*
    @BeforeMethod
    public void setUp() throws Exception {
        GraphControls c = new DummyGraphControls();
        DummyVis v = new DummyVis();
        p = new Pipeline(c, v);
        DummyGraphVisualizer vi = new DummyGraphVisualizer(VisualGraph.EMPTY, p);
        p.setGraphVisualizer(vi);
    }

    @AfterMethod
    public void tearDown() throws Exception {
    }

    @Test
    public void testAddStreamGraphSource() throws Exception {
        this.p.addSource(stringToStreamGraphSource(GraphReadingTest.newStyleGroupString));
        Assert.assertEquals(this.p.getCurrentGraph().getAllNodes().size(), 7);

        for (VisualNode node : this.p.getCurrentGraph().getAllNodes())
            Assert.assertNotSame(node.getPos(), Vec2.ZERO);
        for (VisualEdge edge : this.p.getCurrentGraph().getAllEdges())
            Assert.assertNotSame(edge.getPos(), Vec2.ZERO);
    }

    @Test
    public void testRedoGroupings() throws Exception {
        this.p.settingsChanged(true);
        Logging.info("pipeline", p.toString());
    }
    */
}
