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

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Iterator;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JSlider;

import biomine.bmvis2.VisualEdge;
import biomine.bmvis2.VisualGroupEdge;
import biomine.bmvis2.VisualGroupNode;
import biomine.bmvis2.VisualGroupNodeAutoEdges;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.group.EdgeTripleGrouper.EdgeTriple;

public class PathGrouper extends Grouper {
	public static String NAME = "Path grouper";
	public static String SIMPLE_NAME = "By structure (paths)";

	/**
	 * Development debug printing method.
	 * 
	 * @param s
	 */
	private void d(String s) {
		// System.out.println("D: " + s);
	}

	/**
	 * General debug printout.
	 * 
	 * @param s
	 */
	private void p(String s) {
		System.err.println("Path grouper: " + s);
	}

	private Set<VisualNode> getCandidates(VisualGroupNode group) {
		Set<VisualNode> candidates = new HashSet<VisualNode>();
		for (VisualNode n : group.getChildren())
			if (n.getDegree() < 3)
				candidates.add(n);

		return candidates;
	}

	public void addNeighborToGroup(Set<VisualNode> candidates,
			Set<VisualNode> groupMembers, VisualNode node) {
		for (VisualNode n : node.getNeighbors()) {
			if (!candidates.contains(n))
				continue;
			groupMembers.add(n);
			d("at " + node);
			d("added node " + n);
			candidates.remove(n);
			this.addNeighborToGroup(candidates, groupMembers, n);
		}
	}

	public String makeGroups(VisualGroupNode group) {
		long startedAt = System.currentTimeMillis();
		Set<VisualNode> candidates = this.getCandidates(group);
		int candidateCount = candidates.size();

		Set<VisualGroupNode> groupNodes = new HashSet<VisualGroupNode>();

		VisualNode candidateNode;
		while (!candidates.isEmpty()) {
			Set<VisualNode> groupMembers = new HashSet<VisualNode>();
			candidateNode = candidates.iterator().next();

			candidates.remove(candidateNode);

			groupMembers.add(candidateNode);
			this.addNeighborToGroup(candidates, groupMembers, candidateNode);

			if (groupMembers.size() > 1) {
				VisualGroupNode groupNode = new VisualGroupNodeAutoEdges(group,
						"Path");
				groupNode.setGroupType("path");
				groupNodes.add(groupNode);
				for (VisualNode member : groupMembers)
					groupNode.addChild(member);
				d("created group " + groupNode);
			}
		}

		p(candidateCount + " initial candidates.");
		p(groupNodes.size() + " groups created.");
		p("grouping took " + (System.currentTimeMillis() - startedAt) + " ms.");
		return null;
	}

	@Override
	public boolean settingsDialog(Component parent, VisualGroupNode group) {
		return true;
	}
}