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
import java.util.HashSet;

import biomine.nodeimportancecompression.ImportanceMerger.MergeInfo;

public class FastBruteForceCompressionOnlyEdges implements CompressionAlgorithm{
	
	public static class EdgeKey{
		int u,v;
		public EdgeKey(int a,int b){
			u = Math.min(a,b);
			v = Math.max(a,b);
		}
		
		@Override
		public boolean equals(Object o){
			if(!(o instanceof EdgeKey))return false;
			EdgeKey mo = (EdgeKey)o;
			return mo.u == u && mo.v == v;
		}
		@Override 
		public int hashCode(){
			return u + 31337*v;
		}
	};
	

    HeapMap<EdgeKey,Double> hm;//= new HeapMap<EdgeKey, Double>();
    ImportanceMerger im;
    public void init(ImportanceMerger im){
        hm = new HeapMap<EdgeKey,Double> ();
        this.im=im;
		//add all merges
		//ArrayList<HashSet<EdgeKey>> edges = new ArrayList<HashSet<EdgeKey>>();
		//ImportanceGraph ig = im.getCurrentGraph().copy();
		//for(int i=0;i<=ig.getMaxNodeId();i++)
			//merges.add(new HashSet<MergeKey>());
		
		for (int i:im.getCurrentGraph().getNodes()){
			for (int j : im.getCurrentGraph().getNeighbors(i)){
				if (i >= j)
					continue;
                EdgeKey ek = new EdgeKey(i,j);
                int sr = im.edgeDeleteSizeReduction(i,j);
                double e = Math.sqrt(im.edgeDeleteError(i,j));
				hm.put(ek,e/sr);
			}
		}
		System.out.println("start");
    }
    public boolean doBestEdgeRemoval(){
        if(hm.size()==0)return false;
        EdgeKey bm = hm.topKey();
        int u = bm.u;
        int v = bm.v;

        //set of affected nodes.
        //their edges will have to be updated
        HashSet<Integer> ns = new HashSet<Integer>();
        //ns.addAll(im.getCurrentGraph().getNodes());
        ns.addAll(im.getCurrentGraph().getNeighbors(u));
        ns.addAll(im.getCurrentGraph().getNeighbors(v));
        //ns.addAll(im.getCurrentGraph().getHop2Neighbors(u));
        //ns.addAll(im.getCurrentGraph().getHop2Neighbors(v));
        //
        ns.add(u);
        ns.add(v);

        for (int i : ns){
            for(int j : im.getNeighbors(i)){
                EdgeKey e = new EdgeKey(i,j);
                hm.remove(e);
            }
        }
        im.deleteEdge(u,v);
        for (int i : ns){
            for (int j : im.getNeighbors(i)){
                if(i==j)continue;
                //System.out.println("foo "+i+" "+j);
                EdgeKey ek = new EdgeKey(i,j);
                if(hm.containsKey(ek))continue;
                int sr = im.edgeDeleteSizeReduction(i,j);
                double e = Math.sqrt(im.edgeDeleteError(i,j));
                hm.put(ek,e/sr);
            }
        }
        //hm.checkIntegrity();


        return true;
    }

    public void updateMerge(int u,int v){
    }
	
	@Override
	public void compress(ImportanceMerger im, double goalRatio) {

		int origSize = im.getSize();
		int goalSize = (int) (origSize * goalRatio);

        init(im);
		while(im.getSize()>goalSize){
            if(!doBestEdgeRemoval())break;
		}

	}

}
