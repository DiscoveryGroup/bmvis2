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
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import biomine.bmvis2.VisualGraph.Change;

/**
 * Group node superclass. Creates no edges. Subclass and reimplement createEdges
 * to have custom edge creation behaviour.
 *
 * @author alhartik
 */
public class VisualGroupNode extends VisualNode {
    private Set<VisualNode> children;
    private String groupType;
    private boolean open;

    private int depth;

    public int getDepth() {
        return depth;
    }

    private void initChildren() {
        if (children == null)
            children = new HashSet<VisualNode>();
    }

    private void init() {
        initChildren();
        this.setBaseColor(Color.YELLOW);
        this.setPos(new Vec2(0, 0));
        this.setShape(new Rectangle2D.Double());
        this.setOpen(true);
        if (getParent() == null)
            depth = 0;
        else
            depth = getParent().getDepth() + 1;
        setType("Group");

        StringBuffer id = new StringBuffer();
    }

    public VisualGroupNode(VisualGraph graph, VisualGroupNode parent,
                           String name) {
        super(graph, parent, name);
        init();
    }

    public VisualGroupNode(VisualGraph graph, VisualGroupNode parent) {
        super(graph, parent, "Group");
        init();
    }

    public VisualGroupNode(VisualGroupNode parent) {
        super(parent.getGraph(), parent, "Group");
        init();
    }

    public VisualGroupNode(VisualGroupNode parent, String name) {
        super(parent.getGraph(), parent, name);
        init();
    }

    public void setOpen(boolean state) {
        this.open = state;

        if (this.isVisible()) {
            for (VisualNode n : children) {
                n.setVisible(state);
                n.setSelected(false);
            }
        }

        if (this.open) {
            setHighlight(false);
            setNeighborHighlight(false);
        }

        this.graphViewChanged();
    }

    public Color getColor() {
        if (this.isOpen())
            return getBaseColor();
        return super.getColor();
    }

    protected void setVisible(boolean v) {
        // children are visible only if groupnode is open and visible
        super.setVisible(v);
        initChildren();
        if (v == true && isOpen())
            for (VisualNode n : children)
                n.setVisible(true);
        else {
            for (VisualNode n : children)
                n.setVisible(false);
        }
    }

    public void paint(Graphics2D g) {
        if (isOpen()) {
            // do nothing
        } else {
            super.paint(g);
        }
    }

    public void setName(String n) {
        super.setName(n);
        updateLabels();
    }

    public boolean isOpen() {
        return open;
    }

    public Set<VisualNode> getChildren() {
        if (children == null)
            return Collections.EMPTY_SET;
        return children;
    }

    private GraphCache<ArrayList<String>> infoLabelCache = null;

    private List<String> infoLabel() {
        if (infoLabelCache == null)
            infoLabelCache = new GraphCache<ArrayList<String>>(Change.STRUCTURE);

        ArrayList<String> labels = infoLabelCache.get(getGraph());
        // if(labels!=null)return labels;

        labels = new ArrayList<String>();
        HashSet<VisualNode> desc = new HashSet<VisualNode>(getDescendants());
        labels.add("contains :");
        if (desc.size() <= 3) {
            for (VisualNode c : desc) {
                labels.add(c.getName());
            }
        } else {
            HashMap<String, Integer> typeCounts = new HashMap<String, Integer>();
            for (VisualNode c : desc) {
                Integer i = typeCounts.get(c.getType());
                if (i == null)
                    i = 0;
                typeCounts.put(c.getType(), i + 1);
            }
            int nc = 0;
            for (int i = 0; i < 2; i++) {
                int max = 0;
                String t = "";
                for (String type : typeCounts.keySet()) {
                    int count = typeCounts.get(type);
                    if (count > max) {
                        max = count;
                        t = type;
                    }
                }
                typeCounts.put(t, 0);
                nc += max;
                if (max != 0)
                    labels.add("" + max + " " + t + "s");
            }
            if (nc != desc.size())
                labels.add("and " + (desc.size() - nc) + " other");
        }
        infoLabelCache.put(this.getGraph(), labels);
        return labels;
    }

    public void addChild(VisualNode n) {
        if (children.contains(n))
            return;

        children.add(n);

        if (n.getParent() != this)
            n.setParent(this);

        if (this.isOpen() && this.isVisible())
            n.setVisible(true);
        else
            n.setVisible(false);

        graphStructureChanged();
        updateLabels();
    }

    /**
     * If the group node has no sane position, let's consult its members
     * (or if it's still zero, randomshift!).
     * @return
     */
    public Vec2 getPos() {
        if (super.getPos() == Vec2.ZERO) {
            Vec2 newPos = Vec2.ZERO;
            Set<Vec2> childPositions = new HashSet<Vec2>();
            for (VisualNode child : this.getChildren())
                childPositions.add(child.getPos());

            for (Vec2 pos : childPositions)
                newPos = newPos.plus(pos.scaled(1.0 / childPositions.size()));

            if (newPos.equals(Vec2.ZERO))
                newPos = newPos.randomShift();

            super.setPos(newPos);
        }

        return super.getPos();
    }

    /**
     * removes child. does the same as n.setParent(null)
     *
     * @param n
     */
    public void removeChild(VisualNode n) {

        if (children.contains(n) == false)
            return;
        children.remove(n);

        n.setParent(null);
        // if (n.getParent() == this)
        // n.setParent(this.getParent());
        updateLabels();
        graphStructureChanged();
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public Collection<VisualNode> getDescendants() {
        HashSet<VisualNode> ret = new HashSet<VisualNode>();
        if (getChildren() == null)
            return Collections.EMPTY_LIST;
        for (VisualNode c : getChildren()) {

            if (c instanceof VisualGroupNode) {
                ret.addAll(((VisualGroupNode) c).getDescendants());
            } else {
                ret.add(c);
            }
        }
        return ret;
    }

    private static class Connection {
        VisualNode n;
        double g;
    }

    private ArrayList<Connection> neighbors = new ArrayList<Connection>();

    public void addConnection(VisualNode n, double g) {
        Connection c = new Connection();
        c.n = n;
        c.g = g;
        neighbors.add(c);
    }

    public void createEdges() {
        for (Connection c : neighbors) {
            GroupEdgeUtils.makeGroupEdge(this, c.n, c.g);
        }
    }


    // public String getGroupId(){
    // if(getParent()==null)return "";
    // int c = 0;
    // for(VisualNode n:getParent().getChildren()){
    // if(n==this)break;
    // if(n instanceof VisualGroupNode)
    // c++;
    // }
    // String parentId = getParent().getGroupId();
    // return parentId + (char)('A'+c);
    // }

    // @Override
    // public void addEdge(VisualEdge e){
    // throw new RuntimeException("DON DO DAT");
    // }

    /* LABELS */

    private ArrayList<String> extraLabels = new ArrayList<String>();

    private void updateLabels() {
        ArrayList<String> il = new ArrayList<String>();
        il.add(this.getName());
        il.addAll(infoLabel());
        il.addAll(getExtraLabels());
        super.setLabels(il);
    }

    @Override
    public void addExtraLabel(String str) {
        extraLabels.add(str);
        updateLabels();
    }

    @Override
    public void setExtraLabels(Collection<String> str) {
        extraLabels = new ArrayList<String>(str);
        updateLabels();
    }

    @Override
    public List<String> getExtraLabels() {
        if (extraLabels == null)
            return Collections.EMPTY_LIST;
        return extraLabels;
    }
}
