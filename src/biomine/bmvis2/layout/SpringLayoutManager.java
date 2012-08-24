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

package biomine.bmvis2.layout;

import biomine.bmvis2.*;
import biomine.bmvis2.VisualGraph.Change;

import javax.swing.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class SpringLayoutManager extends LayoutManager {
    private double damping = 0.70;
    private double repulsive_k = 23000;
    private double freezingThreshold = 5.0;
    private static double dt = 0.10;

    private BarnesHut2D barnesHut = new BarnesHut2D(0);

    Future currentSimulation = null;


    public double getDamping() {
        return damping;
    }

    public void setDamping(double damping) {
        this.setActive(true);
        this.damping = damping;
    }

    public double getRepulsive_k() {
        return repulsive_k;
    }

    public void setRepulsive_k(double repulsiveK) {
        this.setActive(true);
        repulsive_k = repulsiveK;
    }

    HashMap<LayoutItem, Vec2> velocity = new HashMap<LayoutItem, Vec2>();
    HashMap<LayoutItem, Vec2> forces = new HashMap<LayoutItem, Vec2>();
    private VisualGraph visualGraph;

    ExecutorService threadPool;

    public SpringLayoutManager(VisualGraph g) {
        Logging.info("layout", "Constructing a SpringLayoutManager for graph " + g);
        this.threadPool = Executors.newFixedThreadPool(1);
        this.visualGraph = g;
        this.velocity = new HashMap<LayoutItem, Vec2>();

        for (LayoutItem item : visualGraph.getZOrderItems()) {
            item.setPos(item.getPos()
                    .plus(0.01 * Math.random(), 0.01 * Math.random()));
        }
    }

    public SpringLayoutManager(VisualGraph graph, SpringLayoutManager oldManager) {
        this(graph);
        this.repulsive_k = oldManager.repulsive_k;
        this.damping = oldManager.damping;
        this.freezingThreshold = oldManager.freezingThreshold;
    }

    private void applySpring(LayoutItem a, LayoutItem b, double k, double len) {
        double deltaX = a.getPos().x - b.getPos().x;
        double deltaY = a.getPos().y - b.getPos().y;

        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (distance < 0.1)
            distance = 0.1;

        double dk = (distance - len);
        double scale = (-dk * k) / distance;
        deltaX *= scale;
        deltaY *= scale;

        Vec2 f = forces.get(a);

        if (f == null)
            f = Vec2.ZERO;

        forces.put(a, f.plus(deltaX, deltaY));
    }

    public final double getMass(LayoutItem it) {
        if (it instanceof VisualGroupNode)
            return 2;
        if (it instanceof VisualNode) {
            Double interestingness = it.getGraph().getNodesOfInterest().get(it);

            if (interestingness == null)
                return 1.5;
            else
                return 1.0 + Math.abs(interestingness);
        }
        return 1;
    }


    synchronized public void simulateInBackground() {
        if (currentSimulation != null && !currentSimulation.isDone())
            return;

        final long startVisVersion = visualGraph.getVersion(Change.VISIBILITY);

        final ArrayList<LayoutItem> items = new ArrayList<LayoutItem>(visualGraph.getZOrderItems());

        for (Entry<LayoutItem, Vec2> forceEntry : forces.entrySet())
            forceEntry.setValue(Vec2.ZERO);

        final ArrayList<VisualEdge> edges = new ArrayList<VisualEdge>(visualGraph.getEdges());

        Runnable simulationRunnable = new Runnable() {
            public void run() {
                SpringLayoutManager.this.iterations++;

                barnesHut.reset(items.size());
                for (int i = 0; i < items.size(); i++) {
                    LayoutItem item = items.get(i);
                    // Logging.debug("layout", "LayoutItem " + item);
                    // Logging.debug("layout", "   pos: " + item.getPos() + "; mass: " + SpringLayoutManager.this.getMass(item));
                    barnesHut.setPos(i, item.getPos());
                    barnesHut.setMass(i, getMass(item));
                }
                barnesHut.simulate(repulsive_k);
                for (int i = 0; i < items.size(); i++) {
                    Vec2 d = forces.get(items.get(i));
                    if (d == null)
                        d = Vec2.ZERO;
                    forces.put(items.get(i), d.plus(barnesHut.getForce(i)));
                }

                for (VisualEdge edge : edges) {
                    VisualNode n1 = edge.getFrom();
                    VisualNode n2 = edge.getTo();
                    double k = 0.2 + 0.3 * edge.getGoodness();
                    if (k > 0.5)
                        k = 0.5;

                    double len = 15;

                    applySpring(n1, edge, k, len);
                    applySpring(edge, n1, k, len);

                    applySpring(n2, edge, k, len);
                    applySpring(edge, n2, k, len);
                }

                double maxVelocity = 0;
                for (Vec2 velocity : SpringLayoutManager.this.velocity.values()) {
                    if (velocity.length() > maxVelocity)
                        maxVelocity = velocity.length();
                }

                double minX = 0;
                double maxX = 0;
                double minY = 0;
                double maxY = 0;
                for (LayoutItem item : items) {
                    if (item.getPos().x < minX)
                        minX = item.getPos().x;
                    if (item.getPos().x > maxX)
                        maxX = item.getPos().x;
                    if (item.getPos().y < minY)
                        minY = item.getPos().y;
                    if (item.getPos().y > maxY)
                        maxY = item.getPos().y;
                }

                double distance = Math.max(maxX - minX, maxY - minY);
                double shouldIStop = maxVelocity / (Math.sqrt(distance) / 100);

                // Logging.info("layout", "maxVelocity: " + maxVelocity);
                // Logging.info("layout", "iterations:                            " + iterations);
                // Logging.info("layout", "distance: " + distance);
                // Logging.info("layout", "shouldIStop? " + shouldIStop);

                // Layout stopping logic ends here

                Runnable layoutUpdater = new Runnable() {
                    public void run() {
                        if (visualGraph.getVersion(Change.VISIBILITY) != startVisVersion)
                            return;

                        for (LayoutItem item : items) {
                            Vec2 velocity = SpringLayoutManager.this.velocity.get(item);

                            if (velocity == null)
                                velocity = Vec2.ZERO;

                            velocity = velocity.scaled(damping);
                            Vec2 springForce = forces.get(item);

                            if (springForce == null)
                                springForce = Vec2.ZERO;

                            velocity = velocity.plus(springForce.scaled(SpringLayoutManager.dt));

                            if (Double.isNaN(velocity.x) || Double.isNaN(velocity.y)) {
                                Logging.warning("layout", "Coordinates went NaN");
                                velocity = new Vec2(Math.random() * 0.02, Math.random() * 0.01);
                                // Velocity seems to have to be something.  Otherwise everything bursts in flames. I.e.
                                // JVM dies if you try to draw something too funky.  Or it might also be a bug in the
                                // JVM.
                            }

                            if (!item.isPositionFixed() && !item.isHighlighted()) {
                                /* item.setPos(item.getPos().plus(
                                        velocity.scaled(SpringLayoutManager.dt)).plus(
                                        Math.random() * 0.01,
                                        Math.random() * 0.01)); */
                                item.setPos(item.getPos().plus(velocity.scaled(SpringLayoutManager.dt)));
                            } else {
                                velocity = Vec2.ZERO;
                            }

                            SpringLayoutManager.this.velocity.put(item, velocity);
                        }
                    }
                };

                if (shouldIStop < freezingThreshold && SpringLayoutManager.this.iterations > 10)
                    SpringLayoutManager.this.setActive(false);
                else {
                    SwingUtilities.invokeLater(layoutUpdater);
                }
            }
        };
        boolean multiThreaded = false;
        if (multiThreaded)
            currentSimulation = threadPool.submit(simulationRunnable);
        else
            simulationRunnable.run();
    }

    private void printForces(Map<LayoutItem, Vec2> forceMap) {
        for (LayoutItem item : forceMap.keySet()) {
            if (item instanceof VisualEdge) {

                if (!((VisualEdge) item).getFrom().isVisible() && !((VisualEdge) item).getTo().isVisible())
                    continue;
            } else if (item instanceof VisualNode && !((VisualNode) item).isVisible())
                continue;
            Logging.debug("layout", " - " + item.toString() + ": " + forceMap.get(item));
        }
    }

    @Override
    public void setActive(boolean newState) {
        // Logging.debug("layout", "setActive(" + newState + ") called");
        super.setActive(newState);
        if (newState == false)
            iterations = 0;
        else
            this.simulateInBackground();
    }

    private int iterations = 0;

    @Override
    public void update() {
        this.setActive(true);
    }

    public void setFreezingThreshold(double freezingThreshold) {
        this.freezingThreshold = freezingThreshold;
    }

    public double getFreezingThreshold() {
        return freezingThreshold;
    }
}
