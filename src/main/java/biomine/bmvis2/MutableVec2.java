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

import java.awt.geom.Point2D;

/**
 * 2d vector class which enables mutation.
 * @author alhartik
 *
 */
public final class MutableVec2 {
	public double x, y;

	public MutableVec2(double xx, double yy) {
		x = xx;
		y = yy;
	}

	public MutableVec2(Point2D p2) {
		x = p2.getX();
		y = p2.getY();
	}

	public MutableVec2(Vec2 tp) {
		x = tp.x;
		y = tp.y;
	}

	public MutableVec2 copy() {
		return new MutableVec2(x, y);
	}

	public Vec2 toVec2() {
		return new Vec2(x, y);
	}

	public double length2() {
		return (x * x + y * y);
	}

	public double dist(Vec2 v) {
		return Math.sqrt((v.x - x) * (v.x - x) + (v.y - y) * (v.y - y));
	}

	public double length() {
		return Math.sqrt(length2());
	}

	public double dot(MutableVec2 o) {
		return x * o.x + y * o.y;
	}

	public void scale(double d) {
		x *= d;
		y *= d;
	}

	public void add(Vec2 v) {
		x += v.x;
		y += v.y;
	}

	public void add(MutableVec2 v) {
		x += v.x;
		y += v.y;
	}

	public void subtract(Vec2 v) {
		x -= v.x;
		y -= v.y;
	}

	public void subtract(MutableVec2 v) {
		x -= v.x;
		y -= v.y;
	}

	Vec2 normal() {
		return new Vec2(y, -x);
	}

	public String toString() {
		return "" + x + "," + y;
	}

	public Point2D toPoint() {
		return new Point2D.Double(x, y);
	}

	public void setZero() {
		x = y = 0;
	}

	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public void set(Vec2 pos) {
		x = pos.x;
		y = pos.y;
	}
}
