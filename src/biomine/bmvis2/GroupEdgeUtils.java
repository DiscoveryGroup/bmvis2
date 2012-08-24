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

package biomine.bmvis2;


/**
 * Friend class for making group edges
 * @author alhartik
 *
 */
public class GroupEdgeUtils {

	public static VisualEdge makeGroupEdge(VisualGroupNode u, VisualNode v,
			double g) {

		VisualGroupEdge mainEdge = new VisualGroupEdge(u, v, g, false);
		if (u.getEdge(v) != null) {
			return u.getEdge(v);
		}
		if (v instanceof VisualGroupNode) {
			VisualGroupNode vg = (VisualGroupNode) v;

			for (VisualNode vc : vg.getChildren()) {
				VisualEdge newEdge = makeGroupEdge(u, vc, g);
				if (newEdge != null)
					mainEdge.addChild(newEdge);
			}

			for (VisualNode uc : u.getChildren()) {

				VisualEdge newEdge = makeGroupEdge(vg, uc, g);
				if (newEdge != null)
					mainEdge.addChild(newEdge);
			}
			if (mainEdge.getChildren().size() == 0)
				return null;
			u.addEdge(mainEdge);

			return mainEdge;
		} else {
			boolean to, from;
			double totalGoodness = 0;
			to = from = false;
			for (VisualNode uc : u.getChildren()) {
				
				VisualEdge ed = uc.getEdge(v);
				if (ed != null) {
					//totalGoodness+=ed.getGoodness();
					if (uc == ed.getFrom())
						from = true;
					if (uc == ed.getTo())
						to = true;
					mainEdge.addChild(ed);
				}

			}
			if (to && from)
				mainEdge.setSymmetric(true);
			else if (to)
				mainEdge.reverse();

			if (mainEdge.getChildren().size() == 0)
				return null;
			u.addEdge(mainEdge);
		//	mainEdge.setGoodness(totalGoodness/mainEdge.getChildren().size()
			return mainEdge;
		}
	}
	
	public static VisualEdge makeGroupEdge(VisualGroupNode u,VisualNode v){
		return makeGroupEdge(u,v,0.5);
	}

}
