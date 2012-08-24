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

import java.util.WeakHashMap;

import biomine.bmvis2.VisualGraph.Change;

/**
 * Implements map-like interface for saving precomputed data associated with
 * specific version of specific graph.
 * 
 * 
 * @author alhartik
 * 
 */

public class GraphCache<T> extends WeakHashMap<VisualGraph, T> {

	private boolean[] meaningfulChanges = new boolean[VisualGraph.Change.values().length];

	public GraphCache(Change ... invalidators) {
		for(Change change:invalidators)
			meaningfulChanges[change.ordinal()] = true;
	}

	WeakHashMap<VisualGraph, long[]> versionMap = new WeakHashMap<VisualGraph, long[]>();

	public T get(Object g) {

		VisualGraph vg = (VisualGraph) g;
		long[] cv = versionMap.get(vg);
		if (cv == null)
			return null;

		boolean sameVersion = true;
		for (int i = 0; i < meaningfulChanges.length; i++) {
			if (meaningfulChanges[i])
				if (cv[i] != vg.getVersion(VisualGraph.Change.values()[i])) {
					sameVersion = false;
					break;
				}
		}

		if (sameVersion) {
			return super.get(vg);
		} else {
			return null;
		}
	}

	public T put(VisualGraph o, T t) {
		T ret = super.put(o, t);
		long[] version = new long[VisualGraph.Change.values().length];
		for (Change c : Change.values()) {
			version[c.ordinal()]=o.getVersion(c);
		}
		versionMap.put(o, version);
		return ret;
	}
}
