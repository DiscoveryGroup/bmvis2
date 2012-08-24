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

import biomine.bmvis2.algoutils.DefaultGraph;
import discovery.compression.kdd2011.old.CompressorCosts;


public interface RatioCompression {
	public class DefaultRatioCompression implements RatioCompression {
		CompressionMergeModel mergeModel=new EdgeAverageMergeModel();
		
		@Override
		public ResultGraph compressGraph(DefaultGraph ig, double goalRatio) {
			mergeModel.init(ig);
			return mergeModel.getResult();
		}

		@Override
		public void setMergeModel(CompressionMergeModel mod) {
			mergeModel = mod;
		}

	}

	public static class ResultGraph{
		public ArrayList<ArrayList<Integer>> partition;
		public DefaultGraph graph;
		
		int getEdgeCount(){
			return graph.getEdgeCount();
		}
		int getNodeCount(){
			int ret=0;
			for(ArrayList<Integer> a:partition){
				if(a.size()!=0)
					ret++;
			}
			return ret;
		}
		
		public DefaultGraph uncompressedGraph(){
			DefaultGraph ret = new DefaultGraph();
			for(ArrayList<Integer> ai:partition)
				for(int x:ai)
					ret.ensureHasNode(x);
			for(int i=0;i<partition.size();i++){
				for(int j:graph.getNeighbors(i)){
					for(int a:partition.get(i)){
						for(int b:partition.get(j)){
							if(a==b)continue;
							ret.addEdge(a,b,graph.getEdgeWeight(i,j));
						}
					}
				}
			}
			return ret;
		}
		public CompressorCosts getCompressorCosts(){
			return new CompressorCosts(getNodeCount(), 0, getEdgeCount(), 0);
		}
	}
	public ResultGraph compressGraph(DefaultGraph ig,double goalRatio);
	public void setMergeModel(CompressionMergeModel mod);
	public static enum ConnectivityType{
		GLOBAL,LOCAL
	}
	
}
