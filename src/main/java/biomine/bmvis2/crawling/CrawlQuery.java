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

package biomine.bmvis2.crawling;

import java.util.Collection;
import java.util.HashSet;

import biomine.bmgraph.BMNode;
import biomine.bmvis2.VisualNode;

/**
 * Set of node-ids used for web queries.
 * @author alhartik
 */
public class CrawlQuery extends HashSet<String>{
	public CrawlQuery(){
		
	}
	public CrawlQuery(Collection<String> codes){
		super(codes);
	}
	public void addVisualNode(VisualNode n){
		BMNode bmn = n.getBMNode();
		addBMNode(bmn);
		
	}
	public void addBMNode(BMNode bmn) {
		add(bmn.getId());
	}
	public void removeVisualNode(VisualNode n){
		BMNode bmn = n.getBMNode();
		removeBMNode(bmn);
	}
	public void removeBMNode(BMNode bmn) {
		remove(bmn.getId());
	}
}
