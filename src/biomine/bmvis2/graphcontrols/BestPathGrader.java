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

package biomine.bmvis2.graphcontrols;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;

import biomine.bmvis2.GraphCache;
import biomine.bmvis2.SimpleVisualGraph;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualGroupNode;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.SimpleVisualGraph.SimpleEdge;
import biomine.bmvis2.VisualGraph.Change;
import biomine.bmvis2.algorithms.ProbDijkstra;
import biomine.bmvis2.utils.AlgoUtils;

/**
 * Grader implementation that calculates distance from interesting nodes.
 * 
 * Caches results and only recalculates them when graph has been changed. This
 * is checked from version.
 * 
 * @author alhartik
 * 
 */
public class BestPathGrader implements NodeGrader {
	
	public static final int ALL =0;
	private int  pathType = 0;
	
	
	public BestPathGrader(int t) {
		setPathK(t);
	}
	
	public BestPathGrader() {
		
	}

	public int getPathK() {
		return pathType;
	}

	public void setPathK(int gradeType) {
		this.pathType = gradeType;
		//clear caches
		cache = new GraphCache<BestPathCalc>(Change.STRUCTURE,Change.POINTS_OF_INTEREST);
	}

	class BestPathCalc {
	
		HashMap<VisualNode, Double> memo;


		double memget(VisualNode n, double def) {
			Double ret = memo.get(n);
			if (ret == null)
				return def;
			return ret;
		}

		double getGroupValue(VisualGroupNode vgn) {
			Double ret = memo.get(vgn);
			if (ret != null)
				return ret;
			double total = 0;
			double max = 0;
			for (VisualNode c : vgn.getChildren()) {
				if (c instanceof VisualGroupNode) {
					double dv = getGroupValue((VisualGroupNode) c);
					total += dv;
					max = Math.max(max, dv);
				} else {
					double dv = memget(c, 999999);
					total += dv;
					max = Math.max(max, dv);
				}
			}
			// ret = total/vgn.getChildren().size();
			ret = max;
			memo.put(vgn, ret);
			return ret;
		}

		private void init(VisualGraph vg) {
			SimpleVisualGraph graph = new SimpleVisualGraph(vg);
			ArrayList<VisualNode> pois = new ArrayList<VisualNode>(vg
					.getNodesOfInterest().keySet());
			ArrayList<Double> values = new ArrayList<Double>(vg
					.getNodesOfInterest().values());
			//ArrayList<HashMap<VisualNode, Double>> dists = new ArrayList<HashMap<VisualNode, Double>>();
			
			ArrayList<ProbDijkstra> ds = new ArrayList<ProbDijkstra>();
			for (int i = 0; i < pois.size(); i++) {
				int gi = graph.getInt(pois.get(i));
				if (gi < 0) {
					pois.remove(i);
					values.remove(i);
					System.out.println("weird? skipping");
					i--;
					continue;
				}
				ds.add(new ProbDijkstra(graph,gi));
			}
			memo = new HashMap<VisualNode, Double>();
			
			if (pois.size() != 0)
				for (VisualNode n : vg.getAllNodes()) {

					if (n instanceof VisualGroupNode)
						continue;
					
					double prob = 1;
					if (pathType == ALL) {
						for (int i = 0; i < pois.size(); i++) {
							double w = values.get(i);
							if (w > 0) {
								prob *= ds.get(i).getProbTo(n);
							} else if (w < 0) {
								prob *= 1-ds.get(i).getProbTo(n);
							}
							// if(firstNode){
							// System.out.println("i = "+i+" prob = "+prob+" w = "+w+" dist = "+memget(dists.get(i),n,-1));
							// }
						}
					} else{
						ArrayList<Double> ps = new ArrayList<Double>();
						for (int i = 0; i < pois.size(); i++) {
							double w = values.get(i);
							ps.add(ds.get(i).getProbTo(n));
						}

						Collections.sort(ps,Collections.reverseOrder());
						
						Collection<Double> s = AlgoUtils.take(ps, pathType);
						for(double d:s)
							prob*=d;
						
					} 
					memo.put(n, prob);

				}

			for(VisualNode p:pois){
				for(ProbDijkstra pd:ds){
					System.out.print(pd.getProbTo(p)+" ");
				}
				System.out.println();
			}
			for (VisualNode n : vg.getAllNodes()) {
				if (n instanceof VisualGroupNode) {
					getGroupValue((VisualGroupNode) n);
				}
			}
		}
	}
	
	GraphCache<BestPathCalc> cache= new GraphCache<BestPathCalc>(Change.STRUCTURE,Change.POINTS_OF_INTEREST);
	
	public double getNodeGoodness(VisualNode n) {
		
		BestPathCalc cur = cache.get(n.getGraph());
		if(cur==null){
			cur = new BestPathCalc();
			cur.init(n.getGraph());
			cache.put(n.getGraph(), cur);
		}
		
		return cur.memget(n, 0);

	}

    public String getReadableAttribute () {
        return "best path goodness";
    }


}
