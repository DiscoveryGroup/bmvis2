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

package biomine.bmvis2.graphics;

import biomine.bmvis2.GraphArea;
import biomine.bmvis2.LayoutItem;
import biomine.bmvis2.Vec2;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;


/**
 * This class implements a state and some actions for zoom in GraphArea.
 *
 * @author ahinkka
 */
public class ZoomController implements ActionListener {
    public static final double MAX_SCALE = 10; // Maximum zoom scale

    double startScale;
    public boolean scaling = false;

    double startX;
    double startY;
    double centerX;
    double centerY;
    double goalScale;

    private Timer timer;

    private boolean stopScaling = false;
    private GraphArea graphArea;

    double speed;
    double x = 0;
    double lx = 0;
    double stepSize;
    double scale;

    double moveX, moveY, dist;


    public ZoomController(GraphArea ga) {
        this.graphArea = ga;
        timer = new Timer(25, this);
    }

    public void stop() {
        this.timer.stop();
        this.scaling = false;
    }

    public void start() {
        scaling = true;
        speed = 1;
        scale = goalScale / startScale;

        speed = 0.5;

        moveX = centerX - startX;
        moveY = centerY - startY;
        dist = 1 + Math.hypot(moveX, moveY) * 0.4 + 2
                * Math.abs(Math.log(scale));
        stepSize = 0.3 / dist;

        x = 0;
        lx = 0;
        timer.start();

    }

    private double inter(double a, double b, double x) {
        double x2 = x;// *x;
        double d = (b - a);
        return b - d * Math.pow(0.5, x * 10);
        // return a*(1-x2)+b*x2;
        // return a*Math.pow(b/a, x);
    }

    public void actionPerformed(ActionEvent e) {
        if (!scaling)
            return;
        if (x < 0.5)
            speed += 3;
        else
            speed -= 3;
        if (speed < 1)
            speed = 1;
        x += speed * stepSize;
        if (x > 1)
            x = 1;

        // double cs = startScale*(1-x)+x*goalScale;
        double cs = Math.pow(goalScale / startScale, x);
        double oldScale = graphArea.transform.getScaleX();
        graphArea.transform.setToIdentity();

        double dx = x - lx;
        double dxk = dx;
        // double dxk = Math.pow(x,4)-Math.pow(lx,4);
        // double dxk = Math.pow(0.5,x)-Math.pow(0.5,lx);
        cs = oldScale * Math.pow(goalScale / startScale, dxk);
        // cs = oldScale+((goalScale-startScale)*dxk);
        // cs =
        // oldScale+(goalScale-startScale)*(x-lx)*Math.pow((x+lx)*0.5,2)*3;
        // cs =
        // oldScale+(goalScale-startScale)*(x-lx)*0.5/Math.sqrt((x+lx)/2);

        cs = Math.min(cs, this.MAX_SCALE);

        graphArea.transform.scale(cs, cs);

        double trueScale = this.graphArea.transform.getScaleX();
        this.graphArea.transform.translate(this.graphArea.getWidth() / (2 * trueScale)
                - inter(startX, centerX, x), this.graphArea.getHeight() / (2 * trueScale)
                - inter(startY, centerY, x));

        if (x >= 1) {
            stop();
        }
        lx = x;
        this.graphArea.repaint();
    }

    /**
     * Starts to zoom to group of nodes/edges. Zooming happens in the background
     * using timers.
     *
     * @param itemsToZoom
     */
    public void zoomTo(Collection<? extends LayoutItem> itemsToZoom) {
        if (itemsToZoom.size() == 0)
            return;
        double maxX, maxY, minX, minY;
        maxX = maxY = Double.MIN_VALUE;
        minX = minY = Double.MAX_VALUE;

        for (LayoutItem item : itemsToZoom) {
            if (!this.graphArea.visualGraph.isVisible(item))
                continue;

            Vec2 pos = item.getPos();

            maxX = Math.max(maxX, pos.x);
            maxY = Math.max(maxY, pos.y);

            minX = Math.min(minX, pos.x);
            minY = Math.min(minY, pos.y);
        }

        minX -= 50;
        maxX += 50;
        minY -= 50;
        maxY += 50;
        double scale = Math.min(this.graphArea.getWidth() / (maxX - minX), this.graphArea.getHeight()
                / (maxY - minY));
        scale = Math.min(scale, 2);

        double centerX = (minX + maxX) / 2;
        double centerY = (minY + maxY) / 2;

        /*
           * transform.setToIdentity(); transform.scale(scale,scale); transform
           * .translate(getWidth()/(2*scale)-centerX,getHeight
           * ()/(2*scale)-centerY);
           */

        if (this.scaling)
            this.stop();

        this.centerX = centerX;
        this.centerY = centerY;
        Vec2 start = this.graphArea.inverseTransform(this.graphArea.getWidth() / 2, this.graphArea.getHeight() / 2);
        this.startX = start.x;
        this.startY = start.y;
        this.goalScale = scale;
        this.startScale = this.graphArea.transform.getScaleX();

        this.start();
    }
}