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
