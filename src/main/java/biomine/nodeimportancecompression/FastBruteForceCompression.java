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

import biomine.nodeimportancecompression.FastBruteForceCompressionOnlyEdges.*;
import biomine.nodeimportancecompression.FastBruteForceCompressionOnlyMerges.*;
import biomine.nodeimportancecompression.ImportanceMerger.MergeInfo;

public class FastBruteForceCompression implements CompressionAlgorithm{

    HeapMap<EdgeKey,Double> edgeHm;
    HeapMap<MergeKey,MergeValue> mergeHm;
    ArrayList<HashSet<MergeKey>> merges;
    boolean doedges=true;
    boolean domerges=true;
    ImportanceMerger im;

    public void init(ImportanceMerger im){
        this.im=im;

		ImportanceGraph ig = im.getCurrentGraph();
        if(domerges){
            merges = new ArrayList<HashSet<MergeKey>>();
            for(int i=0;i<=ig.getMaxNodeId();i++)
                merges.add(new HashSet<MergeKey>());

            mergeHm = new HeapMap<MergeKey,MergeValue>();

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
                    mergeHm.put(mk,mv);
                }
            }
        }

        if(doedges){
            edgeHm = new HeapMap<EdgeKey,Double>();
            for (int i:im.getCurrentGraph().getNodes()){
                for (int j : im.getCurrentGraph().getNeighbors(i)){
                    if (i >= j)
                        continue;
                    EdgeKey ek = new EdgeKey(i,j);
                    int sr = im.edgeDeleteSizeReduction(i,j);
                    double e = Math.sqrt(im.edgeDeleteError(i,j));
                    edgeHm.put(ek,e/sr);
                }
            }
        }
    }

    public double bestMergeError(){
        if(!domerges)return Double.MAX_VALUE;
        if(mergeHm.size()==0)return Double.MAX_VALUE;
        return mergeHm.topValue().normalizedError;
    }
    public double bestEdgeRemovalError(){
        if(!doedges)return Double.MAX_VALUE;
        if(edgeHm.size()==0)return Double.MAX_VALUE;
        return edgeHm.topValue();
    }

    public MergeKey bestMerge(){
        if(mergeHm.size()==0)return null;
        return mergeHm.topKey();
    }
    public void remove(HashSet<Integer> ns){
        for (int i : ns){
            if(domerges){
                for(MergeKey mk:merges.get(i)){
                    mergeHm.remove(mk);
                    merges.get(mk.other(i)).remove(mk);
                }
                merges.get(i).clear();
            }
            if(doedges){
                for(int j : im.getNeighbors(i)){
                    EdgeKey e = new EdgeKey(i,j);
                    edgeHm.remove(e);
                }
            }
        }
    }
    public void add(HashSet<Integer> ns){
        for (int i : ns){
            if(domerges){
                for (int j : im.getCurrentGraph().getHop2Neighbors(i)) {
                    if(i==j)continue;
                    MergeKey mk = new MergeKey(i,j);
                    if(mergeHm.containsKey(mk))continue;
                    merges.get(i).add(mk);
                    merges.get(j).add(mk);

                    ImportanceMerger.MergeInfo info = im.getMergeInformation(mk.u,mk.v);
                    MergeValue mv = new MergeValue();
                    mv.info = info;
                    mv.normalizedError = info.error/info.sizeReduction;
                    mergeHm.put(mk,mv);
                }
            }
            if(doedges){
                for (int j : im.getNeighbors(i)){
                    if(i==j)continue;
                    //System.out.println("foo "+i+" "+j);
                    EdgeKey ek = new EdgeKey(i,j);
                    if(edgeHm.containsKey(ek))continue;
                    int sr = im.edgeDeleteSizeReduction(i,j);
                    double e = Math.sqrt(im.edgeDeleteError(i,j));
                    edgeHm.put(ek,e/sr);
                }
            }
        }

    }


    public boolean doBestMerge(){
        if(mergeHm.size()==0)return false;

        MergeKey bm = mergeHm.topKey();
        MergeValue bmv = mergeHm.topValue();
        int u = bm.u;
        int v = bm.v;

        //set of affected nodes.
        //their merges will have to be updated
        HashSet<Integer> ns = new HashSet<Integer>();
        ns.addAll(im.getCurrentGraph().getNeighbors(u));
        ns.addAll(im.getCurrentGraph().getNeighbors(v));
        //System.out.println("foo");
        for(int x:bmv.info.removedEdges)
            ns.addAll(im.getNeighbors(x));
        //ns.addAll(im.getCurrentGraph().getHop2Neighbors(u));
        //ns.addAll(im.getCurrentGraph().getHop2Neighbors(v));
        ns.add(u);
        ns.add(v);

        remove(ns);
    
        int z = im.merge(bmv.info);
        ns.remove(u);
        ns.remove(v);
        ns.add(z);
        add(ns);
        return true;
    }

    public boolean doBestEdgeRemoval(){
    	if(edgeHm.size()==0)return false;
        EdgeKey bm = edgeHm.topKey();
        int u = bm.u;
        int v = bm.v;

        //set of affected nodes.
        //their edges will have to be updated
        HashSet<Integer> ns = new HashSet<Integer>();
        ns.addAll(im.getCurrentGraph().getNeighbors(u));
        ns.addAll(im.getCurrentGraph().getNeighbors(v));
        ns.add(u);
        ns.add(v);

        remove(ns);
        im.deleteEdge(u,v);
        add(ns);

        return true;
    }
	
    public FastBruteForceCompression(boolean merges,boolean edges){
        this.doedges=edges;
        this.domerges=merges;
    }
	
	public void compress(ImportanceMerger im, double goalRatio) {

        init(im);
		int origSize = im.getSize();
		int goalSize = (int) (origSize * goalRatio);
        while(im.getSize()>goalSize){
            if(bestEdgeRemovalError()<bestMergeError()){
                doBestEdgeRemoval();
            }else{
                if(!doBestMerge())
                    doBestEdgeRemoval();

            }
        }
	}

}
