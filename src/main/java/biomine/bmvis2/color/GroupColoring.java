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
import java.util.Random;

import javax.swing.JComponent;

import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualGroupNode;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.VisualGraph.Change;

/**
 * Colors every group with one color.
 * @author alhartik 
 */
public class GroupColoring implements NodeColoring {
	HashMap<VisualNode, Color> cachedColors;
	VisualGraph currentGraph;
	long lastVersion = 0;

	Color[] colors = ColorPalette.pastelShades;

	Color mixColors(Color x, Color y) {
		int r = x.getRed() + y.getRed();
		int g = x.getGreen() + y.getGreen();
		int b = x.getBlue() + y.getBlue();
		r /= 2;
		g /= 2;
		b /= 2;
		return new Color(r, g, b);
	}

	private void init(VisualGraph graph) {
		cachedColors = new HashMap<VisualNode, Color>();
		currentGraph = graph;
		int curC = 0;
		Random r = new Random();
		for (VisualNode n : graph.getAllNodes()) {
			if (n instanceof VisualGroupNode) {
				VisualGroupNode vgn = (VisualGroupNode) n;
				Color color = null;
				if (curC < colors.length)
					color = colors[curC];
				else {
					int a = curC % colors.length;
					int b = curC / colors.length;
					if (a >= colors.length || b >= colors.length) {

						color = new Color(r.nextInt(255), r.nextInt(255), r
								.nextInt(255));
					} else
						color = mixColors(colors[a], colors[b]);
				}
				for (VisualNode cn : vgn.getChildren()) {
					cachedColors.put(cn, color);
				}
				curC++;
			}
		}
		lastVersion = graph.getVersion(Change.STRUCTURE);
	}

	@Override
	public Color getFillColor(VisualNode n) {
		if (n.getGraph() == currentGraph
				&& lastVersion == currentGraph.getVersion(Change.STRUCTURE))
			return cachedColors.get(n);

		init(n.getGraph());
		return cachedColors.get(n);
	}

	@Override
	public JComponent colorLegendComponent() {
		// TODO Auto-generated method stub
		return null;
	}

    public String getSimpleUIName () {
        return "Color by group";
    }

    public String getByName () {
        return "by group";
    }
}
