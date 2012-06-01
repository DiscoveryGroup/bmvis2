package discovery.compression.kdd2011.ratio;

import java.util.ArrayList;
import java.util.Collection;

import biomine.bmvis2.algoutils.DefaultGraph;


public class SlowGreedyRatioCompression implements RatioCompression{
	boolean hop2=false;
	public SlowGreedyRatioCompression(boolean h){ 
		hop2=h;
		}
	public SlowGreedyRatioCompression(CompressionMergeModel mo,boolean h2){ 
		setMergeModel(mo);
		hop2=h2;
	}
	
	private CompressionMergeModel mergeModel=null;
	
	public CompressionMergeModel getMergeModel() {
		return mergeModel;
	}


	public void setMergeModel(CompressionMergeModel mergeModel) {
		this.mergeModel = mergeModel;
	}


	
	//final double[] thlds = {0,0.01,0.05,0.1,0.2,0.3,0.5,0.8,0.9,1};
	
	final int THRESHOLD_COUNT=5;//thlds.length;
	private double threshold(int n){
		//return thlds[n];
	
		if(n==0)
			return 0;
		
		n--;
		if(n>=THRESHOLD_COUNT)return 1;
		return 1.0/(1<<(THRESHOLD_COUNT-1-n));
	}
	

	@Override
	public ResultGraph compressGraph(DefaultGraph ig, double goalRatio) {
		mergeModel.init(ig);
		
		int totalCost = mergeModel.getStartCost();
		
		int goalCost = (int)(totalCost*goalRatio);
			
		int size=ig.getNodeCount();
		
		int thldN=0;
		System.out.println("goalCost = "+goalCost);
		
		while(totalCost>goalCost){
			
			double minFound=100;
			int a,b;
			a=b=0;
			int theReduction=0;
			for(int i=0;i<size;i++){
				if(!mergeModel.isAlive(i))continue;
				Collection<Integer> mergeCheck ;
				if(hop2)
					mergeCheck= mergeModel.getHopNeighbors(i, 2);
				else {
					mergeCheck = new ArrayList<Integer>();
					for(int j=i+1;j<size;j++)
						mergeCheck.add(j);
				}
				
				// System.out.println("N1("+i+")="+mergeModel.getNeighbors(i).size());
				// System.out.println("N2("+i+")="+foo.size());
				//Collection<Integer> 
					
				 for(int j:mergeCheck){
					if(i==j)continue;
					if(!mergeModel.isAlive(j))continue;
					
					double d = mergeModel.mergeError(i, j);
					int reduction = mergeModel.mergeCostReduction(i,j);
					//if(reduction<=0)continue;
					
					//System.out.println(threshold(thldN));
					//minFound=Math.min(d,minFound);
					//System.out.println("md "+i+" "+j+" = "+d);
					
					double s =d;// d/reduction;
					if(s<minFound){
						minFound=s;
						a=i;
						b=j;
						theReduction=reduction;
					}
					
				}
			}
			if(a!=b)
				mergeModel.merge(a,b);
			totalCost-=theReduction;
			if(minFound>=1.0)
				break;
			
			System.out.println("totalCost = "+totalCost);
		}
		
		System.out.println("totalCost = "+totalCost);
		
		return mergeModel.getResult();
			
	}
	
}