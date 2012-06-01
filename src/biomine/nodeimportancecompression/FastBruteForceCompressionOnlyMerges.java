package biomine.nodeimportancecompression;

import java.util.ArrayList;
import java.util.HashSet;

import biomine.nodeimportancecompression.ImportanceMerger.MergeInfo;

public class FastBruteForceCompressionOnlyMerges implements CompressionAlgorithm{
	
	public static class MergeKey{
		int u,v;
		public MergeKey(int a,int b){
			u = Math.min(a,b);
			v = Math.max(a,b);
		}
        
        int other(int x){
            if(x==v)return u;
            return v;
        }
		
		@Override
		public boolean equals(Object o){
			if(!(o instanceof MergeKey))return false;
			MergeKey mo = (MergeKey)o;
			return mo.u == u && mo.v == v;
		}
		@Override 
		public int hashCode(){
			return u + 31337*v;
		}
	};
	
    public static class MergeValue implements Comparable<MergeValue>{
		MergeInfo info;
		double normalizedError;
		/**
		 * Compares merges normalized errors.
		 */
		@Override
		public int compareTo(MergeValue o) {
			return Double.compare(normalizedError,o.normalizedError);
			//return Double.compare(info.error/info.sizeReduction, o.info.error/o.info.sizeReduction);
		}
		
	}
    HeapMap<MergeKey,MergeValue> hm;//= new HeapMap<MergeKey, MergeValue>();
    ArrayList<HashSet<MergeKey>> merges;//= new ArrayList<HashSet<MergeKey>>();
    ImportanceMerger im;

    public void init(ImportanceMerger im){
        this.im =im;

		ImportanceGraph ig = im.getCurrentGraph();
        merges = new ArrayList<HashSet<MergeKey>>();
		for(int i=0;i<=ig.getMaxNodeId();i++)
			merges.add(new HashSet<MergeKey>());

        hm = new HeapMap<MergeKey,MergeValue>();
		
		for (int i:im.getCurrentGraph().getNodes()){
			for (int j : im.getCurrentGraph().getHop2Neighbors(i)) {
				if (i >= j)
					continue;
				ImportanceMerger.MergeInfo info = im.getMergeInformation(i, j);
				MergeKey mk = new MergeKey(i,j);
				MergeValue mv = new MergeValue();
				merges.get(i).add(mk);
				merges.get(j).add(mk);
				mv.info = info;
				mv.normalizedError = info.error/info.sizeReduction;
				hm.put(mk,mv);
			}
		}
    }

//    public void updateEdgeDeletion(int u,int v){
//        HashSet<Integer> ns = new HashSet<Integer>();
//        ns.add(u);
//        ns.add(v);
//        ns.addAll(im.getNeighbors(u));
//        ns.addAll(im.getNeighbors(v));
//        for(int x:ns){
//            for(MergeKey mk:merges.get(x)){
//                hm.remove(mk);
//                merges.get(mk.other(x)).remove(mk);
//            }
//            merges.get(x).clear();
//        }
//        for(int i:ns){
//            for (int j : im.getCurrentGraph().getHop2Neighbors(i)) {
//                if(i==j)continue;
//
//                MergeKey mk = new MergeKey(i,j);
//                if(hm.containsKey(mk))continue;
//                merges.get(i).add(mk);
//                merges.get(j).add(mk);
//
//                ImportanceMerger.MergeInfo info = im.getMergeInformation(mk.u,mk.v);
//                MergeValue mv = new MergeValue();
//                mv.info = info;
//                mv.normalizedError = info.error/info.sizeReduction;
//                hm.put(mk,mv);
//            }
//
//        }
//    }
    public MergeKey bestMerge(){
        if(hm.size()==0)return null;
        return hm.topKey();
    }
    public double bestMergeError(){
        if(hm.size()==0)return Double.MAX_VALUE;
        return hm.topValue().normalizedError;
    }

    public boolean doBestMerge(){
        if(hm.size()==0)return false;

        MergeKey bm = hm.topKey();
        MergeValue bmv = hm.topValue();
        int u = bm.u;
        int v = bm.v;

        //set of affected nodes.
        //their merges will have to be updated
        HashSet<Integer> ns = new HashSet<Integer>();
        //ns.addAll(im.getCurrentGraph().getNodes());
        ns.addAll(im.getCurrentGraph().getNeighbors(u));
        ns.addAll(im.getCurrentGraph().getNeighbors(v));
        ns.addAll(im.getCurrentGraph().getHop2Neighbors(u));
        ns.addAll(im.getCurrentGraph().getHop2Neighbors(v));
        ns.add(u);
        ns.add(v);
        for (int i : ns){
            for(MergeKey mk:merges.get(i)){
                hm.remove(mk);
                merges.get(mk.other(i)).remove(mk);
            }
            merges.get(i).clear();
        }
        int z = im.merge(bmv.info);
        ns.remove(u);
        ns.remove(v);
        ns.add(z);
        for (int i : ns){
            for (int j : im.getCurrentGraph().getHop2Neighbors(i)) {
                if(i==j)continue;
                MergeKey mk = new MergeKey(i,j);
                if(hm.containsKey(mk))continue;
                merges.get(i).add(mk);
                merges.get(j).add(mk);

                ImportanceMerger.MergeInfo info = im.getMergeInformation(mk.u,mk.v);
                MergeValue mv = new MergeValue();
                mv.info = info;
                mv.normalizedError = info.error/info.sizeReduction;
                hm.put(mk,mv);
            }
        }
        //hm.checkIntegrity();
        return true;
    }
	
	
	@Override
	public void compress(ImportanceMerger im, double goalRatio) {

        init(im);
		int origSize = im.getSize();
		int goalSize = (int) (origSize * goalRatio);
        while(im.getSize()>goalSize){
            if(!doBestMerge())break;
        }
	}

}
