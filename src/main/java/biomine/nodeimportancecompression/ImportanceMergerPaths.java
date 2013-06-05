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

public class ImportanceMergerPaths extends ImportanceMerger{
	

	private boolean keepEdges=false;

	public boolean keepEdges() { return keepEdges;
	}

	public void setKeepEdges(boolean keepEdges) {
		this.keepEdges = keepEdges;
	}
	
	ProbDijkstra[] pds;
	double H;
	public ImportanceMergerPaths(ImportanceGraph graph){
		super(graph);
		for(int x:getCurrentGraph().getNodes())
			H += impSum(x);
		H = H*H;
		pds = null;
	}

	@Override
	public double edgeDeleteError(int from, int to) {
		double q = getConnection(from, to);
		
		//best path in new graph:
		HashMap<Integer,Double> start = new HashMap<Integer, Double>();
		for(int x:getNeighbors(from)){
			if(x==to)continue;
			start.put(x,getCurrentGraph().getEdgeWeight(from, x));
		}
		ProbDijkstra pd = new ProbDijkstra(getCurrentGraph(),start,Collections.singleton(from));
		double qi = pd.getProbTo(to);
		double dmax = Math.abs(qi-q);
		return dmax*Math.sqrt(H);
		//return Double.MAX_VALUE;
		
		//throw new UnsupportedOperationException("Path oriented compression doesn't support edge deletions");
	}
	
	public double getConnection(int u,int v){
		ImportanceGraph cg = getCurrentGraph();
		if(u==v){
			return cg.getEdgeWeight(u,v);
		}
		if(pds==null)
			pds = new ProbDijkstra[cg.getMaxNodeId()+1];
		if(pds[u]!=null){
			return pds[u].getProbTo(v);
		}else if(pds[v]!=null){
			return pds[v].getProbTo(u);
		}else {
			pds[u] = new ProbDijkstra(cg,u);
			return pds[u].getProbTo(v);
		}
	}
	public MergeInfo getMergeInformation(int u, int v) {
		
		MergeInfo info = new MergeInfo();
		info.u = u;
		info.v = v;
		
		ImportanceGraph cg = getCurrentGraph();
		
		HashSet<Integer> xs = new HashSet<Integer>();
		HashSet<Integer> un = new HashSet<Integer>(getNeighbors(u));
		HashSet<Integer> vn  =new HashSet<Integer>(getNeighbors(v));
		xs.addAll(un);
		xs.addAll(vn);
		xs.remove(u);
		xs.remove(v);
		ArrayList<Integer> xsArr = new ArrayList<Integer>(xs);
		
		//int edges=getNeighbors(u).size()+
				  //getNeighbors(v).size();
                  
		int edgesRemoved = 0;
		int nodesRemoved = 1;
		for(int x:xs){
			if(x==u || x==v)continue;
			int count = 0;
			if(un.contains(x)) count++;
			if(vn.contains(x))count++;
			if(count==2)edgesRemoved++;
		}
		
		
		info.removedEdges = new HashSet<Integer>();
		int selfEdgesCount = 0;
		if(getNeighbors(u).contains(u))
			selfEdgesCount++;
		if(getNeighbors(v).contains(v))
			selfEdgesCount++;
		if(getNeighbors(u).contains(v))
			selfEdgesCount++;
		
		if(selfEdgesCount!=0)//if there are no edges to merge into self edge, no such will be added
        {
			info.hasSelfEdge = true;
			edgesRemoved += selfEdgesCount-1;//otherwise possibly multiple edges are replaced by selfedge
        }
		else
			info.hasSelfEdge = false;
		
		
		//figure which edges are kept and what are the new weights
		for(int x:xsArr){
			if( cg.getEdgeWeight(u,x) < getConnection(u, x)-0.001 &&
				cg.getEdgeWeight(v,x) < getConnection(v, x)-0.001){
				xs.remove(x);
                //System.out.println("rem "+x+" "+getNeighbors(x).size());
                if(getNeighbors(x).size()==1){
                    double a = cg.getEdgeWeight(u,x);
                    double b = getConnection(u, x);
                    double c = cg.getEdgeWeight(v,x);
                    double d = getConnection(v, x);
                    System.out.println("ux = "+a+" Qux = "+b+
                        " vx = "+c+" Qvx = "+d);

                    continue;
                }
				info.removedEdges.add(x);
                edgesRemoved++;
			}
		}
		if(info.hasSelfEdge){
			info.selfNewWeight = selfNewWeight(u, v);
		}
		
		xsArr = new ArrayList<Integer>(xs);
		
        info.sizeReduction = nodesRemoved+edgesRemoved;

		info.newWeights = new HashMap<Integer, Double>();
		for(int x:xsArr){
			double uw = getConnection(u, x);
			double vw = getConnection(v, x);
			double nw = (impSum(u)*uw+impSum(v)*vw)/(impSum(u)+impSum(v));
			info.newWeights.put(x,nw);
		//	info.error = Math.max(info.error,Math.abs(uw-vw));
			
		}
		HashSet<Integer> uvs = new HashSet<Integer>();
		uvs.add(u);
		uvs.add(v);
		ProbDijkstra pdi = new ProbDijkstra(getCurrentGraph(),info.newWeights,uvs);
		double dmax = 0 ;
		
		double localError = 0;
		double imps = 0;
		for(int x:xsArr){
			double uw = getConnection(u, x);
			double vw = getConnection(v, x);
			double qi = pdi.getProbTo(x);
			dmax = Math.max(dmax,Math.abs(uw-qi));
			dmax = Math.max(dmax,Math.abs(vw-qi));
			localError += impSum(v)*impSum(x)*(vw-qi)*(vw-qi);
			localError += impSum(u)*impSum(x)*(uw-qi)*(uw-qi);
			imps+=impSum(x)*(impSum(v)+impSum(u));
			//System.out.println("qi = "+qi+" nw = "+info.newWeights.get(x));
		//	info.error = Math.max(info.error,Math.abs(uw-vw));
			
		}
		
		double hh = H - imps;
		
		info.error = Math.sqrt(localError + hh*dmax*dmax);
		
		if(!keepEdges){
			for(int x:xsArr){
				//double h2 = hh-(impSum(v)+impSum(u))*impSum(x);
				double ow = info.newWeights.get(x);
				info.newWeights.remove(x);
				ProbDijkstra pd = new ProbDijkstra(getCurrentGraph(),info.newWeights,uvs);
				double prob = pd.getProbTo(x);
				double vx = getConnection(v, x);
				double ux = getConnection(u, x);
				double le = localError+
					impSum(v)*impSum(x)*(vx-prob)*(vx-prob)+
					impSum(u)*impSum(x)*(ux-prob)*(ux-prob);
				info.newWeights.put(x, ow);
				double dm2 = Math.max(dmax,Math.abs(ux-prob));
				dm2 = Math.max(dm2,Math.abs(vx-prob));
				double er2 = Math.sqrt(le+dm2*dm2*hh);
				
				int sizeRed = 1;
				
				if(getNeighbors(x).size()==1){
					sizeRed++;
				}else if(getNeighbors(x).size()==2){
					if(getNeighbors(x).contains(u)&&
						getNeighbors(x).contains(v)){
						sizeRed++;
					}
				}
                
				
				// System.out.println("extraError for "+x+" = "+er2+" - "+info.error);
				if((er2-info.error)/sizeRed < info.error/info.sizeReduction){
					info.removedEdges.add(x);
					info.newWeights.remove(x);
					info.error = er2;
					info.sizeReduction+=sizeRed;
					localError = le;
					dmax = dm2;
					//xs.remove(x);
				}
			}
		}
		
		
		
		
		//if(info.hasSelfEdge)
			//info.sizeReduction-=1;
		
		
		return info;
	}
	
	@Override
	protected void postMerge(MergeInfo info){
		pds = null;
	}

	@Override
	protected void postEdgeDeletion(int u,int v){
		pds = null;
	}
}
