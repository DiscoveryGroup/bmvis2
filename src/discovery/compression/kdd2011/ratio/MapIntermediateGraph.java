package discovery.compression.kdd2011.ratio;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;

public class MapIntermediateGraph implements IntermediateGraph{
	
	//private HashMap<Integer,Double>[] edges;
	
	private Map<Integer,Double>[] edges;
	
	
	
	private LinkedHashSet<Integer>[] neighbors;
	
	public int size(){
		return edges.length;
	}
	private void init(int size){
		edges = new HashMap[size];
		for(int i=0;i<size;i++)
			edges[i] = new HashMap<Integer,Double>();
		neighbors = new LinkedHashSet[size];
		for(int i=0;i<size;i++)
			neighbors[i] = new LinkedHashSet<Integer>();
		
	}
	public MapIntermediateGraph(int size){
		init(size);
	}
	
	public void setConnection(int from,int to,double w){
		edges[from].put(to,w);
		edges[to].put(from,w);
	}
	
	public void addNeighbors(int a,int b){
		neighbors[a].add(b);
		neighbors[b].add(a);
	}
	public void removeNeighbors(int a,int b){
		neighbors[a].remove(b);
		neighbors[b].remove(a);
	}
	
	public double getConnection(int from,int to){
		Double ret = edges[from].get(to);
		if(ret==null)return 0;
		return ret;
	}
	
	public Collection<Integer> getConnections(int n){
		return edges[n].keySet();
	}
	public Collection<Integer> getNeighbors(int n){
		return neighbors[n];
	}
	
	public MapIntermediateGraph(IntermediateGraph c){
		init(c.size());
		for(int i=0;i<c.size();i++){
			for(int j:c.getConnections(i))
				setConnection(i, j, c.getConnection(i,j));
		}
		for(int i=0;i<c.size();i++){
			for(int j:c.getNeighbors(i))
				addNeighbors(i,j);
		}
	}
	private void gatherHopNeighbors(HashSet<Integer> ns,int hops,int node){
		if(ns.contains(node))return;
		ns.add(node);
		if(hops==0)return;
		for(int x:getNeighbors(node)){
			gatherHopNeighbors(ns, hops-1, x);
		}
		
	}
	public Collection<Integer> getHopNeighbors(int node,int hops){
		HashSet<Integer> ret = new HashSet<Integer>();
		gatherHopNeighbors(ret, hops, node);
		return ret;
	}
	@Override
	public IntermediateGraph copy() {
		return new MapIntermediateGraph(this);
	}
}
