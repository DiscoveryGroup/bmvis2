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

import java.util.HashSet;

import javax.swing.JComponent;

import org.json.simple.JSONObject;

import biomine.bmvis2.VisualGraph;

public class ManualHider implements GraphOperation{

	HashSet<String> hidden  = new HashSet<String>();
	@Override
	public void doOperation(VisualGraph g) throws GraphOperationException {
		
	}

	@Override
	public void fromJSON(JSONObject o) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JComponent getSettingsComponent(SettingsChangeCallback v,
			VisualGraph graph) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getToolTip() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject toJSON() {
		// TODO Auto-generated method stub
		return null;
	}

}
