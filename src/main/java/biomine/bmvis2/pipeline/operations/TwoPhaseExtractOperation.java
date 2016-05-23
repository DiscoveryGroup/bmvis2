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

package biomine.bmvis2.pipeline.operations;

import java.io.IOException;
import java.io.ObjectInputStream;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import biomine.bmvis2.pipeline.GraphOperation;
import biomine.bmvis2.pipeline.SettingsChangeCallback;
import org.json.simple.JSONObject;

import biomine.bmvis2.GraphCache;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualGraph.Change;
import biomine.bmvis2.algorithms.TwoPhase;
import biomine.bmvis2.edgesimplification.SimplificationUtils;
import biomine.bmvis2.edgesimplification.Simplifier;
import biomine.bmvis2.graphcontrols.BestPathGrader;

public class TwoPhaseExtractOperation implements GraphOperation {

	private int target = 1000000;
	
	int oldTot = 1;

	public TwoPhaseExtractOperation() {
	}
	GraphCache<TwoPhase> gc = new GraphCache<TwoPhase>(Change.STRUCTURE,Change.POINTS_OF_INTEREST);

	@Override
	public void doOperation(VisualGraph g) throws GraphOperationException {

		int tot = SimplificationUtils.countNormalEdges(g);
		target = Math.min(target, tot);
		
		TwoPhase p = gc.get(g);
		if(p==null){
			p = new TwoPhase(g);
			gc.put(g, p);
		}
		
		p.doHiding(target);
	}


	@Override
	public JComponent getSettingsComponent(final SettingsChangeCallback v,
			VisualGraph graph) {
		int tot = SimplificationUtils.countNormalEdges(graph);

		if (oldTot != 0) {
			int nt = (target * tot) / oldTot;
			if (nt != target) {
				target = Math.max(nt, target);
			}
		} else {
			target = tot;
		}

		oldTot = tot;
		JPanel ret = new JPanel();

		final JSlider sl = new JSlider(0, tot, Math.min(target, tot));
		sl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (target == sl.getValue())
					return;
				target = sl.getValue();
				v.settingsChanged(false);

			}
		});
		ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));
		ret.add(sl);

		return ret;
	}

	@Override
	public String getTitle() {
		return "Two phase hider";
	}	

	@Override
	public String getToolTip() {
		return null;
	}

	@Override
	public void fromJSON(JSONObject o) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		target = ((Long) o.get("target")).intValue();
	}

	@Override
	public JSONObject toJSON() {
		JSONObject ret = new JSONObject();
		ret.put("target", target);
		return ret;
	}
}
