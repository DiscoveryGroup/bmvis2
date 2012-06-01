package discovery.compression.kdd2011.old;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import biomine.bmvis2.SimpleVisualGraph;
import biomine.bmvis2.VisualGroupNode;

public class RandomizedCompressor extends Compressor{
	
	CompressedGraph lastCGraph;
	@Override
	public String makeGroups(VisualGroupNode group) {
		CompressedGraph cgraph = super.createCompressedGraph(group);
		//System.out.println("dev = "+getDeviation());
		
		startingCosts = cgraph.getCosts();
		Random rand = new Random();
		
		HashSet<Integer> alive = new HashSet<Integer>();
		int n = cgraph.size();
		for(int i=0;i<n;i++)
			alive.add(i);
		
		for(int i=0;i<n*2;i++){
			if(alive.size()<=1)break;
			
			int r = rand.nextInt(n);
			
			if(!alive.contains(r)){
				i--;
				continue;
			}
			double bestRed = -Double.MAX_VALUE;
			int bestMerge = 0;
			Collection<Integer> hopN = cgraph.hopNeighbors(r, 2);
			hopN.addAll(cgraph.hopNeighbors(r,1));
			for(int j:hopN){
				double red = cgraph.reduction(r, j).s;
				//System.out.println("merging of "+r+" and "+j+" has reduction "+red);
				if(red>bestRed){
					bestRed=red;
					bestMerge=j;
				}
			}
			if(bestRed>0){
				//System.out.println("making red of "+bestRed);
				cgraph.merge(r,bestMerge);
				alive.remove(bestMerge);
			} else
				alive.remove(r);
		}
		cgraph.performGrouping(useAuto());
		resultCosts=cgraph.getCosts();
		lastCGraph = cgraph;
		return null;
	}

	CompressorCosts startingCosts;
	CompressorCosts resultCosts;
	@Override
	public CompressorCosts getResultCosts() {
		// TODO Auto-generated method stub
		return resultCosts;
	}

	@Override
	public CompressorCosts getStartingCosts() {
		// TODO Auto-generated method stub
		return startingCosts;
	}	@Override
	public SimpleVisualGraph getUncompressedGraph() {
		return lastCGraph.uncompressedGraph();
	}

	@Override
	public SimpleVisualGraph getOriginalGraph(){
		return lastCGraph.getSimpleGraph();
	}
}
