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

import java.util.Collection;

import biomine.bmvis2.algorithms.ProbDijkstra;
import biomine.bmvis2.algoutils.DefaultGraph;

public class PathAverageMergeModel extends AverageMergeModel{

	DefaultGraph dg;
	
	@Override
	public void addNeighbors(int a, int b) {
		dg.addEdge(a,b,0);
	}

	@Override
	public AverageMergeModel copy() {
		PathAverageMergeModel ret = new PathAverageMergeModel();
		ret.dg = dg.copy();
		ret.startCost=startCost;
		ret.pds = null;
		super.initCopy(ret);
		return ret;
	}
	
	ProbDijkstra[] pds;
	
	public void clear(){
		pds = null;
	}

	@Override
	public double getConnection(int x, int y) {
		if(x==y){
			//System.out.println("get "+x+" "+y);
			//System.out.println("=>"+dg.getEdgeWeight(x,y)+" "+dg.hasEdge(x,y));
			if(dg.hasEdge(x,y)==false){
				return -1;
			}
			return dg.getEdgeWeight(x,y);
		}
		
		if(pds==null){
			pds = new ProbDijkstra[graphSize()];
			//System.out.println("cleared");
		}
		if(pds[x]==null)pds[x] = new ProbDijkstra(dg, x);
		return pds[x].getProbTo(y);
	}

	@Override
	public Collection<Integer> getConnections(int x) {
		return dg.getNeighbors(x);
	}
	
	@Override
		
	public void merge(int to,int from){
		super.merge(to, from);
		
		for(int i:dg.getNeighbors(from))
			dg.removeEdge(from, i);
		
		clear();
	}
	
	@Override
	public Collection<Integer> getNeighbors(int x) {
		return dg.getNeighbors(x);
	}

	@Override
	protected int graphSize() {
		return dg.getNodeCount();
	}

	@Override
	public void removeNeighbors(int a, int b) {
		dg.removeEdge(a,b);
	}

	@Override
	public void setConnection(int x, int y, double d) {
		if(d<1e-7){
			//System.out.println("x,y="+x+","+y);
			//System.out.println("oldConn = "+getConnection(x,y));
			System.out.println(super.getGroups().get(x).size());
		}
		//if(Math.abs(d-dg.getEdgeWeight(x,y))>1e-6)
			//clear();
		
		dg.addEdge(x,y,d);
	}

	@Override
	public int getStartCost() {
		return startCost;
	}

	private int startCost=0;
	@Override
	public void init(DefaultGraph dg) {
		clear();
		super.init(dg);
		startCost = dg.getEdgeCount();
		this.dg = dg.copy();
	}

}
