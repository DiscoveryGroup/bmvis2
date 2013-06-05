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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import biomine.bmgraph.BMGraph;

/**
 * Object that represents edge between group nodes. This is either 
 * group-group or group-node.
 * 
 * @author alhartik
 *
 */
public class VisualGroupEdge extends VisualEdge {

	public VisualGroupEdge(VisualNode from, VisualNode to, double g,
			boolean symmetric) {
		super(from.getGraph(), from, to, g, symmetric, "Multiple edges");
		// TODO Auto-generated constructor stub
		children = new HashSet<VisualEdge>();
		updateGoodness();

	}
	
	Set<VisualEdge> children;

	public Map<String, Set<VisualEdge>> getTypeCounts() {
		TreeMap<String, Set<VisualEdge>> ret = new TreeMap<String, Set<VisualEdge>>();
		for (VisualEdge e : children) {
			if (e instanceof VisualGroupEdge) {
				VisualGroupEdge ge = ((VisualGroupEdge) e);
				Map<String, Set<VisualEdge>> cm = ge.getTypeCounts();
				boolean reverse = false;
				if (e.getFrom() == getTo() || e.getTo() == getFrom()) {
					reverse = true;
				}
				if (e.isSymmetric())
					reverse = false;
				for (String s : cm.keySet()) {
					String s2 = s;
					if (reverse) {
						BMGraph.REVERSE_LINKTYPES.get(s);
						if (s2 == null)
							s2 = "-" + s;
					}
					Set<VisualEdge> count = ret.get(s2);
					Set<VisualEdge> add = cm.get(s);
					if (count == null) {
						count = new HashSet<VisualEdge>();

					}
					count.addAll(add);

					ret.put(s2, count);
				}
			} else {
				String type = e.getType();
				if (e.getFrom() == getTo() || e.getTo() == getFrom()) {
					type = BMGraph.REVERSE_LINKTYPES.get(type);
					if (type == null)
						type = "-" + e.getType();
				}
				Set<VisualEdge> count = ret.get(type);

				if (count == null)
					count = new HashSet<VisualEdge>();
				count.add(e);

				ret.put(type, count);
			}
		}

		return ret;
	}

	public void updateGoodness() {
		double g = 0;
		Collection<VisualEdge> desc = getRealDescendants();
		for (VisualEdge c : desc)
			g += c.getGoodness();
		double newg = g / desc.size();
		super.setGoodness(newg);
//		System.out.println("setting goodness to " + newg + " desc=" + desc
//				+ " children = " + getChildren());
	}

	void updateLabels() {
		updateGoodness();
		Map<String, Set<VisualEdge>> typeCount = getTypeCounts();
		ArrayList<String> nl = new ArrayList<String>();
		int i = 0;
		int total = 0;

		for (String type : typeCount.keySet()) {

			nl.add(typeCount.get(type).size() + "x " + type);
			total += typeCount.get(type).size();
		}
		nl.add("" + Math.round(getGoodness() * 100) * 0.01);

		setLabels(nl);
	}

	public void addChild(VisualEdge e) {
		children.add(e);
		updateLabels();
	}

	public void removeChild(VisualEdge e) {
		children.remove(e);
		updateLabels();
	}

	public Set<VisualEdge> getChildren() {
		return children;
	}

	public Set<VisualEdge> getRealDescendants() {
		HashSet<VisualEdge> ret = new HashSet<VisualEdge>();
		for (VisualEdge e : getChildren()) {
			if (e instanceof VisualGroupEdge) {
				ret.addAll(((VisualGroupEdge) e).getRealDescendants());
			} else {
				ret.add(e);
			}
		}
		return ret;
	}

	@Override
	public boolean equals(Object o) {
		if (o.getClass().equals(this.getClass())) {
			VisualGroupEdge e = (VisualGroupEdge) o;

			// return getRealDescendants().equals(e.getRealDescendants());

			boolean ep = false;
			if (e.getFrom() == getFrom() && e.getTo() == getTo())
				ep = true;
			else if (e.getFrom() == getTo() && e.getTo() == getFrom())
				ep = true;

			if (ep && e.getType().equals(getType())) {
				return true;
				// return getRealDescendants().
				// equals(e.getRealDescendants());
			}
		}
		return false;
	}

	public int hashCode() {
		return (getFrom().hashCode() + 1) * (getTo().hashCode() + 1);
	}
	/*
	 * public String toString(){ return "GROUPEDGE:"+children; }
	 */
	// Map<VisualNode,Vi>
}
