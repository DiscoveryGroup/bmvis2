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

public class PathAverageMergeModelNorefresh extends AverageMergeModel{

	private int startCost=0;
	IntermediateGraph ig;
	@Override
	public void addNeighbors(int a, int b) {
		ig.addNeighbors(a, b);
	}
	

	@Override
	public AverageMergeModel copy() {
		PathAverageMergeModelNorefresh ret = new PathAverageMergeModelNorefresh();
		ret.ig=ig.copy();
		ret.startCost=startCost;
		super.initCopy(ret);
		return ret;
	}

	@Override
	public double getConnection(int x, int y) {
		return ig.getConnection(x,y);
	}

	@Override
	public Collection<Integer> getConnections(int x) {
		return ig.getConnections(x);
	}

	@Override
	public Collection<Integer> getHopNeighbors(int x, int n) {
		return ig.getHopNeighbors(x, n);
	}

	@Override
	public Collection<Integer> getNeighbors(int x) {
		return ig.getNeighbors(x);
	}

	@Override
	protected int graphSize() {
		return ig.size();
	}

	@Override
	public void removeNeighbors(int a, int b) {
		ig.removeNeighbors(a, b);
	}

	@Override
	public void setConnection(int x, int y, double d) {
		ig.setConnection(x,y,d);
	}

	@Override
	public int getStartCost() {
		return startCost;
	}

	@Override
	public void init(DefaultGraph dg) {
		super.init(dg);
		startCost = dg.getEdgeCount();
		
		ig = new MapIntermediateGraph(dg.getNodeCount());
		for(int i=0;i<dg.getNodeCount();i++){
			for(int j:dg.getNeighbors(i)){
				ig.addNeighbors(i,j);
			}
		}
		for(int i=0;i<dg.getNodeCount();i++){
			ProbDijkstra pd = new ProbDijkstra(dg, i);
			for(int j=0;j<i;j++){
				ig.setConnection(i,j,pd.getProbTo(j));
			}
			ig.setConnection(i,i,-1);
		}
	}

}
