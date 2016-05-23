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

package biomine.bmvis2.pipeline.operations;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import biomine.bmvis2.pipeline.GraphOperation;
import biomine.bmvis2.pipeline.SettingsChangeCallback;
import org.json.simple.JSONObject;

import biomine.bmvis2.VisualEdge;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualNode;

public class EdgeGoodnessHider implements GraphOperation {

	private double limit=0;

	public void doOperation(VisualGraph g) throws GraphOperationException {
		HashSet<VisualEdge> hiddenEdges = new HashSet<VisualEdge>();
		hiddenEdges.addAll(g.getHiddenEdges());
		for(VisualEdge e:g.getAllEdges()){
			if(e.getGoodness()<limit){
				hiddenEdges.add(e);
			}
		}
		ArrayList<VisualNode> hiddenNodes = new ArrayList();
		for(VisualNode n:g.getNodes()){
			int edgeCount = 0;
			for(VisualEdge e:n.getEdges()){
				if(!hiddenEdges.contains(e) )
					edgeCount++;
			}
			if(edgeCount==0){
				hiddenNodes.add(n);
			}
		}
		g.hideNodes(hiddenNodes);
		g.hideEdges(hiddenEdges);
		System.out.println("edgeGoodness update!");
	}

	public JComponent getSettingsComponent(final SettingsChangeCallback v,
			VisualGraph graph) {
		double maxEdge=0;
		for(VisualEdge e:graph.getAllEdges()){
			maxEdge = Math.max(maxEdge,e.getGoodness());
		}


		final int scale = 100;
		final JSlider limitSlider =  new JSlider();
		limitSlider.setMinimum(0);
		limitSlider.setMaximum(scale);
		limitSlider.setValue((int)(limit*scale));
		final JTextField limitText=new JTextField();
		limitText.setEditable(false);
		limitText.setText(""+limit);

		limitSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				limit = limitSlider.getValue()/(double)scale;
				limitText.setText(""+limit);
				v.settingsChanged(false);
			}
		});


		JPanel ret = new JPanel();
		GridBagLayout bag = new GridBagLayout();
		ret.setLayout(bag);
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=1;
		c.fill=c.HORIZONTAL;
		ret.add(limitSlider,c);
		c.gridy++;
		ret.add(limitText,c);
		return ret;
	}

	public String getTitle() {
		return "Edge filter";
	}

	public String getToolTip() {
		return "";
	}

	public JSONObject toJSON() {
		JSONObject ret = new JSONObject();
		ret.put("limit",limit);
		return ret;
	}

	public void fromJSON(JSONObject o) throws Exception {
		limit = (Double)o.get("limit");
	}


}
