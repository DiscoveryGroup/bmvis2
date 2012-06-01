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
