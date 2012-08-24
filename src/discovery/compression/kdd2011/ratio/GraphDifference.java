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

import biomine.bmvis2.algorithms.ProbDijkstra;
import biomine.bmvis2.algoutils.DefaultGraph;

public class GraphDifference { 
	
	public static double difference(DefaultGraph a,DefaultGraph b){
		assert(a.getNodeCount()==b.getNodeCount());
		double d = 0;
		for(int i=0;i<a.getNodeCount();i++){
			ProbDijkstra pdA = new ProbDijkstra(a, i);
			ProbDijkstra pdB = new ProbDijkstra(b, i);
			for(int j=i+1;j<a.getNodeCount();j++){
				double aij = pdA.getProbTo(j);
				double bij = pdB.getProbTo(j);
				d+=(aij-bij)*(aij-bij);
			}
		}
		return Math.sqrt(d);
	}

	public static double edgeDifference(DefaultGraph a,
			DefaultGraph b) {
		assert(a.getNodeCount()==b.getNodeCount());
		
		double d = 0;
		for(int i=0;i<a.getNodeCount();i++){
			for(int j=i+1;j<a.getNodeCount();j++){
				double aij = a.getEdgeWeight(i, j);
				double bij = b.getEdgeWeight(i, j);
				d+=(aij-bij)*(aij-bij);
			}
		}
		return Math.sqrt(d);
	}
}
