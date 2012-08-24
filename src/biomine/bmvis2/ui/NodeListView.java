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

package biomine.bmvis2.ui;

import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import biomine.bmvis2.VisualNode;
import biomine.bmvis2.pipeline.Pipeline;

/**
 * 
 * List for points of interest.
 * 
 * @author alhartik
 *
 */
public class NodeListView extends JPanel{
	Pipeline pipeline;
	public Pipeline getPipeline() {
		return pipeline;
	}

	public void setPipeline(Pipeline pipeline) {
		this.pipeline = pipeline;
	}

	JList list = new JList();
	ArrayList<VisualNode> nodes = new ArrayList<VisualNode>();
	
	public NodeListView(Pipeline pipe){
		pipeline=pipe;
		this.setLayout(new GridLayout());
		updateList();
	}
	
	public void addNode(VisualNode n){
		nodes.add(n);
		updateList(); }
	public void removeNode(VisualNode n){
		nodes.remove(n);
		updateList();
	}
	public void setNodes(Collection<VisualNode> ns){
		nodes = new ArrayList<VisualNode>(ns);
		updateList();
	}
	
	private void updateList(){
		if(list!=null)
			this.remove(list);
		list = new JList();
		list.setModel(new ListModel() {
			public void removeListDataListener(ListDataListener arg0) {
			}
			
			public int getSize() {
				return nodes.size();
			}
			
			public Object getElementAt(int i) {
				return nodes.get(i).toString();
			}
			
			public void addListDataListener(ListDataListener arg0) {
			}
		});
		list.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
			}
			
			public void mousePressed(MouseEvent e) {
				int idx = list.locationToIndex(e.getPoint());
				VisualNode vn = nodes.get(idx);
				
				if (e.getButton() == MouseEvent.BUTTON3) {
					if (pipeline == null) return;
					JPopupMenu pop = Menus.getInstance(pipeline).get2ndButtonMenu(vn, pipeline.getVisualizer());
					pop.show(NodeListView.this,e.getX(),e.getY());
				} 
				
			}
			public void mouseExited(MouseEvent e) { }
			public void mouseEntered(MouseEvent e) { }
			public void mouseClicked(MouseEvent e) {
//	Doesn't work. y? :(
//				int idx = list.locationToIndex(e.getPoint());
//				VisualNode vn = nodes.get(idx);
//				
//				if (e.getButton() == MouseEvent.BUTTON1 &&
//						e.getClickCount() == 2)
//					pipeline.getVisualizer().getGraphArea().zoomTo(Collections.singleton(vn));
			}
		});
		
		this.add(list);
		//list = new JList(new String[]{"testi","testi2"});
	}

}
