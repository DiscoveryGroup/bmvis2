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

package discovery.compression.kdd2011.old;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import biomine.bmvis2.GroupEdgeUtils;
import biomine.bmvis2.SimpleVisualGraph;
import biomine.bmvis2.VisualEdge;
import biomine.bmvis2.VisualGroupNode;
import biomine.bmvis2.SimpleVisualGraph.SimpleEdge;
import biomine.bmvis2.algorithms.ProbDijkstra;

public class SimilarityCompressor extends Compressor {

	double[][] distanceHi;
	double[][] distanceLo;
	double[][] origDist;
	ArrayList<ArrayList<Integer>> groups;
	ArrayList<HashSet<Integer>> neighbors;
	boolean[] alive;

	private void merge(int to, int from) {

		groups.get(to).addAll(groups.get(from));
		
		groups.get(from).clear();

		neighbors.get(to).addAll(neighbors.get(from));

		// neighbors.get(to).remove(to);
		neighbors.get(to).remove(new Integer(from));

		for (int n : neighbors.get(to)) {
			if (n != to) {
				neighbors.get(n).add(to);
				neighbors.get(n).remove(from);
			}
		}

		// neighbors.get(to).removeAll(groups.get(to));

		alive[from] = false;

		for (int i = 0; i < distanceHi[0].length; i++) {
			if(distanceHi[to][i]<0){
				distanceHi[to][i]=distanceHi[from][i];
				distanceLo[to][i]=distanceLo[from][i];
				distanceHi[i][to]=distanceHi[from][i];
				distanceLo[i][to]=distanceLo[from][i];
			}else{
				distanceHi[to][i] = Math
						.max(distanceHi[to][i], distanceHi[from][i]);
				distanceLo[to][i] = Math
						.min(distanceLo[to][i], distanceLo[from][i]);
				
				distanceHi[i][to] = distanceHi[to][i];
				distanceLo[i][to] = distanceLo[to][i];
			}
		}
	}

	private boolean onlyStart = false;
	private int thresholdCount = 4;

	public int getThresholdCount() {
		return thresholdCount;
	}

	public void setThresholdCount(int thresholdCount) {
		this.thresholdCount = thresholdCount;
	}

	public boolean isOnlyStart() {
		return onlyStart;
	}

	public void setOnlyStart(boolean onlyStart) {
		this.onlyStart = onlyStart;
	}

	public ArrayList<ArrayList<Integer>> makeGroups(SimpleVisualGraph sg) {

		this.sg = sg;
		distanceHi = new double[sg.n][sg.n];
		distanceLo = new double[sg.n][sg.n];
		origDist = new double[sg.n][sg.n];
		alive = new boolean[sg.n];


		for (int i = 0; i < sg.n; i++) {
			alive[i] = true;
			distanceHi[i][i] =-1;
			distanceLo[i][i] = -1;
			origDist[i][i]   = 1;

			ProbDijkstra pd = new ProbDijkstra(sg, i);
			
			for (int j = i + 1; j < sg.n; j++) {
				double pr = pd.getProbTo(j);
				origDist[i][j] = pr;
				origDist[j][i] = pr;
				distanceHi[i][j] = pr;
				distanceHi[j][i] = pr;
				distanceLo[i][j] = pr;
				distanceLo[j][i] = pr;
			}
		}

		groups = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < sg.n; i++) {
			ArrayList<Integer> ai = new ArrayList<Integer>();
			ai.add(i);
			groups.add(ai);
		}
		if (onlyStart)
			return groups;
		neighbors = new ArrayList<HashSet<Integer>>();
		for (int i = 0; i < sg.n; i++) {
			HashSet<Integer> ns = new HashSet<Integer>();
			for (SimpleEdge se : sg.getEdges(i)) {
				ns.add(se.to);
			}
			neighbors.add(ns);
		}

		for (int t = thresholdCount - 1; t >= 0; t--) {
			for (int i = 0; i < sg.n; i++) {
				if (!alive[i])
					continue;
				for (int j = i + 1; j < sg.n; j++) {
					if (alive[j] && canMerge(i, j,t)) {
						merge(i, j);
					}
				}
			}
		}
		return groups;
	}

	public double[][] getOriginalMatrix() {
		return origDist;
	}

	public double[][] getBestPathMatrix() {

		int n = distanceHi.length;
		double[][] ret = new double[distanceHi.length][distanceHi[0].length];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				double total = 0;
				int count = 0;
				for (int node : groups.get(i)) {
					for (int node2 : groups.get(j)) {
						if(node2==node)continue;
						total += origDist[node][node2];
						count++;
					}
				}

				double avg = 0;
				if (count != 0)
					avg = total / count;
				else
					avg=1;
				ret[i][j] = avg;
				ret[j][i] = avg;
			}
		}
		return ret;
	}

	@Override
	public String makeGroups(VisualGroupNode n) {

		SimpleVisualGraph sg = new SimpleVisualGraph(n);
		makeGroups(sg);
		final double[][] dist = getBestPathMatrix();
		
		final ArrayList<HashSet<Integer>> ns = new ArrayList<HashSet<Integer>>(neighbors.size());
		for(int i=0;i<neighbors.size();i++)
			ns.add(new HashSet<Integer>(neighbors.get(i)));
		
		final VisualGroupNode[] newGroups = new VisualGroupNode[sg.n];

		try {
			n.getGraph().disableObservers();

			for (int i = 0; i < sg.n; i++) {
				final int nodeI = i;
				if (alive[i]) {
					final VisualGroupNode gr = new VisualGroupNode(n) {
						@Override
						public void createEdges() {
							for (int x : ns.get(nodeI)) {
								// System.out.println("making edge "+nodeI+"->"+x);
								GroupEdgeUtils.makeGroupEdge(this,
										newGroups[nodeI], dist[nodeI][x]);
							}
						}
					};
					for (int m : groups.get(i))
						sg.getVisualNode(m).setParent(gr);
					newGroups[i] = gr;
				}
			}
		} finally {
			n.getGraph().enableObservers();
		}
		/*
		for(int i=0;i<distanceHi.length;i++){
			for(int j=0;j<distanceHi[i].length;j++){
				System.out.print(""+distanceLo[i][j]+"-"+distanceHi[i][j]+"\t");
			}
			System.out.println();
		}*/
		return "";
	}

	private boolean canMerge(int a, int b, int thld) {
		ArrayList<Integer> ns = new ArrayList<Integer>();
		ns.addAll(neighbors.get(a));
		ns.addAll(neighbors.get(b));
		double max = 2 * super.getDeviation() / (1 << thld);
		boolean canSucceed = false;
		for (int i : ns) {
			if(distanceHi[a][i]<0 || distanceLo[b][i]<0)
				continue;

			double hi = Math.max(distanceHi[a][i], distanceHi[b][i]);
			double lo = Math.min(distanceLo[a][i], distanceLo[b][i]);
			if (hi - lo > max)
				return false;

			canSucceed = true;
		}

		return canSucceed;
	}

	public CompressorCosts getResultCosts() {
		CompressorCosts ret = new CompressorCosts();
		ret.supernodes = 0;
		for (int i = 0; i < alive.length; i++)
			if (alive[i])
				ret.supernodes++;
		ret.superedges = 0;

		for (int i = 0; i < alive.length; i++)
			if (alive[i]) {
				ret.superedges += neighbors.get(i).size();

				// add 1 extra for self-edges as they are counted only once
				if (neighbors.get(i).contains(i))
					ret.superedges++;
			}

		ret.superedges /= 2;

		return ret;
	}

	SimpleVisualGraph sg;

	@Override
	public SimpleVisualGraph getOriginalGraph() {
		return sg;
	}

	@Override
	public CompressorCosts getStartingCosts() {
		CompressorCosts ret = new CompressorCosts();
		int totalDegree = 0;
		for (int i = 0; i < sg.n; i++) {
			HashSet<Integer> visited = new HashSet<Integer>();
			for (SimpleEdge se : sg.getEdges(i)) {
				if (visited.contains(se.to))
					continue;
				visited.add(se.to);
				if (se.to == i)
					totalDegree += 2;
				else
					totalDegree += 1;
			}
		}

		ret.supernodes = sg.n;
		ret.superedges = totalDegree / 2;
		return ret;
	}

	@Override
	public SimpleVisualGraph getUncompressedGraph() {

		SimpleVisualGraph ret = new SimpleVisualGraph(sg.getVisualNodes());
		double[][] dist = getBestPathMatrix();
		int[] nodeGroup = new int[sg.n];
		for (int i = 0; i < sg.n; i++) {
			for (int x : groups.get(i)) {
				nodeGroup[x] = i;
			}
		}

		for (int i = 0; i < ret.n; i++) {

			// remove all edges
			ArrayList<Integer> remove = new ArrayList<Integer>();
			for (SimpleEdge se : ret.getEdges(i)) {
				remove.add(se.to);
			}

			Collections.reverse(remove);
			for (int to : remove) {
				ret.removeEdge(i, to);
			}
			HashSet<Integer> ns = neighbors.get(nodeGroup[i]);
			int g = nodeGroup[i];
			if(distanceHi[g][g]>=0)
				ns.add(g);
			
			for (int toG : neighbors.get(nodeGroup[i])) {
				for (int to : groups.get(toG)) {
					if (i == to)
						continue;

					VisualEdge fe = new VisualEdge(sg.getVisualNode(i), sg
							.getVisualNode(to), 
							dist[nodeGroup[i]][toG],
							false,
							"uncompressed");

					ret.addEdge(i, to, fe);
				}
			}
		}
		return ret;
	}
}
