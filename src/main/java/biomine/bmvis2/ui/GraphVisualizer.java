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
