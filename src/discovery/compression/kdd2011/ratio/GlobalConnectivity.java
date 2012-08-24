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

import biomine.bmvis2.SimpleVisualGraph;
import biomine.bmvis2.SimpleVisualGraph.SimpleEdge;
import biomine.bmvis2.algorithms.ProbDijkstra;

public class GlobalConnectivity {
	public static IntermediateGraph createGlobalGraph(SimpleVisualGraph sg){
		
		double[][] conns = ProbDijkstra.getProbMatrix(sg);
		IntermediateGraph ret =new ArrayIntermediateGraph(sg.n);
		for(int i=0;i<sg.n;i++)
			for(int j=i+1;j<sg.n;j++)
				ret.setConnection(i, j, conns[i][j]);
		for(int i=0;i<sg.n;i++)
			for(SimpleEdge x:sg.getEdges(i))
				ret.addNeighbors(i,x.to);
		return ret;
	}
}
