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

package biomine.bmvis2.group;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import biomine.bmvis2.SimpleVisualGraph;
import biomine.bmvis2.VisualEdge;
import biomine.bmvis2.VisualGroupNode;
import biomine.bmvis2.SimpleVisualGraph.SimpleEdge;

class PQNode implements Comparable<PQNode> {
	public PQNode(int n, double d) {
		dist = d;
		node = n;
	}

	int node;
	double dist;
	SimpleEdge lastEdge;
	int last;
	public int compareTo(PQNode o) {

		return Double.compare(dist, o.dist);
	}
}



public class EdgeBetweenness {

	public static <T> void inc(HashMap<T, Double> m, T t,double d) {
		Double in = m.get(t);
		if (in == null)
			in = 0.0;
		m.put(t, in + d);
	}

	private static double edgeWeight(SimpleEdge e) {
		return 1.0;
		//return -Math.log(e.weight);
	}
	HashSet<VisualEdge> bannedEdges =new HashSet<VisualEdge>();
	public EdgeBetweenness(VisualGroupNode g) {
		graph = new SimpleVisualGraph(g);
		// TODO Auto-generated constructor stub
	}
	public EdgeBetweenness(VisualGroupNode g,Collection<VisualEdge> ban) {
		graph = new SimpleVisualGraph(g);
		bannedEdges.addAll(ban);
		for(int i=0;i<graph.n;i++){
			int newSize = graph.edges[i].length;
			for(int j=0;j<newSize;j++){
				SimpleEdge se = graph.edges[i][j];
				if(bannedEdges.contains(se.visualEdge)){
					newSize--;
					graph.edges[i][j] = graph.edges[i][newSize];
					j--;
				}
			}
			graph.edges[i] = Arrays.copyOf(graph.edges[i], newSize);
			
		}
		// TODO Auto-generated constructor stub
	}
	private SimpleVisualGraph graph;
	HashMap<VisualEdge, Double> ret = null;//
	
	ArrayList<ArrayList<SimpleEdge>> bestEdges = new ArrayList<ArrayList<SimpleEdge>>();  
	
	int[][] pathCount;
	private int[] calcPathCount(int n){
		if(pathCount[n]!=null)return pathCount[n];
		int[] ret = new int[graph.n];
		pathCount[n] = ret;
		ret[n]=1;
		
		for(SimpleEdge e:bestEdges.get(n)){
			int[] er = calcPathCount(e.to);
			for(int i=0;i<graph.n;i++)
				ret[i]+=er[i];
		}
		return ret;
	}


    private void incShortestsPaths(int start) {
		pathCount = new int[graph.n][];
		bestEdges = new ArrayList<ArrayList<SimpleEdge>>();
		for(int i=0;i<graph.n;i++)
			bestEdges.add(new ArrayList<SimpleEdge>());
		
		//dijkstra with twist of backedges
		PriorityQueue<PQNode> pq = new PriorityQueue<PQNode>();
		PQNode sn = new PQNode(start, 0);
		sn.last = -1;
		pq.add(sn);
		
		double shortestDist[] = new double[graph.n];
		for(int i=0;i<graph.n;i++)
			shortestDist[i] = 100000;
		ArrayList<Integer> order = new ArrayList<Integer>();
		int[] pathsToCount = new int[graph.n];
		
		pathsToCount[start]=1;
		
		while(pq.size()!=0){
			PQNode pn = pq.poll();
			int n = pn.node;
			double dist = pn.dist;
			if(shortestDist[n]<dist){
				continue;
			}
			
			if(pn.last>=0){
				bestEdges.get(pn.last).add(pn.lastEdge);
				pathsToCount[n]+=pathsToCount[pn.last];
			}
			if(shortestDist[n]==dist){
				continue;
			}
			order.add(n);
			shortestDist[n] = dist;
			
			for(int i=0;i<graph.edges[n].length;i++){
				SimpleEdge ed = graph.edges[n][i];
				PQNode nn = new PQNode(ed.to,dist+edgeWeight(ed));
				nn.last = n;
				nn.lastEdge = ed;
				if(shortestDist[nn.node]<nn.dist)continue;
				pq.add(nn);
			}
		}

        int[] totalCounts = calcPathCount(start);
		double[][] scaledCount = new double[graph.n][graph.n];
		double[] total = new double[graph.n];
		for(int i=0;i<graph.n;i++){
			for(int j=0;j<graph.n;j++)
			{
				scaledCount[i][j] = pathCount[i][j]/(double) totalCounts[j];
				total[i]+=scaledCount[i][j];
			}
		}
		
		for(int i=0;i<graph.n;i++){
			for(SimpleEdge e:bestEdges.get(i)){
				int t = e.to;
				
				double w = total[t]*pathsToCount[i];
				inc(ret,e.visualEdge,w);
			}
		}
	}
	

	public HashMap<VisualEdge, Double> getEdgeBetweenness() {

		if (ret != null)
			return ret;
		ret = new HashMap<VisualEdge, Double>();
		for(int i=0;i<graph.n;i++)
			incShortestsPaths(i);
		return ret;
	}
}