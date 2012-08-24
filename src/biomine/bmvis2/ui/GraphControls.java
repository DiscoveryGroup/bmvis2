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
