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

package biomine.bmvis2.graphcontrols;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JLabel;

import biomine.bmvis2.GraphCache;
import biomine.bmvis2.VisualEdge;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.edgesimplification.KappaSimplifier;

public class KappaEdgeHider implements EdgeHider {

	GraphCache<ArrayList<VisualEdge>> cache = new GraphCache<ArrayList<VisualEdge>>();

	@Override
	public void hideEdges(VisualGraph graph, int target) {
		ArrayList<VisualEdge> removeOrder = cache.get(graph);
		if (removeOrder == null) {
			KappaSimplifier sim = new KappaSimplifier();

			removeOrder = new ArrayList<VisualEdge>(sim.getRemovedEdges(graph,1000000000	));
			cache.put(graph, removeOrder);
		}
		
		ArrayList<VisualEdge> rem= new ArrayList<VisualEdge>();
		int remaining = graph.getAllEdges().size();
		for(VisualEdge e:removeOrder){
			if(remaining<=target)break;
			rem.add(e);
			remaining--;
		}
		
		graph.setHiddenEdges(rem);
		
	}

	@Override
	public Component getComponent(VisualGraph vg) {
		// TODO Auto-generated method stub
		return new JLabel("");
	}
	
	
}
