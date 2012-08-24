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

package biomine.bmvis2.group;

import biomine.bmvis2.VisualGroupNode;

public class InitialGrouper extends Grouper {
	public static String NAME = "Initial grouper";

	@Override
	public String makeGroups(VisualGroupNode n) {
		long startedAt = System.currentTimeMillis();
		
		PathGrouper path = new PathGrouper();
		path.makeGroups(n);
		setProgress(0.2);
		
		ParallelGrouper par = new ParallelGrouper();
		par.makeGroups(n);
		
		setProgress(0.5);
		LabelPropagationCommunity lpc = new  LabelPropagationCommunity();
		lpc.setSubdivide(true);
		lpc.makeGroups(n);
		setProgress(1.0);
		System.err.println("Label propagation grouping took " + (System.currentTimeMillis() - startedAt) + " ms.");
		
		System.err.println("Grouping took " + (System.currentTimeMillis() - startedAt) + " ms.");
		return null;
	}
}
