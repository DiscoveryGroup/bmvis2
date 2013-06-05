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

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import biomine.bmvis2.VisualGraph.Change;

/**
 * Group node which creates edges automatically. Edges use average
 * values as goodness values.
 * 
 * @author alhartik
 */
public class VisualGroupNodeAutoEdges extends VisualGroupNode {

	
	public VisualGroupNodeAutoEdges(VisualGraph graph, VisualGroupNode parent,
			String name) {
		super(graph, parent, name);
	
	}

	public VisualGroupNodeAutoEdges(VisualGraph graph, VisualGroupNode parent) {
		super(graph, parent, "Group");
		
	}

	public VisualGroupNodeAutoEdges(VisualGroupNode parent) {
		super(parent.getGraph(), parent, "Group");
		
	}

	public VisualGroupNodeAutoEdges(VisualGroupNode parent, String name) {
		super(parent, name);
		
	}
	
	@Override
	public void createEdges(){
		
		
		final HashMap<VisualNode, VisualGroupEdge> vges = new HashMap<VisualNode, VisualGroupEdge>();
		
		final HashSet<VisualGroupNode> ancestors = new HashSet<VisualGroupNode>();
		VisualGroupNode g = this;
		while(g!=null){
			ancestors.add(g);
			g = g.getParent();
		}
		final Collection<VisualNode> desc = getDescendants();
		//System.out.println("getting edges for "+this);
		class C {
			VisualGroupNode vgn = VisualGroupNodeAutoEdges.this;
			
			
			VisualGroupEdge getEdge(VisualNode to,boolean dirTo){
			//	System.out.println("getEdge("+to+")");
				VisualGroupEdge e = vges.get(to);
				if(e!=null){
				//	/if((e.getTo()==to)^dirTo){
				//		e.setSymmetric(true);
				//	}
					return e;
				}
				//System.out.println("creating");
				VisualGroupEdge ret ;
				
				if(dirTo)
					ret = new VisualGroupEdge(vgn, to, 1, false);
				else
					ret = new VisualGroupEdge(to, vgn, 1, false);
				
				if( to.getParent()!=null && ancestors.contains(to.getParent() ) == false){
					VisualGroupEdge par = getEdge(to.getParent(),dirTo);
					par.addChild(ret);
				}
				
				
				vges.put(to, ret);
				return ret; 
			}
			
			void  gatherEdges(VisualNode from,VisualEdge child) {
				VisualNode o = child.getOther(from);
				if(desc.contains(o))return;
				
				VisualGroupEdge ed=getEdge(child.getOther(from),from==child.getFrom());
				ed.addChild(child);
			}
		}
		C help = new C();
		
		for (VisualNode c : desc) {
			for (VisualEdge ce : c.getEdges()) {
				
				help.gatherEdges(c, ce);
			}
		}
		for(VisualGroupEdge e:vges.values()){
			e.updateLabels();
			//System.out.println("adding "+e);
			addEdge(e);
		}
		
		//System.out.println("has "+myEdges.size()+" edges");
		//return Collections.unmodifiableSet(myEdges);
	}


}
