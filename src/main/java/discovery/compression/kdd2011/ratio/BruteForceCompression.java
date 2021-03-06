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

import biomine.bmvis2.algoutils.DefaultGraph;

public class BruteForceCompression implements RatioCompression{
	private boolean hop2=false;
	private boolean edgeDifference=false;
	private CompressionMergeModel mergeModel = new PathAverageMergeModel();
	
	public BruteForceCompression(boolean hop22,boolean edgeOnly) {
		hop2=hop22;
		edgeDifference = edgeOnly;
        mergeModel = new EdgeAverageMergeModel();
	}

	@Override
	public ResultGraph compressGraph(DefaultGraph ig, double goalRatio) {
		
		mergeModel.init(ig);
		DefaultGraph orig = mergeModel.getResult().uncompressedGraph();
		int goalCost =(int)( goalRatio*mergeModel.getStartCost());
		int cost = mergeModel.getStartCost();
		int count = orig.getNodeCount();
		
		while(cost>goalCost){
			
			if(count<3)break;
			
			double bestDiff = Double.MAX_VALUE;
			
			int a=-1,b=-1;
			for(int i=0;i<ig.getNodeCount();i++){
				if(!mergeModel.isAlive(i))continue;
				
				Collection<Integer> mergeCheck ;
				if(hop2)
					mergeCheck= mergeModel.getHopNeighbors(i, 2);
				else {
					mergeCheck = new ArrayList<Integer>();
					for(int j=i+1;j<ig.getNodeCount();j++)
						mergeCheck.add(j);
				}
				
				// System.out.println("N1("+i+")="+mergeModel.getNeighbors(i).size());
				// System.out.println("N2("+i+")="+foo.size());
				//Collection<Integer> 
					
				 for(int j:mergeCheck){	
					if(j==i)continue;
					if(!mergeModel.isAlive(j))continue;
					CompressionMergeModel mm2 = mergeModel.copy();
					mm2.merge(i,j);
					double diff =0;
					
					if(edgeDifference){
						diff = GraphDifference.edgeDifference(orig,mm2.getResult().uncompressedGraph());
					}else{
						diff = GraphDifference.difference(orig,mm2.getResult().uncompressedGraph());
					}
					if(diff<bestDiff){
						bestDiff=diff;
						a=i;
						b=j;
					}
				}
			}
			if(a<0)break;
			
			
			System.out.println("bd="+bestDiff);
			cost-=mergeModel.mergeCostReduction(a,b);
			mergeModel.merge(a,b);
			count--;
		}
		return mergeModel.getResult();
	}

	@Override
	public void setMergeModel(CompressionMergeModel mod) {
		mergeModel=mod;
	}

}
