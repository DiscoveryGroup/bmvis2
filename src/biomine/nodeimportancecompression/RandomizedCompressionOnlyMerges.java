package biomine.nodeimportancecompression;

import java.util.ArrayList;
import java.util.Random;

import biomine.nodeimportancecompression.ImportanceMerger.MergeInfo;

public class RandomizedCompressionOnlyMerges implements CompressionAlgorithm {

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
		
		 int times = 0;	//added	

		while (im.getSize() > goalSize && im.getSize()>1 && times <=100 ){  //added
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
			if(bestMerge==null){
				//System.out.println("Suitable operations not found, continuing algorithm");
                                  times = times+1;  //added
				continue;
			}
			int addedNode = -1;
                       otherNode = bestMerge.v;
                       addedNode = im.merge(bestMerge);
			 times = 0;   //added

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
