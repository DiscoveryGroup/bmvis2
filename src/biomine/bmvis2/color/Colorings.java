package biomine.bmvis2.color;

import java.util.LinkedHashMap;
import java.util.Map;

import biomine.bmvis2.graphcontrols.BestPathGrader;


public class Colorings {

	public static Map<String,EdgeColoring> getEdgeColorings(){
		Map<String,EdgeColoring> ret = new LinkedHashMap<String, EdgeColoring>();
		ret.put("None (default)",null);
		ret.put("Betweenness",new EdgeBetweennessColoring());
		return ret;
	}
	public static Map<String,NodeColoring> getNodeColorings(){
		Map<String,NodeColoring> ret = new LinkedHashMap<String, NodeColoring>();
		ret.put("Default",new DefaultNodeColoring());
		ret.put("Group",new GroupColoring());
		ret.put("Best path",new NodeGraderColoring(new BestPathGrader()));

		return ret;
	}
}
