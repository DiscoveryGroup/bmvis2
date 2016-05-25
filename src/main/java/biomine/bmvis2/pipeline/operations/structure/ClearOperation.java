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

import biomine.bmvis2.Logging;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.color.DefaultNodeColoring;
import biomine.bmvis2.pipeline.GraphOperation;
import biomine.bmvis2.pipeline.SettingsChangeCallback;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.util.Collections;

public class ClearOperation implements GraphOperation {
    public void doOperation(VisualGraph g) throws GraphOperationException {
        DefaultNodeColoring col = new DefaultNodeColoring();
        Logging.debug("graph_operation", "ClearOperation.doOperation()");
        for (VisualNode n : g.getAllNodes()) {
            n.setExtraLabels(Collections.EMPTY_LIST);
            n.setBaseColor(col.getFillColor(n));
        }
        g.setHiddenEdges(Collections.EMPTY_LIST);
        g.setHiddenNodes(Collections.EMPTY_LIST);
    }

    public JComponent getSettingsComponent(SettingsChangeCallback v,
                                           VisualGraph graph) {
        return new JLabel("This should not be visible");
    }

    public String getTitle() {
        return "clear op: should be hidden";
    }

    public String getToolTip() {

        return null;
    }

    public void fromJSON(JSONObject o) throws Exception {
    }

    public JSONObject toJSON() {
        return new JSONObject();
    }

}
