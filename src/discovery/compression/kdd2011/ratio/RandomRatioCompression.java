package discovery.compression.kdd2011.ratio;

import java.util.ArrayList;
import biomine.bmvis2.algoutils.DefaultGraph;



public class RandomRatioCompression implements RatioCompression{
	private boolean  hop2=false;
	public RandomRatioCompression(boolean h){
        hop2=h;
	}
	
	private CompressionMergeModel mergeModel=new PathAverageMergeModel();
	
	public CompressionMergeModel getMergeModel() { return mergeModel; } 
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

			if(count<=1)break;
			int r = (int)(Math.random()*count);

			int randomNode = aliveNodes[r];
            int best=randomNode;
            if(hop2){
                ArrayList<Integer> neighbors = 
                    new ArrayList<Integer>(mergeModel.getNeighbors(randomNode));
                int r2 = (int)(Math.random()*neighbors.size());
                int rn = neighbors.get(r2);
                neighbors = new ArrayList<Integer>(mergeModel.getNeighbors(rn));
                int r3 = (int)(Math.random()*neighbors.size());
                best = neighbors.get(r3);
            }
            if(best==randomNode){
	            int r2 = r;
	            while(r2==r){
	                r2=(int)(Math.random()*count);
	                
	                best = aliveNodes[r2];
	            }
	        }
			
			int bestReduction = mergeModel.mergeCostReduction(best,randomNode);
			
			mergeModel.merge(best,randomNode);
			
			aliveNodes[r]=aliveNodes[count-1];
			count--;
			cost-=bestReduction;
			System.out.println(cost);
		}
		
		return mergeModel.getResult();
	}
}

