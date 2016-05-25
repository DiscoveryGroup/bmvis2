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

package biomine.bmvis2.pipeline.operations.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import biomine.bmvis2.pipeline.SettingsChangeCallback;
import biomine.bmvis2.pipeline.operations.structure.GrouperOperation;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualGroupNode;
import biomine.bmvis2.VisualNode;

public class ManualGroupOperation extends GrouperOperation {
	
	private static int lastId=0;
	private int id;
	
	HashSet<String> nodes;
	
	public ManualGroupOperation(){
		id = ++lastId;
		nodes=null;
	}
	
	public ManualGroupOperation(Collection<VisualNode> gr){
		id = ++lastId;
		nodes = new HashSet<String>();
		for(VisualNode vn:gr){
			if(vn instanceof VisualGroupNode){
				VisualGroupNode vgn = (VisualGroupNode) vn;
				for(VisualNode vz:vgn.getDescendants())
					nodes.add(vz.getId());
			}else{
				nodes.add(vn.getId());
			}
		}
	}
	
	@Override
	public void doOperation(VisualGraph g) {
		if(nodes==null)return;
		
		ArrayList<VisualNode> vns  =new ArrayList<VisualNode>();
		for(VisualNode n:g.getAllNodes()){
			if(nodes.contains(n.getId())){
				vns.add(n);
			}
		}
		
		HashSet<VisualNode> group = new HashSet<VisualNode>();
		int minDepth =100000; 
		for(VisualNode vn:vns){
			int d=vn.getParent().getDepth();
			minDepth = Math.min(minDepth,d);
		}
		
		for(VisualNode vn:vns){
			int d = vn.getParent().getDepth();
			int p = d-minDepth;
			
			for(int i=0;i<p;i++)
				vn = vn.getParent();
			group.add(vn);
		}
		
		g.makeGroup(group);
	}

	@Override
	public void fromJSON(JSONObject o) {
		JSONArray nodesArr = (JSONArray) o.get("nodes");
		nodes = new HashSet<String>();
		nodes.addAll(nodesArr);
	}

	@Override
	public JComponent getSettingsComponent(SettingsChangeCallback v,
			VisualGraph graph) {
		
		ArrayList<VisualNode> vns  =new ArrayList<VisualNode>();
		JTextArea text = new JTextArea();
		text.append("Group of:\n");
		for(VisualNode n:graph.getAllNodes()){
			if(nodes.contains(n.getId())){
				text.append(n.getName()+"\n");
			}
		}
		
		return text;
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "Manual grouping "+id;
	}

	@Override
	public String getToolTip() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject ret = new JSONObject();
		JSONArray nodesArr = new JSONArray();
		nodesArr.addAll(nodes);
		ret.put("nodes",nodesArr);
		return ret;
	}

}
