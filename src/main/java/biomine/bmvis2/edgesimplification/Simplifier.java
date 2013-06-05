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

package biomine.bmvis2.edgesimplification;

import java.util.Collection;
import java.util.List;

import biomine.bmvis2.VisualEdge;
import biomine.bmvis2.VisualGraph;

public abstract class Simplifier {
	public abstract List<VisualEdge> getRemovedEdges(VisualGraph g,int removeK);
	
	public void simplify(VisualGraph g,int removedEdges){
		Collection<VisualEdge> es = getRemovedEdges(g, removedEdges);
		for(VisualEdge e:es)
			g.deleteEdge(e);
	}

	public abstract String getTitle();	
}
