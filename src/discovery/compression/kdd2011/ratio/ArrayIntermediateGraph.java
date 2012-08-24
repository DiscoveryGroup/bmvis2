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
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class ArrayIntermediateGraph implements IntermediateGraph{
	
	//private HashMap<Integer,Double>[] edges;
	private double[][] edges;
	
	
	private ArrayList<Integer> oneTwoN;
	private LinkedHashSet<Integer>[] neighbors;
	
	public int size(){
		return edges.length;
	}
	private void init(int size){
		edges = new double[size][size];
		neighbors = new LinkedHashSet[size];
		for(int i=0;i<size;i++)
			neighbors[i] = new LinkedHashSet<Integer>();
		oneTwoN = new ArrayList<Integer>();
		for(int i=0;i<size;i++)
			oneTwoN.add(i);
	}
	public ArrayIntermediateGraph(int size){
		init(size);
	}
	
	public void setConnection(int from,int to,double w){
		edges[from][to]=w;
		edges[to][from]=w;
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
		return edges[from][to];
	}
	
	public Collection<Integer> getConnections(int n){
		return oneTwoN;
	}
	public Collection<Integer> getNeighbors(int n){
		return neighbors[n];
	}
	
	public ArrayIntermediateGraph(IntermediateGraph c){
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
		return new ArrayIntermediateGraph(this);
	}
}
