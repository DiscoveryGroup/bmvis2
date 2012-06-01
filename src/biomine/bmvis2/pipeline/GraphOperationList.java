package biomine.bmvis2.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import biomine.bmvis2.color.GroupColoring;
import biomine.bmvis2.edgesimplification.KappaSimplifier;
import biomine.bmvis2.edgesimplification.XreosSimplifier;

public class GraphOperationList {

	private static ArrayList<GraphOperation> ret  = new ArrayList<GraphOperation>();
	
	static{
		ret.add(new EdgeLabelOperation());
		ret.add(new NodeLabelOperation());
		NodeColoringOperation groupColor = new NodeColoringOperation(new GroupColoring());
		groupColor.setName("Group coloring");
		ret.add(groupColor);
		
		ret.add(new BestPathHiderOperation());
		ret.add(new EdgeSimplificationOperation(new KappaSimplifier()));
        ret.add(new EdgeSimplificationOperation(new XreosSimplifier()));
		ret.add(new RepresentiveHighlightOperation());
		ret.add(new KMedoidsHighlight());
		ret.add(new TwoPhaseExtractOperation());
		ret.add(new SizeSliderOperation());
		ret.add(new EdgeGoodnessHider());
		
	} 
	
	public static Collection<GraphOperation> getAvailableOperations(){
		
		return Collections.unmodifiableCollection(ret);
	}
}
