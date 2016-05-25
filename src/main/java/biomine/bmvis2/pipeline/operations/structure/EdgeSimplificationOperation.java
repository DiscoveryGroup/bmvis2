/*
 * Copyright 2012-2016 University of Helsinki.
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

package biomine.bmvis2.pipeline.operations.structure;

import biomine.bmvis2.GraphCache;
import biomine.bmvis2.Logging;
import biomine.bmvis2.VisualEdge;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualGraph.Change;
import biomine.bmvis2.edgesimplification.SimplificationUtils;
import biomine.bmvis2.edgesimplification.Simplifier;
import biomine.bmvis2.pipeline.GraphOperation;
import biomine.bmvis2.pipeline.SettingsChangeCallback;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

/**
 * Settings component of this operation should not run the pipeline.  This
 * is unfortunately a bit too complicated.  It should just modify the
 * VisualGraph instance in place.  Otherwise this operation is a bit too heavy
 * for slider use.
 */
public class EdgeSimplificationOperation implements GraphOperation, EdgeHiderOperation {
    public Simplifier simplifier;
    JLabel costLabel = new JLabel();
    int oldEdgeCount = 1;
    private long target = 1000000; // visible edge count target
    private VisualGraph graph;
    // We need to keep track of the current graph here to be able to hide and show stuff without running the whole
    // pipeline.

    private GraphCache<ArrayList<VisualEdge>> hiddenCache = new GraphCache<ArrayList<VisualEdge>>(
            Change.POINTS_OF_INTEREST, Change.STRUCTURE);

    public EdgeSimplificationOperation() {
        this.simplifier = null;
    }

    public EdgeSimplificationOperation(Simplifier simpl) {
        this.simplifier = simpl;
    }


    public void doOperation(VisualGraph graph) throws GraphOperationException {
        this.graph = graph;
        int edgeCount = SimplificationUtils.countNormalEdges(graph);
        this.target = Math.min(edgeCount, this.target);  // sanity check, if target isn't set from JSON import

        ArrayList<VisualEdge> hidableEdgeList = hiddenCache.get(graph);
        if (hidableEdgeList == null || this.target < hidableEdgeList.size())
            this.reCalculateHidables(graph);

        hideHidables(graph);
        costLabel.setText("C=" + SimplificationUtils.graphConnectivity(graph));
    }


    private void reCalculateHidables(VisualGraph graph) {
        Logging.info("simplifier", "Edge count target: " + this.target);
        ArrayList<VisualEdge> hidableEdgeList = hiddenCache.get(graph);

        if (hidableEdgeList == null) {
            hidableEdgeList = new ArrayList<VisualEdge>(simplifier.getRemovedEdges(graph, (int) this.target + 10));
            hiddenCache.put(graph, hidableEdgeList);
        }
    }

    private void hideHidables(VisualGraph graph) {
        int visibleCount = graph.getAllEdges().size() - graph.getHiddenEdges().size();
        for (VisualEdge edge : this.hiddenCache.get(graph)) {

            if (visibleCount > target && !graph.getHiddenEdges().contains(edge)) {
                graph.hideEdge(edge);
                visibleCount--;
            } else if (visibleCount < target && graph.getHiddenEdges().contains(edge)) {
                graph.unHideEdge(edge);
                visibleCount++;
            } else if (visibleCount == target)
                break;
        }
    }


    public JComponent getSettingsComponent(final SettingsChangeCallback settingsChangeCallback,
                                           VisualGraph graph) {
        this.graph = graph;
        int edgeCount = SimplificationUtils.countNormalEdges(graph);

        if (oldEdgeCount != 0) {
            long newTarget = (this.target * edgeCount) / this.oldEdgeCount;
            if (newTarget != this.target) {
                this.target = Math.max(newTarget, this.target);
                Logging.info("simplifier", "New edge count target: " + this.target);
            }
        } else
            this.target = edgeCount;

        this.oldEdgeCount = edgeCount;

        JPanel ret = new JPanel();
        final JSlider sl = new JSlider(0, edgeCount, (int) Math.min(target, edgeCount));
        sl.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                if (target == sl.getValue())
                    return;
                target = sl.getValue();

                if (EdgeSimplificationOperation.this.getGraph() == null)
                    settingsChangeCallback.settingsChanged(false);
                else
                    EdgeSimplificationOperation.this.hideHidables(EdgeSimplificationOperation.this.getGraph());
            }
        });
        ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));
        ret.add(sl);

        ret.add(costLabel);
        return ret;
    }

    public String getTitle() {
        return simplifier.getTitle();
    }

    public String getToolTip() {
        return null;
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
    }

    public void fromJSON(JSONObject o) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        target = ((Long) o.get("target")).intValue();
        Class simplifierClass;
        String simplifierClassName = o.get("simplifier").toString();
        simplifierClass = getClass().getClassLoader().loadClass(
                simplifierClassName);

        this.simplifier = (Simplifier) simplifierClass.newInstance();
    }

    public JSONObject toJSON() {
        JSONObject ret = new JSONObject();
        ret.put("target", target);
        ret.put("simplifier", simplifier.getClass().getName());
        return ret;
    }

    public long getTargetHiddenEdges() {
        return this.target;
    }

    public void setTargetHiddenEdges(long target) {
        this.target = target;
    }

    public VisualGraph getGraph() {
        return this.graph;
    }

    public void setCurrentGraph(VisualGraph graph) {
        this.graph = graph;
    }
}
