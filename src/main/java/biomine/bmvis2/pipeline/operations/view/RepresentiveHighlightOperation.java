/*
 * Copyright 2012-2016 University of Helsinki.
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

import biomine.bmvis2.GraphCache;
import biomine.bmvis2.SimpleVisualGraph;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualGraph.Change;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.algorithms.ProbDijkstra;
import biomine.bmvis2.pipeline.GraphOperation;
import biomine.bmvis2.pipeline.SettingsChangeCallback;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;

public class RepresentiveHighlightOperation implements GraphOperation {

    private int numberOfNodes = 0;
    private Color hlColor = Color.green;
    private GraphCache<ArrayList<VisualNode>> cache = new GraphCache<ArrayList<VisualNode>>(Change.POINTS_OF_INTEREST, Change.STRUCTURE);

    public ArrayList<VisualNode> initList(VisualGraph g) {
        SimpleVisualGraph sg = new SimpleVisualGraph(g.getRootNode().getDescendants());
        ArrayList<ProbDijkstra> positive = new ArrayList<ProbDijkstra>();
        ArrayList<ProbDijkstra> negative = new ArrayList<ProbDijkstra>();
        int poicount = 0;

        LinkedHashSet<Integer> remaining = new LinkedHashSet<Integer>();
        for (int i = 0; i < sg.n; i++)
            remaining.add(i);

        for (Map.Entry<VisualNode, Double> ent : g.getNodesOfInterest().entrySet()) {
            VisualNode vn = ent.getKey();
            double val = ent.getValue();
            int ivn = sg.getInt(vn);
            remaining.remove(ivn);
            ProbDijkstra pd = new ProbDijkstra(sg, ivn);
            if (val > 0)
                positive.add(pd);
            else
                negative.add(pd);
            poicount++;
        }

        ArrayList<VisualNode> rlist = new ArrayList<VisualNode>();
        ArrayList<Double> posProb = new ArrayList<Double>();
        ArrayList<Double> posSquareSum = new ArrayList<Double>();
        for (int i = 0; i < sg.n; i++) {
            double p = 1;
            double ss = 0;
            for (ProbDijkstra pd : positive) {
                p *= pd.getProbTo(i);
                ss += Math.pow(pd.getProbTo(i), 2);
            }
            posProb.add(p);
            posSquareSum.add(ss);
        }

        ArrayList<Double> negProb = new ArrayList<Double>();
        for (int i = 0; i < sg.n; i++) {
            double p = 1;
            for (ProbDijkstra pd : negative) {
                p *= (1 - pd.getProbTo(i));
            }
            negProb.add(p);
        }

        int it = 0;
        while (negative.size() + positive.size() < sg.n) {
            it++;
            //add
            //select n with highest posProb[n]*(1-negProb[n])
            //in case of a tie, select n with lowest square sum distance from
            //positive nodes

            int best = -1;
            double bestSS = 100000;
            double bestProb = 0;
            for (int n : remaining) {
                double prob = posProb.get(n) * (negProb.get(n));
                double ss = posSquareSum.get(n);
                if (prob == bestProb) {
                    if (ss < bestSS) {
                        bestSS = ss;
                        best = n;
                    }
                }
                if (prob > bestProb) {
                    bestProb = prob;
                    bestSS = ss;
                    best = n;
                }
            }
            if (best < 0) break;

            System.out.println("it " + it + " best = " + best + " prob = " + bestProb);

            rlist.add(sg.getVisualNode(best));
            ProbDijkstra newPD = new ProbDijkstra(sg, best);
            negative.add(newPD);

            remaining.remove(best);
            for (int i : remaining) {
                double ol = negProb.get(i);
                ol *= 1 - newPD.getProbTo(i);
                negProb.set(i, ol);
            }
        }
        return rlist;
    }

    public void doOperation(VisualGraph g) throws GraphOperationException {
        ArrayList<VisualNode> rlist = cache.get(g);
        if (rlist == null) {
            rlist = initList(g);
            cache.put(g, rlist);
        }
        for (int i = 0; i < Math.min(rlist.size(), numberOfNodes); i++) {
            rlist.get(i).setBaseColor(hlColor);
            //System.out.println(rlist.get(i));
            int c = i + 1;
            //rlist.get(i).addExtraLabel(""+c+". representative");
        }
    }

    public JComponent getSettingsComponent(final SettingsChangeCallback v,
                                           VisualGraph graph) {
        final JSlider slider = new JSlider(0, graph.getRootNode().getDescendants().size(), 0);
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                numberOfNodes = slider.getValue();
                v.settingsChanged(false);
            }
        });
        return slider;
    }

    public String getTitle() {
        return "Representive node hilight";
    }

    public String getToolTip() {
        return null;
    }

    public void fromJSON(JSONObject o) throws Exception {
        numberOfNodes = ((Number) o.get("number")).intValue();
    }

    public JSONObject toJSON() {
        JSONObject ret = new JSONObject();
        ret.put("number", numberOfNodes);
        return ret;
    }


}
