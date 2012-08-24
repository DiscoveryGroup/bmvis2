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

package biomine.bmvis2.color;

import java.awt.Color;
import java.util.HashMap;

import biomine.bmvis2.VisualEdge;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualGraph.Change;
import biomine.bmvis2.group.EdgeBetweenness;

/**
 * 
 * @author alhartik
 *
 */
public class EdgeBetweennessColoring implements EdgeColoring {

	VisualGraph currentGraph = null;
	HashMap<VisualEdge, Color> cachedColors;
	long lastVersion = 0;

	Color getGradientColor(double x) {

		double c1[] = { 0, 0, 1 };
		double c2[] = { 1, 1, 0 };
		double cret[] = { c1[0] * (1 - x) + c2[0] * x,
				c1[1] * (1 - x) + c2[1] * x, c1[2] * (1 - x) + c2[2] * x, };

		return new Color((float) cret[0], (float) cret[1], (float) cret[2]);

	}
	

	public void init(VisualGraph graph) {
		currentGraph = graph;
		EdgeBetweenness eb = new EdgeBetweenness(graph.getRootNode());
		HashMap<VisualEdge, Double> betw = eb.getEdgeBetweenness();
		double maxBetw = 0;
		for (double i : betw.values())
			maxBetw = Math.max(maxBetw, Math.log(i));

		cachedColors = new HashMap<VisualEdge, Color>();
		for (VisualEdge e : betw.keySet()) {
			double b = betw.get(e);
			b = Math.log(b);
			cachedColors.put(e, getGradientColor(((double) b) / maxBetw));
		}
		lastVersion = graph.getVersion(Change.STRUCTURE);
		
	}

	@Override
	public Color getColor(VisualEdge e) {
		if (e.getGraph() != currentGraph
				|| e.getGraph().getVersion(Change.STRUCTURE) != lastVersion)
			init(e.getGraph());
		return cachedColors.get(e);
	}

}
