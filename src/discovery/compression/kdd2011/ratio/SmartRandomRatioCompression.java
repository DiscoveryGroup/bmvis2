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

public class SmartRandomRatioCompression implements RatioCompression{
	private boolean hop2;
	public SmartRandomRatioCompression(boolean h){
		hop2=h;
	}
	
	private CompressionMergeModel mergeModel=null;
	
	public CompressionMergeModel getMergeModel() {
		return mergeModel;
	}

	public void setMergeModel(CompressionMergeModel mergeModel) {
		this.mergeModel = mergeModel;
	}

	@Override
	public ResultGraph compressGraph(DefaultGraph ig,double ratio){
		mergeModel.init(ig);
		
		
		int size = ig.getNodeCount();
		int cost = mergeModel.getStartCost();
		int goalCost = (int)(cost*ratio);
		int[] aliveNodes = new int[size];
		for(int i=0;i<size;i++)
			aliveNodes[i]=i;
		int count=size;
		
		while(cost>goalCost){
			int r = (int)(Math.random()*count);
			int randomNode = aliveNodes[r];
			int best = -1;
			int bestReduction = -1000;
			double bestError=1000;
			
			Collection<Integer> mergeCheck ;
			if(hop2){
				mergeCheck= mergeModel.getHopNeighbors(randomNode, 2);
			}else{
				mergeCheck = new ArrayList<Integer>();
				for(int i=0;i<count;i++)
					mergeCheck.add(aliveNodes[i]);
			}
			
			for(int nod:mergeCheck){
				if(nod!=randomNode && mergeModel.isAlive(nod)){
					int red = mergeModel.mergeCostReduction(nod, randomNode);
					double diff = mergeModel.mergeError(nod,randomNode);
					
					//if(red<=0 && bestReduction>0)continue;
					
					//System.out.println("rd: "+red+" "+diff);
					
					if (diff < bestError){
						bestReduction=red;
						best=nod; 
						bestError=diff;
					} 
					
				} 
			}
			
			if(best==-1)break;
			
			mergeModel.merge(best,randomNode);
			
			aliveNodes[r]=aliveNodes[count-1];
			cost -=bestReduction;
			count--;
			System.out.println(cost+" "+count+" d="+bestError);
			
			if(count<=2)break;
		}
		
		return mergeModel.getResult();
	}
}
