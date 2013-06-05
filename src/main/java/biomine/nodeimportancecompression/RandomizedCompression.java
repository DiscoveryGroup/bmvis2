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
import java.util.Random;

import biomine.nodeimportancecompression.ImportanceMerger.MergeInfo;

public class RandomizedCompression implements CompressionAlgorithm {

	@Override
	public void compress(ImportanceMerger im, double ratio) {
		int origSize = im.getSize();
		int goalSize = (int) (origSize * ratio);
		System.out.println("original size = " + origSize);
		System.out.println("goal size = " + goalSize);

		ArrayList<Integer> nodes = new ArrayList<Integer>();
		Random rand = new Random();
		for(int x:im.getCurrentGraph().getNodes())
			nodes.add(x);
		int[] nodeI = new int[im.getCurrentGraph().getMaxNodeId()+1];
		for(int i=0;i<nodes.size();i++)
			nodeI[nodes.get(i)]=i;
		
		
		while (im.getSize() > goalSize){
			int r = rand.nextInt(nodes.size());
			int u = nodes.get(r);

            while(!im.getCurrentGraph().hasNode(u)){
                int b= nodes.get(nodes.size()-1);
                nodes.set(r,b);
				nodeI[b] = r;
				nodes.remove(nodes.size()-1);
                r = rand.nextInt(nodes.size());
                u = nodes.get(r);
            }
			
			int otherNode = 0;
			double mergeCost = 10000;
			MergeInfo bestMerge=null;
			
			for(int v:im.getCurrentGraph().getHop2Neighbors(u)){
				if(v==u)continue;
				MergeInfo inf = im.getMergeInformation(u, v);
				double norm = inf.error/inf.sizeReduction;
				if(inf.sizeReduction>im.getSize()-goalSize)
					continue;
				if(inf.sizeReduction==0)
					continue;
				if(norm < mergeCost){
					mergeCost = norm;
					bestMerge = inf;
				}
			}
			double edgeCost = 100001;
			int bestEdge=-1;
			//check edge deletions
			for(int v:im.getCurrentGraph().getNeighbors(u)){
				double error = im.edgeDeleteError(u,v);
				int sr = im.edgeDeleteSizeReduction(u, v);
				if(sr==0)continue;
				double norm = Math.sqrt(error)/sr;
				if(norm < edgeCost){
					edgeCost = norm;
					bestEdge = v;
				}
			}
			if(bestEdge==-1 && bestMerge==null){
				//System.out.println("Suitable operations not found, continuing algorithm");
				continue;
			}
			int addedNode = -1;
			if(edgeCost < mergeCost){
				otherNode = bestEdge;
				im.deleteEdge(u, bestEdge);
			}else{
				otherNode = bestMerge.v;
				addedNode = im.merge(bestMerge);
			}
			
			if(!im.getCurrentGraph().hasNode(u)){
				int b = nodes.get(nodes.size()-1);
				nodes.set(nodeI[u],b);
				nodeI[b] = nodeI[u];
				nodes.remove(nodes.size()-1);
			}
			if(otherNode!=u && !im.getCurrentGraph().hasNode(otherNode)){
				int v = otherNode;
				int b = nodes.get(nodes.size()-1);
				nodes.set(nodeI[v],b);
				nodeI[b] = nodeI[v];
				nodes.remove(nodes.size()-1);
			}
		}
		
	}
	
}
