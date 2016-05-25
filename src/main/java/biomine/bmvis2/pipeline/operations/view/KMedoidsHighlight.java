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

package biomine.bmvis2.pipeline.operations.view;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import biomine.bmvis2.pipeline.GraphOperation;
import biomine.bmvis2.pipeline.SettingsChangeCallback;
import org.json.simple.JSONObject;

import biomine.bmvis2.GraphCache;
import biomine.bmvis2.SimpleVisualGraph;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.VisualGraph.Change;
import biomine.bmvis2.algorithms.KMedoids;
import biomine.bmvis2.algorithms.KMedoids.KMedoidsResult;
import biomine.bmvis2.color.ColorPalette;
/**
 * Implements visualisation for k-medoids algorithm. Can color either just medoids
 * or clusters.
 * 
 * TODO: k-medoids grouper
 * 
 * @author alhartik
 *
 */
public class KMedoidsHighlight implements GraphOperation {

	private Color hlColor = Color.RED;
	private boolean showClusters=false;
	private GraphCache<ArrayList<VisualNode>> cache = 
		new GraphCache<ArrayList<VisualNode>>(Change.POINTS_OF_INTEREST,Change.STRUCTURE);
	private GraphCache<HashMap<VisualNode,Integer>> clusterCache =
		new GraphCache<HashMap<VisualNode,Integer>>(Change.POINTS_OF_INTEREST,Change.STRUCTURE);
	private int k=3;
	
	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
		cache.clear();
	}

	@Override
	public void doOperation(VisualGraph g) throws GraphOperationException{
		ArrayList<VisualNode> rlist = cache.get(g);
		HashMap<VisualNode,Integer> clusters = clusterCache.get(g);
		if(rlist==null){
			rlist = new ArrayList<VisualNode>();
			SimpleVisualGraph sg = new SimpleVisualGraph(g);
			KMedoidsResult r = KMedoids.runKMedoids(sg, k);
			int[] meds = r.medoids;
			for(int i=0;i<k;i++)
				rlist.add(sg.getVisualNode(meds[i]));
			cache.put(g,rlist);
			clusters = new HashMap<VisualNode, Integer>();
			for(int i=0;i<sg.n;i++){
				clusters.put(sg.getVisualNode(i), r.clusters[i]);
			}
			clusterCache.put(g, clusters);
		}
		
		assert(rlist.size()==k);
		if(showClusters && k<ColorPalette.pastelShades.length){
			for(Entry<VisualNode,Integer> ent:clusters.entrySet()){
				ent.getKey().setBaseColor(ColorPalette.pastelShades[ent.getValue()]);
			}
		}
		for(int i=0;i<rlist.size();i++){
			rlist.get(i).setBaseColor(hlColor);
			//System.out.println(rlist.get(i));
			int c = i+1;
			//rlist.get(i).addExtraLabel(""+c+". representative");
		}
	}

	@Override
	public JComponent getSettingsComponent(final SettingsChangeCallback v,
			VisualGraph graph){
		Box ret =new Box(BoxLayout.X_AXIS);
		ret.add(new JLabel("k:"));
		
		final JSpinner spin =new JSpinner(new SpinnerNumberModel(3, 1, 1000, 1));
		
		spin.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setK(((Number)spin.getValue()).intValue());
				v.settingsChanged(false);
			}
		});
		ret.add(spin);
		
		final JCheckBox showClustersBox=new JCheckBox("Color clusters");
		ret.add(showClustersBox);
		showClustersBox.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				showClusters=showClustersBox.isSelected();
				v.settingsChanged(false);
			}
		});
		return ret;
	}

	@Override
	public String getTitle(){
		return "k-medoids hilight";
	}
	
	@Override
	public String getToolTip() {
		return null;
	}

	@Override
	public void fromJSON(JSONObject o) throws Exception{
		setK(((Number)o.get("k")).intValue());
		Object b = o.get("colorClusters");
		if(b!=null && b instanceof Boolean){
			Boolean bb = (Boolean)b;
			showClusters=bb;
		}
	}

	@Override
	public JSONObject toJSON(){
		JSONObject ret = new JSONObject();
		ret.put("k",getK());
		ret.put("colorClusters", showClusters);
		return ret;
	}
}
