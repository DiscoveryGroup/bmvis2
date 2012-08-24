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
import java.util.HashMap;

import biomine.bmgraph.BMEdge;
import biomine.bmgraph.BMGraph;
import biomine.bmgraph.BMNode;

public class ImportanceGraphWrapper {
	private BMGraph bmgraph;
	private ImportanceGraph importanceGraph;
	private BMNode[] iToNode;
	private HashMap<BMNode, Integer> nodeToI;

	public BMGraph getBMGraph(){
		return bmgraph;
	}
	public ImportanceGraph getImportanceGraph(){
		return importanceGraph;
	}
	
	public ImportanceGraphWrapper(BMGraph bm) {
		bmgraph = bm;
		iToNode = new BMNode[bm.getNodes().size()];
		nodeToI = new HashMap<BMNode, Integer>();
		importanceGraph = new ImportanceGraph();
		int it = 0;
		for (BMNode n : bm.getNodes()) {
			nodeToI.put(n, it);
			iToNode[it++] = n;
		}
		for (BMEdge e : bm.getEdges()) {
			int u = nodeToI.get(e.getFrom());
			int v = nodeToI.get(e.getTo());
            String gs = e.get("goodness");
            if(gs!=null){
                double g = Double.parseDouble(gs);
                if(g > importanceGraph.getEdgeWeight(u,v)){
                    importanceGraph.addEdge(u, v, g);
                }
            }
		}
	}
	public void setImportance(int node,double imp){
		importanceGraph.setImportance(node, imp);
	}
	public double getImportance(int node){
		return importanceGraph.getImportance(node);
	}
	public int nodeToInt(BMNode nod){
		return nodeToI.get(nod);
	}
	public BMNode intToNode(int i){
		return iToNode[i];
	}
	public void setImportance(BMNode nod,double imp){
		setImportance(nodeToInt(nod), imp);
	}
	public double getImportance(BMNode nod){
		return getImportance(nodeToInt(nod));
	}
}
