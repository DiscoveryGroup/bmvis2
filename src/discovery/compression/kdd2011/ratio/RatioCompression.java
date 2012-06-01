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
