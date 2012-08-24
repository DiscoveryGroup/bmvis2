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

package biomine.nodeimportancecompression;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Simple graph data structure with information on node importance.
 * 
 * @author aleksi
 *
 */
public class ImportanceGraph {

	public static class Edge
	{
		public int from;
		public int to;
		public double weight;
		public Edge(int f,int t,double d){
			from = f;
			to = t;
			weight = d;
		}
	};
	
//	public static class DefaultEdge{
//		public final int to;
//		public final double weight;
//		public DefaultEdge(int t,double w){
//			to=t;
//			weight=w;
//		}
//	}
	private ArrayList<Double> importance = new ArrayList<Double>();
	private ArrayList<Boolean> hasNode = new ArrayList<Boolean>();
	
	private int nodeCount=0;
	
	public double getImportance(int node) {
		if(importance.size()<=node)return -1;
		return importance.get(node);
	}

	public void setImportance(int node,double imp) {
		ensureHasNode(node);
		importance.set(node, imp);
	}

	
	private ArrayList<HashMap<Integer,Double>> edges;
	
	public void addEdge(int from,int to,double weight){
		ensureHasNode(from);
		ensureHasNode(to);
		edges.get(from).put(to,weight);
		edges.get(to).put(from,weight);
	}
	
	/**
	 * Returns edge weight of edge from -- to
	 * If there is no edge, returns 0
	 * @param from
	 * @param to
	 * @return
	 */
	public double getEdgeWeight(int from,int to){
		if(from>=edges.size())return 0;
		Double d=edges.get(from).get(to);
		if(d==null)return 0;
		return d;
	}
	
	public void removeEdge(int from,int to){
		edges.get(from).remove(to);
		edges.get(to).remove(from);
	}
	public Collection<Integer> getNeighbors(int from){
		if(edges.size()<=from)return Collections.EMPTY_LIST;
		return edges.get(from).keySet();
	}
	
	public ImportanceGraph(){
		edges = new ArrayList<HashMap<Integer,Double>>();
	}
	
	public int getNodeCount() {
		return nodeCount;
	}
	public int getMaxNodeId(){
		return edges.size()-1;
	}
	public int getEdgeCount() {
		int ret=0;
		for(int i=0;i<=getMaxNodeId();i++)
			for(int j:getNeighbors(i)){
				if(j>=i)
					ret++;
			}
		return ret;
	}
	/**
	 * Makes sure that graph contains node n. 
	 * @param n
	 */
	public void ensureHasNode(int n){
		importance.ensureCapacity(n+1);
		edges.ensureCapacity(n+1);
		hasNode.ensureCapacity(n+1);
		while(n>=importance.size()){
			importance.add(-1.0);
		}
		while(n>=edges.size())
			edges.add(new HashMap<Integer,Double>());
		while(n>=hasNode.size())
			hasNode.add(false);
		if(hasNode.get(n)==false)nodeCount++;
		hasNode.set(n, true);
	}
	
	public void removeNode(int n){
		if(n>=hasNode.size())return;
		
		for(int e:new ArrayList<Integer>(getNeighbors(n)))
			removeEdge(n, e);
		if(n==hasNode.size()-1){
			hasNode.remove(n);
			edges.remove(n);
		}
		nodeCount--;
	}
	

	public ImportanceGraph copy() {
		ImportanceGraph ret = new ImportanceGraph();
		ret.importance = new ArrayList<Double>(importance);
		for(int i=0;i<getNodeCount();i++)
			for(int j:getNeighbors(i))
				ret.addEdge(i,j,getEdgeWeight(i,j));
		return ret;
	}
	public boolean hasNode(int x){
        if(x>=hasNode.size())return false;
		return hasNode.get(x);
	}
	public boolean hasEdge(int x,int to){
		return edges.get(x).containsKey(to);
	}
	/**
	 * Returns total size of graph, which is number of nodes and edges
	 * @return
	 */
	public int getSize(){
		return getNodeCount()+getEdgeCount();
	}
	public List<Edge> getEdges(){
		ArrayList<Edge> ret = new ArrayList<Edge>();
		for(int i=0;i<=getMaxNodeId();i++){
			if(hasNode(i)){
				for(int j:this.getNeighbors(i)){
					Edge e = new Edge(i,j,getEdgeWeight(i, j));
					ret.add(e);
				}
			}
		}
		return ret;
	}
	private void gatherHop(int x,int hop,Set<Integer> n){
		if(n.contains(x))return;
		n.add(x);
		if(hop==0)return;
		for(int y:getNeighbors(x))
			gatherHop(y, hop-1, n);
	}
	
	public Collection<Integer> getHop2Neighbors(int x){
		HashSet<Integer> ret = new HashSet<Integer>();
		gatherHop(x, 2, ret);
		return new ArrayList<Integer>(ret);
	}

	public Collection<Integer> getNodes() {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		for(int i=0;i<=getMaxNodeId();i++)
			if(hasNode(i))
				ret.add(i);
		return ret;
	}
}
