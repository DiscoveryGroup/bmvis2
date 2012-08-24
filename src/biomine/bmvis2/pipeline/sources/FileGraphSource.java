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

package biomine.bmvis2.pipeline.sources;

import java.io.FileNotFoundException;

import org.json.simple.JSONObject;

import biomine.bmgraph.BMGraph;
import biomine.bmgraph.BMGraphUtils;
import biomine.bmgraph.BMNode;
import biomine.bmvis2.VisualNode;

public class FileGraphSource extends GraphSource {
    private String name;
    private BMGraph graph;

    public FileGraphSource() {
        this.name = "_NO_FILE_";
    }

    public FileGraphSource(String n) {
        this.name = n;
    }

    public FileGraphSource(BMGraph bm) {
        this.graph = bm;
    }

    @Override
    public BMGraph getBMGraph() throws GraphOperationException {
        if (graph == null) {
            try {
                graph = BMGraphUtils.readBMGraph(name);
            } catch (FileNotFoundException e) {
                throw new GraphOperationException(e);
            }
        }
        return graph;
    }

    public String getTitle() {
        if (this.name == null || this.name.equals("_NO_FILE"))
            try {
                return this.getQuerySetTitle();
            } catch (GraphOperationException e) {
                e.printStackTrace();
                return "File with no name.";
            }
        else
            return "File: " + name;
    }

    public String getToolTip() {
        return null;
    }

    public void fromJSON(JSONObject o) throws Exception {
        name = o.get("file").toString();
    }

    public JSONObject toJSON() {
        JSONObject ret = new JSONObject();
        ret.put("file", name);
        return ret;
    }

    private String getQuerySetTitle() throws GraphOperationException {
        String ret = "";
        for (BMNode n : this.getBMGraph().getNodes())
            if (n.get("queryset") != null)
                if (ret.length() > 0)
                    ret = ret + " - " + VisualNode.resolveVisibleName(n);
                else
                    ret = VisualNode.resolveVisibleName(n);

        return ret;
    }

    public String toString() {
        return this.getClass().getName() + ": " + this.name;
    }
}