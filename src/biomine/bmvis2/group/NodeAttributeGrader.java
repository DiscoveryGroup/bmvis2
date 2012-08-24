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

package biomine.bmvis2.group;

import biomine.bmgraph.BMNode;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.graphcontrols.NodeGrader;

public class NodeAttributeGrader implements NodeGrader{

	private String attribute;
	public String getAttribute() {
		return attribute;
	}
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	public double getDefaultG() {
		return defaultG;
	}
	public void setDefaultG(double defaultG) {
		this.defaultG = defaultG;
	}
	private double defaultG=0;
	public NodeAttributeGrader(String attr,double def){
		attribute=attr;
		defaultG=def;
	}
	@Override
	public double getNodeGoodness(VisualNode n) {
		BMNode bmn = n.getBMNode();
		if(bmn==null)return defaultG;
		String s = bmn.get(attribute);
		if(s==null)return defaultG;		try{
			return Double.parseDouble(s);
		}catch(NumberFormatException e){
			return defaultG;
		}
	}

	public static final NodeAttributeGrader BEST_PATH = new NodeAttributeGrader("goodness_of_best_path",0);

    public String getReadableAttribute() {
        if (this.attribute == "goodness_of_best_path")
            return "best path goodness";
        else
            return this.attribute + " goodness";
    }
}
