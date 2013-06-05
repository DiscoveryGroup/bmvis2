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

import java.awt.Graphics2D;

import biomine.bmvis2.VisualGraph.Change;

/**
 * Abstract superclass for nodes and edges. Only direct subclass is LabeledItem.
 *
 * @author alhartik
 */
public abstract class LayoutItem {
    private boolean positionFixed = false;
    private boolean highlight = false;
    private boolean neighborHighlight = false;

    /**
     * Position of item
     */
    private Vec2 pos = new Vec2(0, 0);

    private double z; // this is used in determining the drawing order

    /**
     * Graph of which this item is from.
     */
    private VisualGraph graph;

    public LayoutItem(VisualGraph graph) {
        this.graph = graph;
    }

    public void setPos(Vec2 pos) {
        this.pos = pos;
    }

    public Vec2 getPos() {
        if (pos == null) return Vec2.ZERO;
        return pos;
    }

    public void setPositionFixed(boolean newState) {
        this.positionFixed = newState;
        // this.getGraph().graphChanged(Change.VISIBILITY); // this is not called here; see VisualGraph.releaseAll()
    }

    public boolean isPositionFixed() {
        return positionFixed;
    }

    public void paint(Graphics2D g) {

    }


    public boolean containsPoint(Vec2 v) {
        return containsPoint(v.x, v.y);
    }

    public abstract boolean containsPoint(double x, double y);


    public void setHighlight(boolean highlight) {
        boolean old = this.highlight;
        this.highlight = highlight;
        if (old != highlight)
            this.graph.colorsChanged();
    }

    public boolean isHighlighted() {
        return this.highlight;
    }

    public void setNeighborHighlight(boolean neighborHighlight) {
        boolean old = this.neighborHighlight;
        this.neighborHighlight = neighborHighlight;

        if (neighborHighlight != old)
            this.graph.colorsChanged();
    }

    public boolean isNeighborHighlighted() {
        return this.neighborHighlight;
    }

    public void setZ(double zPosition) {
        this.z = zPosition;
    }

    public double getZ() {
        return z;
    }

    protected void graphStructureChanged() {
        graph.graphChanged(Change.STRUCTURE);
    }

    protected void graphViewChanged() {
        graph.graphChanged(Change.VISIBILITY);
    }

    public VisualGraph getGraph() {
        return graph;
    }


}