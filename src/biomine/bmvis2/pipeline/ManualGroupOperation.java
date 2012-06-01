package biomine.bmvis2.pipeline;

import java.awt.TextArea;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTextArea;

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
