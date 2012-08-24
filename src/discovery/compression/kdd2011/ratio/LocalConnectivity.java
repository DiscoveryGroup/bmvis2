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

package discovery.compression.kdd2011.ratio;

import java.util.ArrayList;
import java.util.HashMap;

import biomine.bmvis2.SimpleVisualGraph;
import biomine.bmvis2.SimpleVisualGraph.SimpleEdge;

public class LocalConnectivity {

	static public IntermediateGraph createLocalGraph(SimpleVisualGraph sg,
			int pathlen) {
		if(pathlen==-1)return GlobalConnectivity.createGlobalGraph(sg);
		
		IntermediateGraph ret = new MapIntermediateGraph(sg.n);
		for (int i = 0; i < sg.n; i++)
			for (SimpleEdge x : sg.getEdges(i)) {
				ret.addNeighbors(i, x.to);
				ret.setConnection(i, x.to, x.weight);
			}

		for (int i = 0; i < sg.n; i++) {
			HashMap<Integer, Double> dist = new HashMap<Integer, Double>();
			ArrayList<Integer> q = new ArrayList<Integer>();
			q.add(i);
			dist.put(i,1.0);
			for (int z = 0; z < pathlen; z++) {
				ArrayList<Integer> nq = new ArrayList<Integer>();
				for (int node : q) {
					double d = dist.get(node);
					for (SimpleEdge se : sg.getEdges(node)) {
						Double dd = dist.get(se.to);
						if (dd==null || dd<d*se.weight) {
							nq.add(se.to);
							dist.put(se.to, d * se.weight);
							ret.setConnection(i, se.to, d * se.weight);
						}
					}
				}
				q = nq;
			}
		}
		return ret;
	}

}
