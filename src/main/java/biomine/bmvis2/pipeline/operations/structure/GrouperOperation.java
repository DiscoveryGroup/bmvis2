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
import biomine.bmvis2.group.Grouper;
import biomine.bmvis2.group.GrouperList;
import biomine.bmvis2.pipeline.GraphOperation;
import biomine.bmvis2.pipeline.SettingsChangeCallback;
import org.json.simple.JSONObject;

import javax.swing.*;

public class GrouperOperation extends StructuralOperation implements GraphOperation {
    private Grouper grouper;
    private String toolTipText;

    public GrouperOperation() {

    }

    public GrouperOperation(Grouper g) {
        this.grouper = g;
    }

    public void doOperation(VisualGraph g) {
        this.toolTipText = grouper.makeGroups(g.getRootNode());
    }

    public String getTitle() {
        return GrouperList.grouperName(grouper);
    }

    public String getToolTip() {
        return this.toolTipText;
    }

    public JComponent getSettingsComponent(final SettingsChangeCallback v, final VisualGraph graph) {
        return grouper.getSettingsComponent(v, graph.getRootNode());
    }

    public void fromJSON(JSONObject o) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class grouperClass;
        String grouperClassName = o.get("grouper").toString();
        grouperClass = getClass().getClassLoader().loadClass(
                grouperClassName);

        this.grouper = (Grouper) grouperClass.newInstance();
    }

    public JSONObject toJSON() {
        JSONObject ret = new JSONObject();
        ret.put("grouper", this.grouper.getClass().getName());
        return ret;
    }

    public Grouper getGrouper() {
        return grouper;
    }

    public String toString() {
        return "GrouperOperation: " + this.getTitle();
    }
}
