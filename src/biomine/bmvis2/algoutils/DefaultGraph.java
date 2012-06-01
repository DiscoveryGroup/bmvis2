package biomine.bmvis2.algoutils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import biomine.bmgraph.BMEdge;
import biomine.bmgraph.BMGraph;
import biomine.bmvis2.SimpleVisualGraph;
import biomine.bmvis2.SimpleVisualGraph.SimpleTwoDirEdge;

/**
 * Simple graph structure for generic graphs. Used mainly in new ratio compression
 * algorithms.
 * 
 * @author alhartik
 *
 */

public class DefaultGraph {
	
	
//	public static class DefaultEdge{
//		public final int to;
//		public final double weight;
//		public DefaultEdge(int t,double w){
//			to=t;
//			weight=w;
//		}
//	}
	
	private ArrayList<HashMap<Integer,Double>> edges;
	
	public void addEdge(int from,int to,double weight){
		int m = Math.max(from,to);
		while(m>=edges.size())
			edges.add(new HashMap<Integer, Double>());
		
		edges.get(from).put(to,weight);
		edges.get(to).put(from,weight);
	}
	
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
	
	public DefaultGraph(){
		edges = new ArrayList<HashMap<Integer,Double>>();
	}
	public DefaultGraph(SimpleVisualGraph sg){
		edges = new ArrayList<HashMap<Integer,Double>>();
		for(SimpleTwoDirEdge se:sg.getAllEdges()){
			addEdge(se.from,se.to,se.weight);
		}
	}
	
	public int getNodeCount() {
		return edges.size();
	}
	public int getEdgeCount() {
		int ret=0;
		for(int i=0;i<getNodeCount();i++)
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
		while(n>=edges.size())
			edges.add(new HashMap<Integer,Double>());
	}

	public DefaultGraph copy() {
		DefaultGraph ret = new DefaultGraph();
		for(int i=0;i<getNodeCount();i++)
			for(int j:getNeighbors(i))
				ret.addEdge(i,j,getEdgeWeight(i,j));
		return ret;
	}
	public boolean hasEdge(int x,int to){
		return edges.get(x).containsKey(to);
	}
}
