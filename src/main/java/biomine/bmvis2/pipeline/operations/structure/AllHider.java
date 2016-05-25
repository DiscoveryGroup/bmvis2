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

import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.pipeline.GraphOperation;
import biomine.bmvis2.pipeline.SettingsChangeCallback;
import org.json.simple.JSONObject;

import javax.swing.*;

public class AllHider implements GraphOperation {
    public String getTitle() {
        return "Hide everything";
    }

    public String getToolTip() {
        return null;
    }

    public JComponent getSettingsComponent(SettingsChangeCallback v, VisualGraph graph) {
        return null;
    }

    public void doOperation(VisualGraph g) throws GraphOperationException {
        g.hideNodes(g.getAllNodes());
    }

    public JSONObject toJSON() {
        return null;
    }

    public void fromJSON(JSONObject o) throws Exception {
    }
}
