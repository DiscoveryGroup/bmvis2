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
