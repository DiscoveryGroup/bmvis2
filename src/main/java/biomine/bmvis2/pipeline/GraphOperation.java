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

package biomine.bmvis2.pipeline;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.json.simple.JSONObject;

import biomine.bmvis2.VisualGraph;

/**
 * Due to the intricate use of reflection in constructing the JSON operations
 * list and its serialization to text and de-serialization of it, all
 * GraphOperations in addition to the fromJSON and toJSON methods NEED
 * CONSTRUCTORS WITHOUT PARAMETERS.
 */
public interface GraphOperation {
    class GraphOperationException extends Exception {
        public GraphOperationException(Throwable cause) {
            super(cause);
        }

        public GraphOperationException(String messages) {
            super(messages);
        }
    }

    public String getTitle();

    public String getToolTip();

    public JComponent getSettingsComponent(SettingsChangeCallback v, VisualGraph graph);

    public void doOperation(VisualGraph g) throws GraphOperationException;

    public JSONObject toJSON();

    public void fromJSON(JSONObject o) throws Exception;
}

